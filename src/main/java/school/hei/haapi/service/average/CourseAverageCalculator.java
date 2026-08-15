package school.hei.haapi.service.average;

import java.util.List;

public final class CourseAverageCalculator {

  private static final double PASS_THRESHOLD = 10.0;

  private CourseAverageCalculator() {}

  public static CourseResult compute(
      String courseRef, int semesterNumber, int courseCredits, List<ExamGradeInput> examGrades) {

    if (examGrades == null || examGrades.isEmpty()) {
      return new CourseResult(courseRef, semesterNumber, courseCredits, 0.0, false);
    }

    double weightedSum = 0.0;
    double totalWeight = 0.0;

    for (ExamGradeInput g : examGrades) {
      double w = g.weight();
      weightedSum += g.value() * w;
      totalWeight += w;
    }

    double average = totalWeight == 0.0 ? 0.0 : weightedSum / totalWeight;
    average = Math.round(average * 100.0) / 100.0;

    boolean validated = average >= PASS_THRESHOLD;
    return new CourseResult(courseRef, semesterNumber, courseCredits, average, validated);
  }
}
