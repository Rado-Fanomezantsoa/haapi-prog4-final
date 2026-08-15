package school.hei.haapi.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.exception.BadRequestException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.PromotionRepository;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final AppUserRepository appUserRepository;
  private final ClassGroupRepository classGroupRepository;
  private final PromotionRepository promotionRepository;

  @Transactional(readOnly = true)
  public List<StudentDto> findStudents(String promotionId, String groupId) {
    UUID promotionUuid = parseOptionalUuid(promotionId, "promotionId");
    UUID groupUuid = parseOptionalUuid(groupId, "groupId");

    validateFiltersExist(promotionUuid, groupUuid);

    List<AppUser> students;

    if (groupUuid != null) {
      if (promotionUuid != null) {
        students =
            appUserRepository.findActiveStudentsByGroupIdAndPromotionId(groupUuid, promotionUuid);
      } else {
        students = appUserRepository.findActiveStudentsByGroupId(groupUuid);
      }
    } else if (promotionUuid != null) {
      students = appUserRepository.findByRoleAndPromotion_Id(AppUser.Role.STUDENT, promotionUuid);
    } else {
      students = appUserRepository.findByRole(AppUser.Role.STUDENT);
    }

    return students.stream().map(this::toDto).toList();
  }

  private void validateFiltersExist(UUID promotionId, UUID groupId) {
    if (promotionId != null) {
      promotionRepository
          .findById(promotionId)
          .orElseThrow(() -> new NotFoundException("Promotion not found: " + promotionId));
    }
    if (groupId != null) {
      classGroupRepository
          .findById(groupId)
          .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));
    }
  }

  private UUID parseOptionalUuid(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid UUID for " + fieldName + ": " + value);
    }
  }

  private StudentDto toDto(AppUser user) {
    return StudentDto.builder()
        .id(user.getId().toString())
        .studentRef(user.getStudentRef())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .specialization(user.getSpecialization())
        .build();
  }
}
