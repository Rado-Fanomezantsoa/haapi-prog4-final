package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"transcript_request\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TranscriptRequest implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "student_id")
  @ToString.Exclude
  private AppUser student;

  @ManyToOne
  @JoinColumn(name = "requested_by")
  @ToString.Exclude
  private AppUser requestedBy;

  @Enumerated(STRING)
  @Column(nullable = false, length = 12)
  @Builder.Default
  private Status status = Status.PENDING;

  @Column(name = "s3_key", length = 500)
  private String s3Key;

  @Column(name = "email_sent_to")
  private String emailSentTo;

  @EqualsAndHashCode.Exclude
  @CreationTimestamp
  @Column(name = "requested_at")
  private Instant requestedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  public enum Status {
    PENDING,
    PROCESSING,
    SENT,
    FAILED
  }
}
