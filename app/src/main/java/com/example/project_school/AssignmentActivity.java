package com.example.project_school;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class AssignmentActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView noAssignmentText;
    private Toolbar toolbarAssignment;
    private List<Assessment> assessments = new ArrayList<>();
    private AssignmentAdapter adapter;
    private int teacherId, termId, courseId;
    private String BASE_URL, auth_token,studentId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        noAssignmentText = findViewById(R.id.no_assignment_text);
        recyclerView = findViewById(R.id.assignmentRecycler);











        toolbarAssignment = findViewById(R.id.toolbarAssignment);
        setSupportActionBar(toolbarAssignment);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }





        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AssignmentAdapter(this, assessments);
        recyclerView.setAdapter(adapter);

        studentId = getIntent().getStringExtra("ID");
        termId = getIntent().getIntExtra("term_id", -1);
        courseId = getIntent().getIntExtra("course_id", -1);

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        auth_token = sharedPreferences.getString("auth_token", "");
        BASE_URL = getString(R.string.URL);

        loadAssessments();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(AssignmentActivity.this, CourseListActivity.class);
            // يمكنك إضافة البيانات التي تريد إرجاعها
            intent.putExtra("ID", studentId);
            intent.putExtra("term_id", termId);
            intent.putExtra("course_id", courseId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // حتى لا يرجع لـ AssignmentActivity لما تضغط رجوع في CourseListActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AssignmentActivity.this, CourseListActivity.class);
        intent.putExtra("ID", studentId);
        intent.putExtra("term_id", termId);
        intent.putExtra("course_id", courseId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }



    private void loadAssessments() {
        String url = BASE_URL + "assessments/student.php?student_id="
                + studentId + "&term_id=" + termId  + "&course_id=" + courseId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");

                        // Clear any existing data before adding new items
                        assessments.clear();

                        if (data.length() == 0) {
                            noAssignmentText.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            noAssignmentText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            for (int i = 0; i < data.length(); i++) {

                                JSONObject obj = data.getJSONObject(i);
                                Assessment ass = new Assessment(obj);
                                if ((ass.score == -1) && ass.type.equals("assignment")){
                                assessments.add(ass);}
                            }
                            adapter.notifyDataSetChanged();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                    noAssignmentText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + auth_token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
