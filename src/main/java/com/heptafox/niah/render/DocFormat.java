package com.heptafox.niah.render;

/** Output document formats offered by the catalog. */
public enum DocFormat {

    MD("md", "text/markdown"),
    PDF("pdf", "application/pdf"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final String extension;
    private final String mediaType;

    DocFormat(String extension, String mediaType) {
        this.extension = extension;
        this.mediaType = mediaType;
    }

    public String extension() {
        return extension;
    }

    public String mediaType() {
        return mediaType;
    }
}
