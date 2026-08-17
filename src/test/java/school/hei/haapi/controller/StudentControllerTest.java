package school.hei.haapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.security.JwtAuthenticationFilter;
import school.hei.haapi.security.JwtService;
import school.hei.haapi.security.SecurityConfig;
import school.hei.haapi.security.SecurityExpressions;
import school.hei.haapi.security.TestJwtGenerator;
import school.hei.haapi.service.StudentAverageService;
import school.hei.haapi.service.StudentGradeService;
import school.hei.haapi.service.StudentService;
import school.hei.haapi.service.TranscriptService;

@WebMvcTest(controllers = StudentController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  SecurityExpressions.class
})
@ActiveProfiles("test")
class StudentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private StudentService studentService;
  @MockBean private StudentGradeService studentGradeService;
  @MockBean private StudentAverageService studentAverageService;
  @MockBean private TranscriptService transcriptService;

  @Test
  void listStudents_asAdmin_returns200() throws Exception {
    when(studentService.findStudents(isNull(), isNull()))
        .thenReturn(
            List.of(
                StudentDto.builder()
                    .id(UUID.randomUUID().toString())
                    .studentRef("STD24001")
                    .firstName("Jean")
                    .lastName("Rakoto")
                    .specialization(Specialization.EL)
                    .build()));

    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(get("/api/students").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].studentRef").value("STD24001"));
  }

  @Test
  void listStudents_asTeacher_returns200() throws Exception {
    when(studentService.findStudents(any(), any())).thenReturn(List.of());
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "TEACHER");

    mockMvc
        .perform(get("/api/students").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void listStudents_asStudent_returns403() throws Exception {
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "STUDENT");

    mockMvc
        .perform(get("/api/students").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void listStudents_noToken_returns401() throws Exception {
    mockMvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
  }

  @Test
  void listStudents_withPromotionFilter_passesQueryParam() throws Exception {
    UUID promoId = UUID.randomUUID();
    when(studentService.findStudents(any(), any())).thenReturn(List.of());
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            get("/api/students")
                .param("promotionId", promoId.toString())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void getGrades_asAdmin_returns200() throws Exception {
    UUID studentId = UUID.randomUUID();
    when(studentGradeService.getGradesForStudent(studentId.toString()))
        .thenReturn(
            List.of(
                GradeDetailDto.builder()
                    .id(UUID.randomUUID().toString())
                    .courseRef("PROG4")
                    .value(14.0)
                    .build()));

    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            get("/api/students/" + studentId + "/grades")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].courseRef").value("PROG4"));
  }

  @Test
  void getGrades_noToken_returns401() throws Exception {
    mockMvc
        .perform(get("/api/students/" + UUID.randomUUID() + "/grades"))
        .andExpect(status().isUnauthorized());
  }
}
