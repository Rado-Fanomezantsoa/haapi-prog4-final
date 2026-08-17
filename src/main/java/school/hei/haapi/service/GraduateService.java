package school.hei.haapi.service;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import school.hei.haapi.service.graduate.GraduateExcelGenerator;

@Service
@RequiredArgsConstructor
public class GraduateService {

  private final PromotionRepository promotionRepository;
  private final AppUserRepository appUserRepository;
  private final GraduateExportRepository graduateExportRepository;
  private final StudentAverageService studentAverageService;
  private final BucketComponent bucketComponent;
  private final SecurityExpressions securityExpressions;

  @Transactional(readOnly = true)
  public List<GraduateEntryDto> listGraduates(String promotionIdParam, String specializationParam) {
    UUID promotionId = parseUuid(promotionIdParam, "promotionId");
    Specialization specialization = parseSpecialization(specializationParam);

    promotionRepository
        .findById(promotionId)
        .orElseThrow(() -> new NotFoundException("Promotion not found: " + promotionId));

    List<AppUser> students =
        appUserRepository.findByRoleAndPromotion_IdAndSpecialization(
            AppUser.Role.STUDENT, promotionId, specialization);

    List<GraduateEntryDto> eligible = new ArrayList<>();
    for (AppUser student : students) {
      StudentAveragesDto avg = studentAverageService.getAveragesInternal(student.getId());
      if (avg.isEligibleForDiploma()) {
        eligible.add(
            GraduateEntryDto.builder()
                .studentRef(student.getStudentRef())
                .lastName(student.getLastName())
                .firstName(student.getFirstName())
                .overallAverage(avg.getOverallCursusAverage())
                .creditsTaken(avg.getCreditsTaken())
                .expectedCredits(avg.getExpectedCredits())
                .build());
      }
    }

    eligible.sort(Comparator.comparingDouble(GraduateEntryDto::getOverallAverage).reversed());
    for (int i = 0; i < eligible.size(); i++) {
      eligible.get(i).setRank(i + 1);
    }
    return eligible;
  }

  @Transactional
  @SneakyThrows
  public GraduateExportDto exportGraduates(String promotionIdParam, String specializationParam) {
    UUID promotionId = parseUuid(promotionIdParam, "promotionId");
    Specialization specialization = parseSpecialization(specializationParam);

    Promotion promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(() -> new NotFoundException("Promotion not found: " + promotionId));

    UUID adminId = securityExpressions.currentUserId();
    AppUser admin =
        appUserRepository
            .findById(adminId)
            .orElseThrow(() -> new NotFoundException("User not found: " + adminId));

    List<GraduateEntryDto> graduates = listGraduates(promotionIdParam, specializationParam);

    GraduateExport export =
        GraduateExport.builder()
            .promotion(promotion)
            .specialization(specialization)
            .generatedBy(admin)
            .status(GraduateExport.Status.READY)
            .build();

    try {
      File xlsx = GraduateExcelGenerator.generate(graduates);
      String s3Key =
          "graduates/" + promotionId + "/" + specialization + "-" + UUID.randomUUID() + ".xlsx";
      bucketComponent.upload(xlsx, s3Key);
      export.setS3Key(s3Key);
      export.setStatus(GraduateExport.Status.READY);
      if (xlsx.exists()) {
        xlsx.delete();
      }
    } catch (Exception e) {
      export.setStatus(GraduateExport.Status.FAILED);
      export = graduateExportRepository.save(export);
      throw e;
    }

    export = graduateExportRepository.save(export);
    return toExportDto(export, true);
  }

  @Transactional(readOnly = true)
  public URL getDownloadUrl(String exportIdParam) {
    UUID exportId = parseUuid(exportIdParam, "exportId");
    GraduateExport export =
        graduateExportRepository
            .findById(exportId)
            .orElseThrow(() -> new NotFoundException("Export not found: " + exportId));
    if (export.getStatus() != GraduateExport.Status.READY || export.getS3Key() == null) {
      throw new BadRequestException("Export is not ready for download");
    }
    return bucketComponent.presign(export.getS3Key(), Duration.ofMinutes(10));
  }

  private GraduateExportDto toExportDto(GraduateExport export, boolean withUrl) {
    String url = null;
    if (withUrl && export.getStatus() == GraduateExport.Status.READY && export.getS3Key() != null) {
      url = bucketComponent.presign(export.getS3Key(), Duration.ofMinutes(10)).toString();
    }
    return GraduateExportDto.builder()
        .id(export.getId().toString())
        .promotionId(export.getPromotion().getId().toString())
        .specialization(export.getSpecialization())
        .status(export.getStatus())
        .downloadUrl(url)
        .generatedAt(export.getGeneratedAt())
        .build();
  }

  private Specialization parseSpecialization(String value) {
    if (value == null || value.isBlank()) {
      throw new BadRequestException("specialization is required (EL or TN)");
    }
    try {
      Specialization spec = Specialization.valueOf(value.trim().toUpperCase());
      if (spec == Specialization.COMMUN) {
        throw new BadRequestException("specialization must be EL or TN");
      }
      return spec;
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid specialization: " + value);
    }
  }

  private UUID parseUuid(String value, String fieldName) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid UUID for " + fieldName + ": " + value);
    }
  }
}
