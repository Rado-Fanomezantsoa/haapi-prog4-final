package school.hei.haapi.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "\"course_offering\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SQLDelete(sql = "update \"course_offering\" set is_deleted = true where id = ?")
@SQLRestriction("is_deleted = false")
public class CourseOffering implements Serializable {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private UUID id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "course_id")
  @ToString.Exclude
  private Course course;

  @Column(name = "calendar_year", nullable = false)
  private int calendarYear;

  @ManyToMany
  @JoinTable(
      name = "course_offering_teacher",
      joinColumns = @JoinColumn(name = "course_offering_id"),
      inverseJoinColumns = @JoinColumn(name = "teacher_id"))
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<AppUser> teachers;

  @ManyToMany
  @JoinTable(
      name = "course_offering_group",
      joinColumns = @JoinColumn(name = "course_offering_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<ClassGroup> groups;

  @OneToMany(mappedBy = "courseOffering", fetch = LAZY)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<Exam> exams;

  @EqualsAndHashCode.Exclude @CreationTimestamp private Instant creationDatetime;

  @EqualsAndHashCode.Exclude @Builder.Default private boolean isDeleted = false;
}
