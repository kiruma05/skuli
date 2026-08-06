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
import com.skuli.communication.api.dto.EventDto;
import com.skuli.communication.internal.service.EventService;
import com.skuli.communication.internal.web.EventController;
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
 * Web-layer test for a representative communication resource, covering the admin/teacher rule.
 */
@WebMvcTest(controllers = EventController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService service;

    private static RequestPostProcessor user(String role) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", "default"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private static EventDto dto() {
        return new EventDto(1, "Sports Day", "Annual sports day",
                Instant.parse("2026-05-01T09:00:00Z"), Instant.parse("2026-05-01T15:00:00Z"), null);
    }

    @Test
    void create_returns201_forAdmin() throws Exception {
        when(service.create(any())).thenReturn(dto());

        mockMvc.perform(post("/api/v1/events")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Sports Day"));
    }

    @Test
    void list_isAllowedForTeacherRole() throws Exception {
        mockMvc.perform(get("/api/v1/events").with(user("teacher")))
                .andExpect(status().isOk());
    }

    @Test
    void list_forStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/events").with(user("student")))
                .andExpect(status().isForbidden());
    }
}
