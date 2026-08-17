package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.graduate.GraduateEntryDto;
import school.hei.haapi.dto.student.StudentAveragesDto;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.GraduateExportRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.security.SecurityExpressions;

@ExtendWith(MockitoExtension.class)
class GraduateServiceTest {

  @Mock private PromotionRepository promotionRepository;
  @Mock private AppUserRepository appUserRepository;
  @Mock private GraduateExportRepository graduateExportRepository;
  @Mock private StudentAverageService studentAverageService;
  @Mock private BucketComponent bucketComponent;
  @Mock private SecurityExpressions securityExpressions;

  @InjectMocks private GraduateService graduateService;

  @Test
  void listGraduates_onlyEligible_sortedByAverage() {
    UUID promoId = UUID.randomUUID();
    when(promotionRepository.findById(promoId)).thenReturn(Optional.of(new Promotion()));

    AppUser a =
        AppUser.builder()
            .id(UUID.randomUUID())
            .studentRef("STD1")
            .firstName("A")
            .lastName("Zed")
            .role(AppUser.Role.STUDENT)
            .specialization(Specialization.EL)
            .email("a@x")
            .passwordHash("x")
            .build();
    AppUser b =
        AppUser.builder()
            .id(UUID.randomUUID())
            .studentRef("STD2")
            .firstName("B")
            .lastName("Alpha")
            .role(AppUser.Role.STUDENT)
            .specialization(Specialization.EL)
            .email("b@x")
            .passwordHash("x")
            .build();

    when(appUserRepository.findByRoleAndPromotion_IdAndSpecialization(
            AppUser.Role.STUDENT, promoId, Specialization.EL))
        .thenReturn(List.of(a, b));

    when(studentAverageService.getAveragesInternal(a.getId()))
        .thenReturn(
            StudentAveragesDto.builder()
                .overallCursusAverage(14.0)
                .creditsTaken(180)
                .expectedCredits(180)
                .eligibleForDiploma(true)
                .build());
    when(studentAverageService.getAveragesInternal(b.getId()))
        .thenReturn(
            StudentAveragesDto.builder()
                .overallCursusAverage(16.0)
                .creditsTaken(180)
                .expectedCredits(180)
                .eligibleForDiploma(true)
                .build());

    List<GraduateEntryDto> result = graduateService.listGraduates(promoId.toString(), "EL");

    assertEquals(2, result.size());
    assertEquals(1, result.get(0).getRank());
    assertEquals("STD2", result.get(0).getStudentRef()); // 16 > 14
    assertEquals(2, result.get(1).getRank());
  }
}
