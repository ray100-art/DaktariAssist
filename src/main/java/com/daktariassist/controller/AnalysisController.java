package com.daktariassist.controller;

import com.daktariassist.model.DiagnosisRequest;
import com.daktariassist.model.DiagnosisResponse;
import com.daktariassist.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    @Autowired
    private GroqService geminiService;

    // Main diagnosis analysis endpoint
    @PostMapping("/analyse")
    public ResponseEntity<DiagnosisResponse> analyse(@RequestBody DiagnosisRequest request) {
        try {
            DiagnosisResponse response = geminiService.analyse(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            DiagnosisResponse error = new DiagnosisResponse();
            error.setError("Analysis failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Health check - confirm server is running
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("DaktariAssist is running!");
    }
}