package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import java.util.Calendar;

public class addTeacher extends AppCompatActivity {
    private EditText teacherName, teacherEmail, teacherPhone;
    private Spinner teacherSubjects;
    private EditText teacherQualification, teacherExp;
    private Button submitTeacherButton;
    private ImageView backButton;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_teacher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return insets;
        });

        teacherName = findViewById(R.id.teacherName);
        teacherEmail = findViewById(R.id.teacherEmail);
        teacherPhone = findViewById(R.id.teacherPhone);
        teacherSubjects = findViewById(R.id.teacherSubjectspn);
        teacherQualification = findViewById(R.id.teacherQualificatin);
        teacherExp = findViewById(R.id.teacherexp);
        submitTeacherButton = findViewById(R.id.submitTeacherButton);
        backButton = findViewById(R.id.back_button);

        queue = Volley.newRequestQueue(this);

        // Setup subject spinner (example list)
        String[] subjects = {"Math", "Science", "English", "History"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teacherSubjects.setAdapter(subjectAdapter);

        submitTeacherButton.setOnClickListener(v -> goToAddTeacher2());
        backButton.setOnClickListener(v -> finish());
    }

    private void goToAddTeacher2() {
        String name = teacherName.getText().toString().trim();
        String email = teacherEmail.getText().toString().trim();
        String phone = teacherPhone.getText().toString().trim();
        String subject = teacherSubjects.getSelectedItem().toString();
        String qualification = teacherQualification.getText().toString().trim();
        String experience = teacherExp.getText().toString().trim();

        if (name.isEmpty()) { teacherName.setError("Required"); teacherName.requestFocus(); return; }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            teacherEmail.setError("Valid email required"); teacherEmail.requestFocus(); return;
        }
        if (phone.isEmpty() || phone.length()<8) {
            teacherPhone.setError("Valid phone required"); teacherPhone.requestFocus(); return;
        }
        if (qualification.isEmpty()) {
            teacherQualification.setError("Required"); teacherQualification.requestFocus(); return;
        }
        if (experience.isEmpty()) {
            teacherExp.setError("Required"); teacherExp.requestFocus(); return;
        }

        Intent intent = new Intent(this, AddTeacher2.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("subject", subject);
        intent.putExtra("qualification", qualification);
        intent.putExtra("experience", experience);
        startActivity(intent);
    }
}
