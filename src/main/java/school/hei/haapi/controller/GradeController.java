package school.hei.haapi.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.grade.GradeDto;
import school.hei.haapi.dto.grade.GradeHistoryEntryDto;
import school.hei.haapi.dto.grade.GradeUpdateInputDto;
import school.hei.haapi.security.AuthenticatedUser;
import school.hei.haapi.service.GradeService;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @PutMapping("/{gradeId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<GradeDto> update(
      @PathVariable UUID gradeId,
      @Valid @RequestBody GradeUpdateInputDto input,
      @AuthenticationPrincipal AuthenticatedUser principal) {
    return ResponseEntity.ok(gradeService.update(gradeId, input, principal));
  }

  @GetMapping("/{gradeId}/history")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
  public ResponseEntity<List<GradeHistoryEntryDto>> getHistory(
      @PathVariable UUID gradeId, @AuthenticationPrincipal AuthenticatedUser principal) {
    return ResponseEntity.ok(gradeService.getHistory(gradeId, principal));
  }
}
