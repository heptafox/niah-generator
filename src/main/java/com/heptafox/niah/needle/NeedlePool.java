package com.heptafox.niah.needle;

import java.util.List;

import org.springframework.stereotype.Component;

import com.heptafox.niah.config.NiahProperties;

/** The configured set of counterfactual needles. */
@Component
public class NeedlePool {

    private final List<Needle> needles;

    public NeedlePool(NiahProperties properties) {
        this.needles = properties.needles().stream()
                .map(spec -> new Needle(spec.id(), spec.text(), spec.question(), spec.groundTruthAnswer()))
                .toList();
        if (this.needles.isEmpty()) {
            throw new IllegalStateException("No niah.needles configured");
        }
    }

    public List<Needle> needles() {
        return needles;
    }

    /** The needle assigned to segment {@code index}, cycling if there are fewer needles than segments. */
    public Needle forSegment(int index) {
        return needles.get(index % needles.size());
    }
}
