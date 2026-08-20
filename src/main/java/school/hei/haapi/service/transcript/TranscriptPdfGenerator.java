package school.hei.haapi.service.transcript;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.model.AppUser;

public final class TranscriptPdfGenerator {

  private static final float MARGIN_LEFT = 50f;
  private static final float START_Y = 750f;
  private static final float LINE_LEADING = 16f;
  private static final float BOTTOM_MARGIN = 50f;
  private static final float FONT_SIZE = 11f;

  private TranscriptPdfGenerator() {}

  public static File generate(AppUser student, List<GradeDetailDto> grades) throws IOException {
    File file = File.createTempFile("transcript-", ".pdf");

    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.LETTER);
      document.addPage(page);
      PDPageContentStream contentStream = new PDPageContentStream(document, page);

      float cursorY = START_Y;
      contentStream.beginText();
      contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE + 2);
      contentStream.setLeading(LINE_LEADING);
      contentStream.newLineAtOffset(MARGIN_LEFT, cursorY);
      contentStream.showText("RELEVE DE NOTES - HEI");
      contentStream.newLine();
      contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
      contentStream.showText(
          sanitize("Etudiant : " + student.getLastName() + " " + student.getFirstName()));
      contentStream.newLine();
      contentStream.showText(sanitize("Ref : " + student.getStudentRef()));
      contentStream.newLine();
      contentStream.newLine();
      cursorY -= LINE_LEADING * 4;

      for (GradeDetailDto g : grades) {

        if (cursorY <= BOTTOM_MARGIN) {
          contentStream.endText();
          contentStream.close();

          page = new PDPage(PDRectangle.LETTER);
          document.addPage(page);
          contentStream = new PDPageContentStream(document, page);
          cursorY = START_Y;
          contentStream.beginText();
          contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
          contentStream.setLeading(LINE_LEADING);
          contentStream.newLineAtOffset(MARGIN_LEFT, cursorY);
        }

        String line =
            sanitize(g.getCourseRef() + " - " + g.getExamLabel() + " : " + g.getValue() + "/20");
        contentStream.showText(line);
        contentStream.newLine();
        cursorY -= LINE_LEADING;
      }

      contentStream.endText();
      contentStream.close();
      document.save(file);
    }

    return file;
  }

  private static String sanitize(String text) {
    return text == null ? "" : text.replaceAll("[^\\x20-\\x7E]", "?");
  }
}
