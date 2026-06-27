package com.heptafox.niah.catalog;

import java.util.List;

import org.springframework.stereotype.Component;

import com.heptafox.niah.needle.InjectedNeedle;
import com.heptafox.niah.needle.InjectionResult;
import com.heptafox.niah.needle.NeedleInjector;
import com.heptafox.niah.needle.NeedleMode;
import com.heptafox.niah.render.RendererRegistry;
import com.heptafox.niah.sizing.DocumentSlicer;
import com.heptafox.niah.sizing.TokenEstimator;
import com.heptafox.niah.source.HaystackSource;

/** Orchestrates slice → inject → render → manifest for one catalog item. */
@Component
public class ArtifactBuilder {

    static final String TITLE = "Pride and Prejudice";
    private static final String OFFSET_NOTE =
            "charOffset and depthPercent are measured within the document body text (paragraphs "
            + "joined by blank lines), excluding format-specific chrome such as the title heading "
            + "or PDF pagination.";

    private final HaystackSource haystack;
    private final DocumentSlicer slicer;
    private final NeedleInjector injector;
    private final RendererRegistry renderers;
    private final ManifestWriter manifestWriter;
    private final TokenEstimator tokenEstimator;

    public ArtifactBuilder(HaystackSource haystack, DocumentSlicer slicer, NeedleInjector injector,
            RendererRegistry renderers, ManifestWriter manifestWriter, TokenEstimator tokenEstimator) {
        this.haystack = haystack;
        this.slicer = slicer;
        this.injector = injector;
        this.renderers = renderers;
        this.manifestWriter = manifestWriter;
        this.tokenEstimator = tokenEstimator;
    }

    public Artifact build(CatalogItem item, NeedleMode mode) {
        String sliced = slicer.slice(haystack.document(), item.preset());
        InjectionResult result = injector.inject(sliced, mode);

        byte[] document = renderers.get(item.format()).render(TITLE, result.paragraphs());

        String body = String.join("\n\n", result.paragraphs());
        Manifest manifest = new Manifest(
                item.entry().id(),
                TITLE,
                item.format().extension(),
                item.preset().sizingDescriptor(),
                mode.slug(),
                tokenEstimator.estimate(body),
                item.entry().approxPages(),
                tokenEstimator.note(),
                OFFSET_NOTE,
                toRecords(result.injected()));
        byte[] manifestJson = manifestWriter.toJson(manifest);

        String baseFilename = "pride-and-prejudice-niah-" + item.preset().id() + "-" + mode.slug();
        return new Artifact(baseFilename, item.format(), document, manifestJson);
    }

    private List<Manifest.NeedleRecord> toRecords(List<InjectedNeedle> injected) {
        return injected.stream()
                .map(n -> new Manifest.NeedleRecord(
                        n.needle().id(),
                        n.needle().text(),
                        n.needle().question(),
                        n.needle().groundTruthAnswer(),
                        n.segmentIndex(),
                        n.depthPercent(),
                        n.charOffset()))
                .toList();
    }
}
