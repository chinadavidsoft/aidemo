package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    private static final String API_BASE = "https://api.deepseek.com/v1";
    private static final String CHAT_PATH = "/chat/completions";
    private static final String MODEL = "deepseek-chat";
    private static final Path API_KEY_FILE = Path.of("deepseek.key");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    // Prompt - this is the only field you should change
    private static final String PROMPT = "[Replace this text]";
    private static final Pattern GRADE_PATTERN = Pattern.compile("^(?=.*1)(?=.*2)(?=.*3).*$", Pattern.DOTALL);

    public static void main(String[] args) throws IOException, InterruptedException {
        String apiKey = readApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Missing API key file: " + API_KEY_FILE.toAbsolutePath());
            System.err.println("Create it with your key on a single line.");
            System.exit(1);
        }

        String payload = buildPayload(PROMPT);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + CHAT_PATH))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println("HTTP " + response.statusCode());
        String assistant = extractAssistantMessage(response.body());
        if (assistant == null) {
            System.out.println("No assistant message found. Raw response:");
            System.out.println(response.body());
            return;
        }

        System.out.println(assistant);
        System.out.println("\n--------------------------- GRADING ---------------------------");
        System.out.println("This exercise has been correctly solved: " + gradeExercise(assistant));
    }

    private static String buildPayload(String userPrompt) {
        String escapedPrompt = jsonEscape(userPrompt);
        return "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"messages\":["
                + "{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}"
                + "],"
                + "\"stream\":false"
                + "}";
    }

    private static String jsonEscape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    private static String extractAssistantMessage(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.size() == 0) {
            return null;
        }
        JsonNode message = choices.get(0).path("message");
        if (message.isMissingNode()) {
            return null;
        }
        JsonNode content = message.path("content");
        return content.isMissingNode() ? null : content.asText();
    }

    private static String readApiKey() throws IOException {
        if (!Files.exists(API_KEY_FILE)) {
            return null;
        }
        String raw = Files.readString(API_KEY_FILE, StandardCharsets.UTF_8);
        return raw == null ? null : raw.trim();
    }

    private static boolean gradeExercise(String text) {
        return GRADE_PATTERN.matcher(text).matches();
    }
}
