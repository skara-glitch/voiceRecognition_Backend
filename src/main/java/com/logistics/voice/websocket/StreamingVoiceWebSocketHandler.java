package com.logistics.voice.websocket;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.voice.dto.AnalysisResponse;
import com.logistics.voice.dto.AnalysisResponse.Prediction;
import com.logistics.voice.memory.AudioBufferPool;
import com.logistics.voice.memory.DirectAudioBuffer;
import com.logistics.voice.service.VoiceInferenceService;

@Component
public class StreamingVoiceWebSocketHandler extends BinaryWebSocketHandler {

    private final VoiceInferenceService inferenceService;
    private final AudioBufferPool bufferPool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StreamingVoiceWebSocketHandler(VoiceInferenceService inferenceService, AudioBufferPool bufferPool) {
        this.inferenceService = inferenceService;
        this.bufferPool = bufferPool;
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        long startTime = System.currentTimeMillis();
        ByteBuffer incomingPayload = message.getPayload();

        try (DirectAudioBuffer audioBuffer = bufferPool.acquire()) {
            
            FloatBuffer floatBuffer = audioBuffer.decodePcm16Direct(incomingPayload);
            String quality = inferenceService.assessQualityDirect(floatBuffer);
            Prediction[] preds;
            
            if ("insufficient".equals(quality)) {
                preds = new Prediction[]{
                    new Prediction("unknown", 0.0),
                    new Prediction("unknown", 0.0),
                    new Prediction("unknown", 0.0)
                };
            } else {
                preds = inferenceService.predictDirect(floatBuffer);
            }

            long latency = System.currentTimeMillis() - startTime;

            AnalysisResponse response = new AnalysisResponse(
                UUID.randomUUID().toString(),
                preds[0],
                preds[1],
                latency,
                quality,
                preds[2]
            );

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
    }
}