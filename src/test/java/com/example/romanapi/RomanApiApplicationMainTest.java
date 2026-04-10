package com.example.romanapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class RomanApiApplicationMainTest {

  @Test
  void applicationStartsAndStops() {
    ConfigurableApplicationContext ctx =
        SpringApplication.run(
            RomanApiApplication.class,
            new String[] {"--server.port=0", "--app.api-key.value=main-test-key"});
    try {
      assertNotNull(ctx.getBean(RomanApiApplication.class));
    } finally {
      ctx.close();
    }
  }
}
