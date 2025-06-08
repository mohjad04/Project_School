package com.example.project_school;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courseList;
    private Context context;
    private String source,studentId;



    public CourseAdapter(Context context, List<Course> courseList, String source,String studentId) {
        this.context = context;
        this.courseList = courseList;
        this.source = source;
        this.studentId=studentId;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.txtCourseName.setText(course.getCourse_name());
        holder.txtStudentCount.setText("Grade " + course.getGrade_level());

        holder.btnView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseDetailsActivity.class);
            intent.putExtra("course_id", course.getCourse_id());
            intent.putExtra("source", source);
            intent.putExtra("ID",studentId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView txtCourseName, txtStudentCount;
        ImageView imgCourse;
        Button btnView;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourseName = itemView.findViewById(R.id.txtCourseName);
            txtStudentCount = itemView.findViewById(R.id.txtStudentCount);
            imgCourse = itemView.findViewById(R.id.imgCourse);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
