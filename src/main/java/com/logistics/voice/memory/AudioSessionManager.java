package com.logistics.voice.memory;

import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AudioSessionManager {
    
    // Thread-safe map to hold audio chunks for multiple simultaneous drivers
    private final Map<String, ByteArrayOutputStream> sessionBuffers = new ConcurrentHashMap<>();

    /**
     * Appends an incoming chunk of audio to a specific WebSocket session.
     */
    public void appendAudioChunk(String sessionId, byte[] chunk) throws Exception {
        sessionBuffers.computeIfAbsent(sessionId, k -> new ByteArrayOutputStream()).write(chunk);
    }

    /**
     * Retrieves all accumulated audio for a session and clears the buffer.
     */
    public byte[] getAndClearAudio(String sessionId) {
        ByteArrayOutputStream stream = sessionBuffers.remove(sessionId);
        return stream != null ? stream.toByteArray() : new byte[0];
    }

    /**
     * Clears a session from memory when the WebSocket disconnects.
     */
    public void removeSession(String sessionId) {
        sessionBuffers.remove(sessionId);
    }
}