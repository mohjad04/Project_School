package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

public class TeacherDashboardActivity extends AppCompatActivity {

    private String BASE_URL;

    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private int teacherId;
    private String authToken;
    private int termID, yearID;

    // UI Components
    private GridLayout mainGrid;
    private LinearLayout marksContainer, reportsContainer;

    // Marks components
    private Spinner classSpinner;
    private Spinner courseSpinner;

    private Button loadStudentsBtn, markAttendanceBtn, reportAttendanceBtn;
    private ImageView logoutButton, profilebtn;
    private TextView nametxt, typetxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);
        BASE_URL = getString(R.string.URL);
        initializeComponents();
        setupUI();
    }

    private void initializeComponents() {
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("user_id", 0);
        authToken = sharedPreferences.getString("auth_token", "");
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);

        mainGrid = findViewById(R.id.main_grid);
        marksContainer = findViewById(R.id.marks_container);
        reportsContainer = findViewById(R.id.reports_container);

        classSpinner = findViewById(R.id.class_spinner);
        courseSpinner = findViewById(R.id.course_spinner);
        loadStudentsBtn = findViewById(R.id.load_students_btn);

        markAttendanceBtn = findViewById(R.id.mark_attendance);
        reportAttendanceBtn = findViewById(R.id.report_attendance);

        logoutButton = findViewById(R.id.logoutbtn);
        profilebtn = findViewById(R.id.profilebtn);

        nametxt = findViewById(R.id.nametxt);
        typetxt = findViewById(R.id.typetxt);
    }

    private void setupUI() {

        loadStudentsBtn.setOnClickListener(v -> {
            String selectedClass = classSpinner.getSelectedItem().toString();
            String selectedCourse = courseSpinner.getSelectedItem().toString();
            List<Integer> courseIDs = (List<Integer>) courseSpinner.getTag();
            int selectedCourseId = courseIDs.get(courseSpinner.getSelectedItemPosition());
            Intent intent = new Intent(TeacherDashboardActivity.this, StudentsForMarks.class);
            intent.putExtra("class", selectedClass);
            intent.putExtra("course", selectedCourse);
            intent.putExtra("course_id", selectedCourseId);
            startActivity(intent);
        });

        markAttendanceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, MarkAttendance.class);
            startActivity(intent);
        });

        reportAttendanceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, ReportAttendance.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            // Clear shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("auth_token");
            editor.remove("user_id");
            editor.remove("role");
            editor.remove("name");
            editor.remove("email");
            editor.remove("password");
            editor.remove("phone");
            editor.remove("date_of_birth");
            editor.remove("user_image");
            editor.remove("current_term");
            editor.remove("current_year");
            editor.apply();

            // Redirect to LoginActivity
            Intent intent = new Intent(TeacherDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);

            Toast.makeText(TeacherDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        profilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, Profile.class); // Change to your profile activity
            startActivity(intent);
        });

        nametxt.setText(sharedPreferences.getString("name", ""));
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BASE_URL + "classes/list.php?teacher_id=" + teacherId, null,
                response -> {
                JSONArray dataArray = response.optJSONArray("data");
                String type= dataArray.optJSONObject(0).optString("class_num") + " " + dataArray.optJSONObject(0).optString("class_branch");
                typetxt.setText("Teacher for: " + type);
                },
                error -> {
                    Toast.makeText(TeacherDashboardActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };
        requestQueue.add(request);

        setupGridClickListeners();
    }

    private void setupGridClickListeners() {
        CardView scheduleCard = findViewById(R.id.schedule_card);
        CardView marksCard = findViewById(R.id.marks_card);
        CardView assignmentCard = findViewById(R.id.assignment_card);
        CardView reportsCard = findViewById(R.id.reports_card);

        scheduleCard.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, TeacherScheduleActivity.class);
            startActivity(intent);
        });

        marksCard.setOnClickListener(v -> {
            toggleVisibility(marksContainer);
            if (marksContainer.getVisibility() == View.VISIBLE) {
                loadClasses((classList, rawData) -> {
                    ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, classList);
                    classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    classSpinner.setAdapter(classAdapter);

                    classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedClass = parent.getItemAtPosition(position).toString();
                            List<String> courses = new ArrayList<>();
                            List<Integer> courseIDs = new ArrayList<>();

                            try {
                                for (int i = 0; i < rawData.length(); i++) {
                                    JSONObject obj = rawData.optJSONObject(i);
                                    String classData = obj.getString("class_num") + " " + obj.getString("class_branch");
                                    if (classData.equalsIgnoreCase(selectedClass)) {
                                        courses.add(obj.getString("course_name"));
                                        courseIDs.add(obj.getInt("course_id"));
                                    }
                                }

                                ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(TeacherDashboardActivity.this,
                                        android.R.layout.simple_spinner_item, courses);
                                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                courseSpinner.setAdapter(courseAdapter);
                                courseSpinner.setTag(courseIDs);
                            } catch (JSONException e) {
                                Toast.makeText(TeacherDashboardActivity.this, "Error loading courses", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                });
            }
        });


        assignmentCard.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, CreateAssessment.class);
            startActivity(intent);
        });

        reportsCard.setOnClickListener(v -> {
            toggleVisibility(reportsContainer);
        });
    }

    private void toggleVisibility(LinearLayout container) {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {
            marksContainer.setVisibility(View.GONE);
            reportsContainer.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
        }
    }

    private void loadClasses(DataLoadCallback callback) {
        String classesUrl = BASE_URL + "teachers/listClasses.php?teacher_id=" + teacherId +
                "&term_id=" + termID + "&year_id=" + yearID;

        JsonObjectRequest classesRequest = new JsonObjectRequest(Request.Method.GET, classesUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("data");
                            List<String> classes = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject classObj = dataArray.getJSONObject(i);
                                String classData = classObj.getString("class_num") + " " +
                                        classObj.getString("class_branch");
                                classes.add(classData);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, classes);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            classSpinner.setAdapter(adapter);

                            callback.onDataLoaded(classes, dataArray);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onDataLoaded(new ArrayList<>(), null);
                    }
                },
                error -> {
                    Toast.makeText(this, "No classes found", Toast.LENGTH_SHORT).show();
                    Log.e("Volley", "Error loading classes: " + error.toString());
                    callback.onDataLoaded(new ArrayList<>(), null);
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(classesRequest);
    }

    public static class TeacherScheduleItem {
        private String courseName;
        private String className;
        private List<String> dayTimeList;

        public TeacherScheduleItem(String courseName, String className) {
            this.courseName = courseName;
            this.className = className;
            this.dayTimeList = new ArrayList<>();
        }

        public void addDayTime(String dayOfWeek, String startTime, String endTime) {
            String timeString = dayOfWeek + ": " + startTime + " - " + endTime;
            this.dayTimeList.add(timeString);
        }

        // Getters
        public String getCourseName() { return courseName; }
        public String getClassName() { return className; }
        public List<String> getDayTimeList() { return dayTimeList; }
    }

    public interface DataLoadCallback {
        void onDataLoaded(List<String> dataList, JSONArray rawData);
    }


}