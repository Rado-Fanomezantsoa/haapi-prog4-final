package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import school.hei.haapi.dto.grade.GradeDto;
import school.hei.haapi.dto.grade.GradeEntryInputDto;
import school.hei.haapi.exception.ConflictException;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseOffering;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.CourseOfferingRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private ExamRepository examRepository;
  @Mock private AppUserRepository appUserRepository;
  @Mock private CourseOfferingRepository courseOfferingRepository;

  @InjectMocks private GradeService gradeService;

  private Exam exam(UUID examId, UUID offeringId, List<AppUser> teachers) {
    Course course =
        Course.builder()
            .id(UUID.randomUUID())
            .ref("PROG4")
            .title("Programmation avancée")
            .credits(5)
            .semesterNumber(4)
            .specialization(Specialization.EL)
            .build();
    CourseOffering offering =
        CourseOffering.builder()
            .id(offeringId)
            .course(course)
            .calendarYear(2025)
            .teachers(teachers)
            .build();
    return Exam.builder()
        .id(examId)
        .courseOffering(offering)
        .label("Final")
        .coefficientNumerator(1)
        .coefficientDenominator(1)
        .build();
  }

  private AppUser student(UUID id) {
    return AppUser.builder()
        .id(id)
        .role(AppUser.Role.STUDENT)
        .studentRef("STD1")
        .firstName("A")
        .lastName("B")
        .email("a@hei.school")
        .passwordHash("x")
        .build();
  }

  private AppUser adminUser(UUID id) {
    return AppUser.builder()
        .id(id)
        .role(AppUser.Role.ADMIN)
        .firstName("Admin")
        .lastName("A")
        .email("admin@hei.school")
        .passwordHash("x")
        .build();
  }

  @Test
  void admin_bulkCreatesGrades() {
    UUID examId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    AuthenticatedUser admin = new AuthenticatedUser(adminId, "ADMIN");

    when(examRepository.findById(examId))
        .thenReturn(Optional.of(exam(examId, offeringId, List.of())));
    when(appUserRepository.findById(adminId)).thenReturn(Optional.of(adminUser(adminId)));
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId)));
    when(gradeRepository.findByExam_IdAndStudent_Id(examId, studentId))
        .thenReturn(Optional.empty());
    when(gradeRepository.save(any()))
        .thenAnswer(
            invocation -> {
              Grade g = invocation.getArgument(0);
              g.setId(UUID.randomUUID());
              return g;
            });

    List<GradeDto> result =
        gradeService.bulkCreate(
            examId, List.of(new GradeEntryInputDto(studentId, BigDecimal.valueOf(14))), admin);

    assertEquals(1, result.size());
    assertEquals(BigDecimal.valueOf(14), result.get(0).getValue());
  }

  @Test
  void unassignedTeacher_forbidden() {
    UUID examId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    AuthenticatedUser teacher = new AuthenticatedUser(teacherId, "TEACHER");

    when(examRepository.findById(examId))
        .thenReturn(Optional.of(exam(examId, offeringId, List.of())));
    when(courseOfferingRepository.existsByIdAndTeachers_Id(offeringId, teacherId))
        .thenReturn(false);

    assertThrows(
        ForbiddenException.class,
        () ->
            gradeService.bulkCreate(
                examId,
                List.of(new GradeEntryInputDto(UUID.randomUUID(), BigDecimal.valueOf(14))),
                teacher));
  }

  @Test
  void duplicateGrade_conflict() {
    UUID examId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    AuthenticatedUser admin = new AuthenticatedUser(adminId, "ADMIN");

    when(examRepository.findById(examId))
        .thenReturn(Optional.of(exam(examId, offeringId, List.of())));
    when(appUserRepository.findById(adminId)).thenReturn(Optional.of(adminUser(adminId)));
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId)));
    when(gradeRepository.findByExam_IdAndStudent_Id(examId, studentId))
        .thenReturn(Optional.of(Grade.builder().id(UUID.randomUUID()).build()));

    assertThrows(
        ConflictException.class,
        () ->
            gradeService.bulkCreate(
                examId, List.of(new GradeEntryInputDto(studentId, BigDecimal.valueOf(14))), admin));
  }

  @Test
  void unknownStudent_notFound() {
    UUID examId = UUID.randomUUID();
    UUID offeringId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    AuthenticatedUser admin = new AuthenticatedUser(adminId, "ADMIN");

    when(examRepository.findById(examId))
        .thenReturn(Optional.of(exam(examId, offeringId, List.of())));
    when(appUserRepository.findById(adminId)).thenReturn(Optional.of(adminUser(adminId)));
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () ->
            gradeService.bulkCreate(
                examId, List.of(new GradeEntryInputDto(studentId, BigDecimal.valueOf(14))), admin));
  }

  @Test
  void unknownExam_notFound() {
    UUID examId = UUID.randomUUID();
    AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), "ADMIN");

    when(examRepository.findById(examId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () ->
            gradeService.bulkCreate(
                examId,
                List.of(new GradeEntryInputDto(UUID.randomUUID(), BigDecimal.valueOf(14))),
                admin));
  }
}
