package org.example.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.example.client.StreamingChatClient;
import org.example.client.TokenSink;
import org.example.model.ChatMessage;
import org.example.model.OutputFormat;
import org.example.model.PromptMode;
import org.example.service.ChatSession;

import junit.framework.TestCase;

public class ConsoleChatRunnerTest extends TestCase {
    public void testModeCommandPersistsAcrossRoundsAndClearDoesNotResetMode() throws IOException {
        String input = String.join("\n",
                "/mode role 资深架构师",
                "first question",
                "/clear",
                "second question",
                "/mode normal",
                "/exit",
                "");

        RecordingClient client = new RecordingClient();
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        ConsoleChatRunner runner = new ConsoleChatRunner(
                new ChatSession(),
                client,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        runner.run("dummy");

        assertEquals(2, client.calls);
        assertEquals("role", client.modes.get(0).type());
        assertEquals("资深架构师", client.modes.get(0).identity());
        assertEquals("role", client.modes.get(1).type());
        assertEquals(0, client.historySizes.get(0).intValue());
        assertEquals(0, client.historySizes.get(1).intValue());

        String outText = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(outText.contains("[mode=role: 资深架构师]"));
        assertTrue(outText.contains("[context cleared]"));
        assertTrue(outText.contains("[context cleared due to mode change]"));
        assertTrue(outText.contains("[mode=normal]"));
        assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    public void testSwitchingRoleMidConversationClearsHistoryAndUsesNewRole() throws IOException {
        String input = String.join("\n",
                "/mode role 厨师",
                "你是谁",
                "/mode role java专家",
                "你是谁",
                "/exit",
                "");

        RecordingClient client = new RecordingClient();
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        ConsoleChatRunner runner = new ConsoleChatRunner(
                new ChatSession(),
                client,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        runner.run("dummy");

        assertEquals(2, client.calls);
        assertEquals("role", client.modes.get(0).type());
        assertEquals("厨师", client.modes.get(0).identity());
        assertEquals("role", client.modes.get(1).type());
        assertEquals("java专家", client.modes.get(1).identity());
        assertEquals(0, client.historySizes.get(0).intValue());
        assertEquals(0, client.historySizes.get(1).intValue());

        String outText = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(outText.contains("[mode=role: 厨师]"));
        assertTrue(outText.contains("[mode=role: java专家]"));
        assertTrue(outText.contains("[context cleared due to mode change]"));
        assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    public void testModeCommandInvalidInputShowsUsageAndDoesNotCallClient() throws IOException {
        String input = String.join("\n",
                "/mode role",
                "/exit",
                "");

        RecordingClient client = new RecordingClient();
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        ConsoleChatRunner runner = new ConsoleChatRunner(
                new ChatSession(),
                client,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        runner.run("dummy");

        assertEquals(0, client.calls);
        assertTrue(errBuffer.toString(StandardCharsets.UTF_8).contains("Usage: /mode role <身份文本> | /mode normal"));
    }

    public void testFormatCommandPersistsAcrossRoundsAndClearDoesNotResetFormat() throws IOException {
        String input = String.join("\n",
                "/format json",
                "first question",
                "/clear",
                "second question",
                "/format normal",
                "/exit",
                "");

        RecordingClient client = new RecordingClient();
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        ConsoleChatRunner runner = new ConsoleChatRunner(
                new ChatSession(),
                client,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        runner.run("dummy");

        assertEquals(2, client.calls);
        assertEquals("json", client.formats.get(0).type());
        assertEquals("json", client.formats.get(1).type());
        assertEquals(0, client.historySizes.get(0).intValue());
        assertEquals(0, client.historySizes.get(1).intValue());

        String outText = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(outText.contains("[format=json]"));
        assertTrue(outText.contains("[context cleared]"));
        assertTrue(outText.contains("[format=normal]"));
        assertFalse(outText.contains("[context cleared due to mode change]"));
        assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    public void testModeAndFormatCanCoexist() throws IOException {
        String input = String.join("\n",
                "/mode role 资深架构师",
                "/format md",
                "first question",
                "/exit",
                "");

        RecordingClient client = new RecordingClient();
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        ConsoleChatRunner runner = new ConsoleChatRunner(
                new ChatSession(),
                client,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        runner.run("dummy");

        assertEquals(1, client.calls);
        assertEquals("role", client.modes.get(0).type());
        assertEquals("资深架构师", client.modes.get(0).identity());
        assertEquals("md", client.formats.get(0).type());
        assertEquals("", errBuffer.toString(StandardCharsets.UTF_8));
    }

    public void testFormatCommandInvalidInputShowsUsageAndDoesNotCallClient() throws IOException {
        String input = String.join("\n",
                "/format yaml",
                "/exit",
                "");

        RecordingClient client = new RecordingClient();
        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();

        ConsoleChatRunner runner = new ConsoleChatRunner(
                new ChatSession(),
                client,
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(outBuffer, true, StandardCharsets.UTF_8),
                new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
        runner.run("dummy");

        assertEquals(0, client.calls);
        assertTrue(errBuffer.toString(StandardCharsets.UTF_8).contains("Usage: /format json | /format md | /format normal"));
    }

    private static final class RecordingClient implements StreamingChatClient {
        int calls = 0;
        final List<PromptMode> modes = new ArrayList<>();
        final List<OutputFormat> formats = new ArrayList<>();
        final List<Integer> historySizes = new ArrayList<>();

        @Override
        public String streamReply(
                String apiKey,
                List<ChatMessage> history,
                PromptMode promptMode,
                OutputFormat outputFormat,
                String userPrompt,
                TokenSink tokenSink) {
            calls += 1;
            modes.add(promptMode);
            formats.add(outputFormat);
            historySizes.add(history.size());
            tokenSink.onToken("ok");
            return "ok";
        }
    }
}
