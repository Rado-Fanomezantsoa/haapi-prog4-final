package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.reference.CourseDto;
import school.hei.haapi.dto.reference.PromotionDto;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.PromotionRepository;

@ExtendWith(MockitoExtension.class)
class ReferenceServiceTest {

  @Mock private PromotionRepository promotionRepository;
  @Mock private ClassGroupRepository classGroupRepository;
  @Mock private CourseRepository courseRepository;

  @InjectMocks private ReferenceService referenceService;

  @Test
  void listPromotions_returnsMappedDtos() {
    UUID id = UUID.randomUUID();
    when(promotionRepository.findAll())
        .thenReturn(
            List.of(
                Promotion.builder()
                    .id(id)
                    .code("K")
                    .entryCalendarYear(2024)
                    .expectedGraduationYear(2027)
                    .build()));

    List<PromotionDto> result = referenceService.listPromotions();

    assertEquals(1, result.size());
    assertEquals("K", result.get(0).getCode());
    assertEquals(id.toString(), result.get(0).getId());
  }

  @Test
  void listGroups_unknownPromotion_throws() {
    UUID id = UUID.randomUUID();
    when(promotionRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> referenceService.listGroupsByPromotion(id.toString()));
  }

  @Test
  void listCourses_returnsMappedDtos() {
    when(courseRepository.findAll())
        .thenReturn(
            List.of(
                Course.builder()
                    .id(UUID.randomUUID())
                    .ref("PROG4")
                    .title("Programmation 4")
                    .credits(6)
                    .semesterNumber(4)
                    .specialization(Specialization.EL)
                    .build()));

    List<CourseDto> result = referenceService.listCourses();

    assertEquals(1, result.size());
    assertEquals("PROG4", result.get(0).getRef());
  }
}
