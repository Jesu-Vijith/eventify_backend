package com.eventmanagement.EventManagement.configuration;

import com.eventmanagement.EventManagement.model.dto.EncryptedPasswordKey;
import com.eventmanagement.EventManagement.model.response.TokenInfoCache;
import com.eventmanagement.EventManagement.service.AccessService;
import com.eventmanagement.EventManagement.service.KeyService;
import com.eventmanagement.EventManagement.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.crypto.SecretKey;

@Component
public class CacheRepository extends ObjectMapper {

    private final JedisPool jedisPool;

    @Autowired
    private KeyService keyService;

    @Autowired
    public CacheRepository(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }


    public void setTokenInfo(String idmAccessToken, TokenInfoCache tokenInfoInCache, Long expiresIn) throws JsonProcessingException {
        String userData = writeValueAsString(tokenInfoInCache);
        Jedis jedis = jedisPool.getResource();
        jedis.set(idmAccessToken,userData);
        jedis.expire(idmAccessToken, expiresIn);
        jedis.set("r_"+tokenInfoInCache.getRefreshToken(),userData);
        jedis.expire("r_"+tokenInfoInCache.getRefreshToken(), expiresIn + 120);
        jedis.close();
    }


    public void setKey(String uniqueId, SecretKey key) throws JsonProcessingException {
        String passwordKey=keyService.secretKeyToString(key);
        Jedis jedis=jedisPool.getResource();
        jedis.set(uniqueId,passwordKey);
        jedis.close();
    }

    public SecretKey getKey(String uniqueId) throws JsonProcessingException {
        Jedis jedis=jedisPool.getResource();
        String stringKey=jedis.get(uniqueId);
        jedis.close();
        return keyService.stringToSecretKey(stringKey);
    }

    public TokenInfoCache getTokenInfo(String token){
        Jedis jedis = jedisPool.getResource();
        String tokenInfoResponseStr = jedis.get(token);
        jedis.close();
        if(tokenInfoResponseStr == null){
            return null;
        }

        try {
            return readValue(tokenInfoResponseStr, TokenInfoCache.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void signOutTokenInfo(String accessToken) throws JsonProcessingException {
        Jedis jedis = jedisPool.getResource();
        String tokenInfoCacheStr = jedis.get(accessToken);
        TokenInfoCache tokenInfoCache = readValue(tokenInfoCacheStr,TokenInfoCache.class);
        jedis.del(accessToken,tokenInfoCache.getRefreshToken());
        jedis.close();

    }

    public TokenInfoCache getUserDataByRefreshToken(String refreshToken) {
        Jedis jedis = jedisPool.getResource();
        TokenInfoCache tokenInfoCache = null;
        try {
            tokenInfoCache = readValue(jedis.get("r_"+ refreshToken), TokenInfoCache.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        jedis.close();
        return tokenInfoCache;

    }

    public String getIdToken(String idmAccessToken) throws JsonProcessingException {
        Jedis jedis = jedisPool.getResource();
        String tokenInfoResponseStr = jedis.get(idmAccessToken);
        jedis.close();
        if(tokenInfoResponseStr == null){
            return null;
        }
        TokenInfoCache tokenInfoCache = readValue(tokenInfoResponseStr, TokenInfoCache.class);
        return tokenInfoCache.getCognitoAccessToken();
    }
}
