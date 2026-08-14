package school.hei.haapi.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SQLDelete(sql = "update \"course\" set is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
public class Course implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @Column(nullable = false, unique = true, length = 20)
  private String ref;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private int credits;

  @Column(name = "semester_number", nullable = false)
  private int semesterNumber; // 1..6 (S1..S6)

  @Enumerated(STRING)
  @Column(nullable = false, length = 10)
  private Specialization specialization;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  @EqualsAndHashCode.Exclude @Builder.Default private boolean isDeleted = false;
}
