package com.example.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI userManagementOpenAPI() {
        final String securitySchemeName = "keycloak_oauth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl("http://172.16.16.50:8082/realms/usermanagementrealm/protocol/openid-connect/auth")
                                                        .tokenUrl("http://172.16.16.50:8082/realms/usermanagementrealm/protocol/openid-connect/token")
                                                        .scopes(new Scopes()
                                                                .addString("openid", "openid")
                                                                .addString("profile", "profile"))
                                                )
                                        )
                        )
                )
                .info(new Info().title("User Management API").version("1.0"));
    }
}
