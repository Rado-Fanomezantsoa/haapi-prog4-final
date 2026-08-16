package school.hei.haapi.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.dto.student.StudentAveragesDto;
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.service.StudentAverageService;
import school.hei.haapi.service.StudentGradeService;
import school.hei.haapi.service.StudentService;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;
  private final StudentGradeService studentGradeService;
  private final StudentAverageService studentAverageService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<List<StudentDto>> listStudents(
      @RequestParam(required = false) String promotionId,
      @RequestParam(required = false) String groupId) {
    return ResponseEntity.ok(studentService.findStudents(promotionId, groupId));
  }

  @GetMapping("/{studentId}/grades")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('TEACHER') or @securityExpressions.isSelfStudent(#studentId)")
  public ResponseEntity<List<GradeDetailDto>> getGrades(@PathVariable UUID studentId) {
    return ResponseEntity.ok(studentGradeService.getGradesForStudent(studentId.toString()));
  }

  @GetMapping("/{studentId}/averages")
  @PreAuthorize("hasRole('ADMIN') or @securityExpressions.isSelfStudent(#studentId)")
  public ResponseEntity<StudentAveragesDto> getAverages(@PathVariable UUID studentId) {
    return ResponseEntity.ok(studentAverageService.getAverages(studentId.toString()));
  }
}
