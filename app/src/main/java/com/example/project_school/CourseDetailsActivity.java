package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

public class CourseDetailsActivity extends AppCompatActivity {
    private TextView txtContent;
    private int courseId;
    private String source;
    private String studentId;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private int termID;
    private String Token;

    private ProgressBar totalGradeChart;
    private TextView totalGradeText;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);

        toolbar = findViewById(R.id.toolbarMarks);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        totalGradeChart = findViewById(R.id.total_grade_chart);
        totalGradeText = findViewById(R.id.total_grade_text);
        listView = findViewById(R.id.listGrades);
        txtContent = findViewById(R.id.txtContent);

        courseId = getIntent().getIntExtra("course_id", -1);
        source = getIntent().getStringExtra("source");
        studentId = getIntent().getStringExtra("ID");

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        termID = sharedPreferences.getInt("current_term", -1);
        Token = sharedPreferences.getString("auth_token", "");

        if (source != null && source.equals("grades")) {
            loadGrades();
        } else if (source != null && source.equals("assignments")) {
            Intent intent = new Intent(CourseDetailsActivity.this, AssignmentActivity.class);
            intent.putExtra("ID", studentId);
            intent.putExtra("term_id", termID);
            intent.putExtra("course_id", courseId);
            startActivity(intent);
            finish();
        } else {
            txtContent.setText("Unknown source");
            txtContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGrades() {
        String url = getString(R.string.URL) + "assessments/student.php?student_id=" + studentId + "&term_id=" + termID + "&course_id=" + courseId;
        Log.i("URL", url);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.e("Res", response.toString());
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            List<Grade> gradeList = new ArrayList<>();

                            double totalCalculatedGrade = 0.0;
                            double totalPossiblePercentage = 0.0;

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                String gradeName = obj.getString("assessment_name");
                                String scoreStr = obj.getString("score");

                                if (i == 0) {
                                    toolbar.setTitle(obj.getString("course_name") + " Marks");
                                }

                                if (!scoreStr.equals("-1.00")) {
                                    try {
                                        double score = Double.parseDouble(scoreStr);
                                        double percentage = obj.getDouble("percentage");
                                        double maxScore = obj.getDouble("max_score");

                                        if (maxScore > 0) {
                                            totalCalculatedGrade += (score / maxScore) * percentage;
                                        }

                                        totalPossiblePercentage += percentage;

                                        String percentageStr = String.format(Locale.US, "%.1f%%", percentage);
                                        gradeList.add(new Grade(gradeName, percentageStr, scoreStr));

                                    } catch (JSONException e) {
                                        Log.e("ApiError", "JSON response is missing 'max_score' field for: " + gradeName, e);
                                        Toast.makeText(this, "Error: Missing data for " + gradeName, Toast.LENGTH_SHORT).show();
                                    } catch (NumberFormatException e) {
                                        Log.e("ParsingError", "Could not parse grade data.", e);
                                    }
                                }
                            }

                            if (totalPossiblePercentage > 0) {
                                totalGradeChart.setMax((int) Math.round(totalPossiblePercentage));
                                totalGradeChart.setProgress((int) Math.round(totalCalculatedGrade));
                            } else {
                                totalGradeChart.setMax(100);
                                totalGradeChart.setProgress(0);
                            }

                            String displayText = String.format(Locale.US, "%.1f / %.1f", totalCalculatedGrade, totalPossiblePercentage);
                            totalGradeText.setText(displayText);

                            GradeAdapter adapter = new GradeAdapter(this, gradeList);
                            listView.setAdapter(adapter);

                        } else {
                            txtContent.setText("No grades available");
                            txtContent.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No grades available", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        txtContent.setText("Error parsing data");
                        txtContent.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    txtContent.setText("Error loading data from the server");
                    txtContent.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Request error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + Token);
                return headers;
            }
        };

        queue.add(request);
    }
}
