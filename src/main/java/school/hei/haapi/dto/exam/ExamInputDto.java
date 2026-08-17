package school.hei.haapi.dto.exam;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamInputDto {

  @NotBlank private String label;

  @NotNull private Instant dateExam;

  @NotNull
  @Min(1)
  private Integer coefficientNum;

  @NotNull
  @Min(1)
  private Integer coefficientDen;
}
