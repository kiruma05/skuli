package com.skuli.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata + a global HTTP Bearer (JWT) security scheme, so Swagger UI shows an
 * "Authorize" button. Paste a Keycloak access token there and it is sent as
 * {@code Authorization: Bearer <token>} on every request, matching how the resource server
 * authenticates.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI schoolPlatformOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Skuli School Platform API")
                        .version("v1")
                        .description("Tenant-scoped REST API for the school platform. "
                                + "Authorize with a Keycloak access token (realm role admin/"
                                + "teacher/student/parent)."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
