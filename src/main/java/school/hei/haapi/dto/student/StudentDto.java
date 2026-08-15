package school.hei.haapi.dto.student;

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
public class StudentDto {

  private String id;
  private String studentRef;
  private String firstName;
  private String lastName;
  private Specialization specialization;
}
