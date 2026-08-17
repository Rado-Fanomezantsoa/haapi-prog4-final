package school.hei.haapi.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import school.hei.haapi.dto.graduate.GraduateEntryDto;
import school.hei.haapi.dto.graduate.GraduateExportDto;
import school.hei.haapi.model.GraduateExport;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.security.JwtAuthenticationFilter;
import school.hei.haapi.security.JwtService;
import school.hei.haapi.security.SecurityConfig;
import school.hei.haapi.security.SecurityExpressions;
import school.hei.haapi.security.TestJwtGenerator;
import school.hei.haapi.service.GraduateService;

@WebMvcTest(controllers = AdminController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  SecurityExpressions.class
})
@ActiveProfiles("test")
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PromotionRepository promotionRepository;
  @MockBean private GraduateService graduateService;

  @Test
  void promotions_asAdmin_returns200() throws Exception {
    when(promotionRepository.findAll()).thenReturn(List.of());
    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(get("/admin/promotions").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/promotions"));
  }

  @Test
  void promotions_noToken_returns401() throws Exception {
    mockMvc.perform(get("/admin/promotions")).andExpect(status().isUnauthorized());
  }

  @Test
  void graduates_asAdmin_returns200() throws Exception {
    UUID promoId = UUID.randomUUID();
    when(graduateService.listGraduates(eq(promoId.toString()), eq("EL")))
        .thenReturn(
            List.of(
                GraduateEntryDto.builder()
                    .rank(1)
                    .studentRef("STD1")
                    .lastName("Rakoto")
                    .firstName("Jean")
                    .overallAverage(14.5)
                    .creditsTaken(180)
                    .expectedCredits(180)
                    .build()));

    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            get("/admin/promotions/" + promoId + "/graduates")
                .param("specialization", "EL")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/graduates"));
  }

  @Test
  void export_asAdmin_redirects() throws Exception {
    UUID promoId = UUID.randomUUID();
    when(graduateService.exportGraduates(anyString(), anyString()))
        .thenReturn(
            GraduateExportDto.builder()
                .id(UUID.randomUUID().toString())
                .status(GraduateExport.Status.READY)
                .downloadUrl("https://example.com/file.xlsx")
                .specialization(Specialization.EL)
                .build());

    String token = TestJwtGenerator.generate(UUID.randomUUID(), "ADMIN");

    mockMvc
        .perform(
            post("/admin/promotions/" + promoId + "/graduates/export")
                .param("specialization", "EL")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().is3xxRedirection());
  }
}
