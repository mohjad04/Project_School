package com.example.project_school;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class StudentAdapter2 extends ArrayAdapter<Student2> {
    private Context context;
    private List<Student2> students;

    public StudentAdapter2(Context context, List<Student2> students) {
        super(context, 0, students);
        this.context = context;
        this.students = students;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        }

        Student2 student = students.get(position);

        TextView idView = convertView.findViewById(R.id.student_id);
        TextView nameView = convertView.findViewById(R.id.student_name);
        Button addParentButton = convertView.findViewById(R.id.add_parent_button);

        idView.setText("ID: " + student.getId());
        nameView.setText("Name: " + student.getName());

        addParentButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainAddParent.class);
            intent.putExtra("student_id", student.getId());
            intent.putExtra("student_name", student.getName());
            context.startActivity(intent);
        });

        return convertView;
    }
}
