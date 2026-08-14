package school.hei.haapi.dto.course;

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
public class CourseSummaryDto {

  private String id;
  private String ref;
  private String title;
  private int credits;
  private int semesterNumber;
}
