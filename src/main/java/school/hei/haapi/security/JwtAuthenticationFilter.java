package school.hei.haapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtre exécuté une fois par requête : extrait le JWT du header Authorization, le valide via
 * JwtService, et peuple le SecurityContext avec un AuthenticatedUser + l'authority ROLE_xxx
 * correspondante.
 *
 * <p>Un token absent, invalide ou expiré ne bloque PAS la requête ici : elle continue sans
 * authentification, et c'est ensuite @PreAuthorize / authorizeHttpRequests (SecurityConfig) qui la
 * rejette en 401/403 si la route l'exige.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String HEADER_NAME = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ROLE_CLAIM = "role";

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(HEADER_NAME);

    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(BEARER_PREFIX.length());

    try {
      Claims claims = jwtService.parseClaims(token);

      UUID userId = UUID.fromString(claims.getSubject());
      String role = claims.get(ROLE_CLAIM, String.class);

      if (role == null || role.isBlank()) {
        log.warn("JWT valide mais sans claim 'role' — requête traitée comme non authentifiée");
        filterChain.doFilter(request, response);
        return;
      }

      AuthenticatedUser principal = new AuthenticatedUser(userId, role);

      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

      var authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authToken);

    } catch (JwtException | IllegalArgumentException e) {
      log.debug("JWT invalide ou mal formé : {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
