package school.hei.haapi.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.security.TestJwtGenerator;

/** IT locaux (Postgres Testcontainers via FacadeIT). Ne pas lancer contre Poja preprod. */
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndpointsIT extends FacadeIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private AppUserRepository appUserRepository;

  @BeforeEach
  void seedAdminForBasicAuth() {
    if (appUserRepository.findByEmail("admin@hei.school").isEmpty()) {
      appUserRepository.save(
          AppUser.builder()
              .email("admin@hei.school")
              .passwordHash("password") // NoOpPasswordEncoder
              .firstName("Ada")
              .lastName("Admin")
              .role(AppUser.Role.ADMIN)
              .build());
    }
  }

  @Test
  void adminPromotions_withBasicAuth_returns200() throws Exception {
    mockMvc
        .perform(get("/admin/promotions").with(httpBasic("admin@hei.school", "password")))
        .andExpect(status().isOk());
  }

  @Test
  void adminPromotions_withBadBasicAuth_returns401() throws Exception {
    mockMvc
        .perform(get("/admin/promotions").with(httpBasic("admin@hei.school", "wrong")))
        .andExpect(status().isUnauthorized());
  }

  private String bearer(String role) {
    return "Bearer " + TestJwtGenerator.generate(UUID.randomUUID(), role);
  }

  @Test
  void listStudents_noToken_returns401() throws Exception {
    mockMvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
  }

  @Test
  void listStudents_asAdmin_returns200() throws Exception {
    mockMvc
        .perform(get("/api/students").header("Authorization", bearer("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void listStudents_asTeacher_returns200() throws Exception {
    mockMvc
        .perform(get("/api/students").header("Authorization", bearer("TEACHER")))
        .andExpect(status().isOk());
  }

  @Test
  void listStudents_asStudent_returns403() throws Exception {
    mockMvc
        .perform(get("/api/students").header("Authorization", bearer("STUDENT")))
        .andExpect(status().isForbidden());
  }

  // ---------- GET /api/students/{id}/grades|averages ----------

  @Test
  void getGrades_unknownStudent_returns404() throws Exception {
    UUID unknown = UUID.randomUUID();
    mockMvc
        .perform(
            get("/api/students/" + unknown + "/grades").header("Authorization", bearer("ADMIN")))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAverages_unknownStudent_returns404() throws Exception {
    UUID unknown = UUID.randomUUID();
    mockMvc
        .perform(
            get("/api/students/" + unknown + "/averages").header("Authorization", bearer("ADMIN")))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAverages_asTeacher_returns403() throws Exception {
    UUID anyId = UUID.randomUUID();
    mockMvc
        .perform(
            get("/api/students/" + anyId + "/averages").header("Authorization", bearer("TEACHER")))
        .andExpect(status().isForbidden());
  }

  // ---------- Transcripts ----------

  @Test
  void getTranscriptRequest_unknown_returns404() throws Exception {
    mockMvc
        .perform(
            get("/api/transcript-requests/" + UUID.randomUUID())
                .header("Authorization", bearer("ADMIN")))
        .andExpect(status().isNotFound());
  }

  // ---------- Graduates ----------

  @Test
  void listGraduates_unknownPromotion_returns404() throws Exception {
    mockMvc
        .perform(
            get("/api/promotions/" + UUID.randomUUID() + "/graduates")
                .param("specialization", "EL")
                .header("Authorization", bearer("ADMIN")))
        .andExpect(status().isNotFound());
  }

  @Test
  void listGraduates_asStudent_returns403() throws Exception {
    mockMvc
        .perform(
            get("/api/promotions/" + UUID.randomUUID() + "/graduates")
                .param("specialization", "EL")
                .header("Authorization", bearer("STUDENT")))
        .andExpect(status().isForbidden());
  }

  // ---------- Admin Thymeleaf ----------

  @Test
  void adminPromotions_noToken_returns401() throws Exception {
    mockMvc.perform(get("/admin/promotions")).andExpect(status().isUnauthorized());
  }

  @Test
  void adminPromotions_asAdmin_returnsHtml() throws Exception {
    mockMvc
        .perform(get("/admin/promotions").header("Authorization", bearer("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Promotions")));
  }

  @Test
  void adminPromotions_asStudent_returns403() throws Exception {
    mockMvc
        .perform(get("/admin/promotions").header("Authorization", bearer("STUDENT")))
        .andExpect(status().isForbidden());
  }

  @Test
  void listPromotions_asAdmin_returns200() throws Exception {
    mockMvc
        .perform(get("/api/promotions").header("Authorization", bearer("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void listCourses_asStudent_returns200() throws Exception {
    mockMvc
        .perform(get("/api/courses").header("Authorization", bearer("STUDENT")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void listGroups_unknownPromotion_returns404() throws Exception {
    mockMvc
        .perform(
            get("/api/promotions/" + UUID.randomUUID() + "/groups")
                .header("Authorization", bearer("ADMIN")))
        .andExpect(status().isNotFound());
  }

  @Test
  void listPromotions_noToken_returns401() throws Exception {
    mockMvc.perform(get("/api/promotions")).andExpect(status().isUnauthorized());
  }
}
