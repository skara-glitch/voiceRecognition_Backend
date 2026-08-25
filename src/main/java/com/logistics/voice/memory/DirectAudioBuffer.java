package com.logistics.voice.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class DirectAudioBuffer implements AutoCloseable {

    private final FloatBuffer directFloatBuffer;
    private final int maxSampleCapacity;
    private final AudioBufferPool pool;

    public DirectAudioBuffer(int maxSampleCapacity, AudioBufferPool pool) {
        this.maxSampleCapacity = maxSampleCapacity;
        this.pool = pool;

        // Allocate only the FloatBuffer (1 float = 4 bytes)
        this.directFloatBuffer = ByteBuffer.allocateDirect(maxSampleCapacity * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    public FloatBuffer decodePcm16Direct(ByteBuffer incomingPayload) {
        directFloatBuffer.clear();
        
        int bytesToRead = Math.min(incomingPayload.remaining(), maxSampleCapacity * 2);
        int sampleCount = bytesToRead / 2;

        for (int i = 0; i < sampleCount; i++) {
            short sample = incomingPayload.getShort();
            directFloatBuffer.put(sample / 32768.0f);
        }

        directFloatBuffer.flip();
        return directFloatBuffer;
    }

    public FloatBuffer getDirectFloatBuffer() {
        return directFloatBuffer;
    }

    @Override
    public void close() {
        if (pool != null) {
            pool.release(this);
        }
    }
}