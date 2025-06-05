package com.example.project_school;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<ScheduleItem> items;

    public ScheduleAdapter(List<ScheduleItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseText, teacherText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseText = itemView.findViewById(R.id.courseName);
            teacherText = itemView.findViewById(R.id.teacherName);
        }
    }

    @NonNull
    @Override
    public ScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        holder.courseText.setText(item.courseName);
        holder.teacherText.setText(item.teacherName);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
