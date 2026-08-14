package school.hei.haapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.assign.AssignGroupInputDto;
import school.hei.haapi.dto.assign.AssignTeacherInputDto;
import school.hei.haapi.dto.course.CourseOfferingDto;
import school.hei.haapi.dto.course.CourseOfferingInputDto;
import school.hei.haapi.service.CourseOfferingService;

@RestController
@RequestMapping("/api/course-offerings")
@RequiredArgsConstructor
public class CourseOfferingController {

  private final CourseOfferingService courseOfferingService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CourseOfferingDto> create(
      @Valid @RequestBody CourseOfferingInputDto input) {
    CourseOfferingDto created = courseOfferingService.create(input);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/{offeringId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<CourseOfferingDto> getById(@PathVariable String offeringId) {
    return ResponseEntity.ok(courseOfferingService.getById(offeringId));
  }

  @PostMapping("/{offeringId}/teachers")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CourseOfferingDto> addTeacher(
      @PathVariable String offeringId, @Valid @RequestBody AssignTeacherInputDto input) {
    return ResponseEntity.ok(courseOfferingService.addTeacher(offeringId, input.getTeacherId()));
  }

  @PostMapping("/{offeringId}/groups")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CourseOfferingDto> addGroup(
      @PathVariable String offeringId, @Valid @RequestBody AssignGroupInputDto input) {
    return ResponseEntity.ok(courseOfferingService.addGroup(offeringId, input.getGroupId()));
  }
}
