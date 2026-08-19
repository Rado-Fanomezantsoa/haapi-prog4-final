package school.hei.haapi.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.reference.CourseDto;
import school.hei.haapi.service.ReferenceService;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

  private final ReferenceService referenceService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
  public ResponseEntity<List<CourseDto>> list() {
    return ResponseEntity.ok(referenceService.listCourses());
  }
}
