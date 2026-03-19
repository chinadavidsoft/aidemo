package org.example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.example.model.ChatMessage;
import org.example.model.OutputFormat;
import org.example.model.PromptMode;

public final class ChatSession {
    private final List<ChatMessage> history = new ArrayList<>();
    private PromptMode promptMode = PromptMode.normal();
    private OutputFormat outputFormat = OutputFormat.normal();

    public List<ChatMessage> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    public void addRound(String userPrompt, String assistantReply) {
        history.add(new ChatMessage("user", userPrompt));
        history.add(new ChatMessage("assistant", assistantReply));
    }

    public PromptMode promptMode() {
        return promptMode;
    }

    public void setPromptMode(PromptMode promptMode) {
        this.promptMode = promptMode;
    }

    public OutputFormat outputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void clear() {
        history.clear();
    }
}
