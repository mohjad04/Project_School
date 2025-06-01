package com.example.project_school;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class studentSchedule extends AppCompatActivity {
    TableLayout tableLayout;
    List<ScheduleItem> scheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_schedule);

        // Correct Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbarSchedule);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tableLayout = findViewById(R.id.tableLayout);

        buildTableHeaders();
        fetchSchedule("10", "A", 2); // Example class
    }


    // Handle Toolbar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void buildTableHeaders() {
        TableRow header = new TableRow(this);
        header.addView(makeCell("Time", true));
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};
        for (String day : days) {
            header.addView(makeCell(day, true));
        }
        tableLayout.addView(header);
    }

    private TextView makeCell(String text, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 24, 16, 24);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(14);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void fetchSchedule(String classNum, String classBranch, int termId) {
        String url = getString(R.string.URL) + "schedule/class.php?class_num=" + classNum + "&class_branch=" + classBranch + "&term_id=" + termId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                ScheduleItem item = new ScheduleItem(
                                        obj.getString("day_of_week"),
                                        obj.getString("start_time"),
                                        obj.getString("end_time"),
                                        obj.getString("course_name"),
                                        obj.getString("teacher_name")
                                );
                                scheduleList.add(item);
                            }
                            showScheduleTable();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "API Error", Toast.LENGTH_SHORT).show()
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

    private void showScheduleTable() {
        // Collect all unique time slots
        List<String> timeSlots = new ArrayList<>();
        for (ScheduleItem item : scheduleList) {
            String time = item.startTime + " - " + item.endTime;
            if (!timeSlots.contains(time)) {
                timeSlots.add(time);
            }
        }

        Collections.sort(timeSlots);
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};

        for (String time : timeSlots) {
            TableRow row = new TableRow(this);
            row.addView(makeCell(time, false));
            for (String day : days) {
                String course = "";
                for (ScheduleItem item : scheduleList) {
                    String itemTime = item.startTime + " - " + item.endTime;
                    if (item.dayOfWeek.equalsIgnoreCase(day) && itemTime.equals(time)) {
                        course = item.courseName;
                        break;
                    }
                }
                row.addView(makeCell(course, false));
            }
            tableLayout.addView(row);
        }
    }


}
