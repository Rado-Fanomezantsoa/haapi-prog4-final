package school.hei.haapi.security;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String role) {

  public boolean isAdmin() {
    return "ADMIN".equals(role);
  }

  public boolean isTeacher() {
    return "TEACHER".equals(role);
  }

  public boolean isStudent() {
    return "STUDENT".equals(role);
  }
}
