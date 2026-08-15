package school.hei.haapi.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("app.jwt.secret", () -> "test-secret-only-used-in-tests-min-32-chars");
  }
}
