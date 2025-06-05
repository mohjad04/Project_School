package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class studentInfo extends AppCompatActivity {

    private EditText studentName, studentEmail, studentId, studentPassword, studentDob;
    private ImageView backButton;
    private Button donebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info); // your XML file name

        studentName = findViewById(R.id.studentName);
        studentEmail = findViewById(R.id.studentEmail);
        studentId = findViewById(R.id.studentId);
        studentPassword = findViewById(R.id.studentPassword);
        studentDob = findViewById(R.id.studentDob);
        backButton = findViewById(R.id.back_button);
        donebtn = findViewById(R.id.submitStudentButton);

        // Make all fields non-editable
        disableEditText(studentName);
        disableEditText(studentEmail);
        disableEditText(studentId);
        disableEditText(studentPassword);
        disableEditText(studentDob);

        // Get the data from the intent
        int id = getIntent().getIntExtra("id", -1);
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String dob = getIntent().getStringExtra("dob");
        String password = getIntent().getStringExtra("password");

        // Fill the views
        studentId.setText("ID: " + String.valueOf(id));
        studentName.setText("Name: " + name);
        studentEmail.setText("Email: " + email);
        studentDob.setText("Date of birth: "+dob);
        studentPassword.setText("Password: " + password);

        backButton.setOnClickListener(v -> finish());

        donebtn.setOnClickListener(v -> {
            Intent intent = new Intent(studentInfo.this, RegHome.class);
            startActivity(intent);
        });
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackground(null); // Optional: removes underline
    }
}
