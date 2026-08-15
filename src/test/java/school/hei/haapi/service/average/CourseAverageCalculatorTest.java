package school.hei.haapi.service.average;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.exception.BadRequestException;

class CourseAverageCalculatorTest {

  @Test
  void singleExam_returnsThatValue() {
    var result =
        CourseAverageCalculator.compute("PROG4", 4, 6, List.of(new ExamGradeInput(14.0, 1, 1)));

    assertEquals(14.0, result.average());
    assertTrue(result.validated());
    assertEquals(6, result.validatedCredits());
  }

  @Test
  void weightedAverage_twoExams() {
    // 12*(1/3) + 18*(2/3) = 4 + 12 = 16
    var result =
        CourseAverageCalculator.compute(
            "PROG4", 4, 6, List.of(new ExamGradeInput(12.0, 1, 3), new ExamGradeInput(18.0, 2, 3)));

    assertEquals(16.0, result.average());
    assertTrue(result.validated());
  }

  @Test
  void belowTen_notValidated_zeroCredits() {
    var result =
        CourseAverageCalculator.compute("TN1", 4, 5, List.of(new ExamGradeInput(9.9, 1, 1)));

    assertEquals(9.9, result.average());
    assertFalse(result.validated());
    assertEquals(0, result.validatedCredits());
  }

  @Test
  void exactlyTen_validated() {
    var result =
        CourseAverageCalculator.compute("SYS2", 3, 4, List.of(new ExamGradeInput(10.0, 1, 1)));

    assertTrue(result.validated());
    assertEquals(4, result.validatedCredits());
  }

  @Test
  void emptyExams_notValidated() {
    var result = CourseAverageCalculator.compute("WEB3", 5, 3, List.of());

    assertEquals(0.0, result.average());
    assertFalse(result.validated());
  }

  @Test
  void invalidGradeValue_throwsBadRequest() {
    assertThrows(BadRequestException.class, () -> new ExamGradeInput(21.0, 1, 1));
    assertThrows(BadRequestException.class, () -> new ExamGradeInput(-1.0, 1, 1));
  }

  @Test
  void invalidCoefficient_throwsBadRequest() {
    assertThrows(BadRequestException.class, () -> new ExamGradeInput(12.0, 0, 1));
    assertThrows(BadRequestException.class, () -> new ExamGradeInput(12.0, 1, -3));
  }
}
