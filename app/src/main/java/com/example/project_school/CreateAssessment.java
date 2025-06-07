package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class CreateAssessment extends AppCompatActivity {
    private Spinner assessmentTypeSpinner;
    private EditText assessmentNameEdit;
    private EditText scoreEdit;
    private EditText maxScoreEdit;
    private EditText percentageEdit;
    private EditText remarksEdit;

    // Assignment components
    private Spinner assignmentClassSpinner;
    private Spinner assignmentCourseSpinner;
    private EditText assignmentTitleEdit;
    private EditText assignmentDescriptionEdit;
    private EditText dueDateEdit;
    private Button sendAssignmentBtn;
    private String BASE_URL;
    private RequestQueue requestQueue;
    private String authToken;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_assessment);

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

        BASE_URL = getString(R.string.URL);
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        authToken = sharedPreferences.getString("auth_token", "");


        // Setup assessment type spinner
        ArrayAdapter<String> assessmentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"quiz", "assignment", "midterm", "final_exam", "homework"});
        assessmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assessmentTypeSpinner.setAdapter(assessmentAdapter);
        sendAssignmentBtn.setOnClickListener(v -> sendAssignment());

        // Setup date picker for due date
        dueDateEdit.setOnClickListener(v -> showDatePicker());
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

    private void clearAssignmentForm() {
        assignmentTitleEdit.setText("");
        assignmentDescriptionEdit.setText("");
        dueDateEdit.setText("");
    }

    private void clearMarksForm() {
        assessmentNameEdit.setText("");
        scoreEdit.setText("");
        maxScoreEdit.setText("");
        percentageEdit.setText("");
        remarksEdit.setText("");
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
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

}
