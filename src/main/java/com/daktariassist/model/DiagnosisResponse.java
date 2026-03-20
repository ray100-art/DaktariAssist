package com.daktariassist.model;

import java.util.List;

public class DiagnosisResponse {

    private String consistency;
    private String consistencyTitle;
    private String consistencyDescription;
    private List<String> redFlags;
    private List<Differential> differentials;
    private List<String> recommendedTests;
    private String followupQuestion;
    private String error;

    public String getConsistency() { return consistency; }
    public void setConsistency(String consistency) { this.consistency = consistency; }

    public String getConsistencyTitle() { return consistencyTitle; }
    public void setConsistencyTitle(String consistencyTitle) { this.consistencyTitle = consistencyTitle; }

    public String getConsistencyDescription() { return consistencyDescription; }
    public void setConsistencyDescription(String consistencyDescription) { this.consistencyDescription = consistencyDescription; }

    public List<String> getRedFlags() { return redFlags; }
    public void setRedFlags(List<String> redFlags) { this.redFlags = redFlags; }

    public List<Differential> getDifferentials() { return differentials; }
    public void setDifferentials(List<Differential> differentials) { this.differentials = differentials; }

    public List<String> getRecommendedTests() { return recommendedTests; }
    public void setRecommendedTests(List<String> recommendedTests) { this.recommendedTests = recommendedTests; }

    public String getFollowupQuestion() { return followupQuestion; }
    public void setFollowupQuestion(String followupQuestion) { this.followupQuestion = followupQuestion; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public static class Differential {
        private String name;
        private String reason;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}