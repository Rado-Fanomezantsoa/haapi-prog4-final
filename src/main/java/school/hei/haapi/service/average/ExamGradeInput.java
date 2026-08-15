package school.hei.haapi.service.average;

import school.hei.haapi.exception.BadRequestException;

public record ExamGradeInput(double value, int coefficientNum, int coefficientDen) {

  public ExamGradeInput {
    if (value < 0 || value > 20) {
      throw new BadRequestException("Grade value must be in [0, 20], got: " + value);
    }
    if (coefficientNum <= 0 || coefficientDen <= 0) {
      throw new BadRequestException(
          "Coefficient must be a positive fraction, got: " + coefficientNum + "/" + coefficientDen);
    }
  }

  public double weight() {
    return (double) coefficientNum / coefficientDen;
  }
}
