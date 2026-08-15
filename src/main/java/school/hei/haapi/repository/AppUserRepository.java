package school.hei.haapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import school.hei.haapi.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

  Optional<AppUser> findByIdAndRole(UUID id, AppUser.Role role);

  List<AppUser> findByRole(AppUser.Role role);

  List<AppUser> findByRoleAndPromotion_Id(AppUser.Role role, UUID promotionId);

  @Query(
      """
      SELECT m.student FROM StudentGroupMembership m
      WHERE m.group.id = :groupId
        AND m.endDate IS NULL
        AND m.student.role = :role
      """)
  List<AppUser> findActiveStudentsByGroupId(
      @Param("groupId") UUID groupId, @Param("role") AppUser.Role role);

  @Query(
      """
      SELECT m.student FROM StudentGroupMembership m
      WHERE m.group.id = :groupId
        AND m.endDate IS NULL
        AND m.student.role = :role
        AND m.student.promotion.id = :promotionId
      """)
  List<AppUser> findActiveStudentsByGroupIdAndPromotionId(
      @Param("groupId") UUID groupId,
      @Param("promotionId") UUID promotionId,
      @Param("role") AppUser.Role role);
}
