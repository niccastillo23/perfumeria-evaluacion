package com.evaluacion.backend.bff;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(properties = {
    "app.security.enabled=true",
    "AZURE_ISSUER_URI=https://login.microsoftonline.com/test-tenant/v2.0",
    "app.security.audience=api://test-api"
})
@AutoConfigureMockMvc
class BffSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @TestConfiguration
    static class StubDownstreamConfig {
        @Bean
        @Primary
        RestTemplate stubRestTemplate() {
            return new RestTemplate() {
                @Override
                @SuppressWarnings("unchecked")
                public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) {
                    return (ResponseEntity<T>) ResponseEntity.ok(List.of());
                }
            };
        }
    }

    @Test
    void catalog_withoutToken_returns401() throws Exception {
        int status = mockMvc.perform(get("/api/v1/shop/catalog"))
            .andReturn().getResponse().getStatus();

        assertEquals(401, status);
    }

    @Test
    void admin_withoutRequiredScope_returns403() throws Exception {
        int status = mockMvc.perform(get("/api/v1/admin/stats")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Catalog.Read"))))
            .andReturn().getResponse().getStatus();

        assertEquals(403, status);
    }

    @Test
    void catalog_withRequiredScope_returns200() throws Exception {
        int status = mockMvc.perform(get("/api/v1/shop/catalog")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Catalog.Read"))))
            .andReturn().getResponse().getStatus();

        assertEquals(200, status);
    }

    @Test
    void admin_withRequiredScope_returns200() throws Exception {
        int status = mockMvc.perform(get("/api/v1/admin/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_Admin.Read"))))
            .andReturn().getResponse().getStatus();

        assertEquals(200, status);
    }

    @Test
    void health_isPublic() throws Exception {
        int status = mockMvc.perform(get("/api/v1/health"))
            .andReturn().getResponse().getStatus();

        assertEquals(200, status);
    }
}
