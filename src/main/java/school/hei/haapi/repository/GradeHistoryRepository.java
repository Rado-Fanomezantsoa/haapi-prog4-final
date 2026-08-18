package school.hei.haapi.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.GradeHistory;

public interface GradeHistoryRepository extends JpaRepository<GradeHistory, UUID> {

  List<GradeHistory> findByGrade_IdOrderByModifiedAtDesc(UUID gradeId);
}
