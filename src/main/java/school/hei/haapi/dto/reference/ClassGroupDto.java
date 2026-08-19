package school.hei.haapi.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.haapi.model.AcademicLevel;
import school.hei.haapi.model.Specialization;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassGroupDto {
  private String id;
  private String promotionId;
  private String ref;
  private AcademicLevel academicLevel;
  private Specialization specialization;
}
