package org.example.client;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.example.model.ChatMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class DeepSeekChatClientTest extends TestCase {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void testBuildPayloadIncludesHistoryAndStreaming() throws IOException {
        List<ChatMessage> history = Collections.singletonList(new ChatMessage("assistant", "hello"));
        String payload = DeepSeekChatClient.buildPayload(MAPPER, "deepseek-chat", history, "next question");

        JsonNode root = MAPPER.readTree(payload);
        assertEquals("deepseek-chat", root.path("model").asText());
        assertTrue(root.path("stream").asBoolean());

        JsonNode messages = root.path("messages");
        assertEquals(2, messages.size());
        assertEquals("assistant", messages.get(0).path("role").asText());
        assertEquals("hello", messages.get(0).path("content").asText());
        assertEquals("user", messages.get(1).path("role").asText());
        assertEquals("next question", messages.get(1).path("content").asText());
    }

    public void testExtractDeltaContentTextAndArray() throws IOException {
        String textChunk = "{\"choices\":[{\"delta\":{\"content\":\"Hi\"}}]}";
        assertEquals("Hi", DeepSeekChatClient.extractDeltaContent(MAPPER, textChunk));

        String arrayChunk = "{\"choices\":[{\"delta\":{\"content\":[{\"text\":\"A\"},{\"text\":\"B\"}]}}]}";
        assertEquals("AB", DeepSeekChatClient.extractDeltaContent(MAPPER, arrayChunk));
    }

    public void testExtractDeltaContentError() {
        String errorChunk = "{\"error\":{\"message\":\"bad request\"}}";
        try {
            DeepSeekChatClient.extractDeltaContent(MAPPER, errorChunk);
            fail("Expected IOException");
        } catch (IOException ex) {
            assertTrue(ex.getMessage().contains("bad request"));
        }
    }
}
