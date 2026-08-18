package school.hei.haapi.dto.grade;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeEntryInputDto {

  @NotNull private UUID studentId;

  @NotNull
  @DecimalMin("0")
  @DecimalMax("20")
  private BigDecimal value;
}
