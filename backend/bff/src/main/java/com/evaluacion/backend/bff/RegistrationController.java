package com.evaluacion.backend.bff;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final RestTemplate restTemplate;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;

    public RegistrationController(
            RestTemplate restTemplate,
            @Value("${app.registration.tenant-id:}") String tenantId,
            @Value("${app.registration.client-id:}") String clientId,
            @Value("${app.registration.client-secret:}") String clientSecret) {
        this.restTemplate = restTemplate;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Map<String, Object> body) {
        String displayName = text(body.get("displayName"));
        String email = text(body.get("email"));
        String password = text(body.get("password"));

        if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Nombre, correo y contrasena son obligatorios."));
        }

        if (tenantId.isBlank() || clientId.isBlank() || clientSecret.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "El registro no esta configurado en el servidor."));
        }

        try {
            String accessToken = getGraphToken();

            Map<String, Object> graphUser = Map.of(
                "accountEnabled", true,
                "displayName", displayName,
                "mailNickname", email.split("@")[0],
                "userPrincipalName", email,
                "passwordProfile", Map.of(
                    "forceChangePasswordNextSignIn", false,
                    "password", password
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.postForEntity(
                "https://graph.microsoft.com/v1.0/users",
                new HttpEntity<>(graphUser, headers),
                Object.class
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario creado correctamente.", "email", email));
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("error", "No se pudo crear el usuario.", "detail", e.getResponseBodyAsString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error inesperado al registrar.", "detail", e.getMessage()));
        }
    }

    private String getGraphToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", "https://graph.microsoft.com/.default");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token",
            new HttpEntity<>(form, headers),
            Map.class
        );

        Object token = response.getBody() != null ? response.getBody().get("access_token") : null;
        if (token == null) {
            throw new IllegalStateException("No se obtuvo token de Microsoft Graph.");
        }
        return token.toString();
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
