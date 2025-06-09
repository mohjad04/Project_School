package com.example.project_school;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class StudentAttendanceReportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReportAttendance.AttendanceReportAdapter adapter;
    private List<ReportAttendance.AttendanceRecord> attendanceRecords;
    private SharedPreferences sharedPreferences;
    private String authToken, BASE_URL;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance_report);

        recyclerView = findViewById(R.id.recyclerViewStudentAttendance);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        authToken = sharedPreferences.getString("auth_token", "");
        BASE_URL = getString(R.string.URL);
        studentId = sharedPreferences.getInt("user_id", 0);

        attendanceRecords = new ArrayList<>();
        adapter = new ReportAttendance.AttendanceReportAdapter(attendanceRecords);
        adapter.setReadOnly(true);
        recyclerView.setAdapter(adapter);

        fetchAttendanceData();
    }

    private void fetchAttendanceData() {
        String url = BASE_URL + "attendance/report.php?user_id=" + studentId;
        Log.i("ABS",url);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        attendanceRecords.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            ReportAttendance.AttendanceRecord record = new ReportAttendance.AttendanceRecord(
                                    obj.getInt("user_id"),
                                    obj.getString("name"),
                                    obj.getString("status"),
                                    obj.getString("date"),
                                    obj.optString("remarks", ""),
                                    obj.getString("marked_by"),
                                    obj.getString("marked_at")
                            );
                            attendanceRecords.add(record);
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading attendance", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
