package com.example.project_school;
public class ScheduleItem {
    public String dayOfWeek;
    public String startTime;
    public String endTime;
    public String courseName;
    public String teacherName;

    public ScheduleItem(String dayOfWeek, String startTime, String endTime, String courseName, String teacherName) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseName = courseName;
        this.teacherName = teacherName;
    }
}
