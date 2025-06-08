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

        Intent intent = getIntent();
        parentName.setText(intent.getStringExtra("parent_name"));
        parentEmail.setText(intent.getStringExtra("parent_email"));
        parentId.setText(intent.getStringExtra("parent_id"));
        parentPassword.setText(intent.getStringExtra("parent_password"));
        parentDob.setText(intent.getStringExtra("parent_dob"));
        studentId.setText(intent.getStringExtra("student_id"));
        studentName.setText(intent.getStringExtra("student_name"));

        submit= findViewById(R.id.submitParentStdButton);

        submit.setOnClickListener(v -> {
            String parentIdStr = parentId.getText().toString().trim();
            String studentIdStr = studentId.getText().toString().trim();

            if (parentIdStr.isEmpty() || studentIdStr.isEmpty()) {
                Toast.makeText(ParentsInfo.this, "Parent ID and Student ID are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("student_id", Integer.parseInt(studentIdStr));
                jsonBody.put("parent_id", Integer.parseInt(parentIdStr));
                jsonBody.put("relationship", "father");

                String url = getString(R.string.URL) + "parents/assign.php";

                RequestQueue queue = Volley.newRequestQueue(ParentsInfo.this);
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        response -> {
                            try {
                                String status = response.getString("status");
                                String message = response.getString("message");
                                Toast.makeText(ParentsInfo.this, message, Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(ParentsInfo.this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> Toast.makeText(ParentsInfo.this, "Request failed: " + error.getMessage(), Toast.LENGTH_LONG).show()
                );

                queue.add(request);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ParentsInfo.this, "Failed to create request body", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
