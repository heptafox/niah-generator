package com.heptafox.niah.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heptafox.niah.catalog.Artifact;
import com.heptafox.niah.catalog.ArtifactCache;
import com.heptafox.niah.catalog.CatalogEntry;
import com.heptafox.niah.catalog.CatalogItem;
import com.heptafox.niah.catalog.CatalogService;
import com.heptafox.niah.catalog.ZipPackager;
import com.heptafox.niah.needle.NeedleMode;

/** REST API backing the catalog UI: list entries and download a (optionally answer-keyed) document. */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    private final ArtifactCache cache;
    private final ZipPackager zipPackager;

    public CatalogController(CatalogService catalogService, ArtifactCache cache, ZipPackager zipPackager) {
        this.catalogService = catalogService;
        this.cache = cache;
        this.zipPackager = zipPackager;
    }

    @GetMapping
    public List<CatalogEntry> list() {
        return catalogService.entries();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String id,
            @RequestParam(defaultValue = "isolated") String mode,
            @RequestParam(defaultValue = "false") boolean answerKey) {

        CatalogItem item = catalogService.find(id)
                .orElseThrow(() -> new NoSuchElementException("Unknown catalog entry: " + id));
        Artifact artifact = cache.get(item, NeedleMode.from(mode));

        if (answerKey) {
            return fileResponse(zipPackager.zip(artifact), MediaType.valueOf("application/zip"),
                    artifact.zipFilename());
        }
        return fileResponse(artifact.document(), MediaType.parseMediaType(item.format().mediaType()),
                artifact.documentFilename());
    }

    private ResponseEntity<Resource> fileResponse(byte[] bytes, MediaType mediaType, String filename) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(bytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(bytes));
    }
}
