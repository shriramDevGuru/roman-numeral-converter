package com.example.romanapi.config;

import com.example.romanapi.security.ApiKeyProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  static final String API_KEY_SCHEME_NAME = "ApiKeyAuth";

  @Bean
  public OpenAPI openApi(ApiKeyProperties apiKey) {
    SecurityScheme apiKeyScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name(apiKey.header());

    return new OpenAPI()
        .components(new Components().addSecuritySchemes(API_KEY_SCHEME_NAME, apiKeyScheme))
        // Mark endpoints as requiring API key by default; Swagger UI "Authorize" will apply it.
        .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME_NAME));
  }
}
