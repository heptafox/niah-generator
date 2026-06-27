package com.heptafox.niah.render;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/** Resolves the {@link DocumentRenderer} for a given {@link DocFormat}. */
@Component
public class RendererRegistry {

    private final Map<DocFormat, DocumentRenderer> byFormat = new EnumMap<>(DocFormat.class);

    public RendererRegistry(List<DocumentRenderer> renderers) {
        for (DocumentRenderer renderer : renderers) {
            byFormat.put(renderer.format(), renderer);
        }
    }

    public DocumentRenderer get(DocFormat format) {
        DocumentRenderer renderer = byFormat.get(format);
        if (renderer == null) {
            throw new IllegalArgumentException("No renderer for format " + format);
        }
        return renderer;
    }
}
