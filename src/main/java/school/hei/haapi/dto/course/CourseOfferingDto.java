package school.hei.haapi.dto.course;

import java.util.List;
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
public class CourseOfferingDto {

  private String id;
  private CourseSummaryDto course;
  private int calendarYear;
  private List<String> teacherIds;
  private List<String> groupIds;
}
