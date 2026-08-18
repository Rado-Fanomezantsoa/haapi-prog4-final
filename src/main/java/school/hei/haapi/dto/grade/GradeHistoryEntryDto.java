package school.hei.haapi.dto.grade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
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
public class GradeHistoryEntryDto {

  private UUID id;
  private BigDecimal previousValue;
  private BigDecimal newValue;
  private String reason;
  private UUID modifiedBy;
  private Instant modifiedAt;
}
