package com.example.romanapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  static final String API_KEY_SCHEME_NAME = "ApiKeyAuth";

  @Bean
  public OpenAPI openApi(@Value("${app.api-key.header:x-api-key}") String apiKeyHeaderName) {
    SecurityScheme apiKeyScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name(apiKeyHeaderName);

    return new OpenAPI()
        .components(new Components().addSecuritySchemes(API_KEY_SCHEME_NAME, apiKeyScheme))
        // Mark endpoints as requiring API key by default; Swagger UI "Authorize" will apply it.
        .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME_NAME));
  }
}

