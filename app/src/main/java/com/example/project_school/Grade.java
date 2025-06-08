package com.example.project_school;
public class Grade {
    private String gradeName;
    private String percentage;
    private String score;

    public Grade(String gradeName, String percentage, String score) {
        this.gradeName = gradeName;
        this.percentage = percentage;
        this.score = score;
    }

    public String getGradeName() { return gradeName; }
    public String getPercentage() { return percentage; }
    public String getScore() { return score; }
}
