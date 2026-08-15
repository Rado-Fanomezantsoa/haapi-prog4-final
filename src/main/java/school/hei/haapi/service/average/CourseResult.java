package school.hei.haapi.service.average;

public record CourseResult(
    String courseRef, int semesterNumber, int courseCredits, double average, boolean validated) {

  public int validatedCredits() {
    return validated ? courseCredits : 0;
  }

  public double weightedContribution() {
    return validated ? average * courseCredits : 0.0;
  }
}
