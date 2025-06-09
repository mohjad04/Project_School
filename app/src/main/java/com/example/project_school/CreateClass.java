package com.example.project_school;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateClass extends AppCompatActivity {

    private Spinner classSpinner, teacherSpinner;
    private EditText branchTxt, capacityTxt;
    private Button submitButton;

    private List<Integer> teacherIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        classSpinner = findViewById(R.id.ClassesSpn);
        teacherSpinner = findViewById(R.id.teacherSpn);
        branchTxt = findViewById(R.id.branchTxt);
        capacityTxt = findViewById(R.id.capacityTxt);
        submitButton = findViewById(R.id.submitStudentButton);

        loadClassNumbers();
        fetchAllTeachers();

        submitButton.setOnClickListener(v -> submitClass());
    }

    private void loadClassNumbers() {
        List<String> classes = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            classes.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, classes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(adapter);
    }

    private void fetchAllTeachers() {
        String url = getString(R.string.URL) + "teachers/list.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        List<String> teacherNames = new ArrayList<>();
                        teacherIds.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject teacher = data.getJSONObject(i);
                            teacherNames.add(teacher.getString("name"));
                            teacherIds.add(teacher.getInt("teacher_id"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, teacherNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        teacherSpinner.setAdapter(adapter);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing teachers", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch teachers", Toast.LENGTH_SHORT).show()) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void submitClass() {
        String classNum = classSpinner.getSelectedItem().toString();
        String branch = branchTxt.getText().toString().trim();
        String capacity = capacityTxt.getText().toString().trim();
        int teacherIndex = teacherSpinner.getSelectedItemPosition();

        if (branch.isEmpty() || capacity.isEmpty() || teacherIndex < 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int teacherId = teacherIds.get(teacherIndex);

        String url = getString(R.string.URL) + "classes/create.php";
        JSONObject body = new JSONObject();
        try {
            body.put("class_num", classNum);
            body.put("class_branch", branch);
            body.put("class_teacher_id", teacherId);
            body.put("capacity", capacity);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to prepare request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(this, "Class created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // or navigate elsewhere
                },
                error -> Toast.makeText(this, "Failed to create class", Toast.LENGTH_SHORT).show()) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
