package com.skuli.app.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skuli.academics.api.dto.LessonDto;
import com.skuli.academics.internal.service.LessonService;
import com.skuli.academics.internal.web.LessonController;
import com.skuli.app.config.SecurityConfig;
import com.skuli.common.domain.Day;
import java.time.Instant;
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
 * Web-layer test for a representative academics CRUD resource, also covering the admin/teacher
 * security rule newly applied to lessons/exams/assignments/results/attendance.
 */
@WebMvcTest(controllers = LessonController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService service;

    private static RequestPostProcessor user(String role) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", "default"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static LessonDto dto() {
        return new LessonDto(1, "Algebra", Day.MONDAY, Instant.parse("2026-01-01T09:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"), 1, 2, "teacher-1");
    }

    @Test
    void create_returns201_forAdmin() throws Exception {
        when(service.create(any())).thenReturn(dto());

        mockMvc.perform(post("/api/v1/lessons")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Algebra"));
    }

    @Test
    void list_isAllowedForTeacherRole() throws Exception {
        mockMvc.perform(get("/api/v1/lessons").with(user("teacher")))
                .andExpect(status().isOk());
    }

    @Test
    void list_forStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/lessons").with(user("student")))
                .andExpect(status().isForbidden());
    }
}
