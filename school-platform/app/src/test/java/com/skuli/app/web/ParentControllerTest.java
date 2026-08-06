package com.skuli.app.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skuli.app.config.SecurityConfig;
import com.skuli.student.api.dto.ParentDto;
import com.skuli.student.internal.service.ParentService;
import com.skuli.student.internal.web.ParentController;
import java.util.LinkedHashMap;
import java.util.Map;
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
 * Web-layer test for the parent slice: admin/teacher access, password required only on create,
 * and delegation to the service.
 */
@WebMvcTest(controllers = ParentController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class ParentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParentService service;

    private static RequestPostProcessor user(String role) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", "default"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static ParentDto dto() {
        return new ParentDto("pmum", "pmum", "Pat", "Mum", "p@example.com", "123456",
                "1 Main St", null, null);
    }

    /** Request JSON as a plain map so the write-only password is included; null omits it. */
    private String requestBody(String password) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", "pmum");
        body.put("username", "pmum");
        body.put("name", "Pat");
        body.put("surname", "Mum");
        body.put("email", "p@example.com");
        body.put("phone", "123456");
        body.put("address", "1 Main St");
        if (password != null) {
            body.put("password", password);
        }
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void create_returns201_forAdmin() throws Exception {
        when(service.create(any())).thenReturn(dto());

        mockMvc.perform(post("/api/v1/parents")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(requestBody("password1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pmum"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void create_withoutPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/parents")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(requestBody(null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void list_isAllowedForTeacherRole() throws Exception {
        mockMvc.perform(get("/api/v1/parents").with(user("teacher")))
                .andExpect(status().isOk());
    }

    @Test
    void list_forStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/parents").with(user("student")))
                .andExpect(status().isForbidden());
    }
}
