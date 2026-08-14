package school.hei.haapi.security;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityChainTestController {

  @GetMapping("/api/_test/public")
  public String publicRoute() {
    return "public";
  }

  @GetMapping("/api/_test/admin-only")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminOnly() {
    return "admin";
  }

  @GetMapping("/api/_test/admin-or-teacher")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public String adminOrTeacher() {
    return "admin-or-teacher";
  }

  @GetMapping("/api/_test/students/{studentId}/self")
  @PreAuthorize("hasRole('ADMIN') or @securityExpressions.isSelfStudent(#studentId)")
  public String selfAccess(@PathVariable UUID studentId) {
    return "self";
  }
}
