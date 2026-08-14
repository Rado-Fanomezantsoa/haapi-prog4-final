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
import java.time.LocalDate;
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
@Table(name = "\"app_user\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SQLDelete(sql = "update \"app_user\" set is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
public class AppUser implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  @ToString.Exclude
  private String passwordHash;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Enumerated(STRING)
  @Column(nullable = false, length = 10)
  private Role role;

  @Column(name = "student_ref", unique = true, length = 10)
  private String studentRef;

  @ManyToOne
  @JoinColumn(name = "promotion_id")
  @ToString.Exclude
  private Promotion promotion;

  @Enumerated(STRING)
  @Column(length = 10)
  private Specialization specialization;

  @Column(name = "enrolled_at")
  private LocalDate enrolledAt;

  @Column(name = "teacher_ref", unique = true)
  private String teacherRef;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  @EqualsAndHashCode.Exclude @Builder.Default private boolean isDeleted = false;

  public enum Role {
    STUDENT,
    TEACHER,
    ADMIN
  }
}
