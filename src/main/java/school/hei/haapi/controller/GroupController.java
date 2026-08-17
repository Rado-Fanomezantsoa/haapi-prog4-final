package school.hei.haapi.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.student.GroupAssignStudentsInputDto;
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.service.GroupMembershipService;

@RestController
@RequestMapping("/api/groups/{groupId}/students")
@RequiredArgsConstructor
public class GroupController {

  private final GroupMembershipService groupMembershipService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<List<StudentDto>> getActiveStudents(@PathVariable UUID groupId) {
    return ResponseEntity.ok(groupMembershipService.getActiveStudents(groupId));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> assignStudents(
      @PathVariable UUID groupId, @Valid @RequestBody GroupAssignStudentsInputDto input) {
    groupMembershipService.assignStudents(groupId, input.getStudentIds());
    return ResponseEntity.ok().build();
  }
}
