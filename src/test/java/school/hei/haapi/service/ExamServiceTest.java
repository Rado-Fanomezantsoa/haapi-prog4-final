package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.exam.ExamDto;
import school.hei.haapi.dto.exam.ExamInputDto;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.*;
import school.hei.haapi.repository.CourseOfferingRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

  @Mock private ExamRepository examRepository;
  @Mock private CourseOfferingRepository courseOfferingRepository;

  @InjectMocks private ExamService examService;

  private CourseOffering offering(UUID id, List<AppUser> teachers) {
    Course course =
        Course.builder()
            .id(UUID.randomUUID())
            .ref("PROG4")
            .title("Programmation avancée")
            .credits(5)
            .semesterNumber(4)
            .specialization(Specialization.EL)
            .build();
    return CourseOffering.builder()
        .id(id)
        .course(course)
        .calendarYear(2025)
        .teachers(teachers)
        .build();
  }

  private ExamInputDto input() {
    return new ExamInputDto("Examen final", Instant.now(), 1, 3);
  }

  @Test
  void admin_canCreateExam() {
    UUID offeringId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    AuthenticatedUser admin = new AuthenticatedUser(adminId, "ADMIN");

    when(courseOfferingRepository.findById(offeringId))
        .thenReturn(Optional.of(offering(offeringId, List.of())));
    when(examRepository.save(any()))
        .thenAnswer(
            invocation -> {
              Exam e = invocation.getArgument(0);
              e.setId(UUID.randomUUID());
              return e;
            });

    ExamDto result = examService.create(offeringId, input(), admin);

    assertEquals("Examen final", result.getLabel());
    assertEquals(1, result.getCoefficientNum());
    assertEquals(3, result.getCoefficientDen());
  }

  @Test
  void assignedTeacher_canCreateExam() {
    UUID offeringId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    AuthenticatedUser teacher = new AuthenticatedUser(teacherId, "TEACHER");

    when(courseOfferingRepository.findById(offeringId))
        .thenReturn(Optional.of(offering(offeringId, List.of())));
    when(courseOfferingRepository.existsByIdAndTeachers_Id(offeringId, teacherId)).thenReturn(true);
    when(examRepository.save(any()))
        .thenAnswer(
            invocation -> {
              Exam e = invocation.getArgument(0);
              e.setId(UUID.randomUUID());
              return e;
            });

    ExamDto result = examService.create(offeringId, input(), teacher);

    assertEquals("Examen final", result.getLabel());
  }

  @Test
  void unassignedTeacher_forbidden() {
    UUID offeringId = UUID.randomUUID();
    UUID teacherId = UUID.randomUUID();
    AuthenticatedUser teacher = new AuthenticatedUser(teacherId, "TEACHER");

    when(courseOfferingRepository.findById(offeringId))
        .thenReturn(Optional.of(offering(offeringId, List.of())));
    when(courseOfferingRepository.existsByIdAndTeachers_Id(offeringId, teacherId))
        .thenReturn(false);

    assertThrows(ForbiddenException.class, () -> examService.create(offeringId, input(), teacher));
  }

  @Test
  void unknownOffering_notFound() {
    UUID offeringId = UUID.randomUUID();
    AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), "ADMIN");

    when(courseOfferingRepository.findById(offeringId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> examService.create(offeringId, input(), admin));
  }
}
