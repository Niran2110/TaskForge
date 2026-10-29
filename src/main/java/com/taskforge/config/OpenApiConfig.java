package com.taskforge.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "TaskForge API",
        version = "1.0.0",
        description = "REST API documentation for the TaskForge project management platform."
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local development server"
        )
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Enter your JWT access token."
)
public class OpenApiConfig {

}