package school.hei.haapi.controller;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import school.hei.haapi.dto.graduate.GraduateEntryDto;
import school.hei.haapi.dto.graduate.GraduateExportDto;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.service.GraduateService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final PromotionRepository promotionRepository;
  private final GraduateService graduateService;

  @GetMapping("/promotions")
  public String listPromotions(Model model) {
    List<Promotion> promotions =
        promotionRepository.findAll().stream()
            .sorted(Comparator.comparingInt(Promotion::getExpectedGraduationYear).reversed())
            .toList();
    model.addAttribute("promotions", promotions);
    return "admin/promotions";
  }

  @GetMapping("/promotions/{promotionId}/graduates")
  public String listGraduates(
      @PathVariable String promotionId, @RequestParam String specialization, Model model) {
    List<GraduateEntryDto> graduates = graduateService.listGraduates(promotionId, specialization);
    model.addAttribute("graduates", graduates);
    model.addAttribute("promotionId", promotionId);
    model.addAttribute("specialization", specialization);
    return "admin/graduates";
  }

  @PostMapping("/promotions/{promotionId}/graduates/export")
  public String exportGraduates(
      @PathVariable String promotionId,
      @RequestParam String specialization,
      RedirectAttributes redirectAttributes) {
    GraduateExportDto export = graduateService.exportGraduates(promotionId, specialization);
    redirectAttributes.addFlashAttribute(
        "exportMessage", "Export " + export.getStatus() + " — id=" + export.getId());
    if (export.getDownloadUrl() != null) {
      redirectAttributes.addFlashAttribute("downloadUrl", export.getDownloadUrl());
    }
    return "redirect:/admin/promotions/"
        + promotionId
        + "/graduates?specialization="
        + specialization;
  }
}
