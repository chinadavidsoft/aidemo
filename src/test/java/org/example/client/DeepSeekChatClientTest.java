package org.example.client;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.example.model.ChatMessage;
import org.example.model.OutputFormat;
import org.example.model.PromptMode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class DeepSeekChatClientTest extends TestCase {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void testBuildPayloadNormalModeIncludesHistoryAndStreaming() throws IOException {
        List<ChatMessage> history = Collections.singletonList(new ChatMessage("assistant", "hello"));
        String payload = DeepSeekChatClient.buildPayload(
                MAPPER,
                "deepseek-chat",
                history,
                PromptMode.normal(),
                OutputFormat.normal(),
                "next question");

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

    public void testBuildPayloadRoleModePrependsSystemPrompt() throws IOException {
        List<ChatMessage> history = Collections.singletonList(new ChatMessage("assistant", "hello"));
        String payload = DeepSeekChatClient.buildPayload(
                MAPPER,
                "deepseek-chat",
                history,
                PromptMode.role("资深架构师"),
                OutputFormat.normal(),
                "next question");

        JsonNode root = MAPPER.readTree(payload);
        JsonNode messages = root.path("messages");
        assertEquals(3, messages.size());
        assertEquals("system", messages.get(0).path("role").asText());
        assertTrue(messages.get(0).path("content").asText().contains("资深架构师"));
        assertTrue(messages.get(0).path("content").asText().contains("不要沿用先前身份"));
        assertEquals("assistant", messages.get(1).path("role").asText());
        assertEquals("user", messages.get(2).path("role").asText());
    }

    public void testBuildPayloadJsonFormatAddsSystemPrompt() throws IOException {
        String payload = DeepSeekChatClient.buildPayload(
                MAPPER,
                "deepseek-chat",
                Collections.emptyList(),
                PromptMode.normal(),
                OutputFormat.json(),
                "next question");

        JsonNode root = MAPPER.readTree(payload);
        JsonNode messages = root.path("messages");
        assertEquals(2, messages.size());
        assertEquals("system", messages.get(0).path("role").asText());
        assertTrue(messages.get(0).path("content").asText().contains("请仅输出合法 JSON"));
        assertTrue(messages.get(0).path("content").asText().contains("忽略历史中与当前格式冲突的示例"));
        assertEquals("user", messages.get(1).path("role").asText());
    }

    public void testBuildPayloadRoleAndFormatCombineIntoSingleSystemPrompt() throws IOException {
        String payload = DeepSeekChatClient.buildPayload(
                MAPPER,
                "deepseek-chat",
                Collections.emptyList(),
                PromptMode.role("资深架构师"),
                OutputFormat.md(),
                "next question");

        JsonNode root = MAPPER.readTree(payload);
        JsonNode messages = root.path("messages");
        assertEquals(2, messages.size());
        assertEquals("system", messages.get(0).path("role").asText());
        String system = messages.get(0).path("content").asText();
        assertTrue(system.contains("资深架构师"));
        assertTrue(system.contains("当前输出格式固定为 Markdown"));
        assertTrue(system.contains("不要输出裸 JSON 对象"));
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
