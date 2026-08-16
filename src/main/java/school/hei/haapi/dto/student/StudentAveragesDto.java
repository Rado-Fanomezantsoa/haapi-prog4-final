package school.hei.haapi.dto.student;

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
public class StudentAveragesDto {

  private String studentId;
  private List<SemesterAverageDto> perSemester;
  private List<YearAverageDto> perYear;
  private double overallCursusAverage;
  private int creditsTaken;
  private int expectedCredits;
  private boolean eligibleForDiploma;

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SemesterAverageDto {
    private int semesterNumber;
    private double average;
    private int creditsTaken;
  }

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class YearAverageDto {
    private String academicLevel; // L1, L2, L3
    private double average;
    private int creditsTaken;
  }
}
