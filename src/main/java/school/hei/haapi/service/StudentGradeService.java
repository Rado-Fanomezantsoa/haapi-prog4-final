package school.hei.haapi.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.exception.BadRequestException;
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

@Service
@RequiredArgsConstructor
public class StudentGradeService {

  private final AppUserRepository appUserRepository;
  private final GradeRepository gradeRepository;
  private final SecurityExpressions securityExpressions;

  @Transactional(readOnly = true)
  public List<GradeDetailDto> getGradesForStudent(String studentIdParam) {
    UUID studentId = parseUuid(studentIdParam, "studentId");

    AppUser student =
        appUserRepository
            .findByIdAndRole(studentId, AppUser.Role.STUDENT)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

    assertCanReadGrades(studentId);

    List<Grade> grades = gradeRepository.findAllByStudentIdWithDetails(studentId);

    String role = securityExpressions.currentUserRole();
    UUID currentUserId = securityExpressions.currentUserId();

    return grades.stream()
        .filter(g -> isCourseRelevantForStudent(g, student))
        .filter(g -> isVisibleForCaller(g, role, currentUserId))
        .map(this::toDto)
        .toList();
  }

  private void assertCanReadGrades(UUID studentId) {
    String role = securityExpressions.currentUserRole();
    if ("ADMIN".equals(role)) {
      return;
    }
    if ("STUDENT".equals(role)) {
      if (!securityExpressions.isSelfStudent(studentId)) {
        throw new ForbiddenException("A student can only view their own grades");
      }
      return;
    }
    if ("TEACHER".equals(role)) {
      return;
    }
    throw new ForbiddenException("Insufficient rights to view grades");
  }

  private boolean isCourseRelevantForStudent(Grade grade, AppUser student) {
    Course course = grade.getExam().getCourseOffering().getCourse();
    Specialization courseSpec = course.getSpecialization();
    if (courseSpec == Specialization.COMMUN) {
      return true;
    }
    Specialization studentSpec = student.getSpecialization();
    return studentSpec != null && studentSpec == courseSpec;
  }

  private boolean isVisibleForCaller(Grade grade, String role, UUID currentUserId) {
    if ("ADMIN".equals(role) || "STUDENT".equals(role)) {
      return true;
    }
    if ("TEACHER".equals(role)) {
      CourseOffering offering = grade.getExam().getCourseOffering();
      if (offering.getTeachers() == null) {
        return false;
      }
      return offering.getTeachers().stream().anyMatch(t -> t.getId().equals(currentUserId));
    }
    return false;
  }

  private GradeDetailDto toDto(Grade grade) {
    Exam exam = grade.getExam();
    Course course = exam.getCourseOffering().getCourse();
    return GradeDetailDto.builder()
        .id(grade.getId().toString())
        .examId(exam.getId().toString())
        .studentId(grade.getStudent().getId().toString())
        .value(grade.getValue().doubleValue())
        .updatedAt(grade.getUpdatedAt())
        .courseRef(course.getRef())
        .courseTitle(course.getTitle())
        .examLabel(exam.getLabel())
        .coefficientNum(exam.getCoefficientNumerator())
        .coefficientDen(exam.getCoefficientDenominator())
        .build();
  }

  private UUID parseUuid(String value, String fieldName) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid UUID for " + fieldName + ": " + value);
    }
  }
}
