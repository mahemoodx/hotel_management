package com.royalpearl.hotel.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "Royal Pearl Hyderabad – Hotel Management API",
        version     = "1.0",
        description = "REST backend for Royal Pearl Hyderabad luxury hotel at Road No. 12, Banjara Hills, Hyderabad.",
        contact     = @Contact(
            name  = "Royal Pearl Reservations",
            email = "reservations@royalpearlhyderabad.com",
            url   = "https://royalpearlhyderabad.com"
        )
    ),
    servers = {
        @Server(url = "/", description = "Current server")
    }
)
@SecurityScheme(
    name   = "bearerAuth",
    type   = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
}
