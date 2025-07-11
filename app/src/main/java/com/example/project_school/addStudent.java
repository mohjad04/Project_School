package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class addStudent extends AppCompatActivity {

    private EditText studentName, studentEmail, studentDob, studentPhone;
    private Spinner studentClassSpinner,studentGender;
    private Button submitStudentButton;
    private ImageView backButton;
    private TextView titleText;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_student);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClassSpinner();
        setupDobPicker();

        queue = Volley.newRequestQueue(this);

        submitStudentButton.setOnClickListener(v -> {
            // Optional: validate input here
            goToAddStudent2();
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void initViews() {
        studentName = findViewById(R.id.studentName);
        studentEmail = findViewById(R.id.studentEmail);
        studentDob = findViewById(R.id.studentDob);
        studentPhone = findViewById(R.id.studentPhone);
        studentClassSpinner = findViewById(R.id.studentClassSpinner);
        studentGender = findViewById(R.id.studentGender);
        submitStudentButton = findViewById(R.id.submitStudentButton);
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
    }










    private void setupClassSpinner() {
        String[] classes = {
                "1st Grade", "2nd Grade", "3rd Grade", "4th Grade", "5th Grade",
                "6th Grade", "7th Grade", "8th Grade", "9th Grade", "10th Grade",
                "11th Grade", "Tawjihi (12th Grade)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentClassSpinner.setAdapter(adapter);

        String[] genders = {
                "female", "male"
        };

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentGender.setAdapter(adapter2);
    }

    private void setupDobPicker() {
        studentDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);

            int year = currentYear - 12; // Default age
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                // Format: year-month-day
                String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                studentDob.setText(date);
            }, year, month, day); // Note: order here is year, month, day

            Calendar minDate = Calendar.getInstance();
            minDate.set(currentYear - 18, Calendar.JANUARY, 1);
            Calendar maxDate = Calendar.getInstance();
            maxDate.set(currentYear - 6, Calendar.DECEMBER, 31);

            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            datePickerDialog.show();
        });
    }


    private void goToAddStudent2() {
        String name = studentName.getText().toString().trim();
        String email = studentEmail.getText().toString().trim();
        String dob = studentDob.getText().toString().trim();
        String phone = studentPhone.getText().toString().trim();
        String studentClass = studentClassSpinner.getSelectedItem().toString();
        String gender = studentGender.getSelectedItem().toString();

        // Validate inputs
        if (name.isEmpty()) {
            studentName.setError("Name is required");
            studentName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            studentEmail.setError("Email is required");
            studentEmail.requestFocus();
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            studentEmail.setError("Invalid email format");
            studentEmail.requestFocus();
            return;
        }

        if (dob.isEmpty()) {
            studentDob.setError("Date of Birth is required");
            studentDob.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            studentPhone.setError("Phone number is required");
            studentPhone.requestFocus();
            return;
        } else if (phone.length() < 8 || phone.length() > 15) {
            studentPhone.setError("Invalid phone number length");
            studentPhone.requestFocus();
            return;
        }

        // Extract class number (e.g., "10th Grade" -> 10)
        Pattern pattern = Pattern.compile("(\\d{1,2})");
        Matcher matcher = pattern.matcher(studentClass);
        if (!matcher.find()) {
            Toast.makeText(this, "Invalid class selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int number = Integer.parseInt(matcher.group(1));

        // All checks passed, go to next activity
        Intent intent = new Intent(addStudent.this, addStudent2.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("dob", dob);
        intent.putExtra("phone", phone);
        intent.putExtra("class", number);
        intent.putExtra("gender", gender);

        startActivity(intent);
    }


}
