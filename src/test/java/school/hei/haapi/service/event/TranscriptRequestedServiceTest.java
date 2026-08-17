package school.hei.haapi.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.endpoint.event.model.TranscriptRequested;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.TranscriptRequest;
import school.hei.haapi.repository.TranscriptRequestRepository;
import school.hei.haapi.service.StudentGradeService;

@ExtendWith(MockitoExtension.class)
class TranscriptRequestedServiceTest {

  @Mock private TranscriptRequestRepository transcriptRequestRepository;
  @Mock private StudentGradeService studentGradeService;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  @InjectMocks private TranscriptRequestedService transcriptRequestedService;

  private AppUser student(UUID id) {
    return AppUser.builder()
        .id(id)
        .role(AppUser.Role.STUDENT)
        .email("student@hei.school")
        .firstName("Jean")
        .lastName("Rakoto")
        .studentRef("STD24001")
        .passwordHash("x")
        .build();
  }

  private TranscriptRequest pendingRequest(UUID requestId, AppUser student) {
    return TranscriptRequest.builder()
        .id(requestId)
        .student(student)
        .status(TranscriptRequest.Status.PENDING)
        .build();
  }

  @Test
  void accept_success_setsSent_uploadsAndMails() {
    UUID requestId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    AppUser student = student(studentId);
    TranscriptRequest request = pendingRequest(requestId, student);

    when(transcriptRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
    when(transcriptRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(studentGradeService.getGradesForStudentInternal(studentId))
        .thenReturn(
            List.of(
                GradeDetailDto.builder()
                    .courseRef("PROG4")
                    .examLabel("Final")
                    .value(14.0)
                    .build()));

    TranscriptRequested event =
        TranscriptRequested.builder().transcriptRequestId(requestId).build();

    transcriptRequestedService.accept(event);

    ArgumentCaptor<TranscriptRequest> captor = ArgumentCaptor.forClass(TranscriptRequest.class);
    verify(transcriptRequestRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());

    TranscriptRequest lastSaved = captor.getValue();
    assertEquals(TranscriptRequest.Status.SENT, lastSaved.getStatus());
    assertEquals("student@hei.school", lastSaved.getEmailSentTo());
    assertEquals("transcripts/" + requestId + ".pdf", lastSaved.getS3Key());

    verify(bucketComponent).upload(any(File.class), eq("transcripts/" + requestId + ".pdf"));
    verify(mailer).accept(any(Email.class));
    verify(studentGradeService).getGradesForStudentInternal(studentId);
    verify(studentGradeService, never()).getGradesForStudent(anyString());
  }

  @Test
  void accept_unknownRequest_throws() {
    UUID requestId = UUID.randomUUID();
    when(transcriptRequestRepository.findById(requestId)).thenReturn(Optional.empty());

    TranscriptRequested event =
        TranscriptRequested.builder().transcriptRequestId(requestId).build();

    assertThrows(IllegalStateException.class, () -> transcriptRequestedService.accept(event));
    verify(bucketComponent, never()).upload(any(), anyString());
    verify(mailer, never()).accept(any());
  }

  @Test
  void accept_uploadFails_setsFailed() {
    UUID requestId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    AppUser student = student(studentId);
    TranscriptRequest request = pendingRequest(requestId, student);

    when(transcriptRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
    when(transcriptRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(studentGradeService.getGradesForStudentInternal(studentId)).thenReturn(List.of());
    doThrow(new RuntimeException("S3 down"))
        .when(bucketComponent)
        .upload(any(File.class), anyString());

    TranscriptRequested event =
        TranscriptRequested.builder().transcriptRequestId(requestId).build();

    assertThrows(RuntimeException.class, () -> transcriptRequestedService.accept(event));

    ArgumentCaptor<TranscriptRequest> captor = ArgumentCaptor.forClass(TranscriptRequest.class);
    verify(transcriptRequestRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
    assertEquals(TranscriptRequest.Status.FAILED, captor.getValue().getStatus());
  }
}
