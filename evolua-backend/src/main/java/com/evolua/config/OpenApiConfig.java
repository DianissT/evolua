package com.evolua.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Metadatos de la documentación OpenAPI y esquema de autenticación por token JWT.
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "API de Evolua", version = "v1",
        description = "Servicios REST de la plataforma de aprendizaje adaptativo Evolua."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
