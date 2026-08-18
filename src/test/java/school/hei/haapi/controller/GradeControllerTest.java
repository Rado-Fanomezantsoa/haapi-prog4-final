package school.hei.haapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import school.hei.haapi.dto.grade.GradeDto;
import school.hei.haapi.security.JwtAuthenticationFilter;
import school.hei.haapi.security.JwtService;
import school.hei.haapi.security.SecurityConfig;
import school.hei.haapi.security.SecurityExpressions;
import school.hei.haapi.security.TestJwtGenerator;
import school.hei.haapi.service.GradeService;

@WebMvcTest(controllers = GradeController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  SecurityExpressions.class
})
@ActiveProfiles("test")
class GradeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GradeService gradeService;

  @Test
  void update_asAdmin_returns200() throws Exception {
    UUID gradeId = UUID.randomUUID();
    when(gradeService.update(eq(gradeId), any(), any()))
        .thenReturn(
            GradeDto.builder()
                .id(gradeId)
                .examId(UUID.randomUUID())
                .studentId(UUID.randomUUID())
                .value(BigDecimal.valueOf(15))
                .build());

    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            put("/api/grades/" + gradeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": 15, \"reason\": \"Erreur de saisie\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(15));
  }

  @Test
  void update_missingReason_returns400() throws Exception {
    UUID gradeId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            put("/api/grades/" + gradeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": 15}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void update_asStudent_returns403() throws Exception {
    UUID gradeId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "STUDENT");

    mockMvc
        .perform(
            put("/api/grades/" + gradeId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": 15, \"reason\": \"Erreur\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void getHistory_asStudent_returns200() throws Exception {
    UUID gradeId = UUID.randomUUID();
    when(gradeService.getHistory(eq(gradeId), any())).thenReturn(List.of());

    String token = TestJwtGenerator.generate(UUID.randomUUID(), "STUDENT");

    mockMvc
        .perform(
            get("/api/grades/" + gradeId + "/history").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void getHistory_noToken_returns401() throws Exception {
    UUID gradeId = UUID.randomUUID();

    mockMvc
        .perform(get("/api/grades/" + gradeId + "/history"))
        .andExpect(status().isUnauthorized());
  }
}
