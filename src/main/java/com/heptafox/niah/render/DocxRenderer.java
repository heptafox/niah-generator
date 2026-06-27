package com.heptafox.niah.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

/** Renders a .docx from plain text using Apache POI. */
@Component
public class DocxRenderer implements DocumentRenderer {

    @Override
    public DocFormat format() {
        return DocFormat.DOCX;
    }

    @Override
    public byte[] render(String title, List<String> paragraphs) {
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setText(title);

            for (String text : paragraphs) {
                XWPFParagraph para = document.createParagraph();
                XWPFRun run = para.createRun();
                run.setText(text);
            }

            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to render DOCX", e);
        }
    }
}
