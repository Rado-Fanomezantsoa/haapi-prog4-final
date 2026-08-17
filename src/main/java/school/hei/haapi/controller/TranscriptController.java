package school.hei.haapi.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.student.TranscriptRequestDto;
import school.hei.haapi.service.TranscriptService;

@RestController
@RequestMapping("/api/transcript-requests")
@RequiredArgsConstructor
public class TranscriptController {

  private final TranscriptService transcriptService;

  @GetMapping("/{requestId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
  public ResponseEntity<TranscriptRequestDto> get(@PathVariable UUID requestId) {
    return ResponseEntity.ok(transcriptService.getRequest(requestId.toString()));
  }
}
