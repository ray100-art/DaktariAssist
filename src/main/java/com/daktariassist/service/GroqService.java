package com.daktariassist.service;

import com.daktariassist.model.DiagnosisRequest;
import com.daktariassist.model.DiagnosisResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // -------------------------------------------------------
    // MEDICAL SYSTEM PROMPT
    // This is the core intelligence of DaktariAssist.
    // -------------------------------------------------------
    private static final String SYSTEM_PROMPT = """
        You are DaktariAssist, an AI clinical decision support tool designed for
        doctors and clinical officers in Kenya. Your role is to provide a respectful,
        evidence-based second opinion on a proposed diagnosis.

        You are NOT replacing the doctor. You are a silent, supportive colleague
        who reviews the clinical picture and asks "have you considered...?"

        TONE: Always respectful, humble, and collegial. Never arrogant. Never say
        "the diagnosis is wrong." Say "the clinical picture may also be consistent with..."

        KENYAN CONTEXT: You are aware of common diseases in Kenya including malaria,
        typhoid, TB, HIV, brucellosis, meningitis, and conditions common in
        sub-Saharan Africa. Always consider the local disease burden.

        YOUR TASK: Analyse the patient data and return a JSON object ONLY.
        No preamble. No explanation outside the JSON. No markdown. Just raw JSON.

        REQUIRED JSON FORMAT:
        {
          "consistency": "green" or "amber" or "red",
          "consistencyTitle": "short title e.g. Diagnosis is consistent",
          "consistencyDescription": "1-2 sentence explanation",
          "redFlags": ["flag 1", "flag 2"],
          "differentials": [
            {"name": "Diagnosis name", "reason": "Why to consider this"},
            {"name": "Diagnosis name", "reason": "Why to consider this"}
          ],
          "recommendedTests": ["Test 1", "Test 2", "Test 3"],
          "followupQuestion": "One important question the doctor should ask the patient"
        }

        CONSISTENCY RULES:
        - green: proposed diagnosis fits the clinical picture well
        - amber: diagnosis is possible but other conditions should be ruled out first
        - red: the clinical picture is inconsistent with the proposed diagnosis

        Always provide 2-3 differentials and 3-5 recommended tests.
        Always identify at least 1-2 red flags if any exist in the vitals or symptoms.
        """;

    // -------------------------------------------------------
    // Main analysis method — called by AnalysisController
    // -------------------------------------------------------
    public DiagnosisResponse analyse(DiagnosisRequest request) throws Exception {

        // Build the clinical summary to send as the user message
        String userMessage = buildClinicalSummary(request);

        // Build Groq API request body (OpenAI-compatible format)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("temperature", 0.2);
        requestBody.put("max_tokens", 1024);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user",   "content", userMessage)
        ));

        // Set headers — Groq uses Bearer token auth
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Call Groq API
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class
        );

        // Parse Groq's response
        return parseGroqResponse(response.getBody());
    }

    // -------------------------------------------------------
    // Build a structured clinical summary to send to Groq
    // -------------------------------------------------------
    private String buildClinicalSummary(DiagnosisRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Age: ").append(req.getAge()).append(" years\n");
        sb.append("Sex: ").append(req.getSex()).append("\n");
        sb.append("Chief Complaint: ").append(req.getChiefComplaint()).append("\n");

        if (req.getDuration() != null && !req.getDuration().isEmpty()) {
            sb.append("Duration: ").append(req.getDuration()).append("\n");
        }

        if (req.getSymptoms() != null && !req.getSymptoms().isEmpty()) {
            sb.append("Symptoms: ").append(String.join(", ", req.getSymptoms())).append("\n");
        }

        sb.append("\nVITALS:\n");
        if (req.getTemperature() != null && !req.getTemperature().isEmpty())
            sb.append("Temperature: ").append(req.getTemperature()).append(" C\n");
        if (req.getBloodPressure() != null && !req.getBloodPressure().isEmpty())
            sb.append("Blood Pressure: ").append(req.getBloodPressure()).append(" mmHg\n");
        if (req.getHeartRate() != null && !req.getHeartRate().isEmpty())
            sb.append("Heart Rate: ").append(req.getHeartRate()).append(" bpm\n");
        if (req.getSpo2() != null && !req.getSpo2().isEmpty())
            sb.append("SpO2: ").append(req.getSpo2()).append("%\n");
        if (req.getRespiratoryRate() != null && !req.getRespiratoryRate().isEmpty())
            sb.append("Respiratory Rate: ").append(req.getRespiratoryRate()).append(" breaths/min\n");

        if (req.getMedicalHistory() != null && !req.getMedicalHistory().isEmpty()) {
            sb.append("\nRelevant History: ").append(String.join(", ", req.getMedicalHistory())).append("\n");
        }

        sb.append("\nDoctor's Proposed Diagnosis: ").append(req.getProposedDiagnosis()).append("\n");
        sb.append("\nReturn ONLY the JSON object. No extra text.");

        return sb.toString();
    }

    // -------------------------------------------------------
    // Parse Groq's response into DiagnosisResponse object
    // -------------------------------------------------------
    private DiagnosisResponse parseGroqResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // Extract text from Groq's OpenAI-compatible response structure
        String content = root
                .path("choices").get(0)
                .path("message")
                .path("content").asText();

        // Clean up any markdown formatting the model might add
        content = content.trim();
        if (content.startsWith("```json")) content = content.substring(7);
        if (content.startsWith("```"))     content = content.substring(3);
        if (content.endsWith("```"))       content = content.substring(0, content.length() - 3);
        content = content.trim();

        // Parse the JSON returned by the model
        JsonNode json = objectMapper.readTree(content);

        DiagnosisResponse response = new DiagnosisResponse();
        response.setConsistency(json.path("consistency").asText("amber"));
        response.setConsistencyTitle(json.path("consistencyTitle").asText());
        response.setConsistencyDescription(json.path("consistencyDescription").asText());

        // Red flags
        List<String> flags = new ArrayList<>();
        json.path("redFlags").forEach(f -> flags.add(f.asText()));
        response.setRedFlags(flags);

        // Differentials
        List<DiagnosisResponse.Differential> diffs = new ArrayList<>();
        json.path("differentials").forEach(d -> {
            DiagnosisResponse.Differential diff = new DiagnosisResponse.Differential();
            diff.setName(d.path("name").asText());
            diff.setReason(d.path("reason").asText());
            diffs.add(diff);
        });
        response.setDifferentials(diffs);

        // Recommended tests
        List<String> tests = new ArrayList<>();
        json.path("recommendedTests").forEach(t -> tests.add(t.asText()));
        response.setRecommendedTests(tests);

        response.setFollowupQuestion(json.path("followupQuestion").asText());

        return response;
    }
}