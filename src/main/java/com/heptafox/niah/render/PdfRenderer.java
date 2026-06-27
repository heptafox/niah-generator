package com.heptafox.niah.render;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/** Renders a fresh, paginated PDF from plain text using OpenPDF. */
@Component
public class PdfRenderer implements DocumentRenderer {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.TIMES_BOLD, 20);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11);

    @Override
    public DocFormat format() {
        return DocFormat.PDF;
    }

    @Override
    public byte[] render(String title, List<String> paragraphs) {
        Document document = new Document(PageSize.A4, 56, 56, 56, 56);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph titlePara = new Paragraph(title, TITLE_FONT);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(18f);
            document.add(titlePara);

            for (String text : paragraphs) {
                Paragraph para = new Paragraph(text, BODY_FONT);
                para.setSpacingAfter(8f);
                para.setLeading(15f);
                document.add(para);
            }
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to render PDF", e);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }
}
