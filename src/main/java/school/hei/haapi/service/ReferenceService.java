package school.hei.haapi.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.reference.ClassGroupDto;
import school.hei.haapi.dto.reference.CourseDto;
import school.hei.haapi.dto.reference.PromotionDto;
import school.hei.haapi.exception.BadRequestException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.ClassGroup;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.PromotionRepository;

@Service
@RequiredArgsConstructor
public class ReferenceService {

  private final PromotionRepository promotionRepository;
  private final ClassGroupRepository classGroupRepository;
  private final CourseRepository courseRepository;

  @Transactional(readOnly = true)
  public List<PromotionDto> listPromotions() {
    return promotionRepository.findAll().stream()
        .sorted(Comparator.comparingInt(Promotion::getExpectedGraduationYear).reversed())
        .map(this::toPromotionDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ClassGroupDto> listGroupsByPromotion(String promotionIdParam) {
    UUID promotionId = parseUuid(promotionIdParam, "promotionId");
    promotionRepository
        .findById(promotionId)
        .orElseThrow(() -> new NotFoundException("Promotion not found: " + promotionId));

    return classGroupRepository.findByPromotion_Id(promotionId).stream()
        .map(this::toGroupDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<CourseDto> listCourses() {
    return courseRepository.findAll().stream()
        .sorted(Comparator.comparing(Course::getRef))
        .map(this::toCourseDto)
        .toList();
  }

  private PromotionDto toPromotionDto(Promotion p) {
    return PromotionDto.builder()
        .id(p.getId().toString())
        .code(p.getCode())
        .entryCalendarYear(p.getEntryCalendarYear())
        .expectedGraduationYear(p.getExpectedGraduationYear())
        .build();
  }

  private ClassGroupDto toGroupDto(ClassGroup g) {
    return ClassGroupDto.builder()
        .id(g.getId().toString())
        .promotionId(g.getPromotion().getId().toString())
        .ref(g.getRef())
        .academicLevel(g.getAcademicLevel())
        .specialization(g.getSpecialization())
        .build();
  }

  private CourseDto toCourseDto(Course c) {
    return CourseDto.builder()
        .id(c.getId().toString())
        .ref(c.getRef())
        .title(c.getTitle())
        .credits(c.getCredits())
        .semesterNumber(c.getSemesterNumber())
        .specialization(c.getSpecialization())
        .build();
  }

  private UUID parseUuid(String value, String fieldName) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid UUID for " + fieldName + ": " + value);
    }
  }
}
