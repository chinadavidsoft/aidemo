package org.example.cli;

import junit.framework.TestCase;

public class CliArgsTest extends TestCase {
    public void testIsChatCommand() {
        assertTrue(CliArgs.isChatCommand(new String[] { "chat" }));
        assertFalse(CliArgs.isChatCommand(new String[] {}));
        assertFalse(CliArgs.isChatCommand(new String[] { "ask" }));
        assertFalse(CliArgs.isChatCommand(new String[] { "chat", "extra" }));
    }

    public void testUsage() {
        assertEquals("Usage: aidemo chat", CliArgs.usage());
    }
}
