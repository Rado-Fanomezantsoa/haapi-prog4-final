package school.hei.haapi.service.event;

import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.model.TranscriptRequested;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.TranscriptRequest;
import school.hei.haapi.repository.TranscriptRequestRepository;
import school.hei.haapi.service.StudentGradeService;
import school.hei.haapi.service.transcript.TranscriptPdfGenerator;

@Service
@AllArgsConstructor
@Slf4j
public class TranscriptRequestedService implements Consumer<TranscriptRequested> {

  private final TranscriptRequestRepository transcriptRequestRepository;
  private final StudentGradeService studentGradeService;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  @SneakyThrows
  @Transactional
  public void accept(TranscriptRequested event) {
    TranscriptRequest request =
        transcriptRequestRepository
            .findById(event.getTranscriptRequestId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "TranscriptRequest not found: " + event.getTranscriptRequestId()));

    try {
      request.setStatus(TranscriptRequest.Status.PROCESSING);
      transcriptRequestRepository.save(request);

      AppUser student = request.getStudent();
      var grades = studentGradeService.getGradesForStudentInternal(student.getId());

      File pdf = TranscriptPdfGenerator.generate(student, grades);
      String s3Key = "transcripts/" + request.getId() + ".pdf";
      bucketComponent.upload(pdf, s3Key);

      String to = student.getEmail();
      mailer.accept(
          new Email(
              new InternetAddress(to),
              List.of(),
              List.of(),
              "Votre relevé de notes HEI",
              "<p>Bonjour "
                  + student.getFirstName()
                  + ",</p><p>Votre relevé de notes est en pièce jointe.</p>",
              List.of(pdf)));

      request.setS3Key(s3Key);
      request.setEmailSentTo(to);
      request.setStatus(TranscriptRequest.Status.SENT);
      request.setCompletedAt(Instant.now());
      transcriptRequestRepository.save(request);

      if (pdf.exists()) {
        pdf.delete();
      }
    } catch (Exception e) {
      log.error("Failed to process transcript {}", request.getId(), e);
      request.setStatus(TranscriptRequest.Status.FAILED);
      request.setCompletedAt(Instant.now());
      transcriptRequestRepository.save(request);
      throw e;
    }
  }
}
