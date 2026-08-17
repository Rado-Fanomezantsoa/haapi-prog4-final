package school.hei.haapi.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.ClassGroup;
import school.hei.haapi.model.StudentGroupMembership;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.StudentGroupMembershipRepository;

@Service
@RequiredArgsConstructor
public class GroupMembershipService {

  private final ClassGroupRepository classGroupRepository;
  private final AppUserRepository appUserRepository;
  private final StudentGroupMembershipRepository membershipRepository;

  @Transactional
  public void assignStudents(UUID groupId, List<UUID> studentIds) {
    ClassGroup group =
        classGroupRepository
            .findById(groupId)
            .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));

    for (UUID studentId : studentIds) {
      AppUser student =
          appUserRepository
              .findByIdAndRole(studentId, AppUser.Role.STUDENT)
              .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

      var activeMembership = membershipRepository.findByStudent_IdAndEndDateIsNull(studentId);

      boolean alreadyInGroup =
          activeMembership.map(m -> m.getGroup().getId().equals(groupId)).orElse(false);

      if (alreadyInGroup) {
        continue;
      }

      activeMembership.ifPresent(
          m -> {
            m.setEndDate(LocalDate.now());
            membershipRepository.saveAndFlush(m);
          });

      StudentGroupMembership newMembership =
          StudentGroupMembership.builder()
              .student(student)
              .group(group)
              .startDate(LocalDate.now())
              .build();
      membershipRepository.save(newMembership);
    }
  }

  @Transactional(readOnly = true)
  public List<StudentDto> getActiveStudents(UUID groupId) {
    classGroupRepository
        .findById(groupId)
        .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));

    return appUserRepository.findActiveStudentsByGroupId(groupId, AppUser.Role.STUDENT).stream()
        .map(this::toDto)
        .toList();
  }

  private StudentDto toDto(AppUser user) {
    return StudentDto.builder()
        .id(user.getId().toString())
        .studentRef(user.getStudentRef())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .specialization(user.getSpecialization())
        .build();
  }
}
