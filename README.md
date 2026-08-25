1. Core Design Decisions
Embedded Inference (Java + ONNX): Rather than spinning up a separate Python backend (like FastAPI) just to run the AI, the model is embedded directly into the Spring Boot service. This eliminates network latency between microservices, removes the need to maintain two separate servers, and simplifies the Docker deployment.

Defensive Audio Processing (FFmpeg/JAVE2): Users will inevitably upload .mp3, .m4a, or corrupt files. Instead of trusting the client, the backend uses an embedded FFmpeg wrapper to automatically intercept, clean, and convert every file into the strict 16kHz Mono PCM WAV format the AI requires before prediction.

Dual-Protocol Communication: The system supports standard REST endpoints for simple file uploads, while actively maintaining a WebSocket configuration (StreamingVoiceWebSocketHandler). This future-proofs the architecture for real-time, chunked audio streaming directly from a user's microphone.


2. Model Choice Rationale (Wav2Vec2 via ONNX)
Why Wav2Vec2? Traditional audio models require translating sound into images (spectrograms) before processing. Wav2Vec2 is a transformer-based architecture that reads raw audio waveforms directly. It is exceptionally accurate at extracting human speech features, making it the industry standard for determining age, gender, and language.

Why the ONNX Format? Native PyTorch or TensorFlow models are heavy and difficult to run inside a Java Virtual Machine (JVM). Exporting the model to ONNX (Open Neural Network Exchange) allows the backend to use a highly optimized C++ runtime engine, achieving blazing-fast inference speeds inside Java.


3. Known Limitations & Bottlenecks
High Memory Footprint: Loading a 300MB machine learning model into RAM means the server idles at a high memory baseline. This makes it impossible to host on standard 512MB free-tier cloud environments without strict memory allocation limits (like -Xmx).

Thread Blocking: Currently, the heavy ONNX inference runs synchronously. If 50 users upload audio files at the exact same second, the server will try to process all 50 simultaneously, potentially maxing out the CPU and crashing.

Environmental Sensitivity: Like all audio AI, the model is highly sensitive to background noise and microphone quality. Heavy static or overlapping voices will drastically lower the confidence percentages.


*******************************************************************************************************
Approach and Model Choice
I embedded the Wav2Vec2 machine learning model directly into Spring Boot via ONNX to eliminate the latency of maintaining a separate Python microservice. Wav2Vec2 is ideal because it processes raw waveforms directly, yielding high predictive accuracy for extracting speech features like age and gender. ONNX natively allows Java to leverage optimized C++ binaries for rapid inference. Embedded FFmpeg ensures that all incoming uploads are aggressively standardized before hitting the AI.

Future Improvements
With more time, I would shift to an asynchronous worker model. Instead of making users wait, the API would return a job ID immediately while the frontend listens for results via WebSockets. I would also fine-tune the model against noisy environments and add a PostgreSQL database to cache identical audio hashes.

Scaling to 1,000 Concurrent Calls
To handle 1,000 concurrent calls efficiently, the current single-node lock would fail under pressure. I would decouple the architecture completely. The Spring Boot backend would act solely as an API gateway, saving audio to S3 buckets
