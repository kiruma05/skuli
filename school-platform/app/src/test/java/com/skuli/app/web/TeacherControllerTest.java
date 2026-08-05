package com.skuli.app.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skuli.common.domain.UserSex;
import com.skuli.staff.api.dto.TeacherDto;
import com.skuli.staff.internal.service.TeacherService;
import com.skuli.staff.internal.web.TeacherController;
import com.skuli.app.config.SecurityConfig;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Web-layer test for the teacher slice. Verifies admin/teacher access, that a password is required
 * only on create (the OnCreate validation group), and that the service is delegated to.
 */
@WebMvcTest(controllers = TeacherController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeacherService service;

    private static RequestPostProcessor user(String role) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", "default"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static TeacherDto dto(String password) {
        return new TeacherDto("jdoe", "jdoe", "John", "Doe", "j@example.com", "123456",
                "1 Main St", null, "O+", UserSex.MALE, Instant.parse("1990-01-01T00:00:00Z"),
                Set.of(1), password, null);
    }

    /**
     * Request JSON built as a plain map so the password is included — serialising the DTO would
     * drop it (WRITE_ONLY). Pass {@code null} to omit the password entirely.
     */
    private String requestBody(String password) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", "jdoe");
        body.put("username", "jdoe");
        body.put("name", "John");
        body.put("surname", "Doe");
        body.put("email", "j@example.com");
        body.put("phone", "123456");
        body.put("address", "1 Main St");
        body.put("bloodType", "O+");
        body.put("sex", "MALE");
        body.put("birthday", "1990-01-01T00:00:00Z");
        body.put("subjectIds", List.of(1));
        if (password != null) {
            body.put("password", password);
        }
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void create_returns201_forAdmin() throws Exception {
        when(service.create(any())).thenReturn(dto(null)); // response never carries the password

        mockMvc.perform(post("/api/v1/teachers")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(requestBody("password1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("jdoe"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void create_withoutPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/teachers")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(requestBody(null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void get_isAllowedForTeacherRole() throws Exception {
        when(service.get("jdoe")).thenReturn(dto(null));

        mockMvc.perform(get("/api/v1/teachers/jdoe").with(user("teacher")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surname").value("Doe"));
    }

    @Test
    void get_forStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/teachers/jdoe").with(user("student")))
                .andExpect(status().isForbidden());
    }
}
