package school.hei.haapi.service.transcript;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.model.AppUser;

class TranscriptPdfGeneratorTest {

  @Test
  void generate_createsNonEmptyPdf() throws Exception {
    AppUser student =
        AppUser.builder()
            .id(UUID.randomUUID())
            .firstName("Jean")
            .lastName("Rakoto")
            .studentRef("STD24001")
            .email("j@hei.school")
            .passwordHash("x")
            .role(AppUser.Role.STUDENT)
            .build();

    File pdf =
        TranscriptPdfGenerator.generate(
            student,
            List.of(
                GradeDetailDto.builder()
                    .courseRef("PROG4")
                    .examLabel("Final")
                    .value(14.0)
                    .build()));
    assertTrue(pdf.exists());
    assertTrue(pdf.length() > 0);
    pdf.delete();
  }
}
