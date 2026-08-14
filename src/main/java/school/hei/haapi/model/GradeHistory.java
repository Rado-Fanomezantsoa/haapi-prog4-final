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

@Entity
@Table(name = "\"grade_history\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GradeHistory implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @ManyToOne
  @JoinColumn(name = "grade_id")
  @ToString.Exclude
  private Grade grade;

  @Column(name = "previous_value", precision = 4, scale = 2)
  private BigDecimal previousValue;

  @Column(name = "new_value", nullable = false, precision = 4, scale = 2)
  private BigDecimal newValue;

  @Column(nullable = false, columnDefinition = "text")
  private String reason;

  @ManyToOne
  @JoinColumn(name = "modified_by")
  @ToString.Exclude
  private AppUser modifiedBy;

  @EqualsAndHashCode.Exclude
  @CreationTimestamp
  @Column(name = "modified_at")
  private Instant modifiedAt;
}
