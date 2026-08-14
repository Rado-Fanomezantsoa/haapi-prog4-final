package school.hei.haapi.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SecurityChainTestController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  SecurityExpressions.class
})
@ActiveProfiles("test")
class SecurityChainTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void publicRoute_noToken_returns200() throws Exception {
    mockMvc.perform(get("/api/_test/public")).andExpect(status().isOk());
  }

  @Test
  void protectedRoute_noToken_returns401() throws Exception {
    mockMvc.perform(get("/api/_test/admin-only")).andExpect(status().isUnauthorized());
  }

  @Test
  void protectedRoute_invalidToken_returns401() throws Exception {
    mockMvc
        .perform(get("/api/_test/admin-only").header("Authorization", "Bearer invalid.token.here"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void adminOnly_asAdmin_returns200() throws Exception {
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(get("/api/_test/admin-only").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void adminOnly_asTeacher_returns403() throws Exception {
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "TEACHER");

    mockMvc
        .perform(get("/api/_test/admin-only").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminOnly_asStudent_returns403() throws Exception {
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "STUDENT");

    mockMvc
        .perform(get("/api/_test/admin-only").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminOrTeacher_asTeacher_returns200() throws Exception {
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "TEACHER");

    mockMvc
        .perform(get("/api/_test/admin-or-teacher").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void adminOrTeacher_asStudent_returns403() throws Exception {
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "STUDENT");

    mockMvc
        .perform(get("/api/_test/admin-or-teacher").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void selfAccess_ownId_asStudent_returns200() throws Exception {
    UUID studentId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(studentId, "STUDENT");

    mockMvc
        .perform(
            get("/api/_test/students/" + studentId + "/self")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void selfAccess_otherId_asStudent_returns403() throws Exception {
    UUID ownId = UUID.randomUUID();
    UUID otherId = UUID.randomUUID();

    String token = TestJwtGenerator.generate(ownId, "STUDENT");

    mockMvc
        .perform(
            get("/api/_test/students/" + otherId + "/self")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void selfAccess_anyId_asAdmin_returns200() throws Exception {
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");
    UUID anyId = UUID.randomUUID();

    mockMvc
        .perform(
            get("/api/_test/students/" + anyId + "/self")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }
}
