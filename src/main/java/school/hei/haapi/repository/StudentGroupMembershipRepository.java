package school.hei.haapi.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.StudentGroupMembership;

public interface StudentGroupMembershipRepository
    extends JpaRepository<StudentGroupMembership, UUID> {

  Optional<StudentGroupMembership> findByStudent_IdAndEndDateIsNull(UUID studentId);
}
