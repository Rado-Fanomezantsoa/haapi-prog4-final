package school.hei.haapi.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.grade.GradeDto;
import school.hei.haapi.dto.grade.GradeEntryInputDto;
import school.hei.haapi.exception.ConflictException;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.CourseOffering;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.CourseOfferingRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.security.AuthenticatedUser;

@Service
@RequiredArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final ExamRepository examRepository;
  private final AppUserRepository appUserRepository;
  private final CourseOfferingRepository courseOfferingRepository;

  @Transactional
  public List<GradeDto> bulkCreate(
      UUID examId, List<GradeEntryInputDto> entries, AuthenticatedUser principal) {
    Exam exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new NotFoundException("Exam not found: " + examId));

    CourseOffering offering = exam.getCourseOffering();
    assertTeacherAssignedOrAdmin(offering.getId(), principal);

    AppUser enteredBy = getCurrentAppUser(principal);

    return entries.stream().map(entry -> createOne(exam, entry, enteredBy)).toList();
  }

  private GradeDto createOne(Exam exam, GradeEntryInputDto entry, AppUser enteredBy) {
    AppUser student =
        appUserRepository
            .findByIdAndRole(entry.getStudentId(), AppUser.Role.STUDENT)
            .orElseThrow(() -> new NotFoundException("Student not found: " + entry.getStudentId()));

    if (gradeRepository.findByExam_IdAndStudent_Id(exam.getId(), student.getId()).isPresent()) {
      throw new ConflictException(
          "Grade already exists for student "
              + student.getId()
              + " on exam "
              + exam.getId()
              + " — use PUT /grades/{gradeId} to modify it");
    }

    Grade grade =
        Grade.builder()
            .exam(exam)
            .student(student)
            .value(entry.getValue())
            .enteredBy(enteredBy)
            .build();

    return toDto(gradeRepository.save(grade));
  }

  private void assertTeacherAssignedOrAdmin(UUID offeringId, AuthenticatedUser principal) {
    if (principal.isTeacher()
        && !courseOfferingRepository.existsByIdAndTeachers_Id(offeringId, principal.id())) {
      throw new ForbiddenException("You are not assigned as a teacher on this course-offering");
    }
  }

  private AppUser getCurrentAppUser(AuthenticatedUser principal) {
    return appUserRepository
        .findById(principal.id())
        .orElseThrow(
            () -> new NotFoundException("Authenticated user not found: " + principal.id()));
  }

  private GradeDto toDto(Grade grade) {
    return GradeDto.builder()
        .id(grade.getId())
        .examId(grade.getExam().getId())
        .studentId(grade.getStudent().getId())
        .value(grade.getValue())
        .updatedAt(grade.getUpdatedAt())
        .build();
  }
}
