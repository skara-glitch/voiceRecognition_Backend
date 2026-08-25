package com.logistics.voice.controller;

import com.logistics.voice.dto.AnalysisResponse.Prediction;
import com.logistics.voice.service.VoiceInferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AudioAnalysisController.class)
public class AudioAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // This creates a fake version of your service so we don't trigger FFmpeg or ONNX during unit tests
    @MockBean
    private VoiceInferenceService inferenceService;

    @Test
    public void testAnalyzeAudioEndpoint() throws Exception {
        
    	when(inferenceService.standardizeAudioFormat((org.springframework.web.multipart.MultipartFile) any())).thenReturn(new float[]{0.1f, 0.2f, 0.3f});
    	when(inferenceService.assessQuality((float[]) any())).thenReturn("good");
    	when(inferenceService.predict((float[]) any())).thenReturn(new Prediction[] {
            new Prediction("male", 0.95),
            new Prediction("31-45", 0.82),
            new Prediction("en-US", 0.88)
        });

        // 3. Create a fake file to upload
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.wav", "audio/wav", "dummy audio bytes".getBytes()
        );

        // 4. Fire the test request and expect a 200 OK!
        mockMvc.perform(multipart("/analyze")
                .file(file)
                .param("contact_id", "driver-001"))
                .andExpect(status().isOk());
    }
}