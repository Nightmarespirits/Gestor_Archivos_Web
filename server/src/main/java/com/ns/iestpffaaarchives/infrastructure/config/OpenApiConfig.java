package com.ns.iestpffaaarchives.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IESTPFFAA Archives API")
                        .description("API para gestión de documentos y archivos del IESTPFFAA")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Administrador del Sistema")
                                .email("admin@iestpffaa.edu.pe")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT con el prefijo Bearer: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
