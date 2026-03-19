package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.example.config.ChatConfig;
import org.example.model.ChatMessage;
import org.example.model.PromptMode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class DeepSeekChatClient implements StreamingChatClient {
    private final HttpClient httpClient;
    private final ChatConfig config;
    private final ObjectMapper mapper;

    public DeepSeekChatClient(HttpClient httpClient, ChatConfig config) {
        this(httpClient, config, new ObjectMapper());
    }

    DeepSeekChatClient(HttpClient httpClient, ChatConfig config, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.config = config;
        this.mapper = mapper;
    }

    @Override
    public String streamReply(
            String apiKey,
            List<ChatMessage> history,
            PromptMode promptMode,
            String userPrompt,
            TokenSink tokenSink)
            throws IOException, InterruptedException {
        String payload = buildPayload(mapper, config.model(), history, promptMode, userPrompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.apiBase() + config.chatPath()))
                .timeout(config.requestTimeout())
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int statusCode = response.statusCode();
        if (statusCode / 100 != 2) {
            String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            String message = body.isBlank() ? "HTTP " + statusCode : "HTTP " + statusCode + ": " + body;
            throw new IOException(message);
        }

        StringBuilder assistant = new StringBuilder();
        try (BufferedReader sseReader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = sseReader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if (data.isEmpty()) {
                    continue;
                }
                if ("[DONE]".equals(data)) {
                    break;
                }
                String token = extractDeltaContent(mapper, data);
                if (token == null || token.isEmpty()) {
                    continue;
                }
                tokenSink.onToken(token);
                assistant.append(token);
            }
        }
        return assistant.toString();
    }

    static String buildPayload(
            ObjectMapper mapper,
            String model,
            List<ChatMessage> history,
            PromptMode promptMode,
            String userPrompt)
            throws IOException {
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);

        ArrayNode messages = root.putArray("messages");
        String systemPrompt = promptMode == null ? null : promptMode.systemPrompt();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content", systemPrompt);
        }
        for (ChatMessage message : history) {
            ObjectNode node = messages.addObject();
            node.put("role", message.role());
            node.put("content", message.content());
        }
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", userPrompt);

        root.put("stream", true);
        return mapper.writeValueAsString(root);
    }

    static String extractDeltaContent(ObjectMapper mapper, String json) throws IOException {
        JsonNode root = mapper.readTree(json);

        JsonNode error = root.path("error");
        if (!error.isMissingNode()) {
            JsonNode message = error.path("message");
            if (message.isTextual()) {
                throw new IOException(message.asText());
            }
            throw new IOException(error.toString());
        }

        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }

        JsonNode delta = choices.get(0).path("delta");
        if (delta.isMissingNode()) {
            return null;
        }
        JsonNode content = delta.path("content");
        if (content.isTextual()) {
            return content.asText();
        }
        if (!content.isArray()) {
            return null;
        }

        StringBuilder text = new StringBuilder();
        for (JsonNode part : content) {
            JsonNode partText = part.path("text");
            if (partText.isTextual()) {
                text.append(partText.asText());
            }
        }
        return text.length() == 0 ? null : text.toString();
    }
}
