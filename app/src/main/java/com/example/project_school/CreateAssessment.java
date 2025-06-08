package com.example.project_school;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    private EditText scoreEdit, maxScoreEdit, percentageEdit, remarksEdit;
    private Spinner assignmentClassSpinner, assignmentCourseSpinner;
    private EditText assignmentTitleEdit, dueDateEdit;
    private Button sendAssignmentBtn;

    private String BASE_URL;
    private RequestQueue requestQueue;
    private String authToken;
    private int teacherId, termID, yearID;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_assessment);

        assignmentClassSpinner = findViewById(R.id.assignment_class_spinner);
        assignmentCourseSpinner = findViewById(R.id.assignment_course_spinner);
        assignmentTitleEdit = findViewById(R.id.assignment_title_edit);
        dueDateEdit = findViewById(R.id.due_date_edit);
        assessmentTypeSpinner = findViewById(R.id.assessment_type_spinner);
        scoreEdit = findViewById(R.id.score_edit);
        maxScoreEdit = findViewById(R.id.max_score_edit);
        percentageEdit = findViewById(R.id.percentage_edit);
        remarksEdit = findViewById(R.id.remarks_edit);
        sendAssignmentBtn = findViewById(R.id.send_assignment_btn);

        BASE_URL = getString(R.string.URL);
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        authToken = sharedPreferences.getString("auth_token", "");
        teacherId = sharedPreferences.getInt("user_id", 0);
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);

        ArrayAdapter<String> assessmentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"quiz", "assignment", "midterm", "final_exam", "homework"});
        assessmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assessmentTypeSpinner.setAdapter(assessmentAdapter);

        dueDateEdit.setOnClickListener(v -> showDatePicker());

        loadClasses((classList, rawData) -> {
            ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, classList);
            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            assignmentClassSpinner.setAdapter(classAdapter);

            assignmentClassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

                        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(CreateAssessment.this,
                                android.R.layout.simple_spinner_item, courses);
                        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        assignmentCourseSpinner.setAdapter(courseAdapter);
                        assignmentCourseSpinner.setTag(courseIDs);
                    } catch (JSONException e) {
                        Log.e("CourseLoad", "JSONException: " + e.getMessage());
                        Toast.makeText(CreateAssessment.this, "Error loading courses", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });

        sendAssignmentBtn.setOnClickListener(v -> sendAssignment());
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

    private boolean validateAssignmentInput() {
        if (assignmentTitleEdit.getText().toString().trim().isEmpty()) {
            assignmentTitleEdit.setError("Assignment title is required");
            return false;
        }
        if (dueDateEdit.getText().toString().trim().isEmpty()) {
            dueDateEdit.setError("Due date is required");
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

    private void clearAssignmentForm() {
        assignmentTitleEdit.setText("");
        dueDateEdit.setText("");
    }

    private void sendAssignment() {
        if (!validateAssignmentInput()) return;

        List<Integer> courseIds = (List<Integer>) assignmentCourseSpinner.getTag();
        if (courseIds == null || courseIds.isEmpty()) {
            Toast.makeText(this, "Please select a valid course", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedCourseId = courseIds.get(assignmentCourseSpinner.getSelectedItemPosition());
        String classData = assignmentClassSpinner.getSelectedItem().toString();
        String[] classParts = classData.split(" ");
        String classNum = classParts[0];
        String classBranch = classParts[1];
        String title = assignmentTitleEdit.getText().toString();
        String dueDate = dueDateEdit.getText().toString();
        String assessmentType = assessmentTypeSpinner.getSelectedItem().toString();
        String score = scoreEdit.getText().toString().trim().isEmpty() ? "-1" : scoreEdit.getText().toString();
        String maxScore = maxScoreEdit.getText().toString();
        String percentage = percentageEdit.getText().toString();
        String remarks = remarksEdit.getText().toString();

        progressDialog = ProgressDialog.show(this, "Sending", "Creating assessment...", true, false);

        getStudents(classNum, classBranch, (dataList, rawData) -> {
            if (rawData == null || rawData.length() == 0) {
                progressDialog.dismiss();
                Toast.makeText(this, "No students found", Toast.LENGTH_SHORT).show();
                return;
            }

            final int[] successCount = {0};
            final int[] errorCount = {0};
            int totalStudents = rawData.length();

            String url = BASE_URL + "assessments/create.php";

            for (int i = 0; i < rawData.length(); i++) {
                try {
                    JSONObject studentObj = rawData.getJSONObject(i);
                    int studentId = studentObj.getInt("student_id");

                    JSONObject assessmentObj = new JSONObject();
                    assessmentObj.put("student_id", studentId);
                    assessmentObj.put("course_id", selectedCourseId);
                    assessmentObj.put("teacher_id", teacherId);
                    assessmentObj.put("assessment_type", assessmentType);
                    assessmentObj.put("assessment_name", title);
                    assessmentObj.put("score", score);
                    assessmentObj.put("max_score", maxScore);
                    assessmentObj.put("percentage", percentage);
                    assessmentObj.put("date_assessed", dueDate);
                    assessmentObj.put("term_id", termID);
                    assessmentObj.put("remarks", remarks);

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, assessmentObj,
                            response -> {
                                successCount[0]++;
                                if (successCount[0] + errorCount[0] == totalStudents) {
                                    progressDialog.dismiss();
                                    Toast.makeText(CreateAssessment.this,
                                            "Success: " + successCount[0] + ", Failed: " + errorCount[0],
                                            Toast.LENGTH_LONG).show();
                                    clearAssignmentForm();
                                }
                            },
                            error -> {
                                errorCount[0]++;
                                Log.e("AssessmentSend", "Error: " + error.toString());
                                if (successCount[0] + errorCount[0] == totalStudents) {
                                    progressDialog.dismiss();
                                    Toast.makeText(CreateAssessment.this,
                                            "Success: " + successCount[0] + ", Failed: " + errorCount[0],
                                            Toast.LENGTH_LONG).show();
                                }
                            }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "Bearer " + authToken);
                            return headers;
                        }
                    };

                    requestQueue.add(request);

                } catch (JSONException e) {
                    Log.e("CreateAssessment", "JSON error: " + e.getMessage());
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void getStudents(String classNum, String classBranch, TeacherDashboardActivity.DataLoadCallback callback) {
        String url = BASE_URL + "students/list.php?class_num=" + classNum + "&class_branch=" + classBranch;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            callback.onDataLoaded(new ArrayList<>(), data);
                        } else {
                            callback.onDataLoaded(new ArrayList<>(), null);
                        }
                    } catch (JSONException e) {
                        Log.e("getStudents", "Parse error: " + e.getMessage());
                        callback.onDataLoaded(new ArrayList<>(), null);
                    }
                },
                error -> {
                    Log.e("getStudents", "Volley error: " + error.toString());
                    callback.onDataLoaded(new ArrayList<>(), null);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void loadClasses(TeacherDashboardActivity.DataLoadCallback callback) {
        String url = BASE_URL + "teachers/listClasses.php?teacher_id=" + teacherId + "&term_id=" + termID + "&year_id=" + yearID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            List<String> classList = new ArrayList<>();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                classList.add(obj.getString("class_num") + " " + obj.getString("class_branch"));
                            }
                            callback.onDataLoaded(classList, data);
                        }
                    } catch (JSONException e) {
                        Log.e("loadClasses", "JSONException: " + e.getMessage());
                        callback.onDataLoaded(new ArrayList<>(), null);
                    }
                },
                error -> {
                    Log.e("loadClasses", "Volley error: " + error.toString());
                    callback.onDataLoaded(new ArrayList<>(), null);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }
}