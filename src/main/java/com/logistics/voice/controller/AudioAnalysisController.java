package com.logistics.voice.controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.logistics.voice.dto.AnalysisResponse;
import com.logistics.voice.dto.AnalysisResponse.Prediction;
import com.logistics.voice.service.VoiceInferenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class AudioAnalysisController {

    // 1. Initialize the Logger for this specific class
    private static final Logger log = LoggerFactory.getLogger(AudioAnalysisController.class);
    
    private final VoiceInferenceService inferenceService;

    public AudioAnalysisController(VoiceInferenceService inferenceService) {
        this.inferenceService = inferenceService;
    }

    @PostMapping(value = "/analyze", consumes = {"multipart/form-data"})
    public ResponseEntity<AnalysisResponse> analyzeAudio(
            @RequestParam(value = "contact_id", required = false) String contactId,
            @RequestParam("file") MultipartFile file
    ) {
        long startTime = System.currentTimeMillis();
        String activeContactId = (contactId != null && !contactId.isBlank()) ? contactId : UUID.randomUUID().toString();

        if (file.isEmpty()) {
            log.warn("Rejected request: Received empty file for contact_id [{}]", activeContactId);
            return ResponseEntity.badRequest().build();
        }

        // 2. Log the incoming request details
        log.info("Starting audio analysis for contact_id [{}] | File size: {} bytes", activeContactId, file.getSize());

        try {
        	float[] audioSamples = inferenceService.standardizeAudioFormat(file);

            String quality = inferenceService.assessQuality(audioSamples);
            Prediction gender, age, lang;

            if ("insufficient".equals(quality)) {
                gender = new Prediction("unknown", 0.0);
                age = new Prediction("unknown", 0.0);
                lang = new Prediction("unknown", 0.0);
            } else {
                Prediction[] preds = inferenceService.predict(audioSamples);
                gender = preds[0];
                age = preds[1];
                lang = preds[2];
            }

            long elapsedMs = System.currentTimeMillis() - startTime;

            // 3. Log the successful processing time and key metrics
            log.info("Completed audio analysis for contact_id [{}] in {} ms | Quality: {}", 
                     activeContactId, elapsedMs, quality);

            AnalysisResponse response = new AnalysisResponse(
                    activeContactId, gender, age, elapsedMs, quality, lang
            );

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 4. Log any exceptions that occur during processing
            log.error("Failed to process audio for contact_id [{}]: {}", activeContactId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}