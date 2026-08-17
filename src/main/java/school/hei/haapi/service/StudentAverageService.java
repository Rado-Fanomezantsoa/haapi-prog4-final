package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.student.StudentAveragesDto;
import school.hei.haapi.exception.BadRequestException;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.security.SecurityExpressions;
import school.hei.haapi.service.average.CourseAverageCalculator;
import school.hei.haapi.service.average.CourseResult;
import school.hei.haapi.service.average.ExamGradeInput;
import school.hei.haapi.service.average.OverallAverageCalculator;
import school.hei.haapi.service.average.StudentAveragesResult;

@Service
@RequiredArgsConstructor
public class StudentAverageService {

  private final AppUserRepository appUserRepository;
  private final GradeRepository gradeRepository;
  private final SecurityExpressions securityExpressions;

  @Transactional(readOnly = true)
  public StudentAveragesDto getAverages(String studentIdParam) {
    UUID studentId = parseUuid(studentIdParam, "studentId");
    AppUser student = findStudentOrThrow(studentId);
    assertCanReadAverages(studentId);
    return computeAverages(student);
  }

  @Transactional(readOnly = true)
  public StudentAveragesDto getAveragesInternal(UUID studentId) {
    AppUser student = findStudentOrThrow(studentId);
    return computeAverages(student);
  }

  private AppUser findStudentOrThrow(UUID studentId) {
    return appUserRepository
        .findByIdAndRole(studentId, AppUser.Role.STUDENT)
        .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
  }

  private StudentAveragesDto computeAverages(AppUser student) {
    UUID studentId = student.getId();
    List<Grade> grades = gradeRepository.findAllByStudentIdWithDetails(studentId);

    List<Grade> relevant =
        grades.stream().filter(g -> isCourseRelevantForStudent(g, student)).toList();

    Map<String, List<Grade>> byCourse =
        relevant.stream()
            .collect(
                Collectors.groupingBy(g -> g.getExam().getCourseOffering().getCourse().getRef()));

    List<CourseResult> courseResults = new ArrayList<>();
    for (Map.Entry<String, List<Grade>> entry : byCourse.entrySet()) {
      List<Grade> courseGrades = entry.getValue();
      Course course = courseGrades.get(0).getExam().getCourseOffering().getCourse();

      List<ExamGradeInput> inputs =
          courseGrades.stream()
              .map(
                  g -> {
                    Exam exam = g.getExam();
                    return new ExamGradeInput(
                        g.getValue().doubleValue(),
                        exam.getCoefficientNumerator(),
                        exam.getCoefficientDenominator());
                  })
              .toList();

      courseResults.add(
          CourseAverageCalculator.compute(
              course.getRef(), course.getSemesterNumber(), course.getCredits(), inputs));
    }

    StudentAveragesResult computed = OverallAverageCalculator.compute(courseResults);
    return toDto(studentId, computed, courseResults);
  }

  private void assertCanReadAverages(UUID studentId) {
    String role = securityExpressions.currentUserRole();
    if ("ADMIN".equals(role)) {
      return;
    }
    if ("STUDENT".equals(role) && securityExpressions.isSelfStudent(studentId)) {
      return;
    }
    throw new ForbiddenException("Only ADMIN or the student themself can view averages");
  }

  private boolean isCourseRelevantForStudent(Grade grade, AppUser student) {
    Specialization courseSpec = grade.getExam().getCourseOffering().getCourse().getSpecialization();
    if (courseSpec == Specialization.COMMUN) {
      return true;
    }
    Specialization studentSpec = student.getSpecialization();
    return studentSpec != null && studentSpec == courseSpec;
  }

  private StudentAveragesDto toDto(
      UUID studentId, StudentAveragesResult result, List<CourseResult> courseResults) {

    Map<Integer, Integer> creditsBySemester = new HashMap<>();
    for (CourseResult cr : courseResults) {
      if (cr.validated()) {
        creditsBySemester.merge(cr.semesterNumber(), cr.courseCredits(), Integer::sum);
      }
    }

    List<StudentAveragesDto.SemesterAverageDto> perSemester =
        result.perSemester().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(
                e ->
                    StudentAveragesDto.SemesterAverageDto.builder()
                        .semesterNumber(e.getKey())
                        .average(e.getValue())
                        .creditsTaken(creditsBySemester.getOrDefault(e.getKey(), 0))
                        .build())
            .toList();

    List<StudentAveragesDto.YearAverageDto> perYear =
        List.of(
            yearDto("L1", result.perYear().getOrDefault("L1", 0.0), courseResults, 1, 2),
            yearDto("L2", result.perYear().getOrDefault("L2", 0.0), courseResults, 3, 4),
            yearDto("L3", result.perYear().getOrDefault("L3", 0.0), courseResults, 5, 6));

    return StudentAveragesDto.builder()
        .studentId(studentId.toString())
        .perSemester(perSemester)
        .perYear(perYear)
        .overallCursusAverage(result.overallCursusAverage())
        .creditsTaken(result.creditsTaken())
        .expectedCredits(result.expectedCredits())
        .eligibleForDiploma(result.eligibleForDiploma())
        .build();
  }

  private StudentAveragesDto.YearAverageDto yearDto(
      String level, double average, List<CourseResult> courses, int semFrom, int semTo) {
    int credits =
        courses.stream()
            .filter(CourseResult::validated)
            .filter(c -> c.semesterNumber() >= semFrom && c.semesterNumber() <= semTo)
            .mapToInt(CourseResult::courseCredits)
            .sum();
    return StudentAveragesDto.YearAverageDto.builder()
        .academicLevel(level)
        .average(average)
        .creditsTaken(credits)
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
