package school.hei.haapi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

public class TestJwtGenerator {
  // DOIT être identique à app.jwt.secret dans application-test.properties
  private static final String SECRET = "test-secret-only-used-in-tests-min-32-chars";

  public static void main(String[] args) {
    System.out.println("ADMIN token:");
    System.out.println(generate(UUID.randomUUID(), "ADMIN"));

    System.out.println("\nTEACHER token:");
    System.out.println(generate(UUID.randomUUID(), "TEACHER"));

    System.out.println("\nSTUDENT token:");
    System.out.println(generate(UUID.randomUUID(), "STUDENT"));
  }

  public static String generate(UUID userId, String role) {
    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .subject(userId.toString())
        .claim("role", role)
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
        .signWith(key)
        .compact();
  }
}
