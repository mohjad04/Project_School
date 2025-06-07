package com.example.project_school;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class TeacherScheduleActivity extends AppCompatActivity {

    private String BASE_URL;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private int teacherId;
    private String authToken;

    // Schedule components
    private RecyclerView scheduleRecyclerView;
    private TeacherScheduleAdapter scheduleAdapter;
    private List<TeacherDashboardActivity.TeacherScheduleItem> scheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_schedule);

        BASE_URL = getString(R.string.URL);
        initializeComponents();
        setupUI();
        loadSchedule();
    }

    private void initializeComponents() {
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("user_id", 0);
        authToken = sharedPreferences.getString("auth_token", "");

        scheduleList = new ArrayList<>();
        scheduleRecyclerView = findViewById(R.id.schedule_recycler_view);
    }

    private void setupUI() {
        // Setup ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Schedule");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup schedule RecyclerView
        scheduleAdapter = new TeacherScheduleAdapter(scheduleList);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setAdapter(scheduleAdapter);
    }

    private void loadSchedule() {
        String url = BASE_URL + "schedule/teacher.php?teacher_id=" + teacherId +
                "&term_name=second term&year_name=2024-2025";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("data");
                            scheduleList.clear();

                            // Map to group by course + class
                            Map<String, TeacherDashboardActivity.TeacherScheduleItem> scheduleMap = new HashMap<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);
                                String courseName = item.getString("course_name");
                                String dayOfWeek = item.getString("day_of_week");
                                String startTime = item.getString("start_time");
                                String endTime = item.getString("end_time");
                                String className = item.getString("class_num") + item.getString("class_branch");

                                String key = courseName + "_" + className;

                                if (!scheduleMap.containsKey(key)) {
                                    scheduleMap.put(key, new TeacherDashboardActivity.TeacherScheduleItem(courseName, className));
                                }

                                scheduleMap.get(key).addDayTime(dayOfWeek, startTime, endTime);
                            }

                            // Add grouped items to the list
                            scheduleList.addAll(scheduleMap.values());
                            scheduleAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No schedule data found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading schedule", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}