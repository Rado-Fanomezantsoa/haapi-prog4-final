package school.hei.haapi.service.graduate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import school.hei.haapi.dto.graduate.GraduateEntryDto;

public final class GraduateExcelGenerator {

  private GraduateExcelGenerator() {}

  public static File generate(List<GraduateEntryDto> graduates) throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Diplomes");
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Rang");
      header.createCell(1).setCellValue("Reference");
      header.createCell(2).setCellValue("Nom");
      header.createCell(3).setCellValue("Prenom");
      header.createCell(4).setCellValue("Moyenne");
      header.createCell(5).setCellValue("Credits");

      int rowIdx = 1;
      for (GraduateEntryDto g : graduates) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(g.getRank());
        row.createCell(1).setCellValue(g.getStudentRef());
        row.createCell(2).setCellValue(g.getLastName());
        row.createCell(3).setCellValue(g.getFirstName());
        row.createCell(4).setCellValue(g.getOverallAverage());
        row.createCell(5).setCellValue(g.getCreditsTaken());
      }

      File file = File.createTempFile("graduates-", ".xlsx");
      try (FileOutputStream out = new FileOutputStream(file)) {
        workbook.write(out);
      }
      return file;
    }
  }
}
