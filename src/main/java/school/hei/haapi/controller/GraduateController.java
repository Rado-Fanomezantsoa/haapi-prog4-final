package school.hei.haapi.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.graduate.GraduateEntryDto;
import school.hei.haapi.dto.graduate.GraduateExportDto;
import school.hei.haapi.service.GraduateService;

@RestController
@RequiredArgsConstructor
public class GraduateController {

  private final GraduateService graduateService;

  @GetMapping("/api/promotions/{promotionId}/graduates")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<GraduateEntryDto>> list(
      @PathVariable UUID promotionId, @RequestParam String specialization) {
    return ResponseEntity.ok(graduateService.listGraduates(promotionId.toString(), specialization));
  }

  @PostMapping("/api/promotions/{promotionId}/graduates/export")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GraduateExportDto> export(
      @PathVariable UUID promotionId, @RequestParam String specialization) {
    GraduateExportDto dto = graduateService.exportGraduates(promotionId.toString(), specialization);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @GetMapping("/api/graduate-exports/{exportId}/download")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> download(@PathVariable UUID exportId) {
    var url = graduateService.getDownloadUrl(exportId.toString());
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url.toString())).build();
  }
}
