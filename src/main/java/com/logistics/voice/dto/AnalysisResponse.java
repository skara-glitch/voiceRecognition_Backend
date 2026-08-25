package com.logistics.voice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalysisResponse {

    @JsonProperty("contact_id")
    private String contactId;

    private Prediction gender;

    @JsonProperty("age_bracket")
    private Prediction ageBracket;

    @JsonProperty("processing_ms")
    private long processingMs;

    @JsonProperty("audio_quality")
    private String audioQuality;

    private Prediction language;

    // Constructor
    public AnalysisResponse(String contactId, Prediction gender, Prediction ageBracket, 
                            long processingMs, String audioQuality, Prediction language) {
        this.contactId = contactId;
        this.gender = gender;
        this.ageBracket = ageBracket;
        this.processingMs = processingMs;
        this.audioQuality = audioQuality;
        this.language = language;
    }

    // Getters (Required for Spring Boot to convert this object into JSON)
    public String getContactId() { return contactId; }
    public Prediction getGender() { return gender; }
    public Prediction getAgeBracket() { return ageBracket; }
    public long getProcessingMs() { return processingMs; }
    public String getAudioQuality() { return audioQuality; }
    public Prediction getLanguage() { return language; }

    // Inner class for the prediction blocks
    public static class Prediction {
        private String prediction;
        private double confidence;

        public Prediction(String prediction, double confidence) {
            this.prediction = prediction;
            this.confidence = confidence;
        }

        public String getPrediction() { return prediction; }
        public double getConfidence() { return confidence; }
    }
}