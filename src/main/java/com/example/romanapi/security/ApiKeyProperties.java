package com.example.romanapi.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.api-key")
public record ApiKeyProperties(@NotBlank String header, String value) {}

