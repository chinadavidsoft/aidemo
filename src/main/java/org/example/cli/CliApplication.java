package org.example.cli;

import java.io.IOException;
import java.net.http.HttpClient;

import org.example.client.DeepSeekChatClient;
import org.example.client.StreamingChatClient;
import org.example.config.ApiKeyProvider;
import org.example.config.ChatConfig;
import org.example.service.ChatSession;

public final class CliApplication {
    private final ChatConfig config;

    public CliApplication() {
        this(ChatConfig.defaultConfig());
    }

    CliApplication(ChatConfig config) {
        this.config = config;
    }

    public int run(String[] args) throws IOException {
        if (!CliArgs.isChatCommand(args)) {
            System.err.println(CliArgs.usage());
            return 1;
        }

        String apiKey = ApiKeyProvider.read(config.apiKeyFile());
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Missing API key file: " + config.apiKeyFile().toAbsolutePath());
            System.err.println("Create it with your key on a single line.");
            return 1;
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(config.connectTimeout())
                .build();
        StreamingChatClient chatClient = new DeepSeekChatClient(httpClient, config);

        ConsoleChatRunner runner = new ConsoleChatRunner(
                new ChatSession(),
                chatClient,
                System.in,
                System.out,
                System.err);
        runner.run(apiKey);
        return 0;
    }
}
