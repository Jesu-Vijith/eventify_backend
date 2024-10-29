package com.eventmanagement.EventManagement.security;

import com.eventmanagement.EventManagement.configuration.CacheRepository;
import com.eventmanagement.EventManagement.exception.CustomException;
import com.eventmanagement.EventManagement.model.entity.User;
import com.eventmanagement.EventManagement.model.response.SignInResponse;
import com.eventmanagement.EventManagement.model.response.TokenInfoCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cache.CacheException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CacheRepository cacheRepository;

    public SignInResponse createIdmToken(User user, Long expiresIn,
                                         final String accessToken, String refreshToken, String idToken) throws JsonProcessingException {
        try{

            final String idmAccessToken = UUID.randomUUID().toString();
            final String idmRefreshToken = UUID.randomUUID().toString();
            TokenInfoCache tokenInfoInCache = modelMapper.map(user, TokenInfoCache.class);
            tokenInfoInCache.setCognitoAccessToken(accessToken);
            tokenInfoInCache.setCognitoRefreshToken(refreshToken);
            tokenInfoInCache.setIdToken(idToken);
            tokenInfoInCache.setRefreshToken(idmRefreshToken);
//        tokenInfoInCache.setRoles(userRepository.);
            cacheRepository.setTokenInfo(idmAccessToken, tokenInfoInCache,expiresIn);
            return new SignInResponse(user.getUserId(),idmAccessToken,idmRefreshToken,expiresIn, user.getEmail(),
                    user.getName(),user.getIsActive(),user.getRoles().getRoleName());

        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid argument",e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (CacheException e) {
            throw new CustomException("Failed to store token information in cache.",e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new CustomException("An error occurred while creating the IDM token. Please try again.",e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public SignInResponse userRefreshIdmToken(User user,String cognitoRefreshToken, String refreshToken, String accessToken,String idToken,Long expiresIn) throws JsonProcessingException {
        try {
            String idmAccessToken = UUID.randomUUID().toString();
            TokenInfoCache tokenInfoCache = modelMapper.map(user, TokenInfoCache.class);
            tokenInfoCache.setCognitoAccessToken(accessToken);
            tokenInfoCache.setCognitoRefreshToken(cognitoRefreshToken);
            tokenInfoCache.setIdToken(idToken);
            tokenInfoCache.setRefreshToken(refreshToken);
            tokenInfoCache.setUserId(user.getUserId());
            cacheRepository.setTokenInfo(idmAccessToken, tokenInfoCache, expiresIn);
            return new SignInResponse(user.getUserId(),
                                    idmAccessToken,
                                    refreshToken,
                                    expiresIn,
                                    user.getEmail(),
                                    user.getName(),
                                    user.getIsActive(),
                                    user.getRole());
        } catch (MappingException e) {
            throw new CustomException("Failed to map user to token info.", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CacheException e) {
            throw new CustomException("Failed to store token information in cache.", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new CustomException("An error occurred while refreshing the IDM token.", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token){
        try{
            if (token == null || token.trim().isEmpty()) {
                log.error("Token is null or empty");
                throw new CustomException("Token cannot be null or empty", HttpStatus.BAD_REQUEST);
            }
            String accessToken = cacheRepository.getIdToken(token);

            if (accessToken == null || accessToken.trim().isEmpty()) {
                log.error("Access token not found for the given token: {}", token);
                throw new CustomException("Access token not found for the provided token", HttpStatus.UNAUTHORIZED);
            }
            return true;
        } catch (IllegalArgumentException e) {
            log.error("Invalid token format: {}", e.getMessage(), e);
            throw new CustomException("Invalid token format", HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            log.error("Failed to process JSON while validating token: {}", e.getMessage(), e);
            throw new CustomException("Error processing token", HttpStatus.INTERNAL_SERVER_ERROR);
        }  catch (CustomException e) {
            log.error("Custom exception occurred: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while validating the token: {}", e.getMessage(), e);
            throw new CustomException("An unexpected error occurred while validating the token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Authentication getAuthentication(String token) throws JsonProcessingException {
        TokenInfoCache tokenInfoCache = cacheRepository.getTokenInfo(token);

        final UserDetails userDetails = org.springframework.security.core.userdetails.User//
                .withUsername(tokenInfoCache.getEmail())//
                .password("password")//
                .authorities(Collections.emptyList())
                .accountExpired(false)//
                .accountLocked(false)//
                .credentialsExpired(false)//
                .disabled(false)//
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

    }
}
