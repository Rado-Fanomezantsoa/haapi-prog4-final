package school.hei.haapi.service.average;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OverallAverageCalculator {

  public static final int EXPECTED_CREDITS_3_YEARS = 180;
  private static final double PASS_THRESHOLD = 10.0;

  private OverallAverageCalculator() {}

  public static StudentAveragesResult compute(List<CourseResult> courses) {
    List<CourseResult> validated = courses.stream().filter(CourseResult::validated).toList();

    int creditsTaken = validated.stream().mapToInt(CourseResult::courseCredits).sum();

    double overall =
        creditsTaken == 0
            ? 0.0
            : validated.stream().mapToDouble(CourseResult::weightedContribution).sum()
                / creditsTaken;
    overall = Math.round(overall * 100.0) / 100.0;

    Map<Integer, Double> perSemester = averageByKey(validated, CourseResult::semesterNumber);

    Map<String, Double> perYear = new HashMap<>();
    perYear.put("L1", averageForSemesters(validated, 1, 2));
    perYear.put("L2", averageForSemesters(validated, 3, 4));
    perYear.put("L3", averageForSemesters(validated, 5, 6));

    boolean eligible = overall >= PASS_THRESHOLD && creditsTaken >= EXPECTED_CREDITS_3_YEARS;

    return new StudentAveragesResult(
        perSemester, perYear, overall, creditsTaken, EXPECTED_CREDITS_3_YEARS, eligible);
  }

  private static Map<Integer, Double> averageByKey(
      List<CourseResult> courses, Function<CourseResult, Integer> keyFn) {

    Map<Integer, List<CourseResult>> grouped =
        courses.stream().collect(Collectors.groupingBy(keyFn));

    Map<Integer, Double> result = new HashMap<>();
    grouped.forEach(
        (k, list) -> {
          int credits = list.stream().mapToInt(CourseResult::courseCredits).sum();
          double avg =
              credits == 0
                  ? 0.0
                  : list.stream().mapToDouble(CourseResult::weightedContribution).sum() / credits;
          result.put(k, Math.round(avg * 100.0) / 100.0);
        });
    return result;
  }

  private static double averageForSemesters(List<CourseResult> courses, int semFrom, int semTo) {
    List<CourseResult> filtered =
        courses.stream()
            .filter(c -> c.semesterNumber() >= semFrom && c.semesterNumber() <= semTo)
            .toList();
    int credits = filtered.stream().mapToInt(CourseResult::courseCredits).sum();
    if (credits == 0) {
      return 0.0;
    }
    double avg = filtered.stream().mapToDouble(CourseResult::weightedContribution).sum() / credits;
    return Math.round(avg * 100.0) / 100.0;
  }
}
