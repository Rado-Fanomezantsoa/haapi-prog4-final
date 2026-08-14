package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "\"grade\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Grade implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "exam_id", updatable = false)
  @ToString.Exclude
  private Exam exam;

  @ManyToOne
  @JoinColumn(name = "student_id", updatable = false)
  @ToString.Exclude
  private AppUser student;

  @Column(nullable = false, precision = 4, scale = 2)
  private BigDecimal value; // 0..20

  @ManyToOne
  @JoinColumn(name = "entered_by")
  @ToString.Exclude
  private AppUser enteredBy;

  @EqualsAndHashCode.Exclude
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @EqualsAndHashCode.Exclude
  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
