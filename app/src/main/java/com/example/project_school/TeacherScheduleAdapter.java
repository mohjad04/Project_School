package com.example.project_school;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TeacherScheduleAdapter extends RecyclerView.Adapter<TeacherScheduleAdapter.ScheduleViewHolder> {

    private List<TeacherDashboardActivity.TeacherScheduleItem> scheduleList;

    public TeacherScheduleAdapter(List<TeacherDashboardActivity.TeacherScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        TeacherDashboardActivity.TeacherScheduleItem item = scheduleList.get(position);

        holder.courseNameText.setText(item.getCourseName());

        // Join all (day: time) entries into one multi-line string
        StringBuilder dayTimeBuilder = new StringBuilder();
        for (String dayTime : item.getDayTimeList()) {
            dayTimeBuilder.append(dayTime).append("\n");
        }
        holder.dayTimeText.setText(dayTimeBuilder.toString().trim()); // remove trailing \n

        holder.classText.setText("Class: " + item.getClassName());

        // Optional: use first day for color logic
        int backgroundColor = getBackgroundColorForDay(item.getDayTimeList().get(0).split(":")[0]);
        holder.scheduleCard.setCardBackgroundColor(backgroundColor);
    }


    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    private int getBackgroundColorForDay(String day) {
        switch (day.toLowerCase()) {
            case "monday":
                return 0xFFE3F2FD; // Light Blue
            case "tuesday":
                return 0xFFE8F5E8; // Light Green
            case "wednesday":
                return 0xFFFFF3E0; // Light Orange
            case "thursday":
                return 0xFFF3E5F5; // Light Purple
            case "friday":
                return 0xFFFFEBEE; // Light Red
            case "saturday":
                return 0xFFF1F8E9; // Light Lime
            case "sunday":
                return 0xFFFAF2FF; // Light Lavender
            default:
                return 0xFFE3EDF0; // Default light blue
        }
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        CardView scheduleCard;
        TextView courseNameText, dayTimeText;

        TextView classText;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);

            scheduleCard = itemView.findViewById(R.id.schedule_card);
            courseNameText = itemView.findViewById(R.id.course_name_text);
            dayTimeText = itemView.findViewById(R.id.day_time_text);
            classText = itemView.findViewById(R.id.class_text);
        }
    }
}