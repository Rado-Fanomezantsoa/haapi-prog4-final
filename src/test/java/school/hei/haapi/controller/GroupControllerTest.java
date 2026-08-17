package school.hei.haapi.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.security.JwtAuthenticationFilter;
import school.hei.haapi.security.JwtService;
import school.hei.haapi.security.SecurityConfig;
import school.hei.haapi.security.SecurityExpressions;
import school.hei.haapi.security.TestJwtGenerator;
import school.hei.haapi.service.GroupMembershipService;

@WebMvcTest(controllers = GroupController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  SecurityExpressions.class
})
@ActiveProfiles("test")
class GroupControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GroupMembershipService groupMembershipService;

  @Test
  void getActiveStudents_asAdmin_returns200() throws Exception {
    UUID groupId = UUID.randomUUID();
    when(groupMembershipService.getActiveStudents(groupId))
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
        .perform(
            get("/api/groups/" + groupId + "/students").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].studentRef").value("STD24001"));
  }

  @Test
  void getActiveStudents_asTeacher_returns200() throws Exception {
    UUID groupId = UUID.randomUUID();
    when(groupMembershipService.getActiveStudents(groupId)).thenReturn(List.of());
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "TEACHER");

    mockMvc
        .perform(
            get("/api/groups/" + groupId + "/students").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void getActiveStudents_asStudent_returns403() throws Exception {
    UUID groupId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "STUDENT");

    mockMvc
        .perform(
            get("/api/groups/" + groupId + "/students").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void getActiveStudents_noToken_returns401() throws Exception {
    UUID groupId = UUID.randomUUID();

    mockMvc
        .perform(get("/api/groups/" + groupId + "/students"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void assignStudents_asAdmin_returns200() throws Exception {
    UUID groupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            post("/api/groups/" + groupId + "/students")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentIds\": [\"" + studentId + "\"]}"))
        .andExpect(status().isOk());

    verify(groupMembershipService).assignStudents(eq(groupId), eq(List.of(studentId)));
  }

  @Test
  void assignStudents_asTeacher_returns403() throws Exception {
    UUID groupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "TEACHER");

    mockMvc
        .perform(
            post("/api/groups/" + groupId + "/students")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentIds\": [\"" + studentId + "\"]}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void assignStudents_emptyStudentIds_returns400() throws Exception {
    UUID groupId = UUID.randomUUID();
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            post("/api/groups/" + groupId + "/students")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentIds\": []}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void assignStudents_noToken_returns401() throws Exception {
    UUID groupId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/api/groups/" + groupId + "/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentIds\": []}"))
        .andExpect(status().isUnauthorized());
  }
}
