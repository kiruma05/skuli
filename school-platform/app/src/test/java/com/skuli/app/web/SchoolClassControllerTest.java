package com.skuli.app.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skuli.academics.api.dto.SchoolClassDto;
import com.skuli.academics.internal.service.SchoolClassService;
import com.skuli.academics.internal.web.SchoolClassController;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Web-layer test for the class-group slice. Classes are reachable by admin OR teacher, so this
 * verifies the teacher path is allowed (unlike subjects, which are admin-only) and that an
 * unprivileged role is rejected.
 */
@WebMvcTest(controllers = SchoolClassController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class SchoolClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SchoolClassService service;

    private static RequestPostProcessor user(String role) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", "default"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void list_isAllowedForTeacher() throws Exception {
        when(service.list(any(), any()))
                .thenReturn(PageResponse.of(List.of(new SchoolClassDto(1, "1A", 30, null, 1)), 0, 10, 1));

        mockMvc.perform(get("/api/v1/classes").with(user("teacher")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("1A"));
    }

    @Test
    void create_returns201_forAdmin() throws Exception {
        when(service.create(any())).thenReturn(new SchoolClassDto(7, "2B", 25, null, 2));

        mockMvc.perform(post("/api/v1/classes")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SchoolClassDto(null, "2B", 25, null, 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void create_invalidCapacity_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/classes")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SchoolClassDto(null, "2B", 0, null, 2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacity").exists());
    }

    @Test
    void list_forStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/classes").with(user("student")))
                .andExpect(status().isForbidden());
    }
}
