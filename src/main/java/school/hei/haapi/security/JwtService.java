package school.hei.haapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.AppUser;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {

    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    this.expirationMs = expirationMs;
  }

  public String generateToken(AppUser user) {

    Date now = new Date();
    Date expiration = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("role", user.getRole().name())
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }

  public Claims parseClaims(String token) throws JwtException {

    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
