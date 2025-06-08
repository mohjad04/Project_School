package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ParentsForm extends AppCompatActivity {
    String studentId, studentName;
    EditText name, email, address, phone, dob, password;
    Spinner genderSpinner;
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_form);

        // Get student info from previous screen
        Intent intent2 = getIntent();
        studentId = intent2.getStringExtra("student_id");
        studentName = intent2.getStringExtra("student_name");

        // Bind UI elements
        name = findViewById(R.id.ParentName);
        email = findViewById(R.id.ParentEmail);
        address = findViewById(R.id.ParentAdd);
        phone = findViewById(R.id.ParentPhone);
        dob = findViewById(R.id.ParentDob);
        password = findViewById(R.id.ParentPas);
        genderSpinner = findViewById(R.id.ParentGenderSpinner);
        nextBtn = findViewById(R.id.submitParentButton);

        // Spinner for gender
        String[] genders = {"female", "male"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Date Picker for DOB
        dob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, y, m, d) -> dob.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)),
                    year, month, day);
            dialog.show();
        });

        // Submit action
        nextBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                saveParent();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });
    }

    private boolean validateInputs() {
        return !name.getText().toString().isEmpty() &&
                !email.getText().toString().isEmpty() &&
                !address.getText().toString().isEmpty() &&
                !phone.getText().toString().isEmpty() &&
                !dob.getText().toString().isEmpty() &&
                !password.getText().toString().isEmpty();
    }

    private void saveParent() {
        String nameStr = name.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String addressStr = address.getText().toString().trim();
        String phoneStr = phone.getText().toString().trim();
        String dobStr = dob.getText().toString().trim();
        String genderStr = genderSpinner.getSelectedItem().toString();
        String passwordStr = password.getText().toString().trim();

        String url = getString(R.string.URL) + "parents/create.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", nameStr);
            jsonBody.put("email", emailStr);
            jsonBody.put("address", addressStr);
            jsonBody.put("phone", phoneStr);
            jsonBody.put("date_of_birth", dobStr);
            jsonBody.put("gender", genderStr);
            jsonBody.put("password", passwordStr);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to build parent JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        if (response.has("status") && response.getString("status").equals("success")) {
                            JSONObject data = response.optJSONObject("data");
                            String parentId = data != null ? data.optString("parent_id", emailStr) : emailStr;

                            Toast.makeText(this, "Parent saved successfully", Toast.LENGTH_SHORT).show();
                            goToNextScreen(parentId);
                        } else {
                            Toast.makeText(this, "Failed to save parent", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void goToNextScreen(String parentId) {
        Intent intent = new Intent(ParentsForm.this, ParentsInfo.class);
        intent.putExtra("student_id", studentId);
        intent.putExtra("student_name", studentName);
        intent.putExtra("parent_name", name.getText().toString());
        intent.putExtra("parent_email", email.getText().toString());
        intent.putExtra("parent_password", password.getText().toString());
        intent.putExtra("parent_dob", dob.getText().toString());
        intent.putExtra("parent_id", parentId);
        startActivity(intent);
    }
}
