package school.hei.haapi.dto.student;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import school.hei.haapi.model.TranscriptRequest;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptRequestDto {

  private String id;
  private String studentId;
  private TranscriptRequest.Status status;
  private String emailSentTo;
  private Instant requestedAt;
  private Instant completedAt;
}
