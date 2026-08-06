package com.skuli.app.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skuli.academics.api.dto.ResultDto;
import com.skuli.academics.internal.service.ResultService;
import com.skuli.academics.internal.web.ResultController;
import com.skuli.app.config.SecurityConfig;
import com.skuli.common.error.BusinessRuleException;
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
 * Web-layer test for the result slice: happy path, the exam-XOR-assignment rule surfaced as a
 * 409 problem+json, and the admin/teacher security rule.
 */
@WebMvcTest(controllers = ResultController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "keycloak.issuer-uri=http://localhost:9/realms/test")
class ResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResultService service;

    private static RequestPostProcessor user(String role) {
        return jwt()
                .jwt(builder -> builder.claim("tenant_id", "default"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void create_returns201_forAdmin() throws Exception {
        ResultDto body = new ResultDto(null, 85, 10, null, "student-1");
        when(service.create(any())).thenReturn(new ResultDto(1, 85, 10, null, "student-1"));

        mockMvc.perform(post("/api/v1/results")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_violatingXorRule_returns409() throws Exception {
        when(service.create(any()))
                .thenThrow(new BusinessRuleException("exactly one of exam or assignment"));

        mockMvc.perform(post("/api/v1/results")
                        .with(user("admin"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ResultDto(null, 85, 10, 20, "student-1"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Business rule violation"));
    }

    @Test
    void list_forStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/results").with(user("student")))
                .andExpect(status().isForbidden());
    }
}
