package org.example.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.example.client.StreamingChatClient;
import org.example.model.PromptMode;
import org.example.service.ChatSession;

public final class ConsoleChatRunner {
    private static final String COMMAND_CLEAR = "/clear";
    private static final String COMMAND_EXIT = "/exit";
    private static final String COMMAND_MODE = "/mode";
    private static final String MODE_ROLE = "role";
    private static final String MODE_NORMAL = "normal";
    private static final String MODE_USAGE = "Usage: /mode role <身份文本> | /mode normal";
    private static final String MODE_SWITCH_CLEAR_NOTICE = "[context cleared due to mode change]";

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
            if (trimmed.startsWith(COMMAND_MODE)) {
                handleModeCommand(trimmed);
                continue;
            }

            out.print("assistant> ");
            out.flush();
            try {
                String assistant = chatClient.streamReply(apiKey, session.snapshot(), session.promptMode(), trimmed, token -> {
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

    private void handleModeCommand(String commandLine) {
        if ((COMMAND_MODE + " " + MODE_NORMAL).equals(commandLine)) {
            applyPromptMode(PromptMode.normal());
            return;
        }

        String rolePrefix = COMMAND_MODE + " " + MODE_ROLE;
        if (commandLine.equals(rolePrefix) || commandLine.startsWith(rolePrefix + " ")) {
            String identity = commandLine.length() <= rolePrefix.length()
                    ? ""
                    : commandLine.substring(rolePrefix.length()).trim();
            if (identity.isEmpty()) {
                err.println(MODE_USAGE);
                return;
            }
            applyPromptMode(PromptMode.role(identity));
            return;
        }

        err.println(MODE_USAGE);
    }

    private void applyPromptMode(PromptMode newMode) {
        PromptMode oldMode = session.promptMode();
        if (!newMode.equals(oldMode)) {
            session.clear();
            out.println(MODE_SWITCH_CLEAR_NOTICE);
        }
        session.setPromptMode(newMode);
        out.println("[mode=" + session.promptMode().display() + "]");
    }
}
