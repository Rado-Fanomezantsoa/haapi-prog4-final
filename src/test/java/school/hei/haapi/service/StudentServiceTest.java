package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.haapi.dto.student.StudentDto;
import school.hei.haapi.exception.BadRequestException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.ClassGroup;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.Specialization;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.PromotionRepository;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  @Mock private AppUserRepository appUserRepository;
  @Mock private ClassGroupRepository classGroupRepository;
  @Mock private PromotionRepository promotionRepository;

  @InjectMocks private StudentService studentService;

  private AppUser student(UUID id, String ref) {
    return AppUser.builder()
        .id(id)
        .studentRef(ref)
        .firstName("Jean")
        .lastName("Rakoto")
        .email(ref + "@hei.school")
        .passwordHash("x")
        .role(AppUser.Role.STUDENT)
        .specialization(Specialization.EL)
        .build();
  }

  @Test
  void listAllStudents_noFilter() {
    UUID id = UUID.randomUUID();
    when(appUserRepository.findByRole(AppUser.Role.STUDENT))
        .thenReturn(List.of(student(id, "STD24001")));

    List<StudentDto> result = studentService.findStudents(null, null);

    assertEquals(1, result.size());
    assertEquals("STD24001", result.get(0).getStudentRef());
    assertEquals(id.toString(), result.get(0).getId());
    verify(appUserRepository).findByRole(AppUser.Role.STUDENT);
  }

  @Test
  void listByPromotion() {
    UUID promoId = UUID.randomUUID();
    when(promotionRepository.findById(promoId)).thenReturn(Optional.of(new Promotion()));
    when(appUserRepository.findByRoleAndPromotion_Id(AppUser.Role.STUDENT, promoId))
        .thenReturn(List.of(student(UUID.randomUUID(), "STD24002")));

    List<StudentDto> result = studentService.findStudents(promoId.toString(), null);

    assertEquals(1, result.size());
    assertEquals("STD24002", result.get(0).getStudentRef());
  }

  @Test
  void listByGroup() {
    UUID groupId = UUID.randomUUID();
    when(classGroupRepository.findById(groupId)).thenReturn(Optional.of(new ClassGroup()));
    when(appUserRepository.findActiveStudentsByGroupId(groupId, AppUser.Role.STUDENT))
        .thenReturn(List.of(student(UUID.randomUUID(), "STD24003")));

    List<StudentDto> result = studentService.findStudents(null, groupId.toString());

    assertEquals(1, result.size());
    assertEquals("STD24003", result.get(0).getStudentRef());
  }

  @Test
  void listByGroupAndPromotion() {
    UUID groupId = UUID.randomUUID();
    UUID promoId = UUID.randomUUID();
    when(promotionRepository.findById(promoId)).thenReturn(Optional.of(new Promotion()));
    when(classGroupRepository.findById(groupId)).thenReturn(Optional.of(new ClassGroup()));
    when(appUserRepository.findActiveStudentsByGroupIdAndPromotionId(
            groupId, promoId, AppUser.Role.STUDENT))
        .thenReturn(List.of(student(UUID.randomUUID(), "STD24004")));

    List<StudentDto> result = studentService.findStudents(promoId.toString(), groupId.toString());

    assertEquals(1, result.size());
    verify(appUserRepository)
        .findActiveStudentsByGroupIdAndPromotionId(
            eq(groupId), eq(promoId), eq(AppUser.Role.STUDENT));
  }

  @Test
  void unknownPromotion_throwsNotFound() {
    UUID missing = UUID.randomUUID();
    when(promotionRepository.findById(missing)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> studentService.findStudents(missing.toString(), null));
  }

  @Test
  void invalidGroupUuid_throwsBadRequest() {
    assertThrows(BadRequestException.class, () -> studentService.findStudents(null, "not-a-uuid"));
  }

  @Test
  void unknownGroup_throwsNotFound() {
    UUID groupId = UUID.randomUUID();
    when(classGroupRepository.findById(groupId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> studentService.findStudents(null, groupId.toString()));
  }
}
