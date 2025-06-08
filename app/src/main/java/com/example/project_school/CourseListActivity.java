package com.example.project_school;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
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

public class CourseListActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    List<Course> courseList = new ArrayList<>();
    CourseAdapter adapter;
    String source,studentId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_list);

        Toolbar toolbar = findViewById(R.id.toolbarCourse);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        recyclerView = findViewById(R.id.recyclerViewCourses);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        source = getIntent().getStringExtra("source");
        studentId=getIntent().getStringExtra("ID");
        adapter = new CourseAdapter(this, courseList, source,studentId);
        recyclerView.setAdapter(adapter);

        fetchCourses(10);
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




    private void fetchCourses(int gradeLevel) {
        String url = getString(R.string.URL) + "courses/list.php?grade_level=" + gradeLevel;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("API_RESPONSE", response.toString());
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("data");

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject obj = dataArray.getJSONObject(i);

                                Course course = new Course();
                                course.setCourse_id(obj.getInt("course_id"));
                                course.setCourse_name(obj.getString("course_name"));
                                course.setDescription(obj.getString("description"));
                                course.setGrade_level(String.valueOf(obj.getInt("grade_level")));
                                course.setMandatory(obj.getInt("is_mandatory") == 1);
                                courseList.add(course);
                            }

                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        queue.add(request);
    }


}