package school.hei.haapi.service.average;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class OverallAverageCalculatorTest {

  @Test
  void onlyValidatedCoursesCountInAverage() {
    var courses =
        List.of(
            new CourseResult("PROG4", 4, 6, 16.0, true), new CourseResult("TN1", 4, 5, 8.0, false));

    var result = OverallAverageCalculator.compute(courses);

    assertEquals(16.0, result.overallCursusAverage());
    assertEquals(6, result.creditsTaken());
    assertFalse(result.eligibleForDiploma());
  }

  @Test
  void eligibleWhenAverageAndCreditsOk() {
    // 30 cours × 6 crédits à 12/20 → 180 crédits, moyenne 12
    var courses =
        IntStream.range(0, 30)
            .mapToObj(i -> new CourseResult("C" + i, (i % 6) + 1, 6, 12.0, true))
            .toList();

    var result = OverallAverageCalculator.compute(courses);

    assertEquals(12.0, result.overallCursusAverage());
    assertEquals(180, result.creditsTaken());
    assertTrue(result.eligibleForDiploma());
  }

  @Test
  void highAverageButIncompleteCredits_notEligible() {
    var courses = List.of(new CourseResult("PROG4", 4, 6, 18.0, true));

    var result = OverallAverageCalculator.compute(courses);

    assertEquals(18.0, result.overallCursusAverage());
    assertEquals(6, result.creditsTaken());
    assertFalse(result.eligibleForDiploma());
  }

  @Test
  void perSemesterGrouping() {
    var courses =
        List.of(
            new CourseResult("A", 1, 6, 14.0, true),
            new CourseResult("B", 1, 4, 12.0, true),
            new CourseResult("C", 2, 5, 16.0, true));

    var result = OverallAverageCalculator.compute(courses);

    // S1 : (14*6 + 12*4) / 10 = 13.2
    assertEquals(13.2, result.perSemester().get(1));
    assertEquals(16.0, result.perSemester().get(2));
  }

  @Test
  void emptyList_zeroAverage_notEligible() {
    var result = OverallAverageCalculator.compute(List.of());

    assertEquals(0.0, result.overallCursusAverage());
    assertEquals(0, result.creditsTaken());
    assertEquals(180, result.expectedCredits());
    assertFalse(result.eligibleForDiploma());
  }
}
