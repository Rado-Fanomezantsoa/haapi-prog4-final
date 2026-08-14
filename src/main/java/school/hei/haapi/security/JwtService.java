package school.hei.haapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;

  public JwtService(@Value("${app.jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Parse et valide la signature du token. Lève JwtException si le token est invalide, expiré ou
   * mal signé.
   */
  public Claims parseClaims(String token) throws JwtException {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
