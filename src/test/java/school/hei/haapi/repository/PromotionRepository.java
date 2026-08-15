package school.hei.haapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.model.Promotion;

class PromotionRepositoryIT extends FacadeIT {

  @Autowired private PromotionRepository promotionRepository;

  @Test
  void save_then_findById_returns_same_promotion() {

    var promotion =
        Promotion.builder().code("K").entryCalendarYear(2023).expectedGraduationYear(2026).build();

    var saved = promotionRepository.save(promotion);
    var found = promotionRepository.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getCode()).isEqualTo("K");
    assertThat(found.get().getEntryCalendarYear()).isEqualTo(2023);
    assertThat(found.get().getId()).isNotNull();
  }

  @Test
  void findById_unknownId_returnsEmpty() {
    var found = promotionRepository.findById(UUID.randomUUID());

    assertThat(found).isEmpty();
  }
}
