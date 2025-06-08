package com.example.project_school;
public class ScheduleItem {
    public String dayOfWeek;
    public String startTime;
    public String endTime;
    public String courseName;
    public String teacherName;
    public String timerange;
    public int courseId;
    public int teacherId;

    public ScheduleItem(String dayOfWeek, String startTime, String endTime, String courseName, String teacherName) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseName = courseName;
        this.teacherName = teacherName;
    }
    public ScheduleItem(){

    }
    public ScheduleItem(String dayOfWeek, String timerange, String courseName, String teacherName) {
        this.dayOfWeek = dayOfWeek;
        this.timerange = timerange;
        this.courseName = courseName;
        this.teacherName = teacherName;
    }

    public ScheduleItem(String courseName, String teacherName){
        this.courseName = courseName;
        this.teacherName = teacherName;
    }

}
