package com.example.project_school;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
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

public class AssessmentsCalendar extends AppCompatActivity {


    private RecyclerView asseRecyclerView;
    private Toolbar toolbarAssessments;

    private StudentAssessmentAdapter adapter;
    private List<Assessment> upcomingAssessments = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private String authToken;
    private int studentId, termId;
    private String BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessments_calendar);

        toolbarAssessments = findViewById(R.id.toolbarAssessments);
        setSupportActionBar(toolbarAssessments);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }




        asseRecyclerView = findViewById(R.id.asseRecyclerView);
        asseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        authToken = sharedPreferences.getString("auth_token", "");
        studentId = sharedPreferences.getInt("user_id", -1);
        termId = sharedPreferences.getInt("current_term", -1);
        BASE_URL = getString(R.string.URL);

        fetchAssessments();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void fetchAssessments() {
        String url = BASE_URL + "/assessments/student.php?student_id=" + studentId + "&term_id=" + termId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                Assessment assessment = new Assessment(obj);
                                if (assessment.score == -1) {
                                    upcomingAssessments.add(assessment);
                                }
                            }

                             adapter = new StudentAssessmentAdapter(upcomingAssessments);
                            asseRecyclerView.setAdapter(adapter);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show()
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
}
