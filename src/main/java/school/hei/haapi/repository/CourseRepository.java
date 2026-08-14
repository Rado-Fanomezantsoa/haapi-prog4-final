package school.hei.haapi.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Course;

public interface CourseRepository extends JpaRepository<Course, UUID> {}
