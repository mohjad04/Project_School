package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentsForMarks extends AppCompatActivity {
    private static String BASE_URL;
    private ListView students;
    private TextView course_name_text;
    private String authToken;
    private int teacherId;
    private int termID, yearID;
    private SharedPreferences sharedPreferences;
    private String classData;
    private String course;
    private RequestQueue queue;
    private List<String> studentsData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_marks);
        studentsData = new ArrayList<>();
        BASE_URL = getString(R.string.URL);
        students = findViewById(R.id.students_list_view);
        course_name_text = findViewById(R.id.course_name_text);
        Intent intent = getIntent();
        classData = intent.getStringExtra("class");
        course = intent.getStringExtra("course");
        int courseID = intent.getIntExtra("course_id", -1);
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("user_id", 0);
        course_name_text.setText(course + " " + classData + " Students");
        queue = Volley.newRequestQueue(this);
        authToken = sharedPreferences.getString("auth_token", "");
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);
        loadStudents();
        students.setOnItemClickListener((parent, view, position, id) -> {
            String selected = studentsData.get(position);

            String[] parts = selected.split(" ");
            int studentId = Integer.parseInt(parts[parts.length - 2]);

            Intent intent1 = new Intent(StudentsForMarks.this, AssessmentActivity.class);
            intent1.putExtra("student_id", studentId);
            intent1.putExtra("term_id", termID);
            intent1.putExtra("year_id", yearID);
            intent1.putExtra("teacher_id", teacherId);
            intent1.putExtra("course_id", courseID);
            startActivity(intent1);
        });

    }

    private void loadStudents() {
        if (classData == null || !classData.contains(" ")) {
            Toast.makeText(this, "Invalid class data", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] params = classData.split(" ");
        if (params.length < 2) {
            Toast.makeText(this, "Invalid class format", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentsUrl = BASE_URL + "students/list.php?class_num=" + params[0] + "&class_branch=" + params[1];

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, studentsUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("data");

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject studentObj = dataArray.getJSONObject(i);
                                String studentData = studentObj.getString("name") + " ( ID: " +
                                        studentObj.getString("student_id") + " )";
                                studentsData.add(studentData);
                            }

                            if (studentsData.isEmpty()) {
                                Toast.makeText(this, "No students found in this class", Toast.LENGTH_SHORT).show();
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, studentsData);
                            students.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing student data", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        queue.add(request);
    }

}
