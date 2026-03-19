package org.example.client;

@FunctionalInterface
public interface TokenSink {
    void onToken(String token);
}
