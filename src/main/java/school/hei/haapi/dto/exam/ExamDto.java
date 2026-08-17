package school.hei.haapi.dto.exam;

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
public class ExamDto {

  private UUID id;
  private UUID courseOfferingId;
  private String label;
  private Instant dateExam;
  private Integer coefficientNum;
  private Integer coefficientDen;
}
