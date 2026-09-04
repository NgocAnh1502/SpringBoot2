package com.example.usermanagement.client;

import com.example.usermanagement.constants.MessageKey;
import com.example.usermanagement.exception.DuplicateResourceException;
import com.example.usermanagement.exception.KeycloakIntegrationException;
import com.example.usermanagement.exception.ResourceNotFoundException;
import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KeycloakAdminClient {
    private final RestClient keycloakTokenClient;
    private final RestClient keycloakAdminApiClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${keycloak.admin-client-secret}")
    private String adminClientSecret;

    public KeycloakAdminClient(RestClient keycloakTokenClient, RestClient keycloakAdminApiClient) {
        this.keycloakTokenClient = keycloakTokenClient;
        this.keycloakAdminApiClient = keycloakAdminApiClient;
    }

    private String getAdminAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", adminClientId);
        form.add("client_secret", adminClientSecret);

        JsonNode response = keycloakTokenClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new KeycloakIntegrationException(MessageKey.ERROR_KEYCLOAK_TOKEN_NULL);
        }
        return response.get("access_token").asString();
    }

    private HttpHeaders authHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminAccessToken());
        return headers;
    }

    public String createUser(String username, String email, String password) {
        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );
        Map<String, Object> body = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(credential)
        );

        var response = keycloakAdminApiClient.post()
                .uri("/{realm}/users", realm)
                .headers(h -> h.addAll(authHeader()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, res) -> {
                    throw new DuplicateResourceException(MessageKey.ERROR_USERNAME_DUPLICATE, username);
                })
                .toBodilessEntity();

        String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (location == null) {
            throw new KeycloakIntegrationException(MessageKey.ERROR_KEYCLOAK_LOCATION_NULL);
        }
        return location.substring(location.lastIndexOf('/') + 1);
    }

    public JsonNode getUserById(String id) {
        return keycloakAdminApiClient.get()
                .uri("/{realm}/users/{id}", realm, id)
                .headers(h -> h.addAll(authHeader()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new ResourceNotFoundException(MessageKey.ERROR_USER_NOT_FOUND, id);
                })
                .body(JsonNode.class);
    }

    public void updatePassword(String id, String newPassword) {
        getUserById(id);

        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false
        );

        keycloakAdminApiClient.put()
                .uri("/{realm}/users/{id}/reset-password", realm, id)
                .headers(h -> h.addAll(authHeader()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(credential)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteUser(String id) {
        getUserById(id); // ném ResourceNotFoundException nếu không tồn tại

        keycloakAdminApiClient.delete()
                .uri("/{realm}/users/{id}", realm, id)
                .headers(h -> h.addAll(authHeader()))
                .retrieve()
                .toBodilessEntity();
    }

    public List<JsonNode> searchUsers(String username, int first, int max) {
        return keycloakAdminApiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{realm}/users")
                        .queryParamIfPresent("username", java.util.Optional.ofNullable(username))
                        .queryParam("first", first)
                        .queryParam("max", max)
                        .build(realm))
                .headers(h -> h.addAll(authHeader()))
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<JsonNode>>() {});
    }

    public long countUsers(String username) {
        Long count = keycloakAdminApiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{realm}/users/count")
                        .queryParamIfPresent("username", java.util.Optional.ofNullable(username))
                        .build(realm))
                .headers(h -> h.addAll(authHeader()))
                .retrieve()
                .body(Long.class);
        return count != null ? count : 0L;
    }
}
