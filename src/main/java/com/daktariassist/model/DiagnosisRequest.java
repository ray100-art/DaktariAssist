package com.daktariassist.model;

import java.util.List;

public class DiagnosisRequest {

    private int age;
    private String sex;
    private String chiefComplaint;
    private List<String> symptoms;
    private String duration;
    private String temperature;
    private String bloodPressure;
    private String heartRate;
    private String spo2;
    private String respiratoryRate;
    private List<String> medicalHistory;
    private String proposedDiagnosis;
    private String followupQuestion;

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public String getHeartRate() { return heartRate; }
    public void setHeartRate(String heartRate) { this.heartRate = heartRate; }

    public String getSpo2() { return spo2; }
    public void setSpo2(String spo2) { this.spo2 = spo2; }

    public String getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(String respiratoryRate) { this.respiratoryRate = respiratoryRate; }

    public List<String> getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(List<String> medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getProposedDiagnosis() { return proposedDiagnosis; }
    public void setProposedDiagnosis(String proposedDiagnosis) { this.proposedDiagnosis = proposedDiagnosis; }

    public String getFollowupQuestion() { return followupQuestion; }
    public void setFollowupQuestion(String followupQuestion) { this.followupQuestion = followupQuestion; }
}