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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "\"graduate_export\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GraduateExport implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "promotion_id")
  @ToString.Exclude
  private Promotion promotion;

  @Enumerated(STRING)
  @Column(nullable = false, length = 10)
  private Specialization specialization;

  @ManyToOne
  @JoinColumn(name = "generated_by")
  @ToString.Exclude
  private AppUser generatedBy;

  @Enumerated(STRING)
  @Column(nullable = false, length = 12)
  @Builder.Default
  private Status status = Status.READY;

  @Column(name = "s3_key", length = 500)
  private String s3Key;

  @EqualsAndHashCode.Exclude
  @CreationTimestamp
  @Column(name = "generated_at")
  private Instant generatedAt;

  public enum Status {
    READY,
    FAILED
  }
}
