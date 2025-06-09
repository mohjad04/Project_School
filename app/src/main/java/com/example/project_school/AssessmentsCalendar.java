package com.example.project_school;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import java.util.Locale;
import java.util.Map;


public class AssessmentsCalendar extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView detailsTextView;

    private List<Assessment> upcomingAssessments = new ArrayList<>();
    private Map<String, List<Assessment>> dateToAssessmentsMap = new HashMap<>();
SharedPreferences sharedPreferences;
    private String authToken;
    private int studentId, termId;
    private String BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessments_calendar);

        calendarView = findViewById(R.id.calendarView);
        detailsTextView = findViewById(R.id.eventTextView);

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        authToken = sharedPreferences.getString("auth_token", "");
        studentId = sharedPreferences.getInt("user_id", -1);
        termId = sharedPreferences.getInt("current_term", -1);
        BASE_URL = getString(R.string.URL);

        fetchAssessments();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showAssessmentsForDate(selectedDate);
        });
    }

    private void fetchAssessments() {
        String url = BASE_URL + "/assessments/student.php?student_id=" + studentId + "&term_id=" + termId;
        Log.i("Resl",url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.i("Res",response.toString());
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                Assessment assessment = new Assessment(obj);
                                if (assessment.score == -1) {
                                    upcomingAssessments.add(assessment);

                                    if (!dateToAssessmentsMap.containsKey(assessment.date)) {
                                        dateToAssessmentsMap.put(assessment.date, new ArrayList<>());
                                    }
                                    dateToAssessmentsMap.get(assessment.date).add(assessment);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
        }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        queue.add(request);
    }

    private void showAssessmentsForDate(String date) {
        List<Assessment> assessments = dateToAssessmentsMap.get(date);
        if (assessments == null || assessments.isEmpty()) {
            detailsTextView.setText("No upcoming assessments on this date.");
            return;
        }

        StringBuilder details = new StringBuilder();
        for (Assessment a : assessments) {
            details.append("\u2022 ").append(a.name).append(" (type: ").append(a.type).append(")\n")
                    .append("Remarks: ").append(a.remarks).append("\n\n");
        }
        detailsTextView.setText(details.toString().trim());
    }
}
