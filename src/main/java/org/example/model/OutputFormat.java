package org.example.model;

import java.util.Objects;

public final class OutputFormat {
    private static final String TYPE_NORMAL = "normal";
    private static final String TYPE_MD = "md";
    private static final String TYPE_JSON = "json";

    private static final String MD_PROMPT =
            "当前输出格式固定为 Markdown。忽略历史中与当前格式冲突的示例。除非用户明确要求，否则不要输出裸 JSON 对象。";
    private static final String JSON_PROMPT =
            "当前输出格式固定为 JSON。忽略历史中与当前格式冲突的示例。请仅输出合法 JSON，不要附加解释、前后缀文本或代码围栏。";

    private final String type;

    private OutputFormat(String type) {
        this.type = type;
    }

    public static OutputFormat normal() {
        return new OutputFormat(TYPE_NORMAL);
    }

    public static OutputFormat md() {
        return new OutputFormat(TYPE_MD);
    }

    public static OutputFormat json() {
        return new OutputFormat(TYPE_JSON);
    }

    public static OutputFormat fromType(String type) {
        if (TYPE_MD.equals(type)) {
            return md();
        }
        if (TYPE_JSON.equals(type)) {
            return json();
        }
        if (TYPE_NORMAL.equals(type)) {
            return normal();
        }
        throw new IllegalArgumentException("Unsupported output format: " + type);
    }

    public boolean isNormal() {
        return TYPE_NORMAL.equals(type);
    }

    public String systemPrompt() {
        if (TYPE_MD.equals(type)) {
            return MD_PROMPT;
        }
        if (TYPE_JSON.equals(type)) {
            return JSON_PROMPT;
        }
        return null;
    }

    public String display() {
        return type;
    }

    public String type() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OutputFormat)) {
            return false;
        }
        OutputFormat that = (OutputFormat) other;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
