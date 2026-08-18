package school.hei.haapi.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.grade.GradeDto;
import school.hei.haapi.dto.grade.GradeEntryInputDto;
import school.hei.haapi.dto.grade.GradeHistoryEntryDto;
import school.hei.haapi.dto.grade.GradeUpdateInputDto;
import school.hei.haapi.exception.ConflictException;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.*;
import school.hei.haapi.repository.*;
import school.hei.haapi.security.AuthenticatedUser;

@Service
@RequiredArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final ExamRepository examRepository;
  private final AppUserRepository appUserRepository;
  private final CourseOfferingRepository courseOfferingRepository;
  private final GradeHistoryRepository gradeHistoryRepository;

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

  @Transactional
  public GradeDto update(UUID gradeId, GradeUpdateInputDto input, AuthenticatedUser principal) {
    Grade grade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(() -> new NotFoundException("Grade not found: " + gradeId));

    CourseOffering offering = grade.getExam().getCourseOffering();
    assertTeacherAssignedOrAdmin(offering.getId(), principal);

    AppUser modifiedBy = getCurrentAppUser(principal);
    BigDecimal previousValue = grade.getValue();

    GradeHistory history =
        GradeHistory.builder()
            .grade(grade)
            .previousValue(previousValue)
            .newValue(input.getValue())
            .reason(input.getReason())
            .modifiedBy(modifiedBy)
            .build();
    gradeHistoryRepository.save(history);

    grade.setValue(input.getValue());
    return toDto(gradeRepository.save(grade));
  }

  public List<GradeHistoryEntryDto> getHistory(UUID gradeId, AuthenticatedUser principal) {
    Grade grade =
        gradeRepository
            .findById(gradeId)
            .orElseThrow(() -> new NotFoundException("Grade not found: " + gradeId));

    if (principal.isStudent()) {
      if (!grade.getStudent().getId().equals(principal.id())) {
        throw new ForbiddenException("You can only view the history of your own grades");
      }
    } else if (principal.isTeacher()) {
      assertTeacherAssignedOrAdmin(grade.getExam().getCourseOffering().getId(), principal);
    }

    return gradeHistoryRepository.findByGrade_IdOrderByModifiedAtDesc(gradeId).stream()
        .map(this::toDto)
        .toList();
  }

  private GradeHistoryEntryDto toDto(GradeHistory history) {
    return GradeHistoryEntryDto.builder()
        .id(history.getId())
        .previousValue(history.getPreviousValue())
        .newValue(history.getNewValue())
        .reason(history.getReason())
        .modifiedBy(history.getModifiedBy().getId())
        .modifiedAt(history.getModifiedAt())
        .build();
  }
}
