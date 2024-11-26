package com.match_intel.backend.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
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
        server.setUrl("http://localhost:8080");
        server.setDescription("Development");

        Info information = new Info()
                .title("Match Intel API")
                .version("1.0")
                .description("This API exposes endpoints to Match Intel system backend.");

        Components components = new Components()
                .addSchemas(
                        "MessageResponse",
                        new Schema<Map<String, String>>()
                                .addProperty("message", new StringSchema())
                )
                .addSchemas(
                        "RegisterResponse",
                        new Schema<Map<String, String>>()
                                .addProperty("sessionToken", new StringSchema()
                                        .example("af4c19dd-e13f-4d72-84a5-a4328eb7c44c"))
                );

        return new OpenAPI()
                .info(information)
                .servers(List.of(server))
                .components(components);
    }
}
