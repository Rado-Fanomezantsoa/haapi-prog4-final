package school.hei.haapi.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.ClassGroup;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {

  List<ClassGroup> findByPromotion_Id(UUID promotionId);
}
