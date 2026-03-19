package org.example.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ApiKeyProvider {
    private ApiKeyProvider() {
    }

    public static String read(Path keyFile) throws IOException {
        if (!Files.exists(keyFile)) {
            return null;
        }
        String raw = Files.readString(keyFile, StandardCharsets.UTF_8);
        return raw == null ? null : raw.trim();
    }
}
