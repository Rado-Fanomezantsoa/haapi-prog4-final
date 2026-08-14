package school.hei.haapi.security;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityExpressions")
public class SecurityExpressions {

  public UUID currentUserId() {
    var principal =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.id();
  }

  public String currentUserRole() {
    var principal =
        (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return principal.role();
  }

  public boolean isSelfStudent(UUID studentId) {
    if (!"STUDENT".equals(currentUserRole())) {
      return false;
    }
    return currentUserId().equals(studentId);
  }

  public boolean teachesCourseOfferingOf(UUID studentId) {
    // throw new UnsupportedOperationException
    return false;
  }

  public boolean isOwnGrade(UUID gradeId) {
    // throw new UnsupportedOperationException
    return false;
  }

  public boolean ownsGrade(UUID gradeId) {
    // throw new UnsupportedOperationException
    return false;
  }
}
