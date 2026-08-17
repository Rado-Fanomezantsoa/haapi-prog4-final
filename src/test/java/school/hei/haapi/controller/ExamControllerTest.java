package school.hei.haapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import school.hei.haapi.dto.exam.ExamDto;
import school.hei.haapi.security.JwtAuthenticationFilter;
import school.hei.haapi.security.JwtService;
import school.hei.haapi.security.SecurityConfig;
import school.hei.haapi.security.SecurityExpressions;
import school.hei.haapi.security.TestJwtGenerator;
import school.hei.haapi.service.ExamService;

@WebMvcTest(controllers = ExamController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  SecurityExpressions.class
})
@ActiveProfiles("test")
class ExamControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ExamService examService;

  @Test
  void create_asAdmin_returns201() throws Exception {
    UUID offeringId = UUID.randomUUID();
    when(examService.create(eq(offeringId), any(), any()))
        .thenReturn(
            ExamDto.builder()
                .id(UUID.randomUUID())
                .courseOfferingId(offeringId)
                .label("Examen final")
                .dateExam(Instant.now())
                .coefficientNum(1)
                .coefficientDen(3)
                .build());

    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            post("/api/course-offerings/" + offeringId + "/exams")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "label": "Examen final",
                      "dateExam": "2026-06-01T09:00:00Z",
                      "coefficientNum": 1,
                      "coefficientDen": 3
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.label").value("Examen final"));
  }

  @Test
  void create_asStudent_returns403() throws Exception {
    UUID offeringId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "STUDENT");

    mockMvc
        .perform(
            post("/api/course-offerings/" + offeringId + "/exams")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "label": "Examen final",
                      "dateExam": "2026-06-01T09:00:00Z",
                      "coefficientNum": 1,
                      "coefficientDen": 3
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void create_noToken_returns401() throws Exception {
    UUID offeringId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/api/course-offerings/" + offeringId + "/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void create_invalidBody_returns400() throws Exception {
    UUID offeringId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            post("/api/course-offerings/" + offeringId + "/exams")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"label\": \"\"}"))
        .andExpect(status().isBadRequest());
  }
}
