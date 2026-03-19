package org.example.config;

import java.nio.file.Path;
import java.time.Duration;

public final class ChatConfig {
    private final String apiBase;
    private final String chatPath;
    private final String model;
    private final Path apiKeyFile;
    private final Duration connectTimeout;
    private final Duration requestTimeout;

    public ChatConfig(
            String apiBase,
            String chatPath,
            String model,
            Path apiKeyFile,
            Duration connectTimeout,
            Duration requestTimeout) {
        this.apiBase = apiBase;
        this.chatPath = chatPath;
        this.model = model;
        this.apiKeyFile = apiKeyFile;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    public static ChatConfig defaultConfig() {
        return new ChatConfig(
                "https://api.deepseek.com/v1",
                "/chat/completions",
                "deepseek-chat",
                Path.of("deepseek.key"),
                Duration.ofSeconds(20),
                Duration.ofSeconds(60));
    }

    public String apiBase() {
        return apiBase;
    }

    public String chatPath() {
        return chatPath;
    }

    public String model() {
        return model;
    }

    public Path apiKeyFile() {
        return apiKeyFile;
    }

    public Duration connectTimeout() {
        return connectTimeout;
    }

    public Duration requestTimeout() {
        return requestTimeout;
    }
}
