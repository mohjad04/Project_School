package com.example.project_school;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class GradeAdapter extends ArrayAdapter<Grade> {
    private Context context;
    private List<Grade> gradeList;

    public GradeAdapter(Context context, List<Grade> grades) {
        super(context, 0, grades);
        this.context = context;
        this.gradeList = grades;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_grade, parent, false);
        }

        Grade grade = gradeList.get(position);

        TextView txtGradeName = convertView.findViewById(R.id.txtGradeName);
        TextView txtPercentage = convertView.findViewById(R.id.txtPercentage);
        TextView txtScore = convertView.findViewById(R.id.txtScore);

        txtGradeName.setText(grade.getGradeName());
        txtPercentage.setText(grade.getPercentage());
        txtScore.setText(grade.getScore());

        return convertView;
    }
}
