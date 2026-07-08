package com.heptafox.niah.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heptafox.niah.catalog.Artifact;
import com.heptafox.niah.catalog.ArtifactCache;
import com.heptafox.niah.catalog.CatalogEntry;
import com.heptafox.niah.catalog.CatalogItem;
import com.heptafox.niah.catalog.CatalogService;
import com.heptafox.niah.catalog.ZipPackager;
import com.heptafox.niah.needle.NeedleMode;

/**
 * REST API backing the two-tab UI:
 * <ul>
 *   <li><b>RAG</b> — downloadable dataset files ({@code .md}/{@code .docx}/{@code .pdf}), optionally
 *       zipped with the answer key, via {@code GET /api/catalog/{id}/download}.</li>
 *   <li><b>Agent crawl</b> — each haystack served as an inline HTML page at a stable URL that an
 *       agent crawls ({@code GET /haystack/{id}}), plus a JSON answer key
 *       ({@code GET /api/catalog/{id}/answer-key}) for the evaluation harness.</li>
 * </ul>
 * Both tabs share the same slice → inject → render → manifest pipeline; they differ only in format.
 */
@RestController
public class CatalogController {

    private final CatalogService catalogService;
    private final ArtifactCache cache;
    private final ZipPackager zipPackager;

    public CatalogController(CatalogService catalogService, ArtifactCache cache, ZipPackager zipPackager) {
        this.catalogService = catalogService;
        this.cache = cache;
        this.zipPackager = zipPackager;
    }

    /** Size × format catalog for the UI (both tabs filter this list by format). */
    @GetMapping("/api/catalog")
    public List<CatalogEntry> list() {
        return catalogService.entries();
    }

    /** RAG tab: download a rendered document (optionally zipped with the answer-key manifest). */
    @GetMapping("/api/catalog/{id}/download")
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

    /** Crawl tab target: the haystack (with needles injected) as an inline HTML page. */
    @GetMapping("/haystack/{id}")
    public ResponseEntity<Resource> crawl(
            @PathVariable String id,
            @RequestParam(defaultValue = "isolated") String mode) {
        Artifact artifact = artifact(id, mode);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/html; charset=UTF-8"))
                .cacheControl(CacheControl.noCache())
                .body(new ByteArrayResource(artifact.document()));
    }

    /** Crawl tab answer key: every injected needle with its question, ground-truth answer, and depth. */
    @GetMapping("/api/catalog/{id}/answer-key")
    public ResponseEntity<Resource> answerKey(
            @PathVariable String id,
            @RequestParam(defaultValue = "isolated") String mode) {
        Artifact artifact = artifact(id, mode);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ByteArrayResource(artifact.manifestJson()));
    }

    private Artifact artifact(String id, String mode) {
        CatalogItem item = catalogService.find(id)
                .orElseThrow(() -> new NoSuchElementException("Unknown catalog entry: " + id));
        return cache.get(item, NeedleMode.from(mode));
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
