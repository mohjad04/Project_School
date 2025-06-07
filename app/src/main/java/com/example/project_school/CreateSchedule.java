package com.example.project_school;

import android.app.AlertDialog;
import android.graphics.Color;
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

    private final String[] days = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

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
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        addButton.setOnClickListener(v -> showAddCourseDialog());

        saveButton.setOnClickListener(v -> {
            Toast.makeText(this, "Schedule saved", Toast.LENGTH_SHORT).show();
        });

        drawScheduleTable();
    }

    private void fetchBranchesFromApi(int classNum) {
        String url = getString(R.string.URL) + "classes/list.php?class_num=" + classNum;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
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
                error -> Toast.makeText(this, "Error fetching branches", Toast.LENGTH_SHORT).show()
        ) {
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

        TableRow header = new TableRow(this);
        header.addView(createCell("Period", true));
        for (String day : days) {
            header.addView(createCell(day, true));
        }
        scheduleTable.addView(header);

        for (int period = 1; period <= 7; period++) {
            TableRow row = new TableRow(this);
            row.addView(createCell("P" + period, true));

            for (String day : days) {
                ScheduleItem matched = null;
                for (ScheduleItem item : scheduleList) {
                    if (item.dayOfWeek.equalsIgnoreCase(day)) {
                        int start = Integer.parseInt(item.startTime);
                        int end = Integer.parseInt(item.endTime);
                        int currentHour = 6 + period;
                        if (currentHour >= start && currentHour < end) {
                            matched = item;
                            break;
                        }
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
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(8, 8, 8, 8);
        tv.setTextSize(12);
        tv.setBackgroundResource(android.R.drawable.editbox_background);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private void showAddCourseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_course, null);

        Spinner courseSpinner = dialogView.findViewById(R.id.courseSpinner);
        Spinner teacherSpinner = dialogView.findViewById(R.id.teacherSpinner);
        Spinner daySpinner = dialogView.findViewById(R.id.daySpinner);
        EditText startEt = dialogView.findViewById(R.id.startPeriodEt);
        EditText endEt = dialogView.findViewById(R.id.endPeriodEt);

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Math", "Science", "English"));
        courseSpinner.setAdapter(courseAdapter);

        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Dr. A", "Dr. B", "Dr. C"));
        teacherSpinner.setAdapter(teacherAdapter);

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList(days));
        daySpinner.setAdapter(dayAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Course")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String course = courseSpinner.getSelectedItem().toString();
                    String teacher = teacherSpinner.getSelectedItem().toString();
                    String day = daySpinner.getSelectedItem().toString();
                    String start = String.valueOf(6 + Integer.parseInt(startEt.getText().toString()));
                    String end = String.valueOf(6 + Integer.parseInt(endEt.getText().toString()));

                    scheduleList.add(new ScheduleItem(day, start, end, course, teacher));
                    drawScheduleTable();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
