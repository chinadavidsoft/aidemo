package org.example.cli;

public final class CliArgs {
    private static final String COMMAND_CHAT = "chat";

    private CliArgs() {
    }

    public static boolean isChatCommand(String[] args) {
        return args != null && args.length == 1 && COMMAND_CHAT.equals(args[0]);
    }

    public static String usage() {
        return "Usage: aidemo chat";
    }
}
