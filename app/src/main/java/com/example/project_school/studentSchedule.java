package com.example.project_school;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class studentSchedule extends AppCompatActivity {
    TableLayout scheduleTable;
    List<ScheduleItem> scheduleList = new ArrayList<>();
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"};
    String classNum, classBranch;

    private SharedPreferences sharedPreferences;
    private int termID;
    private int yearID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_schedule);

        Toolbar toolbar = findViewById(R.id.toolbarSchedule);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        scheduleTable = findViewById(R.id.scheduleTable);

        classNum = getIntent().getStringExtra("CLASS");
        classBranch = getIntent().getStringExtra("BRANCH");

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);

        fetchSchedule(classNum, classBranch, termID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchSchedule(String classNum, String classBranch, int termId) {
        String url = getString(R.string.URL) + "schedule/class.php?class_num=" + classNum + "&class_branch=" + classBranch + "&term_id=" + termId;
        Log.i("DATA", url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                res -> {
                    try {
                        JSONArray data = res.getJSONArray("data");
                        scheduleList.clear();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject o = data.getJSONObject(i);
                            scheduleList.add(new ScheduleItem(
                                    o.getString("day_of_week"),
                                    o.getString("start_time"),
                                    o.getString("end_time"),
                                    o.getString("course_name"),
                                    ""
                            ));
                        }
                        drawScheduleTable();
                    } catch (Exception e) {
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "API error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Content-Type", "application/json");
                h.put("Authorization", "Bearer " + MainActivity.token);
                return h;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void drawScheduleTable() {
        scheduleTable.removeAllViews();
        String[][] timeSlots = {
                {"08:00", "08:40"}, {"08:45", "09:25"}, {"09:30", "10:10"},
                {"10:30", "11:10"}, {"11:15", "11:55"}, {"12:00", "12:40"},
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
                    if (item.dayOfWeek.equalsIgnoreCase(day) && format(item.startTime).equals(slot[0])) {
                        matched = item;
                        break;
                    }
                }
                TextView cell = createCell(matched != null ? matched.courseName : "", false);
               /* if (matched != null) {
                    cell.setBackgroundColor(Color.parseColor("#59bfe5"));
                }*/
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
        tv.setTextSize(10);
        tv.setBackgroundResource(android.R.drawable.editbox_background);
        tv.setWidth(200);
        tv.setHeight(130);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private String format(String t) {
        try {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(t));
        } catch (ParseException e) {
            return t;
        }
    }
}