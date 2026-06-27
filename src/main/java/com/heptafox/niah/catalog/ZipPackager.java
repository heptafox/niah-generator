package com.heptafox.niah.catalog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

/** Bundles a document and its answer-key manifest into a single zip download. */
@Component
public class ZipPackager {

    public byte[] zip(Artifact artifact) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ZipOutputStream zip = new ZipOutputStream(out)) {

            zip.putNextEntry(new ZipEntry(artifact.documentFilename()));
            zip.write(artifact.document());
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("manifest.json"));
            zip.write(artifact.manifestJson());
            zip.closeEntry();

            zip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build zip for " + artifact.baseFilename(), e);
        }
    }
}
