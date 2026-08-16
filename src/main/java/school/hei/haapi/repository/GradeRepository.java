package school.hei.haapi.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.model.Grade;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

  @Query(
      """
      SELECT g FROM Grade g
      JOIN FETCH g.exam e
      JOIN FETCH e.courseOffering co
      JOIN FETCH co.course c
      WHERE g.student.id = :studentId
      """)
  List<Grade> findAllByStudentIdWithDetails(@Param("studentId") UUID studentId);
}
