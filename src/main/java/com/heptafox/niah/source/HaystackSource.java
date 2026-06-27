package com.heptafox.niah.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.heptafox.niah.config.NiahProperties;

import jakarta.annotation.PostConstruct;

/**
 * Loads the source PDF once at startup, cleans it, and caches the canonical text in memory so that
 * downstream slicing/injection never re-parses the PDF.
 */
@Component
public class HaystackSource {

    private static final Logger log = LoggerFactory.getLogger(HaystackSource.class);

    private final NiahProperties properties;
    private final ResourceLoader resourceLoader;
    private final PdfTextExtractor extractor;
    private final TextCleaner cleaner;

    private CanonicalDocument document;

    public HaystackSource(NiahProperties properties, ResourceLoader resourceLoader,
            PdfTextExtractor extractor, TextCleaner cleaner) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.extractor = extractor;
        this.cleaner = cleaner;
    }

    @PostConstruct
    void load() {
        Resource resource = resourceLoader.getResource(properties.sourcePdf());
        try (InputStream in = resource.getInputStream()) {
            List<String> rawPages = extractor.extractPages(in);
            List<String> cleanedPages = new ArrayList<>(rawPages.size());
            for (String raw : rawPages) {
                String cleaned = cleaner.cleanPage(raw);
                if (!cleaned.isBlank()) {
                    cleanedPages.add(cleaned);
                }
            }
            String fullText = String.join("\n\n", cleanedPages);
            this.document = new CanonicalDocument(cleanedPages, fullText);
            log.info("Loaded haystack: {} pages, ~{} chars from {}",
                    cleanedPages.size(), fullText.length(), properties.sourcePdf());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load source PDF: " + properties.sourcePdf(), e);
        }
    }

    public CanonicalDocument document() {
        return document;
    }
}
