package org.example.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.example.client.StreamingChatClient;
import org.example.service.ChatSession;

public final class ConsoleChatRunner {
    private static final String COMMAND_CLEAR = "/clear";
    private static final String COMMAND_EXIT = "/exit";

    private final ChatSession session;
    private final StreamingChatClient chatClient;
    private final BufferedReader input;
    private final PrintStream out;
    private final PrintStream err;

    public ConsoleChatRunner(
            ChatSession session,
            StreamingChatClient chatClient,
            InputStream in,
            PrintStream out,
            PrintStream err) {
        this.session = session;
        this.chatClient = chatClient;
        this.input = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
        this.err = err;
    }

    public void run(String apiKey) throws IOException {
        while (true) {
            out.print("you> ");
            out.flush();

            String inputLine = input.readLine();
            if (inputLine == null) {
                out.println();
                return;
            }

            String trimmed = inputLine.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (COMMAND_EXIT.equals(trimmed)) {
                return;
            }
            if (COMMAND_CLEAR.equals(trimmed)) {
                session.clear();
                out.println("[context cleared]");
                continue;
            }

            out.print("assistant> ");
            out.flush();
            try {
                String assistant = chatClient.streamReply(apiKey, session.snapshot(), trimmed, token -> {
                    out.print(token);
                    out.flush();
                });
                out.println();
                session.addRound(trimmed, assistant);
            } catch (InterruptedException ex) {
                out.println();
                err.println("Request interrupted: " + ex.getMessage());
                Thread.currentThread().interrupt();
                return;
            } catch (IOException ex) {
                out.println();
                err.println("Request failed: " + ex.getMessage());
            }
        }
    }
}
