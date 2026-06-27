package com.heptafox.niah.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** Extracts raw per-page text from a PDF using PDFBox 3.x. */
@Component
public class PdfTextExtractor {

    /** Returns one raw text string per page, in document order. */
    public List<String> extractPages(InputStream pdf) throws IOException {
        byte[] bytes = pdf.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            int pageCount = document.getNumberOfPages();
            List<String> pages = new ArrayList<>(pageCount);
            PDFTextStripper stripper = new PDFTextStripper();
            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                pages.add(stripper.getText(document));
            }
            return pages;
        }
    }
}
