package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.student.TranscriptRequestDto;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.TranscriptRequested;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.TranscriptRequest;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.TranscriptRequestRepository;
import school.hei.haapi.security.SecurityExpressions;

@ExtendWith(MockitoExtension.class)
class TranscriptServiceTest {

  @Mock private AppUserRepository appUserRepository;
  @Mock private TranscriptRequestRepository transcriptRequestRepository;
  @Mock private EventProducer<TranscriptRequested> eventProducer;
  @Mock private SecurityExpressions securityExpressions;

  @InjectMocks private TranscriptService transcriptService;

  @Test
  void request_asAdmin_publishesEvent() {
    UUID studentId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();

    AppUser student =
        AppUser.builder()
            .id(studentId)
            .role(AppUser.Role.STUDENT)
            .email("s@hei.school")
            .firstName("A")
            .lastName("B")
            .passwordHash("x")
            .build();
    AppUser admin =
        AppUser.builder()
            .id(adminId)
            .role(AppUser.Role.ADMIN)
            .email("a@hei.school")
            .firstName("Ad")
            .lastName("Min")
            .passwordHash("x")
            .build();

    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student));
    when(securityExpressions.currentUserRole()).thenReturn("ADMIN");
    when(securityExpressions.currentUserId()).thenReturn(adminId);
    when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
    when(transcriptRequestRepository.save(any()))
        .thenAnswer(
            inv -> {
              TranscriptRequest r = inv.getArgument(0);
              r.setId(UUID.randomUUID());
              return r;
            });

    TranscriptRequestDto dto = transcriptService.requestTranscript(studentId.toString());

    assertEquals(TranscriptRequest.Status.PENDING, dto.getStatus());
    verify(eventProducer).accept(any());
  }
}
