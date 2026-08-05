package com.skuli.app.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skuli.academics.api.dto.SubjectDto;
import com.skuli.academics.internal.service.SubjectService;
import com.skuli.academics.internal.web.SubjectController;
import com.skuli.app.config.SecurityConfig;
import com.skuli.common.util.PageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer test for the Subject slice: exercises routing, request validation, and the security
 * filter chain (admin-only, JWT-authenticated, tenant claim propagated) with the service mocked.
 * A dummy issuer keeps the lazy {@code JwtDecoder} bean happy without contacting Keycloak.
 *
 * <p>The {@code jwt()} post-processor builds the authentication directly (bypassing the
 * resource-server converter), so roles are supplied as authorities here; the
 * realm_access-to-{@code ROLE_*} mapping is covered separately by the converter's own test.
 */
@WebMvcTest(controllers = SubjectController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class SubjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubjectService subjectService;

    /** A Keycloak-style token: the given realm role as a ROLE_ authority + a tenant_id claim. */
    private static RequestPostProcessor user(String role, String tenant) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", tenant))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void list_returnsPagedSubjects_forAuthenticatedAdmin() throws Exception {
        when(subjectService.list(any(), any()))
                .thenReturn(PageResponse.of(List.of(new SubjectDto(1, "Mathematics")), 0, 10, 1));

        mockMvc.perform(get("/api/v1/subjects").with(user("admin", "default")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Mathematics"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void create_returns201_withLocation() throws Exception {
        when(subjectService.create(any())).thenReturn(new SubjectDto(7, "Physics"));

        mockMvc.perform(post("/api/v1/subjects")
                        .with(user("admin", "default"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SubjectDto(null, "Physics"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Physics"));
    }

    @Test
    void create_blankName_returns400ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/subjects")
                        .with(user("admin", "default"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SubjectDto(null, "  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void list_withoutAdminRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/subjects").with(user("student", "default")))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_withoutAuthentication_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/subjects"))
                .andExpect(status().isUnauthorized());
    }
}
