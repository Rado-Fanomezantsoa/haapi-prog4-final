package school.hei.haapi.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.exam.ExamDto;
import school.hei.haapi.dto.exam.ExamInputDto;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.CourseOffering;
import school.hei.haapi.model.Exam;
import school.hei.haapi.repository.CourseOfferingRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.security.AuthenticatedUser;

@Service
@RequiredArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseOfferingRepository courseOfferingRepository;

  @Transactional
  public ExamDto create(UUID offeringId, ExamInputDto input, AuthenticatedUser principal) {
    CourseOffering offering =
        courseOfferingRepository
            .findById(offeringId)
            .orElseThrow(() -> new NotFoundException("Course-offering not found: " + offeringId));

    if (principal.isTeacher()
        && !courseOfferingRepository.existsByIdAndTeachers_Id(offeringId, principal.id())) {
      throw new ForbiddenException("You are not assigned as a teacher on this course-offering");
    }

    Exam exam =
        Exam.builder()
            .courseOffering(offering)
            .label(input.getLabel())
            .dateExam(input.getDateExam())
            .coefficientNumerator(input.getCoefficientNum())
            .coefficientDenominator(input.getCoefficientDen())
            .build();

    return toDto(examRepository.save(exam));
  }

  private ExamDto toDto(Exam exam) {
    return ExamDto.builder()
        .id(exam.getId())
        .courseOfferingId(exam.getCourseOffering().getId())
        .label(exam.getLabel())
        .dateExam(exam.getDateExam())
        .coefficientNum(exam.getCoefficientNumerator())
        .coefficientDen(exam.getCoefficientDenominator())
        .build();
  }
}
