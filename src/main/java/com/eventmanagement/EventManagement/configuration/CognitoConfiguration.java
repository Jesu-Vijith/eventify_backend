package com.eventmanagement.EventManagement.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class CognitoConfiguration {
    @Value(value = "${COGNITO_REGION:us-east-1}")
    private String clientRegion;

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient(){
        return CognitoIdentityProviderClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(clientRegion))
                .build();
    }


}
