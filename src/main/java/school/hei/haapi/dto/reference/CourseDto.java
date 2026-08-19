package school.hei.haapi.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.haapi.model.Specialization;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
  private String id;
  private String ref;
  private String title;
  private int credits;
  private int semesterNumber;
  private Specialization specialization;
}
