package com.example.project_school;
public class Course {
    private int course_id;
    private String course_name;
    private String description;
    private String grade_level;
    private boolean is_mandatory;

    // Getters and Setters
    public int getCourse_id() { return course_id; }
    public void setCourse_id(int course_id) { this.course_id = course_id; }

    public String getCourse_name() { return course_name; }
    public void setCourse_name(String course_name) { this.course_name = course_name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGrade_level() { return grade_level; }
    public void setGrade_level(String grade_level) { this.grade_level = grade_level; }

    public boolean isMandatory() { return is_mandatory; }
    public void setMandatory(boolean is_mandatory) { this.is_mandatory = is_mandatory; }
}
