package school.hei.haapi.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.haapi.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

  Optional<AppUser> findByIdAndRole(UUID id, AppUser.Role role);
}
