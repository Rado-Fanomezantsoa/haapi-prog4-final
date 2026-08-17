package school.hei.haapi.service.graduate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import school.hei.haapi.dto.graduate.GraduateEntryDto;

class GraduateExcelGeneratorTest {

  @Test
  void generate_createsNonEmptyXlsx() throws Exception {
    File file =
        GraduateExcelGenerator.generate(
            List.of(
                GraduateEntryDto.builder()
                    .rank(1)
                    .studentRef("STD24001")
                    .lastName("Rakoto")
                    .firstName("Jean")
                    .overallAverage(15.5)
                    .creditsTaken(180)
                    .expectedCredits(180)
                    .build()));
    assertTrue(file.exists());
    assertTrue(file.length() > 100);
    file.delete();
  }

  @Test
  void generate_emptyList_stillCreatesFile() throws Exception {
    File file = GraduateExcelGenerator.generate(List.of());
    assertTrue(file.exists());
    assertTrue(file.length() > 0);
    file.delete();
  }
}
