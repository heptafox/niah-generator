package com.heptafox.niah.catalog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.heptafox.niah.needle.NeedleMode;

/**
 * Lazily builds artifacts on first request and caches them in memory, so large documents (e.g. the
 * full book) are rendered at most once per process. Keyed by entry id and needle mode.
 */
@Component
public class ArtifactCache {

    private final ArtifactBuilder builder;
    private final ConcurrentMap<String, Artifact> cache = new ConcurrentHashMap<>();

    public ArtifactCache(ArtifactBuilder builder) {
        this.builder = builder;
    }

    public Artifact get(CatalogItem item, NeedleMode mode) {
        String key = item.entry().id() + ":" + mode.slug();
        return cache.computeIfAbsent(key, k -> builder.build(item, mode));
    }
}
