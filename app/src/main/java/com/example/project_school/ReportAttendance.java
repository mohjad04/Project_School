package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportAttendance extends AppCompatActivity {

    private Spinner classSpinner, statusSpinner;
    private EditText startDateEditText, endDateEditText;
    private Button generateReportBtn;
    private RecyclerView attendanceReportRecyclerView;
    private RequestQueue requestQueue;
    private AttendanceReportAdapter reportAdapter;
    private List<AttendanceRecord> attendanceRecords;
    private String selectedStartDate, selectedEndDate;
    private String BASE_URL;
    private String authToken;
    private int teacherId;
    private int termID, yearID;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_attendance);

        requestQueue = Volley.newRequestQueue(this);
        attendanceRecords = new ArrayList<>();

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        authToken = sharedPreferences.getString("auth_token", "");
        BASE_URL = getString(R.string.URL);
        teacherId = sharedPreferences.getInt("user_id", 0);
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);

        initializeViews();
        setupSpinners();
        setupDatePickers();
        setupClickListeners();


        // Set default date range (last 30 days)
        Calendar calendar = Calendar.getInstance();
        selectedEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        endDateEditText.setText(selectedEndDate);

        calendar.add(Calendar.DAY_OF_MONTH, -30);
        selectedStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        startDateEditText.setText(selectedStartDate);
    }

    private void initializeViews() {
        classSpinner = findViewById(R.id.classSpinner);
        statusSpinner = findViewById(R.id.statusSpinner);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        generateReportBtn = findViewById(R.id.generateReportBtn);
        attendanceReportRecyclerView = findViewById(R.id.attendanceReportRecyclerView);

        attendanceReportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadClasses(TeacherDashboardActivity.DataLoadCallback callback) {
        String classesUrl = BASE_URL + "teachers/listClasses.php?teacher_id=" + teacherId +
                "&term_id=" + termID + "&year_id=" + yearID;

        JsonObjectRequest classesRequest = new JsonObjectRequest(Request.Method.GET, classesUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("data");
                            List<String> classes = new ArrayList<>();
                            classes.add("All Classes");

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

    private void setupSpinners() {
        loadClasses((a, b) ->{});
        // Status options
        String[] statuses = {"All Status", "present", "absent", "late", "sick"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
    }

    private void setupDatePickers() {
        startDateEditText.setOnClickListener(v -> showDatePicker(true));
        endDateEditText.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                    if (isStartDate) {
                        selectedStartDate = selectedDate;
                        startDateEditText.setText(selectedDate);
                    } else {
                        selectedEndDate = selectedDate;
                        endDateEditText.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupClickListeners() {
        generateReportBtn.setOnClickListener(v -> generateReport());
    }

    private void generateReport() {
        String url = buildApiUrl();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    attendanceRecords.clear();
                    try {
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject recordObj = data.getJSONObject(i);
                            AttendanceRecord record = new AttendanceRecord(
                                    recordObj.getInt("user_id"),
                                    recordObj.getString("name"),
                                    recordObj.getString("status"),
                                    recordObj.getString("date"),
                                    recordObj.optString("remarks", ""),
                                    recordObj.getString("marked_by"),
                                    recordObj.getString("marked_at")
                            );
                            attendanceRecords.add(record);
                        }
                        setupReportRecyclerView();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing attendance data", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading attendance report", Toast.LENGTH_SHORT).show()
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

    private String buildApiUrl() {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL + "attendance/report.php?");

        // Add class filter
        String selectedClass = classSpinner.getSelectedItem().toString();
        if (!selectedClass.equals("All Classes")) {
            urlBuilder.append("class_num=").append(selectedClass.split(" ")[0]).append("&");
            urlBuilder.append("class_branch=").append(selectedClass.split(" ")[1]).append("&");
        }

        // Add status filter
        String selectedStatus = statusSpinner.getSelectedItem().toString();
        if (!selectedStatus.equals("All Status")) {
            urlBuilder.append("status=").append(selectedStatus).append("&");
        }

        // Add date range
        if (selectedStartDate != null && !selectedStartDate.isEmpty()) {
            urlBuilder.append("start_date=").append(selectedStartDate).append("&");
        }
        if (selectedEndDate != null && !selectedEndDate.isEmpty()) {
            urlBuilder.append("end_date=").append(selectedEndDate).append("&");
        }

        // Remove trailing '&' if present
        String url = urlBuilder.toString();
        if (url.endsWith("&")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    private void setupReportRecyclerView() {
        reportAdapter = new AttendanceReportAdapter(attendanceRecords);
        attendanceReportRecyclerView.setAdapter(reportAdapter);

        showAttendanceSummary();
    }

    private void showAttendanceSummary() {
        int totalRecords = attendanceRecords.size();
        int presentCount = 0, absentCount = 0, lateCount = 0, sickCount = 0;

        for (AttendanceRecord record : attendanceRecords) {
            switch (record.getStatus()) {
                case "present":
                    presentCount++;
                    break;
                case "absent":
                    absentCount++;
                    break;
                case "late":
                    lateCount++;
                    break;
                case "sick":
                    sickCount++;
                    break;
            }
        }

        String summary = String.format(Locale.getDefault(),
                "Total: %d | Present: %d | Absent: %d | Late: %d | Sick: %d",
                totalRecords, presentCount, absentCount, lateCount, sickCount);

        Toast.makeText(this, summary, Toast.LENGTH_LONG).show();
    }

    public static class AttendanceRecord {
        private int userId;
        private String studentName;
        private String status;
        private String date;
        private String remarks;
        private String markedByName;
        private String createdAt;

        public AttendanceRecord(int userId, String studentName, String status, String date,
                                String remarks, String markedByName, String createdAt) {
            this.userId = userId;
            this.studentName = studentName;
            this.status = status;
            this.date = date;
            this.remarks = remarks;
            this.markedByName = markedByName;
            this.createdAt = createdAt;
        }

        public int getUserId() { return userId; }
        public String getStudentName() { return studentName; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public String getRemarks() { return remarks; }
        public String getMarkedByName() { return markedByName; }
        public String getCreatedAt() { return createdAt; }
    }

    private class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.ViewHolder> {
        private List<AttendanceRecord> records;

        public AttendanceReportAdapter(List<AttendanceRecord> records) {
            this.records = records;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_attendance_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AttendanceRecord record = records.get(position);
            holder.bind(record);
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private android.widget.TextView studentNameTextView;
            private android.widget.TextView statusTextView, dateTextView, remarksTextView, markedByTextView;
            private View statusIndicator;

            public ViewHolder(View itemView) {
                super(itemView);
                studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                remarksTextView = itemView.findViewById(R.id.remarksTextView);
                markedByTextView = itemView.findViewById(R.id.markedByTextView);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
            }

            public void bind(AttendanceRecord record) {
                studentNameTextView.setText(record.getStudentName());
                statusTextView.setText(record.getStatus().toUpperCase());
                dateTextView.setText(record.getDate());
                remarksTextView.setText(record.getRemarks().isEmpty() ? "No remarks" : record.getRemarks());
                markedByTextView.setText("Marked by: " + record.getMarkedByName());

                int statusColor;
                switch (record.getStatus()) {
                    case "present":
                        statusColor = getResources().getColor(android.R.color.holo_green_dark);
                        break;
                    case "absent":
                        statusColor = getResources().getColor(android.R.color.holo_red_dark);
                        break;
                    case "late":
                        statusColor = getResources().getColor(android.R.color.holo_orange_dark);
                        break;
                    case "sick":
                        statusColor = getResources().getColor(android.R.color.holo_purple);
                        break;
                    default:
                        statusColor = getResources().getColor(android.R.color.darker_gray);
                        break;
                }

                statusTextView.setTextColor(statusColor);
                statusIndicator.setBackgroundColor(statusColor);
            }
        }
    }
}
