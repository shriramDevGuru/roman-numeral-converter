package com.example.romanapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

  @Test
  void registersApiKeySecurityScheme() {
    OpenApiConfig cfg = new OpenApiConfig();
    var openApi = cfg.openApi("x-api-key");

    assertNotNull(
        openApi.getComponents().getSecuritySchemes().get(OpenApiConfig.API_KEY_SCHEME_NAME));
    SecurityScheme scheme =
        openApi.getComponents().getSecuritySchemes().get(OpenApiConfig.API_KEY_SCHEME_NAME);
    assertEquals(SecurityScheme.In.HEADER, scheme.getIn());
    assertEquals("x-api-key", scheme.getName());
    assertEquals(SecurityScheme.Type.APIKEY, scheme.getType());
    assertEquals(1, openApi.getSecurity().size());
  }
}
