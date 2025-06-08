package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MarkAttendance extends AppCompatActivity {

    private Spinner classSpinner, branchSpinner;
    private EditText dateEditText;
    private Button loadStudentsBtn, submitAttendanceBtn;
    private RecyclerView studentsRecyclerView;
    private RequestQueue requestQueue;
    private AttendanceAdapter attendanceAdapter;
    private List<Student> studentsList;
    private String selectedDate;
    private String authToken;
    private String BASE_URL;
    private int termID, yearID;
    private int teacherId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        initializeViews();
        setupSpinners();
        setupDatePicker();
        setupClickListeners();

        requestQueue = Volley.newRequestQueue(this);
        studentsList = new ArrayList<>();

        BASE_URL = getString(R.string.URL);
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        teacherId = sharedPreferences.getInt("user_id", 0);
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);
        authToken = sharedPreferences.getString("auth_token", "");

        // Set today's date as default
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dateEditText.setText(selectedDate);
    }

    private void initializeViews() {
        classSpinner = findViewById(R.id.classSpinner);
        branchSpinner = findViewById(R.id.branchSpinner);
        dateEditText = findViewById(R.id.dateEditText);
        loadStudentsBtn = findViewById(R.id.loadStudentsBtn);
        submitAttendanceBtn = findViewById(R.id.submitAttendanceBtn);
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);

        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSpinners() {
        // Class numbers (1-12)
        String[] classes = new String[12];
        for (int i = 0; i < 12; i++) {
            classes[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

        // Branch options
        String[] branches = {"A", "B", "C", "D"};
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branches);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchSpinner.setAdapter(branchAdapter);
    }

    private void setupDatePicker() {
        dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                        dateEditText.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        loadStudentsBtn.setOnClickListener(v -> loadStudents());
        submitAttendanceBtn.setOnClickListener(v -> submitAttendance());
    }

    private void loadStudents() {
        String classNum = classSpinner.getSelectedItem().toString();
        String branch = branchSpinner.getSelectedItem().toString();

        // API call to fetch students by class and branch
        String url = BASE_URL + "students/list.php?class_num=" + classNum + "&class_branch=" + branch;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    studentsList.clear();
                    try {
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject studentObj = data.getJSONObject(i);
                            Student student = new Student(
                                    studentObj.getInt("student_id"),
                                    studentObj.getString("name"),
                                    classNum,
                                    branch
                            );
                            studentsList.add(student);
                        }
                        setupRecyclerView();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing student data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void setupRecyclerView() {
        attendanceAdapter = new AttendanceAdapter(studentsList);
        studentsRecyclerView.setAdapter(attendanceAdapter);

        submitAttendanceBtn.setVisibility(View.VISIBLE);

    }
    private void submitAttendance() {
        if (studentsList.isEmpty()) {
            Toast.makeText(this, "Please load students first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Submit attendance for each student
        for (Student student : studentsList) {
            submitSingleAttendance(student);
        }

        Toast.makeText(this, "Attendance submitted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void submitSingleAttendance(Student student) {
        String url = getString(R.string.URL) + "attendance/mark.php";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", student.getId());
            requestBody.put("status", student.getAttendanceStatus());
            requestBody.put("remarks", student.getRemarks());
            requestBody.put("marked_by", teacherId);
            requestBody.put("date", selectedDate);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    // Handle success
                },
                error -> {
                    Toast.makeText(this, "Error submitting attendance for " + student.getName(), Toast.LENGTH_SHORT).show();
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // Student model class
    public static class Student {
        private int id;
        private String name;
        private String classNum;
        private String branch;
        private String attendanceStatus = "present";
        private String remarks = "";

        public Student(int id, String name, String classNum, String branch) {
            this.id = id;
            this.name = name;
            this.classNum = classNum;
            this.branch = branch;
        }

        // Getters and setters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getClassNum() { return classNum; }
        public String getBranch() { return branch; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }

    // RecyclerView Adapter
    private class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
        private List<Student> students;

        public AttendanceAdapter(List<Student> students) {
            this.students = students;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_student_attendance, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Student student = students.get(position);
            holder.bind(student);
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        public void markAllStudents(String status) {
            for (Student student : students) {
                student.setAttendanceStatus(status);
            }
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private android.widget.TextView nameTextView;
            private RadioGroup statusRadioGroup;
            private RadioButton presentRadio, absentRadio, lateRadio, sickRadio;
            private EditText remarksEditText;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.studentNameTextView);
                statusRadioGroup = itemView.findViewById(R.id.statusRadioGroup);
                presentRadio = itemView.findViewById(R.id.presentRadio);
                absentRadio = itemView.findViewById(R.id.absentRadio);
                lateRadio = itemView.findViewById(R.id.lateRadio);
                sickRadio = itemView.findViewById(R.id.sickRadio);
                remarksEditText = itemView.findViewById(R.id.remarksEditText);
            }

            public void bind(Student student) {
                nameTextView.setText(student.getName());

                // Set radio button based on current status
                switch (student.getAttendanceStatus()) {
                    case "present":
                        presentRadio.setChecked(true);
                        break;
                    case "absent":
                        absentRadio.setChecked(true);
                        break;
                    case "late":
                        lateRadio.setChecked(true);
                        break;
                    case "sick":
                        sickRadio.setChecked(true);
                        break;
                }

                remarksEditText.setText(student.getRemarks());

                // Set listeners
                statusRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == R.id.presentRadio) {
                        student.setAttendanceStatus("present");
                    } else if (checkedId == R.id.absentRadio) {
                        student.setAttendanceStatus("absent");
                    } else if (checkedId == R.id.lateRadio) {
                        student.setAttendanceStatus("late");
                    } else if (checkedId == R.id.sickRadio) {
                        student.setAttendanceStatus("sick");
                    }
                });

                remarksEditText.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        student.setRemarks(remarksEditText.getText().toString());
                    }
                });
            }
        }
    }
}
