package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.ClassGroup;
import school.hei.haapi.model.StudentGroupMembership;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.StudentGroupMembershipRepository;

@ExtendWith(MockitoExtension.class)
class GroupMembershipServiceTest {

  @Mock private ClassGroupRepository classGroupRepository;
  @Mock private AppUserRepository appUserRepository;
  @Mock private StudentGroupMembershipRepository membershipRepository;

  @InjectMocks private GroupMembershipService groupMembershipService;

  private AppUser student(UUID id) {
    return AppUser.builder()
        .id(id)
        .role(AppUser.Role.STUDENT)
        .studentRef("STD1")
        .firstName("A")
        .lastName("B")
        .email("a@hei.school")
        .passwordHash("x")
        .build();
  }

  private ClassGroup group(UUID id) {
    return ClassGroup.builder().id(id).ref("K1").build();
  }

  @Test
  void assignStudents_newStudent_createsMembership() {
    UUID groupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    when(classGroupRepository.findById(groupId)).thenReturn(Optional.of(group(groupId)));
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId)));
    when(membershipRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.empty());

    groupMembershipService.assignStudents(groupId, List.of(studentId));

    ArgumentCaptor<StudentGroupMembership> captor =
        ArgumentCaptor.forClass(StudentGroupMembership.class);
    verify(membershipRepository).save(captor.capture());

    StudentGroupMembership saved = captor.getValue();
    assertEquals(studentId, saved.getStudent().getId());
    assertEquals(groupId, saved.getGroup().getId());
    assertEquals(LocalDate.now(), saved.getStartDate());
  }

  @Test
  void assignStudents_changingGroup_closesOldMembership_opensNew() {
    UUID oldGroupId = UUID.randomUUID();
    UUID newGroupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    StudentGroupMembership oldMembership =
        StudentGroupMembership.builder()
            .id(UUID.randomUUID())
            .student(student(studentId))
            .group(group(oldGroupId))
            .startDate(LocalDate.now().minusMonths(3))
            .build();

    when(classGroupRepository.findById(newGroupId)).thenReturn(Optional.of(group(newGroupId)));
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId)));
    when(membershipRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(oldMembership));

    groupMembershipService.assignStudents(newGroupId, List.of(studentId));

    verify(membershipRepository).saveAndFlush(any());
    verify(membershipRepository).save(any());
    assertEquals(LocalDate.now(), oldMembership.getEndDate());
  }

  @Test
  void assignStudents_alreadyInTargetGroup_noOp() {
    UUID groupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    StudentGroupMembership currentMembership =
        StudentGroupMembership.builder()
            .id(UUID.randomUUID())
            .student(student(studentId))
            .group(group(groupId))
            .startDate(LocalDate.now().minusMonths(1))
            .build();

    when(classGroupRepository.findById(groupId)).thenReturn(Optional.of(group(groupId)));
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.of(student(studentId)));
    when(membershipRepository.findByStudent_IdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(currentMembership));

    groupMembershipService.assignStudents(groupId, List.of(studentId));

    verify(membershipRepository, never()).save(any());
  }

  @Test
  void assignStudents_unknownGroup_throwsNotFound() {
    UUID groupId = UUID.randomUUID();
    when(classGroupRepository.findById(groupId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> groupMembershipService.assignStudents(groupId, List.of(UUID.randomUUID())));
  }

  @Test
  void assignStudents_unknownStudent_throwsNotFound() {
    UUID groupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    when(classGroupRepository.findById(groupId)).thenReturn(Optional.of(group(groupId)));
    when(appUserRepository.findByIdAndRole(studentId, AppUser.Role.STUDENT))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> groupMembershipService.assignStudents(groupId, List.of(studentId)));
  }

  @Test
  void getActiveStudents_returnsStudentDtos() {
    UUID groupId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();

    when(classGroupRepository.findById(groupId)).thenReturn(Optional.of(group(groupId)));
    when(appUserRepository.findActiveStudentsByGroupId(groupId, AppUser.Role.STUDENT))
        .thenReturn(List.of(student(studentId)));

    List<StudentDto> result = groupMembershipService.getActiveStudents(groupId);

    assertEquals(1, result.size());
    assertEquals(studentId.toString(), result.get(0).getId());
  }

  @Test
  void getActiveStudents_unknownGroup_throwsNotFound() {
    UUID groupId = UUID.randomUUID();
    when(classGroupRepository.findById(groupId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> groupMembershipService.getActiveStudents(groupId));
  }
}
