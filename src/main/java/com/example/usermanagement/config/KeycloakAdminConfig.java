package com.example.usermanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakAdminConfig {
    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Bean
    public RestClient keycloakTokenClient(){
        return RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    @Bean
    public RestClient keycloakAdminApiClient() {
        return RestClient.builder()
                .baseUrl(serverUrl + "/admin/realms")
                .build();
    }
}
