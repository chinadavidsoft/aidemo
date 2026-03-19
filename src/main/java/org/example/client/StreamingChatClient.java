package org.example.client;

import java.io.IOException;
import java.util.List;

import org.example.model.ChatMessage;

public interface StreamingChatClient {
    String streamReply(String apiKey, List<ChatMessage> history, String userPrompt, TokenSink tokenSink)
            throws IOException, InterruptedException;
}
