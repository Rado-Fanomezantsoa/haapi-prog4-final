package school.hei.haapi.service.transcript;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import school.hei.haapi.dto.student.GradeDetailDto;
import school.hei.haapi.model.AppUser;

public final class TranscriptPdfGenerator {

  private TranscriptPdfGenerator() {}

  public static File generate(AppUser student, List<GradeDetailDto> grades) throws IOException {
    StringBuilder body = new StringBuilder();
    body.append("RELEVE DE NOTES - HEI\n");
    body.append("Etudiant : ")
        .append(student.getLastName())
        .append(" ")
        .append(student.getFirstName())
        .append("\n");
    body.append("Ref : ").append(student.getStudentRef()).append("\n\n");
    for (GradeDetailDto g : grades) {
      body.append(g.getCourseRef())
          .append(" - ")
          .append(g.getExamLabel())
          .append(" : ")
          .append(g.getValue())
          .append("/20\n");
    }

    String content = body.toString().replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    String pdf =
        """
%%PDF-1.1
1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj
2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj
3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources<< /Font<< /F1 5 0 R >> >> >>endobj
4 0 obj<< /Length %d >>stream
BT /F1 10 Tf 50 750 Td (%s) Tj ET
endstream
endobj
5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj
xref
0 6
0000000000 65535 f\s
trailer<< /Size 6 /Root 1 0 R >>
startxref
0
%%%%EOF
"""
            .formatted(content.length() + 30, content.replace("\n", ") Tj T* ("));

    File file = File.createTempFile("transcript-", ".pdf");
    try (FileOutputStream out = new FileOutputStream(file)) {
      out.write(pdf.getBytes(StandardCharsets.US_ASCII));
    }
    return file;
  }
}
