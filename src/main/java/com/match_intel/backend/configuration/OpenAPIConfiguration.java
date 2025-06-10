package com.match_intel.backend.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();
        server.setUrl("http://localhost:8080/api");
        server.setDescription("Development");

        Info information = new Info()
                .title("Match Intel API")
                .version("1.0")
                .description("This API exposes endpoints to Match Intel system backend.");


        Components components = new Components()
                .addSecuritySchemes
                        ("Bearer Authentication", createAPIKeyScheme())

                .addSchemas(
                        "MessageResponse",
                        new Schema<Map<String, String>>()
                                .addProperty("message", new StringSchema()
                                        .description("Appropriate response message"))
                )
                .addSchemas(
                        "RegisterResponse",
                        new Schema<Map<String, String>>()
                                .addProperty("sessionToken", new StringSchema()
                                        .description("Registration session token")
                                        .example("af4c19dd-e13f-4d72-84a5-a4328eb7c44c"))
                )

                .addSchemas(
                        "EmailRequest",
                        new Schema<Map<String, String>>()
                                .addProperty("email", new StringSchema()
                                        .description("User's email address")
                                        .example("example@example.com"))
                );

        return new OpenAPI()
                .info(information)
                .servers(List.of(server))
                .addSecurityItem(new SecurityRequirement().
                        addList("Bearer Authentication"))
                .components(components);
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
