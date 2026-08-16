package school.hei.haapi.dto.student;

import java.time.Instant;
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
public class GradeDetailDto {

  private String id;
  private String examId;
  private String studentId;
  private double value;
  private Instant updatedAt;

  private String courseRef;
  private String courseTitle;
  private String examLabel;
  private Integer coefficientNum;
  private Integer coefficientDen;
}
