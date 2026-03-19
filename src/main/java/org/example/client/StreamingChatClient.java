package org.example.client;

import java.io.IOException;
import java.util.List;

import org.example.model.ChatMessage;
import org.example.model.OutputFormat;
import org.example.model.PromptMode;

public interface StreamingChatClient {
    String streamReply(
            String apiKey,
            List<ChatMessage> history,
            PromptMode promptMode,
            OutputFormat outputFormat,
            String userPrompt,
            TokenSink tokenSink)
            throws IOException, InterruptedException;
}
