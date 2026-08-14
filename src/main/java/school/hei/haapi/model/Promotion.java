package school.hei.haapi.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "\"promotion\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SQLDelete(sql = "update \"promotion\" set is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
public class Promotion implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @Column(nullable = false, length = 5)
  private String code; // 'K', 'J', 'H', 'N'

  @Column(name = "entry_calendar_year", nullable = false)
  private int entryCalendarYear;

  @Column(name = "expected_graduation_year", nullable = false)
  private int expectedGraduationYear;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  @EqualsAndHashCode.Exclude @Builder.Default private boolean isDeleted = false;
}
