package org.example;

import org.example.cli.CliApplication;

public class App {
    public static void main(String[] args) throws Exception {
        int exitCode = new CliApplication().run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
