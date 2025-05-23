package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegHome extends AppCompatActivity {

    private Button addStudentBtn, addTeacherBtn, scheduleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initButtons();
        setupNavigation();
    }

    private void initButtons() {
        addStudentBtn = findViewById(R.id.addStudentbtn);
        addTeacherBtn = findViewById(R.id.addTeacherbtn);
        scheduleBtn = findViewById(R.id.schedulebtn);
    }

    private void setupNavigation() {
        addStudentBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, addStudent.class);
            startActivity(intent);
        });

        addTeacherBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, addTeacher.class);
            startActivity(intent);
        });

//        scheduleBtn.setOnClickListener(v -> {
//            Intent intent = new Intent(RegHome.this, ScheduleActivity.class);
//            startActivity(intent);
//        });
    }
}
