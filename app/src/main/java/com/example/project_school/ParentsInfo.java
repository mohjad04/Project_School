package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ParentsInfo extends AppCompatActivity {
    EditText parentName, parentEmail, parentId, parentPassword, parentDob, studentId, studentName;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parents_info);

        parentName = findViewById(R.id.ParentName);
        parentEmail = findViewById(R.id.ParentEmail);
        parentId = findViewById(R.id.ParentId);
        parentPassword = findViewById(R.id.ParentPassword);
        parentDob = findViewById(R.id.ParentDob);
        studentId = findViewById(R.id.Studentid);
        studentName = findViewById(R.id.StudentName);
        submit = findViewById(R.id.submitParentStdButton);

        Intent intent = getIntent();
        parentName.setText(intent.getStringExtra("parent_name"));
        parentEmail.setText(intent.getStringExtra("parent_email"));
        parentId.setText(intent.getStringExtra("parent_id"));
        parentPassword.setText(intent.getStringExtra("parent_password"));
        parentDob.setText(intent.getStringExtra("parent_dob"));
        studentId.setText(intent.getStringExtra("student_id"));
        studentName.setText(intent.getStringExtra("student_name"));

        submit.setOnClickListener(v -> submitAssignment());
    }

    private void submitAssignment() {
        String parentIdStr = parentId.getText().toString().trim();
        String studentIdStr = studentId.getText().toString().trim();

        if (parentIdStr.isEmpty() || studentIdStr.isEmpty()) {
            Toast.makeText(this, "Parent ID and Student ID are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("student_id", Integer.parseInt(studentIdStr));
            jsonBody.put("parent_id", Integer.parseInt(parentIdStr));
            jsonBody.put("relationship", "father");

            String url = getString(R.string.URL) + "parents/assign.php";
            String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            if (status.equals("success")) {
                                finish(); // or go to another screen if needed
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Invalid response format", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Request failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
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

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request body", Toast.LENGTH_SHORT).show();
        }
    }


}

