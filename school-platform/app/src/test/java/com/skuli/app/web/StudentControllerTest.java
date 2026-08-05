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
import com.skuli.common.domain.UserSex;
import com.skuli.student.api.dto.StudentDto;
import com.skuli.student.internal.service.StudentService;
import com.skuli.student.internal.web.StudentController;
import java.time.Instant;
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
 * Web-layer test for the student slice: admin/teacher access, password required only on create,
 * and delegation to the service.
 */
@WebMvcTest(controllers = StudentController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService service;

    private static RequestPostProcessor user(String role) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", "default"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static StudentDto dto() {
        return new StudentDto("sjones", "sjones", "Sam", "Jones", "s@example.com", "123456",
                "1 Main St", null, "A+", UserSex.FEMALE, Instant.parse("2010-01-01T00:00:00Z"),
                "parent-1", 2, 1, null, null);
    }

    /** Request JSON as a plain map so the write-only password is included; null omits it. */
    private String requestBody(String password) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", "sjones");
        body.put("username", "sjones");
        body.put("name", "Sam");
        body.put("surname", "Jones");
        body.put("email", "s@example.com");
        body.put("phone", "123456");
        body.put("address", "1 Main St");
        body.put("bloodType", "A+");
        body.put("sex", "FEMALE");
        body.put("birthday", "2010-01-01T00:00:00Z");
        body.put("parentId", "parent-1");
        body.put("classId", 2);
        body.put("gradeId", 1);
        if (password != null) {
            body.put("password", password);
        }
        return objectMapper.writeValueAsString(body);
    }

    @Test
    void create_returns201_forAdmin() throws Exception {
        when(service.create(any())).thenReturn(dto());

        mockMvc.perform(post("/api/v1/students")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(requestBody("password1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("sjones"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void create_withoutPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/students")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(requestBody(null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void get_isAllowedForTeacherRole() throws Exception {
        when(service.get("sjones")).thenReturn(dto());

        mockMvc.perform(get("/api/v1/students/sjones").with(user("teacher")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.surname").value("Jones"));
    }

    @Test
    void get_forStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/students/sjones").with(user("student")))
                .andExpect(status().isForbidden());
    }
}
