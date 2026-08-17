package school.hei.haapi.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.student.TranscriptRequestDto;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.TranscriptRequested;
import school.hei.haapi.exception.BadRequestException;
import school.hei.haapi.exception.ForbiddenException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.TranscriptRequest;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.TranscriptRequestRepository;
import school.hei.haapi.security.SecurityExpressions;

@Service
@RequiredArgsConstructor
public class TranscriptService {

  private final AppUserRepository appUserRepository;
  private final TranscriptRequestRepository transcriptRequestRepository;
  private final EventProducer<TranscriptRequested> eventProducer;
  private final SecurityExpressions securityExpressions;

  @Transactional
  public TranscriptRequestDto requestTranscript(String studentIdParam) {
    UUID studentId = parseUuid(studentIdParam, "studentId");
    AppUser student =
        appUserRepository
            .findByIdAndRole(studentId, AppUser.Role.STUDENT)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

    assertCanRequest(studentId);

    UUID requesterId = securityExpressions.currentUserId();
    AppUser requestedBy =
        appUserRepository
            .findById(requesterId)
            .orElseThrow(() -> new NotFoundException("Requester not found: " + requesterId));

    TranscriptRequest request =
        TranscriptRequest.builder()
            .student(student)
            .requestedBy(requestedBy)
            .status(TranscriptRequest.Status.PENDING)
            .build();
    request = transcriptRequestRepository.save(request);

    eventProducer.accept(
        List.of(TranscriptRequested.builder().transcriptRequestId(request.getId()).build()));

    return toDto(request);
  }

  @Transactional(readOnly = true)
  public TranscriptRequestDto getRequest(String requestIdParam) {
    UUID requestId = parseUuid(requestIdParam, "requestId");
    TranscriptRequest request =
        transcriptRequestRepository
            .findById(requestId)
            .orElseThrow(() -> new NotFoundException("Transcript request not found: " + requestId));

    assertCanView(request);
    return toDto(request);
  }

  private void assertCanRequest(UUID studentId) {
    String role = securityExpressions.currentUserRole();
    if ("ADMIN".equals(role)) {
      return;
    }
    if ("STUDENT".equals(role) && securityExpressions.isSelfStudent(studentId)) {
      return;
    }
    throw new ForbiddenException("Only ADMIN or the student can request a transcript");
  }

  private void assertCanView(TranscriptRequest request) {
    String role = securityExpressions.currentUserRole();
    if ("ADMIN".equals(role)) {
      return;
    }
    if ("STUDENT".equals(role) && securityExpressions.isSelfStudent(request.getStudent().getId())) {
      return;
    }
    throw new ForbiddenException("Not allowed to view this transcript request");
  }

  private TranscriptRequestDto toDto(TranscriptRequest r) {
    return TranscriptRequestDto.builder()
        .id(r.getId().toString())
        .studentId(r.getStudent().getId().toString())
        .status(r.getStatus())
        .emailSentTo(r.getEmailSentTo())
        .requestedAt(r.getRequestedAt())
        .completedAt(r.getCompletedAt())
        .build();
  }

  private UUID parseUuid(String value, String fieldName) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid UUID for " + fieldName + ": " + value);
    }
  }
}
