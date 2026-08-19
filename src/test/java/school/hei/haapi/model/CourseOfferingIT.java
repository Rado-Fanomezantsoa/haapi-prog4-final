package school.hei.haapi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.dto.course.CourseOfferingDto;
import school.hei.haapi.repository.AppUserRepository;
import school.hei.haapi.repository.ClassGroupRepository;
import school.hei.haapi.repository.CourseOfferingRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.security.TestJwtGenerator;

class CourseOfferingIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @LocalServerPort private int port;

  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseOfferingRepository courseOfferingRepository;
  @Autowired private AppUserRepository appUserRepository;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private ClassGroupRepository classGroupRepository;

  private String baseUrl() {
    return "http://localhost:" + port + "/api";
  }

  private HttpHeaders headers(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return headers;
  }

  private AppUser persistAdmin() {
    return appUserRepository.save(
        AppUser.builder()
            .email("admin-" + UUID.randomUUID() + "@hei.school")
            .passwordHash("hash")
            .firstName("Admin")
            .lastName("Test")
            .role(AppUser.Role.ADMIN)
            .build());
  }

  private AppUser persistTeacher() {
    return appUserRepository.save(
        AppUser.builder()
            .email("teacher-" + UUID.randomUUID() + "@hei.school")
            .passwordHash("hash")
            .firstName("Teacher")
            .lastName("Test")
            .role(AppUser.Role.TEACHER)
            .teacherRef("TCH-" + UUID.randomUUID().toString().substring(0, 8))
            .build());
  }

  private AppUser persistStudent() {
    return appUserRepository.save(
        AppUser.builder()
            .email("student-" + UUID.randomUUID() + "@hei.school")
            .passwordHash("hash")
            .firstName("Student")
            .lastName("Test")
            .role(AppUser.Role.STUDENT)
            .studentRef("STD" + UUID.randomUUID().toString().replace("-", "").substring(0, 7))
            .specialization(Specialization.EL)
            .build());
  }

  private Course persistCourse() {
    return courseRepository.save(
        Course.builder()
            .ref("CRS-" + UUID.randomUUID().toString().substring(0, 8))
            .title("Cours de test")
            .credits(5)
            .semesterNumber(4)
            .specialization(Specialization.EL)
            .build());
  }

  private CourseOffering persistOffering(Course course) {
    return courseOfferingRepository.save(
        CourseOffering.builder().course(course).calendarYear(2026).build());
  }

  private Promotion persistPromotion() {
    return promotionRepository.save(
        Promotion.builder()
            .code("P" + UUID.randomUUID().toString().substring(0, 4))
            .entryCalendarYear(2023)
            .expectedGraduationYear(2026)
            .build());
  }

  private ClassGroup persistGroup(Promotion promotion) {
    return classGroupRepository.save(
        ClassGroup.builder()
            .promotion(promotion)
            .ref("G" + UUID.randomUUID().toString().substring(0, 4))
            .academicLevel(AcademicLevel.L2)
            .specialization(Specialization.EL)
            .build());
  }

  // ============================================================
  // POST /course-offerings
  // ============================================================

  @Test
  void create_asAdmin_returns201_andPersists() {
    Course course = persistCourse();
    AppUser admin = persistAdmin();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");

    String body = "{\"courseId\": \"" + course.getId() + "\", \"calendarYear\": 2026}";

    ResponseEntity<CourseOfferingDto> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            CourseOfferingDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCalendarYear()).isEqualTo(2026);
    assertThat(courseOfferingRepository.findById(UUID.fromString(response.getBody().getId())))
        .isPresent();
  }

  @Test
  void create_duplicateCourseAndYear_returns409() {
    Course course = persistCourse();
    AppUser admin = persistAdmin();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");
    String body = "{\"courseId\": \"" + course.getId() + "\", \"calendarYear\": 2026}";

    restTemplate.exchange(
        baseUrl() + "/course-offerings",
        HttpMethod.POST,
        new HttpEntity<>(body, headers(token)),
        String.class);

    ResponseEntity<String> secondResponse =
        restTemplate.exchange(
            baseUrl() + "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            String.class);

    assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void create_asTeacher_returns403() {
    Course course = persistCourse();
    AppUser teacher = persistTeacher();
    String token = TestJwtGenerator.generate(teacher.getId(), "TEACHER");
    String body = "{\"courseId\": \"" + course.getId() + "\", \"calendarYear\": 2026}";

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void create_unknownCourse_returns404() {
    AppUser admin = persistAdmin();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");
    String body = "{\"courseId\": \"" + UUID.randomUUID() + "\", \"calendarYear\": 2026}";

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void create_noToken_returns401() {
    Course course = persistCourse();
    String body = "{\"courseId\": \"" + course.getId() + "\", \"calendarYear\": 2026}";

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(null)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  // ============================================================
  // GET /course-offerings/{offeringId}
  // ============================================================

  @Test
  void getById_asTeacher_returns200() {
    CourseOffering offering = persistOffering(persistCourse());
    AppUser teacher = persistTeacher();
    String token = TestJwtGenerator.generate(teacher.getId(), "TEACHER");

    ResponseEntity<CourseOfferingDto> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId(),
            HttpMethod.GET,
            new HttpEntity<>(headers(token)),
            CourseOfferingDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(offering.getId().toString());
  }

  @Test
  void getById_unknownId_returns404() {
    AppUser admin = persistAdmin();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + UUID.randomUUID(),
            HttpMethod.GET,
            new HttpEntity<>(headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void getById_asStudent_returns403() {
    CourseOffering offering = persistOffering(persistCourse());
    AppUser student = persistStudent();
    String token = TestJwtGenerator.generate(student.getId(), "STUDENT");

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId(),
            HttpMethod.GET,
            new HttpEntity<>(headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  // ============================================================
  // POST /course-offerings/{offeringId}/teachers
  // ============================================================

  @Test
  void addTeacher_asAdmin_nominal_appearsInTeacherIds() {
    CourseOffering offering = persistOffering(persistCourse());
    AppUser admin = persistAdmin();
    AppUser teacher = persistTeacher();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");
    String body = "{\"teacherId\": \"" + teacher.getId() + "\"}";

    ResponseEntity<CourseOfferingDto> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId() + "/teachers",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            CourseOfferingDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getTeacherIds()).contains(teacher.getId().toString());
  }

  @Test
  void addTeacher_sameTeacherTwice_notDuplicated() {
    CourseOffering offering = persistOffering(persistCourse());
    AppUser admin = persistAdmin();
    AppUser teacher = persistTeacher();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");
    String body = "{\"teacherId\": \"" + teacher.getId() + "\"}";

    restTemplate.exchange(
        baseUrl() + "/course-offerings/" + offering.getId() + "/teachers",
        HttpMethod.POST,
        new HttpEntity<>(body, headers(token)),
        CourseOfferingDto.class);

    ResponseEntity<CourseOfferingDto> secondResponse =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId() + "/teachers",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            CourseOfferingDto.class);

    assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    long occurrences =
        secondResponse.getBody().getTeacherIds().stream()
            .filter(id -> id.equals(teacher.getId().toString()))
            .count();
    assertThat(occurrences).isEqualTo(1);
  }

  @Test
  void addTeacher_asTeacher_returns403() {
    CourseOffering offering = persistOffering(persistCourse());
    AppUser teacher = persistTeacher();
    AppUser otherTeacher = persistTeacher();
    String token = TestJwtGenerator.generate(teacher.getId(), "TEACHER");
    String body = "{\"teacherId\": \"" + otherTeacher.getId() + "\"}";

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId() + "/teachers",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void addTeacher_unknownTeacher_returns404() {
    CourseOffering offering = persistOffering(persistCourse());
    AppUser admin = persistAdmin();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");
    String body = "{\"teacherId\": \"" + UUID.randomUUID() + "\"}";

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId() + "/teachers",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  // ============================================================
  // POST /course-offerings/{offeringId}/groups
  // ============================================================

  @Test
  void addGroup_asAdmin_nominal_appearsInGroupIds() {
    CourseOffering offering = persistOffering(persistCourse());
    ClassGroup group = persistGroup(persistPromotion());
    AppUser admin = persistAdmin();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");
    String body = "{\"groupId\": \"" + group.getId() + "\"}";

    ResponseEntity<CourseOfferingDto> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId() + "/groups",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            CourseOfferingDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getGroupIds()).contains(group.getId().toString());
  }

  @Test
  void addGroup_unknownGroup_returns404() {
    CourseOffering offering = persistOffering(persistCourse());
    AppUser admin = persistAdmin();
    String token = TestJwtGenerator.generate(admin.getId(), "ADMIN");
    String body = "{\"groupId\": \"" + UUID.randomUUID() + "\"}";

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId() + "/groups",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void addGroup_asTeacher_returns403() {
    CourseOffering offering = persistOffering(persistCourse());
    ClassGroup group = persistGroup(persistPromotion());
    AppUser teacher = persistTeacher();
    String token = TestJwtGenerator.generate(teacher.getId(), "TEACHER");
    String body = "{\"groupId\": \"" + group.getId() + "\"}";

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/course-offerings/" + offering.getId() + "/groups",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
