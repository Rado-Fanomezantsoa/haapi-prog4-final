package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.student.StudentAveragesDto;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseOffering;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.security.SecurityExpressions;

@ExtendWith(MockitoExtension.class)
class StudentAverageServiceTest {

  @Mock private AppUserRepository appUserRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private SecurityExpressions securityExpressions;

  @InjectMocks private StudentAverageService studentAverageService;

  private Grade grade(UUID studentId, String ref, int semester, int credits, double value) {
    Course course =
        Course.builder()
            .id(UUID.randomUUID())
            .ref(ref)
            .title(ref)
            .credits(credits)
            .semesterNumber(semester)
            .specialization(Specialization.COMMUN)
            .build();
    CourseOffering offering =
        CourseOffering.builder().id(UUID.randomUUID()).course(course).calendarYear(2025).build();
    Exam exam =
        Exam.builder()
            .id(UUID.randomUUID())
            .label("E")
            .courseOffering(offering)
            .coefficientNumerator(1)
            .coefficientDenominator(1)
            .build();
    return Grade.builder()
        .id(UUID.randomUUID())
        .exam(exam)
        .student(AppUser.builder().id(studentId).build())
        .value(BigDecimal.valueOf(value))
        .build();
  }

  @Test
  void admin_computesAverage_excludesFailedCourse() {
    UUID studentId = UUID.randomUUID();
    AppUser student =
        AppUser.builder()
            .id(studentId)
            .role(AppUser.Role.STUDENT)
            .specialization(Specialization.EL)
            .firstName("A")
            .lastName("B")
            .email("a@x")
            .passwordHash("x")
            .build();

    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student));
    when(securityExpressions.currentUserRole()).thenReturn("ADMIN");
    when(gradeRepository.findAllByStudentIdWithDetails(studentId))
        .thenReturn(
            List.of(grade(studentId, "PROG4", 4, 6, 16.0), grade(studentId, "TN1", 4, 5, 8.0)));

    StudentAveragesDto dto = studentAverageService.getAverages(studentId.toString());

    assertEquals(16.0, dto.getOverallCursusAverage());
    assertEquals(6, dto.getCreditsTaken());
    assertFalse(dto.isEligibleForDiploma());
  }

  @Test
  void teacher_forbidden() {
    UUID studentId = UUID.randomUUID();
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(
            Optional.of(
                AppUser.builder()
                    .id(studentId)
                    .role(AppUser.Role.STUDENT)
                    .firstName("A")
                    .lastName("B")
                    .email("a@x")
                    .passwordHash("x")
                    .build()));
    when(securityExpressions.currentUserRole()).thenReturn("TEACHER");

    assertThrows(
        ForbiddenException.class, () -> studentAverageService.getAverages(studentId.toString()));
  }
}
