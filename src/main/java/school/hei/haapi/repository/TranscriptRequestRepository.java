package school.hei.haapi.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.TranscriptRequest;

public interface TranscriptRequestRepository extends JpaRepository<TranscriptRequest, UUID> {}
