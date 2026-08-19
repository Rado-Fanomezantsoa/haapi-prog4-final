package school.hei.haapi.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.dto.reference.ClassGroupDto;
import school.hei.haapi.dto.reference.PromotionDto;
import school.hei.haapi.service.ReferenceService;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

  private final ReferenceService referenceService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
  public ResponseEntity<List<PromotionDto>> list() {
    return ResponseEntity.ok(referenceService.listPromotions());
  }

  @GetMapping("/{promotionId}/groups")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
  public ResponseEntity<List<ClassGroupDto>> groups(@PathVariable UUID promotionId) {
    return ResponseEntity.ok(referenceService.listGroupsByPromotion(promotionId.toString()));
  }
}
