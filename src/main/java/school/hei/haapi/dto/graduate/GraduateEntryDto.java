package school.hei.haapi.dto.graduate;

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
public class GraduateEntryDto {
  private int rank;
  private String studentRef;
  private String lastName;
  private String firstName;
  private double overallAverage;
  private int creditsTaken;
  private int expectedCredits;
}
