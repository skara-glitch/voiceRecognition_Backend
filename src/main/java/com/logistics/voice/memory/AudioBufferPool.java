package com.logistics.voice.memory;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AudioBufferPool {

    private static final int MAX_SAMPLES = 80_000;
    private static final int MAX_POOL_SIZE = 128;

    private final ConcurrentLinkedQueue<DirectAudioBuffer> pool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger createdCount = new AtomicInteger(0);

    public DirectAudioBuffer acquire() {
        DirectAudioBuffer buffer = pool.poll();
        if (buffer != null) {
            return buffer;
        }

        if (createdCount.get() < MAX_POOL_SIZE) {
            createdCount.incrementAndGet();
            return new DirectAudioBuffer(MAX_SAMPLES, this);
        }

        return new DirectAudioBuffer(MAX_SAMPLES, null);
    }

    public void release(DirectAudioBuffer buffer) {
        pool.offer(buffer);
    }
}