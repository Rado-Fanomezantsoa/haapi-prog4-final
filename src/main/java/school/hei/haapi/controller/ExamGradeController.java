package school.hei.haapi.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.grade.GradeDto;
import school.hei.haapi.dto.grade.GradeEntryInputDto;
import school.hei.haapi.security.AuthenticatedUser;
import school.hei.haapi.service.GradeService;

@RestController
@RequestMapping("/api/exams/{examId}/grades")
@RequiredArgsConstructor
@Validated
public class ExamGradeController {

  private final GradeService gradeService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<List<GradeDto>> bulkCreate(
      @PathVariable UUID examId,
      @NotEmpty @Valid @RequestBody List<GradeEntryInputDto> entries,
      @AuthenticationPrincipal AuthenticatedUser principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(gradeService.bulkCreate(examId, entries, principal));
  }
}
