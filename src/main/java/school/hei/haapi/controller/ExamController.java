package school.hei.haapi.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.exam.ExamDto;
import school.hei.haapi.dto.exam.ExamInputDto;
import school.hei.haapi.security.AuthenticatedUser;
import school.hei.haapi.service.ExamService;

@RestController
@RequestMapping("/api/course-offerings/{offeringId}/exams")
@RequiredArgsConstructor
public class ExamController {

  private final ExamService examService;

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<ExamDto> create(
      @PathVariable UUID offeringId,
      @Valid @RequestBody ExamInputDto input,
      @AuthenticationPrincipal AuthenticatedUser principal) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(examService.create(offeringId, input, principal));
  }
}
