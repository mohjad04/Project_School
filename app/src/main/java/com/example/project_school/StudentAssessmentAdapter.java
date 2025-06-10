package com.example.project_school;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class StudentAssessmentAdapter extends RecyclerView.Adapter<StudentAssessmentAdapter.StudentAssessmentViewHolder> {

    private List<Assessment> assessments;

    public StudentAssessmentAdapter(List<Assessment> assessments) {
        this.assessments = assessments;
    }

    @NonNull
    @Override
    public StudentAssessmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assessment_card, parent, false);
        return new StudentAssessmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAssessmentViewHolder holder, int position) {
        Assessment assessment = assessments.get(position);
        holder.bind(assessment);
    }

    @Override
    public int getItemCount() {
        return assessments.size();
    }

    static class StudentAssessmentViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, typeTextView, dateTextView, remarksTextView;

        public StudentAssessmentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            remarksTextView = itemView.findViewById(R.id.remarksTextView);
        }

        public void bind(Assessment assessment) {
            nameTextView.setText(assessment.name);
            typeTextView.setText("Course: " + assessment.course);
            dateTextView.setText("Date: " + assessment.date);
            remarksTextView.setText("Percentage: " + assessment.percentage+" %");
        }
    }
}
