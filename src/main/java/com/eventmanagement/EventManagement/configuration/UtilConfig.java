package com.eventmanagement.EventManagement.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class UtilConfig {
    @Bean
    @Primary
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Value("${cors.url}")
    private String corsUrl;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        System.out.println(corsUrl);
        config.setAllowCredentials(false);
        config.setAllowedOrigins(Collections.singletonList(corsUrl));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept","Authorization","ApiKey"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}
