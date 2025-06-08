package com.example.project_school;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
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


public class CourseDetailsActivity extends AppCompatActivity {
    private TextView txtContent;
    private int courseId;
    private String source;
    private String studentId;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private int termID;
    private int yearID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_details);




         toolbar = findViewById(R.id.toolbarMarks);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        courseId = getIntent().getIntExtra("course_id", -1);
        source = getIntent().getStringExtra("source");
        studentId=getIntent().getStringExtra("ID");


        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);

        txtContent=findViewById(R.id.txtContent);






        if (source.equals("grades")) {
            loadGrades();
        } else if (source.equals("assignments")) {
          //  loadAssignments();
        } else {
            txtContent.setText("Unknown source");
        }
    }


    /// for toolbar  (back to home )
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void loadGrades() {
        String url = getString(R.string.URL) + "assessments/student.php?student_id=" + studentId + "&term_id="+termID+"&course_id=" + courseId;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
            Log.e("Res",response.toString());
            try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            List<Grade> gradeList = new ArrayList<>();

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                String gradeName = obj.getString("assessment_name");
                                String percentage = obj.getString("percentage") + "%";
                                String score = obj.getString("score");
                                toolbar.setTitle(obj.getString("course_name")+" Marks");

                                gradeList.add(new Grade(gradeName, percentage, score));
                            }

                            ListView listView = findViewById(R.id.listGrades);
                            GradeAdapter adapter = new GradeAdapter(this, gradeList);
                            listView.setAdapter(adapter);
                        } else {
                            Toast.makeText(this, "No grades available", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Request error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
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




  /*  private void loadAssignments() {
        // مثال: الاتصال بـ API لجلب الواجبات
        String url = "http://yourdomain.com/api/assignments/course.php?course_id=" + courseId;
        txtContent.setText("Loading assignments for course " + courseId + " ...");

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray assignments = response.getJSONArray("assignments");
                            StringBuilder sb = new StringBuilder("Assignments:\n");

                            for (int i = 0; i < assignments.length(); i++) {
                                JSONObject a = assignments.getJSONObject(i);
                                sb.append("- ").append(a.getString("title"))
                                        .append(" (Due: ").append(a.getString("due_date")).append(")\n");
                            }
                            txtContent.setText(sb.toString());
                        } else {
                            txtContent.setText("No assignments available.");
                        }
                    } catch (JSONException e) {
                        txtContent.setText("Error parsing assignments.");
                    }
                },
                error -> txtContent.setText("Failed to load assignments: " + error.getMessage())
        );

        queue.add(request);
    }*/

}