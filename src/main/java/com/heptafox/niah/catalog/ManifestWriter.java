package com.heptafox.niah.catalog;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Serializes a {@link Manifest} to pretty-printed JSON using Jackson 3. */
@Component
public class ManifestWriter {

    private final JsonMapper mapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public byte[] toJson(Manifest manifest) {
        return mapper.writeValueAsString(manifest).getBytes(StandardCharsets.UTF_8);
    }
}
