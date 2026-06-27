package com.heptafox.niah.catalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.heptafox.niah.config.NiahProperties;
import com.heptafox.niah.render.DocFormat;
import com.heptafox.niah.sizing.DocumentSlicer;
import com.heptafox.niah.sizing.SizePreset;
import com.heptafox.niah.sizing.TokenEstimator;
import com.heptafox.niah.source.CanonicalDocument;
import com.heptafox.niah.source.HaystackSource;

import jakarta.annotation.PostConstruct;

/** Defines the pre-built catalog matrix (size presets × formats) and resolves entries by id. */
@Service
public class CatalogService {

    private final NiahProperties properties;
    private final HaystackSource haystack;
    private final DocumentSlicer slicer;
    private final TokenEstimator tokenEstimator;

    private final Map<String, CatalogItem> items = new LinkedHashMap<>();

    public CatalogService(NiahProperties properties, HaystackSource haystack,
            DocumentSlicer slicer, TokenEstimator tokenEstimator) {
        this.properties = properties;
        this.haystack = haystack;
        this.slicer = slicer;
        this.tokenEstimator = tokenEstimator;
    }

    @PostConstruct
    void build() {
        CanonicalDocument document = haystack.document();
        int fullChars = Math.max(document.fullText().length(), 1);
        int pageCount = document.pageCount();

        for (SizePreset preset : buildPresets(pageCount)) {
            String sliced = slicer.slice(document, preset);
            int approxTokens = tokenEstimator.estimate(sliced);
            int approxPages = approxPages(preset, sliced.length(), fullChars, pageCount);

            for (DocFormat format : DocFormat.values()) {
                String id = preset.id() + "-" + format.extension();
                CatalogEntry entry = new CatalogEntry(
                        id, preset.label(), format.extension(),
                        preset.sizingDescriptor(), approxTokens, approxPages);
                items.put(id, new CatalogItem(entry, preset, format));
            }
        }
    }

    private List<SizePreset> buildPresets(int pageCount) {
        List<SizePreset> presets = new ArrayList<>();
        for (int pages : properties.pagePresets()) {
            presets.add(new SizePreset.PagePreset(
                    "pages-" + pages, "First " + pages + " pages", pages, false));
        }
        presets.add(new SizePreset.PagePreset(
                "pages-full", "Full book (" + pageCount + " pages)", pageCount, true));
        for (int tokens : properties.tokenPresets()) {
            presets.add(new SizePreset.TokenPreset(
                    "tokens-" + humanTokens(tokens), "~" + humanTokens(tokens) + " tokens", tokens));
        }
        return presets;
    }

    private int approxPages(SizePreset preset, int slicedChars, int fullChars, int pageCount) {
        if (preset instanceof SizePreset.PagePreset page) {
            return page.full() ? pageCount : Math.min(page.pages(), pageCount);
        }
        return (int) Math.round((double) pageCount * slicedChars / fullChars);
    }

    private String humanTokens(int tokens) {
        return tokens % 1000 == 0 ? (tokens / 1000) + "k" : String.valueOf(tokens);
    }

    public List<CatalogEntry> entries() {
        return items.values().stream().map(CatalogItem::entry).toList();
    }

    public Optional<CatalogItem> find(String id) {
        return Optional.ofNullable(items.get(id));
    }
}
