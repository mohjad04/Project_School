package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class addStudent2 extends AppCompatActivity {

    private String name,email,dob,phone,gender;
    private int studentClass;
    private EditText studentPassword, studentAddress, studentMedicalState, studentPreviousSchool;
    private Spinner studentClassBranch, studentBloodGroup;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_student2);
        initViews();
        Intent intent = getIntent();
         name = intent.getStringExtra("name");
         email = intent.getStringExtra("email");
         dob = intent.getStringExtra("dob");
         phone = intent.getStringExtra("phone");
         studentClass = intent.getIntExtra("class", 0);
         gender = intent.getStringExtra("gender");

         setupBloodGroupSpinner();

        queue = Volley.newRequestQueue(this);
        fetchBranchesFromApi(studentClass);
// Now you can use these values in addStudent2


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        studentPassword = findViewById(R.id.studentPassword);
        studentAddress = findViewById(R.id.studentAddress);
        studentMedicalState = findViewById(R.id.studentMedicalState);
        studentPreviousSchool = findViewById(R.id.studentPreviousSchool);
        studentClassBranch = findViewById(R.id.studentClassBransh);
        studentBloodGroup = findViewById(R.id.studentBloodGroup);
    }
    private void fetchBranchesFromApi(int classNum) {
        String url = getString(R.string.URL) + "classes/list.php?class_num=" + classNum;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        ArrayList<String> branches = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            branches.add(item.getString("class_branch"));
                            Log.d("BRANCHES", item.getString("class_branch"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, branches);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        studentClassBranch.setAdapter(adapter);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse branches", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching branches", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void setupBloodGroupSpinner() {
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bloodGroups);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentBloodGroup.setAdapter(bloodAdapter);
    }


    private void submitStudentDataToServer() {
        String password = studentPassword.getText().toString().trim();
        String address = studentAddress.getText().toString().trim();
        String medicalState = studentMedicalState.getText().toString().trim();
        String previousSchool = studentPreviousSchool.getText().toString().trim();
        String branch = studentClassBranch.getSelectedItem().toString();
        String bloodGroup = studentBloodGroup.getSelectedItem().toString();

        String url = getString(R.string.URL) + "students/create.php"; // Your server endpoint

        JSONObject jsonBody = new JSONObject();
        try {
            // Data from addStudent activity
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("dob", dob);
            jsonBody.put("phone", phone);
            jsonBody.put("student_class", studentClass);
            jsonBody.put("gender", gender);

            // Data from addStudent2 activity
            jsonBody.put("password", password);
            jsonBody.put("address", address);
            jsonBody.put("medical_state", medicalState);
            jsonBody.put("previous_school", previousSchool);
            jsonBody.put("class_branch", branch);
            jsonBody.put("blood_group", bloodGroup);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to build JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // or redirect
                },
                error -> {
                    Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        queue.add(request);
    }



}