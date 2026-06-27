package com.heptafox.niah.catalog;

/**
 * Public catalog listing row (serialized to JSON for the UI).
 *
 * @param id           stable download id, e.g. {@code pages-50-pdf}
 * @param label        size label, e.g. "First 50 pages"
 * @param format       file extension, e.g. {@code pdf}
 * @param sizing       compact size descriptor, e.g. {@code pages=50}
 * @param approxTokens approximate token count (see token estimate note)
 * @param approxPages  approximate source-page count
 */
public record CatalogEntry(
        String id,
        String label,
        String format,
        String sizing,
        int approxTokens,
        int approxPages) {
}
