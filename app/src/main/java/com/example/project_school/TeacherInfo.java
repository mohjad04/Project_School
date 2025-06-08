package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class TeacherInfo extends AppCompatActivity {
    private EditText tName, tEmail, tId, tPassword, tDob;
    private ImageView backButton;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacherinfo);
        tName = findViewById(R.id.teacherName);
        tEmail = findViewById(R.id.teacherEmail);
        tId = findViewById(R.id.teacherId);
        tPassword = findViewById(R.id.teacherPassword);
        tDob = findViewById(R.id.teacherDob);
        backButton = findViewById(R.id.back_button);
        doneButton = findViewById(R.id.submitTeacherButton);

        disable(tName); disable(tEmail); disable(tId); disable(tPassword); disable(tDob);

        int id = getIntent().getIntExtra("id", -1);
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String dob = getIntent().getStringExtra("dob");
        String password = getIntent().getStringExtra("password");

        tId.setText("ID: " + id);
        tName.setText("Name: " + name);
        tEmail.setText("Email: " + email);
        tDob.setText("DOB: " + dob);
        tPassword.setText("Password: " + password);

        backButton.setOnClickListener(v -> finish());
        doneButton.setOnClickListener(v -> {
            Intent i = new Intent(this, RegHome.class);
            startActivity(i);
        });
    }

    private void disable(EditText et){
        et.setFocusable(false);
        et.setClickable(false);
        et.setCursorVisible(false);
        et.setKeyListener(null);
        et.setBackground(null);
    }
}
