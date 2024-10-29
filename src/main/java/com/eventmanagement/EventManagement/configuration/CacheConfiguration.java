package com.eventmanagement.EventManagement.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class CacheConfiguration {

    @Value("${redis.issl:false}")
    private boolean isSSl;

    @Value("${redis.hostname:localhost}")
    private String host;

    @Value("${redis.port:6379}")
    private int port;

    @Value("${redis.timeout:2000}")
    private int timeout;

    @Value("${redis.auth:auth}")
    private String auth;

    @Bean
    public JedisPool prepareJedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool;
        if (isSSl) {
            jedisPool = new JedisPool(jedisPoolConfig,
                    host,
                    port,
                    timeout,
                    auth,
                    isSSl);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port);
        }
        return jedisPool;
    }

}
