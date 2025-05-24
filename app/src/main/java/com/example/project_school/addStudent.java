package com.example.project_school;

import android.app.DatePickerDialog;
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

public class addStudent extends AppCompatActivity {

    private EditText studentName, studentEmail, studentDob, studentPhone;
    private Spinner studentClassSpinner;
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

        submitStudentButton.setOnClickListener(v -> submitStudentData());
    }

    private void initViews() {
        studentName = findViewById(R.id.studentName);
        studentEmail = findViewById(R.id.studentEmail);
        studentDob = findViewById(R.id.studentDob);
        studentPhone = findViewById(R.id.studentPhone);
        studentClassSpinner = findViewById(R.id.studentClassSpinner);
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


    private void submitStudentData() {
        String name = studentName.getText().toString();
        String email = studentEmail.getText().toString();
        String dob = studentDob.getText().toString();
        String phone = studentPhone.getText().toString();
        String studentClass = studentClassSpinner.getSelectedItem().toString();
        String url = getString(R.string.URL)+"addUser.php";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            public String getBodyContentType() {
                // as we are passing data in the form of url encoded
                // so we are passing the content type below
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("date_of_birth", dob);
//                params.put("phone", phone);
//                params.put("student_class", studentClass);
                params.put("password",name);
                params.put("role","student");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        queue.add(postRequest);
    }
}
