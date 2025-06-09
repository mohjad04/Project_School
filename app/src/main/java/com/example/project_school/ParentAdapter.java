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

public class ParentAdapter extends ArrayAdapter<Parent> {
    private final Context context;
    private final List<Parent> parents;
    private final AvailableParents activity;

    public ParentAdapter(AvailableParents activity, List<Parent> parents) {
        super(activity, 0, parents);
        this.context = activity;
        this.activity = activity;
        this.parents = parents;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parentView) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.parent_item, parentView, false);
        }

        Parent parent = parents.get(position);

        TextView nameView = convertView.findViewById(R.id.parent_name);
        TextView emailView = convertView.findViewById(R.id.parent_email);
        Button assignBtn = convertView.findViewById(R.id.assign_button);

        nameView.setText(parent.name);
        emailView.setText(parent.email);

        assignBtn.setOnClickListener(v -> {
            activity.submitAssignment(parent.id); // ✅ Call the method in the activity
        });

        return convertView;
    }
}
