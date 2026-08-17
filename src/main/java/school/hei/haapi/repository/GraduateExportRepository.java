package school.hei.haapi.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.GraduateExport;

public interface GraduateExportRepository extends JpaRepository<GraduateExport, UUID> {}
