package school.hei.haapi.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDto {
  private String id;
  private String code;
  private int entryCalendarYear;
  private int expectedGraduationYear;
}
