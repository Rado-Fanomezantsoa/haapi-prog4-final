package school.hei.haapi.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.CourseOffering;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, UUID> {

  boolean existsByCourse_IdAndCalendarYear(UUID courseId, int calendarYear);

  boolean existsByIdAndTeachers_Id(UUID id, UUID teacherId);

  Optional<CourseOffering> findByCourse_IdAndCalendarYear(UUID courseId, int calendarYear);
}
