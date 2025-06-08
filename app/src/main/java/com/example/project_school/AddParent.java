package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
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

public class AddParent extends AppCompatActivity {
    private ListView studentListView;
    private List<Student> studentList = new ArrayList<>();
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pernat);
        Intent intent = getIntent();

        studentListView = findViewById(R.id.student_list_view);
        adapter = new StudentAdapter(this, studentList);
        studentListView.setAdapter(adapter);

        fetchStudents();

        studentListView.setOnItemClickListener((parent, view, position, id) -> {
            Student selectedStudent = studentList.get(position);
            String studentId = selectedStudent.getId();
            String studentName = selectedStudent.getName();

            Intent intent2 = new Intent(AddParent.this, MainAddParent.class);
            intent2.putExtra("student_id", studentId);
            intent2.putExtra("student_name", studentName);
            startActivity(intent2);
        });

    }

    private void fetchStudents() {
        String url = getString(R.string.URL) + "students/noParent.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        List<Student> newStudents = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String id = item.getString("student_id");
                            String name = item.getString("name");

                            newStudents.add(new Student(id, name));
                        }

                        studentList.clear();
                        studentList.addAll(newStudents);
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse students", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching students", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}
