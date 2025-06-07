package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private LinearLayout marksContainer;

    // Marks components
    private Spinner classSpinner;
    private Spinner courseSpinner;

    private Button loadStudentsBtn;

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

        // Find views
        mainGrid = findViewById(R.id.main_grid);
        marksContainer = findViewById(R.id.marks_container);

        // Marks views
        classSpinner = findViewById(R.id.class_spinner);
        courseSpinner = findViewById(R.id.course_spinner);
        loadStudentsBtn = findViewById(R.id.load_students_btn);


    }

    private void setupUI() {

        // Setup click listeners
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

        // Setup main grid click listeners
        setupGridClickListeners();
    }

    private void setupGridClickListeners() {
        CardView scheduleCard = findViewById(R.id.schedule_card);
        CardView marksCard = findViewById(R.id.marks_card);
        CardView assignmentCard = findViewById(R.id.assignment_card);

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
    }

    private void toggleVisibility(LinearLayout container) {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {
            marksContainer.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
        }
    }

    private void loadClasses(ClassLoadCallback callback) {
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

                            callback.onClassesLoaded(classes, dataArray);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.onClassesLoaded(new ArrayList<>(), null);
                    }
                },
                error -> {
                    Toast.makeText(this, "No classes found", Toast.LENGTH_SHORT).show();
                    Log.e("Volley", "Error loading classes: " + error.toString());
                    callback.onClassesLoaded(new ArrayList<>(), null);
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

    // ScheduleItem class (kept for compatibility)
    public static class TeacherScheduleItem {
        private String courseName;
        private String className;
        private List<String> dayTimeList;

        public TeacherScheduleItem(String courseName, String className) {
            this.courseName = courseName;
            this.className = className;
            this.dayTimeList = new ArrayList<>();
        }

        // Add a day/time entry
        public void addDayTime(String dayOfWeek, String startTime, String endTime) {
            String timeString = dayOfWeek + ": " + startTime + " - " + endTime;
            this.dayTimeList.add(timeString);
        }

        // Getters
        public String getCourseName() { return courseName; }
        public String getClassName() { return className; }
        public List<String> getDayTimeList() { return dayTimeList; }
    }

    public interface ClassLoadCallback {
        void onClassesLoaded(List<String> classList, JSONArray rawData);
    }


}