package com.example.project_school;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

    private Spinner classSpinner, branchSpinner;
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

        saveButton.setOnClickListener(v -> {
            Toast.makeText(this, "Schedule saved", Toast.LENGTH_SHORT).show();
        });

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
                + "&start_time=" + startTime
                + "&end_time=" + endTime
                + "&day_of_week=" + day.toLowerCase()
                + "&subject_specialization=" + subject;


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        List<String> availableTeachers = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject teacher = data.getJSONObject(i);
                            String teacherName = teacher.getString("name");
                            availableTeachers.add(teacherName);
                        }

                        if (availableTeachers.isEmpty()) {
                            availableTeachers.add("No available teachers");
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, availableTeachers);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        teacherSpinner.setAdapter(adapter);
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

        Spinner courseSpinner = dialogView.findViewById(R.id.courseSpinner);
        Spinner teacherSpinner = dialogView.findViewById(R.id.teacherSpinner);
        Spinner daySpinner = dialogView.findViewById(R.id.daySpinner);
        Spinner startSpinner = dialogView.findViewById(R.id.startPeriodSpinner);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(days));
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                Arrays.asList("08:00 - 08:40", "08:45 - 09:25", "09:30 - 10:10", "10:30 - 11:10", "11:15 - 11:55", "12:00 - 12:40", "12:45 - 13:25"));
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

            if (selectedCourse != null && selectedDay != null && selectedPeriod != null && selectedTeacher != null) {
                ScheduleItem newItem = new ScheduleItem();
                newItem.courseName = selectedCourse;
                newItem.dayOfWeek = selectedDay;
                newItem.startTime = selectedPeriod;
                newItem.teacherName = selectedTeacher;

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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        List<String> filteredCourses = new ArrayList<>();
                        Map<String, String> courseMap = new HashMap<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String name = item.getString("course_name");
                            String desc = item.getString("description");

                            if (selectedClass <= 10 ||
                                    ("A".equalsIgnoreCase(selectedBranch) && desc.equalsIgnoreCase("scientific")) ||
                                    ("B".equalsIgnoreCase(selectedBranch) && desc.equalsIgnoreCase("literary"))) {
                                filteredCourses.add(name);
                                courseMap.put(name, desc);
                            }
                        }

                        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filteredCourses);
                        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        courseSpinner.setAdapter(courseAdapter);

                        final String[] selectedCourseDesc = {null};
                        final String[] selectedDayWrapper = {null};
                        final String[] selectedPeriodWrapper = {null};

                        AdapterView.OnItemSelectedListener fetchListener = new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (parent == courseSpinner) {
                                    String course = (String) courseSpinner.getSelectedItem();
                                    selectedCourseDesc[0] = courseMap.get(course);
                                } else if (parent == daySpinner) {
                                    selectedDayWrapper[0] = (String) daySpinner.getSelectedItem();
                                } else if (parent == startSpinner) {
                                    selectedPeriodWrapper[0] = (String) startSpinner.getSelectedItem();
                                }

                                if (selectedCourseDesc[0] != null && selectedDayWrapper[0] != null && selectedPeriodWrapper[0] != null) {
                                    String[] times = selectedPeriodWrapper[0].split(" - ");
                                    String start = times[0] + ":00";
                                    String end = times[1] + ":00";
                                    fetchAvailableTeachers(selectedCourseDesc[0], selectedDayWrapper[0], start, end, teacherSpinner);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        };

                        courseSpinner.setOnItemSelectedListener(fetchListener);
                        daySpinner.setOnItemSelectedListener(fetchListener);
                        startSpinner.setOnItemSelectedListener(fetchListener);

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
    }

}