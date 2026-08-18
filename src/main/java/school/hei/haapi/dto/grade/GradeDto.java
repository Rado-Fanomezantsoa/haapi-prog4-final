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
public class GradeDto {

  private UUID id;
  private UUID examId;
  private UUID studentId;
  private BigDecimal value;
  private Instant updatedAt;
}
