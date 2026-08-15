package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.math.Fraction;

@Entity
@Table(name = "\"exam\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Exam implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private UUID id;

  @Setter(PRIVATE)
  @Column(name = "coefficient_numerator")
  private Integer coefficientNumerator;

  @Setter(PRIVATE)
  @Column(name = "coefficient_denominator")
  private Integer coefficientDenominator;

  private String label;

  @ManyToOne
  @JoinColumn(name = "course_offering_id")
  @ToString.Exclude
  private CourseOffering courseOffering;

  @OneToMany(mappedBy = "exam")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<Grade> grades;

  @Column(name = "date_exam", nullable = false)
  private Instant dateExam;

  public Fraction getCoefficientFraction() {
    return Fraction.getFraction(coefficientNumerator, coefficientDenominator);
  }

  public void setCoefficientFraction(Fraction fraction) {
    setCoefficientNumerator(fraction.getNumerator());
    setCoefficientDenominator(fraction.getDenominator());
  }
}
