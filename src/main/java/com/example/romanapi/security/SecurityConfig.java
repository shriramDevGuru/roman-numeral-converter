package com.example.romanapi.security;

import com.example.romanapi.logging.RequestLoggingFilter;
import com.example.romanapi.metrics.ApiMetrics;
import com.example.romanapi.metrics.RomanMetricsFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(ApiKeyProperties.class)
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      ApiKeyProperties apiKey,
      ObjectMapper objectMapper,
      ApiMetrics metrics)
      throws Exception {
    ApiKeyAuthFilter apiKeyFilter =
        new ApiKeyAuthFilter(apiKey.header(), apiKey.value(), objectMapper);
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(new RequestLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new RomanMetricsFilter(metrics), ApiKeyAuthFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/actuator/metrics/**")
                    .hasRole("API")
                    .requestMatchers("/__it/**")
                    .hasRole("API")
                    .requestMatchers("/romannumeral")
                    .hasRole("API")
                    .anyRequest()
                    .denyAll())
        .httpBasic(httpBasic -> httpBasic.disable());

    return http.build();
  }
}
