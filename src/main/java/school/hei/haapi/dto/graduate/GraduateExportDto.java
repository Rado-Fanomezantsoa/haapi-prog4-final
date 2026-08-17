package school.hei.haapi.dto.graduate;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.haapi.model.GraduateExport;
import school.hei.haapi.model.Specialization;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraduateExportDto {
  private String id;
  private String promotionId;
  private Specialization specialization;
  private GraduateExport.Status status;
  private String downloadUrl;
  private Instant generatedAt;
}
