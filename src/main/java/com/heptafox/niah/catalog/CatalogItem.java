package com.heptafox.niah.catalog;

import com.heptafox.niah.render.DocFormat;
import com.heptafox.niah.sizing.SizePreset;

/** Pairing of a public {@link CatalogEntry} with the inputs needed to build its artifact. */
public record CatalogItem(CatalogEntry entry, SizePreset preset, DocFormat format) {
}
