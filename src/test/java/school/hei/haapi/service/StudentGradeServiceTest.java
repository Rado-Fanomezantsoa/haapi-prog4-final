package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
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
class StudentGradeServiceTest {

  @Mock private AppUserRepository appUserRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private SecurityExpressions securityExpressions;

  @InjectMocks private StudentGradeService studentGradeService;

  private AppUser student(UUID id, Specialization spec) {
    return AppUser.builder()
        .id(id)
        .role(AppUser.Role.STUDENT)
        .specialization(spec)
        .firstName("A")
        .lastName("B")
        .email("a@hei.school")
        .passwordHash("x")
        .studentRef("STD1")
        .build();
  }

  private Grade grade(UUID studentId, String courseRef, Specialization courseSpec, UUID teacherId) {
    Course course =
        Course.builder()
            .id(UUID.randomUUID())
            .ref(courseRef)
            .title(courseRef)
            .credits(6)
            .semesterNumber(4)
            .specialization(courseSpec)
            .build();

    AppUser teacher =
        AppUser.builder()
            .id(teacherId)
            .role(AppUser.Role.TEACHER)
            .firstName("T")
            .lastName("T")
            .email("t@hei.school")
            .passwordHash("x")
            .build();

    CourseOffering offering =
        CourseOffering.builder()
            .id(UUID.randomUUID())
            .course(course)
            .calendarYear(2025)
            .teachers(List.of(teacher))
            .build();

    Exam exam =
        Exam.builder()
            .id(UUID.randomUUID())
            .label("Final")
            .courseOffering(offering)
            .coefficientNumerator(1)
            .coefficientDenominator(1)
            .build();

    return Grade.builder()
        .id(UUID.randomUUID())
        .exam(exam)
        .student(AppUser.builder().id(studentId).build())
        .value(BigDecimal.valueOf(14))
        .build();
  }

  @Test
  void admin_seesAllRelevantCourses() {
    UUID studentId = UUID.randomUUID();
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId, Specialization.EL)));
    when(securityExpressions.currentUserRole()).thenReturn("ADMIN");
    when(securityExpressions.currentUserId()).thenReturn(UUID.randomUUID());

    Grade prog4 = grade(studentId, "PROG4", Specialization.EL, UUID.randomUUID());
    Grade tn1 = grade(studentId, "TN1", Specialization.TN, UUID.randomUUID());
    Grade sys = grade(studentId, "SYS1", Specialization.COMMUN, UUID.randomUUID());
    when(gradeRepository.findAllByStudentIdWithDetails(studentId))
        .thenReturn(List.of(prog4, tn1, sys));

    List<GradeDetailDto> result = studentGradeService.getGradesForStudent(studentId.toString());

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(g -> "PROG4".equals(g.getCourseRef())));
    assertTrue(result.stream().anyMatch(g -> "SYS1".equals(g.getCourseRef())));
  }

  @Test
  void student_otherId_forbidden() {
    UUID studentId = UUID.randomUUID();
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId, Specialization.EL)));
    when(securityExpressions.currentUserRole()).thenReturn("STUDENT");
    when(securityExpressions.isSelfStudent(studentId)).thenReturn(false);

    assertThrows(
        ForbiddenException.class,
        () -> studentGradeService.getGradesForStudent(studentId.toString()));
  }

  @Test
  void teacher_onlySeesOwnCourses() {
    UUID studentId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    UUID otherTeacher = UUID.randomUUID();

    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId, Specialization.EL)));
    when(securityExpressions.currentUserRole()).thenReturn("TEACHER");
    when(securityExpressions.currentUserId()).thenReturn(teacherId);

    Grade mine = grade(studentId, "PROG4", Specialization.EL, teacherId);
    Grade notMine = grade(studentId, "WEB4", Specialization.EL, otherTeacher);
    when(gradeRepository.findAllByStudentIdWithDetails(studentId))
        .thenReturn(List.of(mine, notMine));

    List<GradeDetailDto> result = studentGradeService.getGradesForStudent(studentId.toString());

    assertEquals(1, result.size());
    assertEquals("PROG4", result.get(0).getCourseRef());
  }

  @Test
  void unknownStudent_notFound() {
    UUID studentId = UUID.randomUUID();
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> studentGradeService.getGradesForStudent(studentId.toString()));
  }
}
