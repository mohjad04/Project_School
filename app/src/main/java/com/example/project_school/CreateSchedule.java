package com.example.project_school;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class CreateSchedule extends AppCompatActivity {

    private Spinner classSpinner, branchSpinner, courseSpinner, teacherSpinner, daySpinner, startSpinner;
    private Button addButton, saveButton;
    private TableLayout scheduleTable;
    private RequestQueue queue;
    private ArrayList<String> classList = new ArrayList<>();
    private ArrayList<String> branchList = new ArrayList<>();
    private ArrayAdapter<String> branchAdapter;

    private final List<ScheduleItem> scheduleList = new ArrayList<>();

    private final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_schedule);

        classSpinner = findViewById(R.id.classSpinner);
        branchSpinner = findViewById(R.id.branchSpinner);
        addButton = findViewById(R.id.addButton);
        saveButton = findViewById(R.id.saveButton);
        scheduleTable = findViewById(R.id.scheduleTable);

        queue = Volley.newRequestQueue(this);

        for (int i = 1; i <= 12; i++) {
            classList.add("Class " + i);
        }
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchBranchesFromApi(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        addButton.setOnClickListener(v -> showAddCourseDialog());

        saveButton.setOnClickListener(v -> saveSchedule());


        drawScheduleTable();
    }

    private void fetchBranchesFromApi(int classNum) {
        String url = getString(R.string.URL) + "classes/list.php?class_num=" + classNum;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        branchList.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            branchList.add(item.getString("class_branch"));
                        }
                        branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branchList);
                        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        branchSpinner.setAdapter(branchAdapter);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse branches", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching branches", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void drawScheduleTable() {
        scheduleTable.removeAllViews();

        String[][] timeSlots = {
                {"08:00", "08:40"},
                {"08:45", "09:25"},
                {"09:30", "10:10"},
                {"10:30", "11:10"},
                {"11:15", "11:55"},
                {"12:00", "12:40"},
                {"12:45", "13:25"}
        };

        TableRow header = new TableRow(this);
        header.addView(createCell("Time/Day", true));
        for (String day : days) {
            header.addView(createCell(day, true));
        }
        scheduleTable.addView(header);

        for (String[] slot : timeSlots) {
            String timeRange = slot[0] + " - " + slot[1];
            TableRow row = new TableRow(this);
            row.addView(createCell(timeRange, true));

            for (String day : days) {
                ScheduleItem matched = null;
                for (ScheduleItem item : scheduleList) {
                    if (item.dayOfWeek.equalsIgnoreCase(day) && item.startTime.equals(timeRange)) {
                        matched = item;
                        break;
                    }
                }

                TextView cell = createCell(matched != null ? matched.courseName : "", false);
                if (matched != null) {
                    cell.setBackgroundColor(Color.parseColor("#59bfe5"));
                }
                row.addView(cell);
            }
            scheduleTable.addView(row);
        }
    }

    private TextView createCell(String text, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(4, 16, 4, 16);
        tv.setTextSize(8);
        tv.setBackgroundResource(android.R.drawable.editbox_background);
        tv.setWidth(200);
        tv.setHeight(130);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void fetchAvailableTeachers(String subject, String day, String startTime, String endTime, Spinner teacherSpinner) {
        String url = getString(R.string.URL) + "teachers/list.php?busy=true"
                + "&start_time=" + startTime + ":00"
                + "&end_time=" + endTime + ":00"
                + "&day_of_week=" + day.toLowerCase()
                + "&subject_specialization=" + subject;


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        List<String> availableTeachers = new ArrayList<>();
                        List<Integer> teachersId = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject teacher = data.getJSONObject(i);
                            String teacherName = teacher.getString("name");
                            int teacherid = teacher.getInt("teacher_id");
                            availableTeachers.add(teacherName);
                            teachersId.add(teacherid);

                        }

                        if (availableTeachers.isEmpty()) {
                            availableTeachers.add("No available teachers");
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, availableTeachers);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        teacherSpinner.setAdapter(adapter);
                        teacherSpinner.setTag(teachersId);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse teacher data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching teachers", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void showAddCourseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_course, null);

        courseSpinner = dialogView.findViewById(R.id.courseSpinner);
        teacherSpinner = dialogView.findViewById(R.id.teacherSpinner);
        daySpinner = dialogView.findViewById(R.id.daySpinner);
        startSpinner = dialogView.findViewById(R.id.startPeriodSpinner);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(days));
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        List<String> periods = Arrays.asList("08:00 - 08:40", "08:45 - 09:25", "09:30 - 10:10", "10:30 - 11:10", "11:15 - 11:55", "12:00 - 12:40", "12:45 - 13:25");
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startSpinner.setAdapter(periodAdapter);

        int selectedClass = classSpinner.getSelectedItemPosition() + 1;
        String selectedBranch = (String) branchSpinner.getSelectedItem();
        String url = getString(R.string.URL) + "courses/list.php?grade_level=" + selectedClass;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Add Course");

        builder.setPositiveButton("Add", (dialog, which) -> {
            String selectedCourse = (String) courseSpinner.getSelectedItem();
            String selectedTeacher = (String) teacherSpinner.getSelectedItem();
            String selectedDay = (String) daySpinner.getSelectedItem();
            String selectedPeriod = (String) startSpinner.getSelectedItem();

            List<Integer> courseIds = (List<Integer>) courseSpinner.getTag();

            if (courseIds == null || courseIds.isEmpty()) {
                Toast.makeText(this, "Please select a valid course", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Integer> teachersids = (List<Integer>) teacherSpinner.getTag();

            if (teachersids == null || teachersids.isEmpty()) {
                Toast.makeText(this, "Please select a valid course", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCourse != null && selectedDay != null && selectedPeriod != null && selectedTeacher != null) {
                ScheduleItem newItem = new ScheduleItem();
                newItem.courseName = selectedCourse;
                newItem.dayOfWeek = selectedDay;
                newItem.startTime = selectedPeriod;
                newItem.teacherName = selectedTeacher;
                newItem.courseId = courseIds.get(courseSpinner.getSelectedItemPosition());
                newItem.teacherId = teachersids.get(teacherSpinner.getSelectedItemPosition());


                scheduleList.add(newItem);
                drawScheduleTable();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.show();

        // Fetch courses
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        List<String> filteredCourses = new ArrayList<>();
                        List<Integer> coursesid = new ArrayList<>();
                        Map<String, String> courseMap = new HashMap<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String name = item.getString("course_name");
                            String desc = item.getString("description");
                            int courseId = item.getInt("course_id");

                            if (selectedClass <= 10 ||
                                    ("A".equalsIgnoreCase(selectedBranch) && desc.equalsIgnoreCase("scientific")) ||
                                    ("B".equalsIgnoreCase(selectedBranch) && desc.equalsIgnoreCase("literary"))) {
                                filteredCourses.add(name);
                                courseMap.put(name, desc);
                                coursesid.add(courseId);

                            }
                        }

                        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredCourses);
                        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        courseSpinner.setAdapter(courseAdapter);
                        courseSpinner.setTag(coursesid);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse courses", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching courses", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        queue.add(request);

        // Fetch teachers when course, day, or period changes
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = (String) courseSpinner.getSelectedItem();
                String selectedDay = (String) daySpinner.getSelectedItem();
                String selectedPeriod = (String) startSpinner.getSelectedItem();

                if (selectedCourse != null && selectedDay != null && selectedPeriod != null) {
                    String[] times = selectedPeriod.split(" - ");
                    fetchAvailableTeachers(selectedCourse, selectedDay, times[0].trim(), times[1].trim(), teacherSpinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        courseSpinner.setOnItemSelectedListener(listener);
        daySpinner.setOnItemSelectedListener(listener);
        startSpinner.setOnItemSelectedListener(listener);
    }

    private void saveSchedule() {
        int[] dailyLimits = {7, 7, 7, 7, 6}; // Sunday–Thursday
        int[] dailyCount = new int[5];

        for (ScheduleItem item : scheduleList) {
            for (int i = 0; i < days.length; i++) {
                if (item.dayOfWeek.equalsIgnoreCase(days[i])) {
                    dailyCount[i]++;
                }
            }
        }

        for (int i = 0; i < dailyCount.length; i++) {
            if (dailyCount[i] < dailyLimits[i]) {
                Toast.makeText(this, "Schedule not complete!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Set<String> assignedCourses = new HashSet<>();

        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        for (ScheduleItem item : scheduleList) {
            String[] times = item.startTime.split(" - ");
            JSONObject body = new JSONObject();
            try {
                int classNum = classSpinner.getSelectedItemPosition() + 1;
                String branch = branchSpinner.getSelectedItem().toString();
                body.put("class_num", classNum);
                body.put("class_branch", branch);
                body.put("course_id", item.courseId);
                body.put("teacher_id", item.teacherId);
                body.put("day_of_week", item.dayOfWeek);
                body.put("start_time", times[0] + ":00");
                body.put("end_time", times[1] + ":00");
                body.put("term_id", 2);
            } catch (JSONException e) {
                continue;
            }

            String scheduleUrl = getString(R.string.URL) + "schedule/create.php";
            JsonObjectRequest scheduleRequest = new JsonObjectRequest(Request.Method.POST, scheduleUrl, body,
                    response -> {},
                    error -> {
                        Log.d("error:",error.toString());
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            scheduleRequest.setShouldCache(false);
            queue.add(scheduleRequest);

            assignTeacherIfNeeded(item, assignedCourses, token);
        }

        Toast.makeText(this, "Schedule saved successfully!", Toast.LENGTH_SHORT).show();
    }



    private void assignTeacherIfNeeded(ScheduleItem item, Set<String> assignedCourses, String token) {
        String key = item.courseId + "-" + item.teacherId;
        if (assignedCourses.contains(key)) return;

        assignedCourses.add(key);

        JSONObject assignBody = new JSONObject();
        try {
            assignBody.put("teacher_id", item.teacherId);
            assignBody.put("course_id", item.courseId);
            assignBody.put("class_num", classSpinner.getSelectedItemPosition() + 1);
            assignBody.put("class_branch", branchSpinner.getSelectedItem().toString());
            assignBody.put("year_id", 1); // update if needed
            assignBody.put("term_id", 2); // update if needed
        } catch (JSONException e) {
            return;
        }

        String assignUrl = getString(R.string.URL) + "teachers/assign.php";
        JsonObjectRequest assignRequest = new JsonObjectRequest(Request.Method.POST, assignUrl, assignBody,
                response -> {},
                error -> {}) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        assignRequest.setShouldCache(false);
        queue.add(assignRequest);
    }


}