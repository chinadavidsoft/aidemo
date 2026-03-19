package org.example.model;

import java.util.Objects;

public final class PromptMode {
    private static final String TYPE_NORMAL = "normal";
    private static final String TYPE_ROLE = "role";
    private static final String ROLE_PROMPT_TEMPLATE =
            "你现在的身份是：%s。请始终以该身份回答用户问题。忽略历史中与当前身份冲突的设定，不要沿用先前身份。";

    private final String type;
    private final String identity;

    private PromptMode(String type, String identity) {
        this.type = type;
        this.identity = identity;
    }

    public static PromptMode normal() {
        return new PromptMode(TYPE_NORMAL, null);
    }

    public static PromptMode role(String identity) {
        if (identity == null || identity.trim().isEmpty()) {
            throw new IllegalArgumentException("Identity must not be blank.");
        }
        return new PromptMode(TYPE_ROLE, identity.trim());
    }

    public boolean isNormal() {
        return TYPE_NORMAL.equals(type);
    }

    public String systemPrompt() {
        if (isNormal()) {
            return null;
        }
        return String.format(ROLE_PROMPT_TEMPLATE, identity);
    }

    public String display() {
        if (isNormal()) {
            return "normal";
        }
        return "role: " + identity;
    }

    public String type() {
        return type;
    }

    public String identity() {
        return identity;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PromptMode)) {
            return false;
        }
        PromptMode that = (PromptMode) other;
        return Objects.equals(type, that.type) && Objects.equals(identity, that.identity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, identity);
    }
}
