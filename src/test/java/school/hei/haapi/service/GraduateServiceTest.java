package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.graduate.GraduateEntryDto;
import school.hei.haapi.dto.graduate.GraduateExportDto;
import school.hei.haapi.dto.student.StudentAveragesDto;
import school.hei.haapi.exception.BadRequestException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.GraduateExport;
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

  private AppUser student(UUID id, String ref, Specialization spec) {
    return AppUser.builder()
        .id(id)
        .studentRef(ref)
        .firstName("Jean")
        .lastName("Rakoto")
        .email(ref + "@hei.school")
        .passwordHash("x")
        .role(AppUser.Role.STUDENT)
        .specialization(spec)
        .build();
  }

  @Test
  void listGraduates_onlyEligible_sortedByAverageDesc() {
    UUID promoId = UUID.randomUUID();
    when(promotionRepository.findById(promoId)).thenReturn(Optional.of(new Promotion()));

    AppUser a = student(UUID.randomUUID(), "STD1", Specialization.EL);
    AppUser b = student(UUID.randomUUID(), "STD2", Specialization.EL);

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
    assertEquals("STD2", result.get(0).getStudentRef());
    assertEquals(2, result.get(1).getRank());
    assertEquals("STD1", result.get(1).getStudentRef());
  }

  @Test
  void listGraduates_excludesNotEligible() {
    UUID promoId = UUID.randomUUID();
    when(promotionRepository.findById(promoId)).thenReturn(Optional.of(new Promotion()));

    AppUser a = student(UUID.randomUUID(), "STD1", Specialization.EL);
    when(appUserRepository.findByRoleAndPromotion_IdAndSpecialization(
            AppUser.Role.STUDENT, promoId, Specialization.EL))
        .thenReturn(List.of(a));
    when(studentAverageService.getAveragesInternal(a.getId()))
        .thenReturn(
            StudentAveragesDto.builder()
                .overallCursusAverage(9.0)
                .creditsTaken(100)
                .expectedCredits(180)
                .eligibleForDiploma(false)
                .build());

    List<GraduateEntryDto> result = graduateService.listGraduates(promoId.toString(), "EL");

    assertEquals(0, result.size());
  }

  @Test
  void listGraduates_unknownPromotion_throwsNotFound() {
    UUID promoId = UUID.randomUUID();
    when(promotionRepository.findById(promoId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> graduateService.listGraduates(promoId.toString(), "EL"));
  }

  @Test
  void listGraduates_invalidSpecialization_throwsBadRequest() {
    assertThrows(
        BadRequestException.class,
        () -> graduateService.listGraduates(UUID.randomUUID().toString(), "COMMUN"));
  }

  @Test
  void exportGraduates_uploadsAndReturnsReady() throws Exception {
    UUID promoId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();

    Promotion promo =
        Promotion.builder()
            .id(promoId)
            .code("K")
            .entryCalendarYear(2024)
            .expectedGraduationYear(2027)
            .build();
    AppUser admin =
        AppUser.builder()
            .id(adminId)
            .role(AppUser.Role.ADMIN)
            .email("admin@hei.school")
            .firstName("Ad")
            .lastName("Min")
            .passwordHash("x")
            .build();

    when(promotionRepository.findById(promoId)).thenReturn(Optional.of(promo));
    when(appUserRepository.findByRoleAndPromotion_IdAndSpecialization(
            AppUser.Role.STUDENT, promoId, Specialization.EL))
        .thenReturn(List.of());
    when(securityExpressions.currentUserId()).thenReturn(adminId);
    when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
    when(graduateExportRepository.save(any()))
        .thenAnswer(
            inv -> {
              GraduateExport e = inv.getArgument(0);
              if (e.getId() == null) {
                e.setId(UUID.randomUUID());
              }
              return e;
            });
    when(bucketComponent.presign(anyString(), any(Duration.class)))
        .thenReturn(new URL("https://s3.example.com/file.xlsx"));

    GraduateExportDto dto = graduateService.exportGraduates(promoId.toString(), "EL");

    assertEquals(GraduateExport.Status.READY, dto.getStatus());
    verify(bucketComponent).upload(any(File.class), anyString());
  }

  @Test
  void getDownloadUrl_ready_returnsPresignedUrl() throws Exception {
    UUID exportId = UUID.randomUUID();
    GraduateExport export =
        GraduateExport.builder()
            .id(exportId)
            .status(GraduateExport.Status.READY)
            .s3Key("graduates/file.xlsx")
            .build();
    when(graduateExportRepository.findById(exportId)).thenReturn(Optional.of(export));
    when(bucketComponent.presign(eq("graduates/file.xlsx"), any(Duration.class)))
        .thenReturn(new URL("https://s3.example.com/file.xlsx"));

    URL url = graduateService.getDownloadUrl(exportId.toString());

    assertEquals("https://s3.example.com/file.xlsx", url.toString());
  }

  @Test
  void getDownloadUrl_notReady_throwsBadRequest() {
    UUID exportId = UUID.randomUUID();
    GraduateExport export =
        GraduateExport.builder()
            .id(exportId)
            .status(GraduateExport.Status.FAILED)
            .s3Key(null)
            .build();
    when(graduateExportRepository.findById(exportId)).thenReturn(Optional.of(export));

    assertThrows(
        BadRequestException.class, () -> graduateService.getDownloadUrl(exportId.toString()));
  }

  @Test
  void getDownloadUrl_unknown_throwsNotFound() {
    UUID exportId = UUID.randomUUID();
    when(graduateExportRepository.findById(exportId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> graduateService.getDownloadUrl(exportId.toString()));
  }
}
