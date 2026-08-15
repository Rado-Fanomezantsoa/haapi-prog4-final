package school.hei.haapi.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {}
