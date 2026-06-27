package com.heptafox.niah.catalog;

import com.heptafox.niah.render.DocFormat;

/**
 * A fully built, cacheable download: the rendered document bytes plus its answer-key manifest JSON.
 */
public record Artifact(String baseFilename, DocFormat format, byte[] document, byte[] manifestJson) {

    public String documentFilename() {
        return baseFilename + "." + format.extension();
    }

    public String zipFilename() {
        return baseFilename + ".zip";
    }
}
