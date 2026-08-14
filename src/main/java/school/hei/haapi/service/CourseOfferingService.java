package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.dto.course.CourseOfferingDto;
import school.hei.haapi.dto.course.CourseOfferingInputDto;
import school.hei.haapi.dto.course.CourseSummaryDto;
import school.hei.haapi.exception.ConflictException;
import school.hei.haapi.exception.NotFoundException;
import school.hei.haapi.model.AppUser;
import school.hei.haapi.model.ClassGroup;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseOffering;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.CourseOfferingRepository;
import school.hei.haapi.repository.CourseRepository;

@Service
@RequiredArgsConstructor
public class CourseOfferingService {

  private final CourseOfferingRepository courseOfferingRepository;
  private final CourseRepository courseRepository;
  private final AppUserRepository appUserRepository;
  private final ClassGroupRepository classGroupRepository;

  @Transactional
  public CourseOfferingDto create(CourseOfferingInputDto input) {

    UUID courseId = UUID.fromString(input.getCourseId());

    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + input.getCourseId()));

    if (courseOfferingRepository.existsByCourse_IdAndCalendarYear(
        courseId, input.getCalendarYear())) {

      throw new ConflictException(
          "A course-offering already exists for course "
              + input.getCourseId()
              + " and year "
              + input.getCalendarYear());
    }

    CourseOffering offering =
        CourseOffering.builder().course(course).calendarYear(input.getCalendarYear()).build();

    return toDto(courseOfferingRepository.save(offering));
  }

  public CourseOfferingDto getById(String offeringId) {
    return toDto(findOfferingOrThrow(UUID.fromString(offeringId)));
  }

  @Transactional
  public CourseOfferingDto addTeacher(String offeringId, String teacherId) {

    CourseOffering offering = findOfferingOrThrow(UUID.fromString(offeringId));

    UUID teacherUuid = UUID.fromString(teacherId);

    AppUser teacher =
        appUserRepository
            .findByIdAndRole(teacherUuid, AppUser.Role.TEACHER)
            .orElseThrow(() -> new NotFoundException("Teacher not found: " + teacherId));

    if (offering.getTeachers() == null) {
      offering.setTeachers(new ArrayList<>());
    }

    boolean alreadyAssigned =
        offering.getTeachers().stream().anyMatch(t -> t.getId().equals(teacherUuid));

    if (!alreadyAssigned) {
      offering.getTeachers().add(teacher);
    }

    return toDto(courseOfferingRepository.save(offering));
  }

  @Transactional
  public CourseOfferingDto addGroup(String offeringId, String groupId) {

    CourseOffering offering = findOfferingOrThrow(UUID.fromString(offeringId));

    UUID groupUuid = UUID.fromString(groupId);

    ClassGroup group =
        classGroupRepository
            .findById(groupUuid)
            .orElseThrow(() -> new NotFoundException("Group not found: " + groupId));

    if (offering.getGroups() == null) {
      offering.setGroups(new ArrayList<>());
    }

    boolean alreadyAssociated =
        offering.getGroups().stream().anyMatch(g -> g.getId().equals(groupUuid));

    if (!alreadyAssociated) {
      offering.getGroups().add(group);
    }

    return toDto(courseOfferingRepository.save(offering));
  }

  private CourseOffering findOfferingOrThrow(UUID offeringId) {
    return courseOfferingRepository
        .findById(offeringId)
        .orElseThrow(() -> new NotFoundException("Course-offering not found: " + offeringId));
  }

  private CourseOfferingDto toDto(CourseOffering offering) {

    return CourseOfferingDto.builder()
        .id(offering.getId().toString())
        .course(
            CourseSummaryDto.builder()
                .id(offering.getCourse().getId().toString())
                .ref(offering.getCourse().getRef())
                .title(offering.getCourse().getTitle())
                .credits(offering.getCourse().getCredits())
                .semesterNumber(offering.getCourse().getSemesterNumber())
                .build())
        .calendarYear(offering.getCalendarYear())
        .teacherIds(
            offering.getTeachers() == null
                ? java.util.List.of()
                : offering.getTeachers().stream().map(t -> t.getId().toString()).toList())
        .groupIds(
            offering.getGroups() == null
                ? java.util.List.of()
                : offering.getGroups().stream().map(g -> g.getId().toString()).toList())
        .build();
  }
}
