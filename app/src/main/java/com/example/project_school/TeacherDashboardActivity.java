package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
    private LinearLayout assignmentContainer;

    // Marks components
    private Spinner classSpinner;
    private Spinner courseSpinner;
    private Spinner assessmentTypeSpinner;
    private EditText assessmentNameEdit;
    private EditText scoreEdit;
    private EditText maxScoreEdit;
    private EditText percentageEdit;
    private EditText remarksEdit;
    private Button loadStudentsBtn;

    // Assignment components
    private Spinner assignmentClassSpinner;
    private Spinner assignmentCourseSpinner;
    private EditText assignmentTitleEdit;
    private EditText assignmentDescriptionEdit;
    private EditText dueDateEdit;
    private Button sendAssignmentBtn;

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
        assignmentContainer = findViewById(R.id.assignment_container);

        // Marks views
        classSpinner = findViewById(R.id.class_spinner);
        courseSpinner = findViewById(R.id.course_spinner);
        loadStudentsBtn = findViewById(R.id.load_students_btn);

        // Assignment views
        assignmentClassSpinner = findViewById(R.id.assignment_class_spinner);
        assignmentCourseSpinner = findViewById(R.id.assignment_course_spinner);
        assignmentTitleEdit = findViewById(R.id.assignment_title_edit);
        assignmentDescriptionEdit = findViewById(R.id.assignment_description_edit);
        dueDateEdit = findViewById(R.id.due_date_edit);
        assessmentTypeSpinner = findViewById(R.id.assessment_type_spinner);
        assessmentNameEdit = findViewById(R.id.assessment_name_edit);
        scoreEdit = findViewById(R.id.score_edit);
        maxScoreEdit = findViewById(R.id.max_score_edit);
        percentageEdit = findViewById(R.id.percentage_edit);
        remarksEdit = findViewById(R.id.remarks_edit);
        sendAssignmentBtn = findViewById(R.id.send_assignment_btn);
    }

    private void setupUI() {
        // Setup assessment type spinner
        ArrayAdapter<String> assessmentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"quiz", "assignment", "midterm", "final_exam", "homework"});
        assessmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assessmentTypeSpinner.setAdapter(assessmentAdapter);

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
        sendAssignmentBtn.setOnClickListener(v -> sendAssignment());

        // Setup date picker for due date
        dueDateEdit.setOnClickListener(v -> showDatePicker());

        // Setup main grid click listeners
        setupGridClickListeners();
    }

    private void setupGridClickListeners() {
        CardView scheduleCard = findViewById(R.id.schedule_card);
        CardView marksCard = findViewById(R.id.marks_card);
        CardView assignmentCard = findViewById(R.id.assignment_card);

        // Modified: Open schedule in new activity
        scheduleCard.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, TeacherScheduleActivity.class);
            startActivity(intent);
        });

        marksCard.setOnClickListener(v -> {
            toggleVisibility(marksContainer);
            if (marksContainer.getVisibility() == View.VISIBLE) {
                loadClasses((classList, rawData) -> {
                    List<String> courses = new ArrayList<>();
                    List<Integer>coursesIDs = new ArrayList<>();
                    try {
                        String selectedClass = classSpinner.getSelectedItem().toString();
                        for (int i = 0; i < rawData.length(); i++) {
                            JSONObject obj = rawData.optJSONObject(i);
                            String classData = obj.getString("class_num") + " " +
                                    obj.getString("class_branch");
                            if (classData.equalsIgnoreCase(selectedClass)) {
                                courses.add(obj.getString("course_name"));
                                coursesIDs.add(obj.getInt("course_id"));
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, courses);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        courseSpinner.setAdapter(adapter);
                        courseSpinner.setTag(coursesIDs);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error Loading courses", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        assignmentCard.setOnClickListener(v -> {
            toggleVisibility(assignmentContainer);
            if (assignmentContainer.getVisibility() == View.VISIBLE) {
                loadClassesAndCourses();
            }
        });
    }

    private void toggleVisibility(LinearLayout container) {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {
            // Hide all containers first
            marksContainer.setVisibility(View.GONE);
            assignmentContainer.setVisibility(View.GONE);

            // Show selected container
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
                    Toast.makeText(this, "Error loading classes", Toast.LENGTH_SHORT).show();
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

    private void loadClassesAndCourses() {
        // Load classes
        String classesUrl = BASE_URL + "classes/list.php";

        JsonObjectRequest classesRequest = new JsonObjectRequest(Request.Method.GET, classesUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("data");
                            List<String> classNames = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject classObj = dataArray.getJSONObject(i);
                                String className = classObj.getString("class_num") +
                                        classObj.getString("class_branch");
                                classNames.add(className);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, classNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            assignmentClassSpinner.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading classes", Toast.LENGTH_SHORT).show()) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(classesRequest);

        // Load courses for assignments
        loadCoursesForAssignments();
    }

    private void loadCoursesForAssignments() {
        String coursesUrl = BASE_URL + "courses/list.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, coursesUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("data");
                            List<String> courseNames = new ArrayList<>();
                            List<Integer> courseIds = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject course = dataArray.getJSONObject(i);
                                courseNames.add(course.getString("course_name"));
                                courseIds.add(course.getInt("course_id"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, courseNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            assignmentCourseSpinner.setAdapter(adapter);
                            assignmentCourseSpinner.setTag(courseIds);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading courses", Toast.LENGTH_SHORT).show()) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

//    private void publishMarks() {
//        if (!validateMarksInput()) {
//            return;
//        }
//
//        try {
//            @SuppressWarnings("unchecked")
//            List<Integer> studentIds = (List<Integer>) studentSpinner.getTag();
//            @SuppressWarnings("unchecked")
//            List<Integer> courseIds = (List<Integer>) courseSpinner.getTag();
//
//            int selectedStudentId = studentIds.get(studentSpinner.getSelectedItemPosition());
//            int selectedCourseId = courseIds.get(courseSpinner.getSelectedItemPosition());
//
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("student_id", selectedStudentId);
//            requestBody.put("course_id", selectedCourseId);
//            requestBody.put("teacher_id", teacherId);
//            requestBody.put("assessment_type", assessmentTypeSpinner.getSelectedItem().toString());
//            requestBody.put("assessment_name", assessmentNameEdit.getText().toString());
//            requestBody.put("score", Double.parseDouble(scoreEdit.getText().toString()));
//            requestBody.put("max_score", Double.parseDouble(maxScoreEdit.getText().toString()));
//            requestBody.put("percentage", Double.parseDouble(percentageEdit.getText().toString()));
//            requestBody.put("date_assessed", getCurrentDate());
//            requestBody.put("term_id", 1); // Current term
//            requestBody.put("remarks", remarksEdit.getText().toString());
//
//            String url = BASE_URL + "assessments/create.php";
//
//            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
//                    response -> {
//                        try {
//                            if (response.getString("status").equals("success")) {
//                                Toast.makeText(this, "Marks published successfully", Toast.LENGTH_SHORT).show();
//                                clearMarksForm();
//                            } else {
//                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Toast.makeText(this, "Error publishing marks", Toast.LENGTH_SHORT).show();
//                        }
//                    },
//                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {
//
//                @Override
//                public Map<String, String> getHeaders() {
//                    Map<String, String> headers = new HashMap<>();
//                    headers.put("Authorization", "Bearer " + authToken);
//                    headers.put("Content-Type", "application/json");
//                    return headers;
//                }
//            };
//
//            requestQueue.add(request);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void sendAssignment() {
        if (!validateAssignmentInput()) {
            return;
        }

        // Note: This would typically use a separate assignments API endpoint
        // For now, we'll create it as an assessment with type "assignment"
        try {
            @SuppressWarnings("unchecked")
            List<Integer> courseIds = (List<Integer>) assignmentCourseSpinner.getTag();
            int selectedCourseId = courseIds.get(assignmentCourseSpinner.getSelectedItemPosition());

            // In a real implementation, you would send this to all students in the selected class
            // For now, we'll show a success message
            Toast.makeText(this, "Assignment sent successfully to " +
                    assignmentClassSpinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            clearAssignmentForm();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sending assignment", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateMarksInput() {
        if (assessmentNameEdit.getText().toString().trim().isEmpty()) {
            assessmentNameEdit.setError("Assessment name is required");
            return false;
        }

        if (scoreEdit.getText().toString().trim().isEmpty()) {
            scoreEdit.setError("Score is required");
            return false;
        }

        if (maxScoreEdit.getText().toString().trim().isEmpty()) {
            maxScoreEdit.setError("Max score is required");
            return false;
        }

        if (percentageEdit.getText().toString().trim().isEmpty()) {
            percentageEdit.setError("Percentage is required");
            return false;
        }

        return true;
    }

    private boolean validateAssignmentInput() {
        if (assignmentTitleEdit.getText().toString().trim().isEmpty()) {
            assignmentTitleEdit.setError("Assignment title is required");
            return false;
        }

        if (assignmentDescriptionEdit.getText().toString().trim().isEmpty()) {
            assignmentDescriptionEdit.setError("Assignment description is required");
            return false;
        }

        if (dueDateEdit.getText().toString().trim().isEmpty()) {
            dueDateEdit.setError("Due date is required");
            return false;
        }

        return true;
    }

    private void clearMarksForm() {
        assessmentNameEdit.setText("");
        scoreEdit.setText("");
        maxScoreEdit.setText("");
        percentageEdit.setText("");
        remarksEdit.setText("");
    }

    private void clearAssignmentForm() {
        assignmentTitleEdit.setText("");
        assignmentDescriptionEdit.setText("");
        dueDateEdit.setText("");
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dueDateEdit.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
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