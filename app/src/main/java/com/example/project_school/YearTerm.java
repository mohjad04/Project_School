package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YearTerm extends AppCompatActivity {

    EditText yearStart, yearEnd, termStart, termEnd;
    Spinner isCurrentSpinner, yearSelectSpinner, termTypeSpinner;
    Button createYearBtn, createTermBtn,update;
    int createdYearId = -1;
    String yearName = "";

    List<Integer> yearIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year_term);

        yearStart = findViewById(R.id.year_start_date);
        yearEnd = findViewById(R.id.year_end_date);
        termStart = findViewById(R.id.term_start_date);
        termEnd = findViewById(R.id.term_end_date);

        createYearBtn = findViewById(R.id.createYbtn);
        createTermBtn = findViewById(R.id.createTbtn);

        isCurrentSpinner = findViewById(R.id.year_spinner);
        yearSelectSpinner = findViewById(R.id.term_year_spinner);
        termTypeSpinner = findViewById(R.id.term_type_spinner);

        update = findViewById(R.id.updatebtn);
        update.setOnClickListener(v -> {
            Intent intent = new Intent(YearTerm.this, UpdateTY.class);
            startActivity(intent);
        });

        // Static options for is_current spinner
        ArrayAdapter<String> currentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"true", "false"});
        isCurrentSpinner.setAdapter(currentAdapter);

        // Static options for term type
        ArrayAdapter<String> termAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"First Term", "Second Term"});
        termTypeSpinner.setAdapter(termAdapter);

        createYearBtn.setOnClickListener(v -> createAcademicYear());
        createTermBtn.setOnClickListener(v -> createTerm());

        yearStart.setOnClickListener(v -> showDatePicker(yearStart));
        yearEnd.setOnClickListener(v -> showDatePicker(yearEnd));
        termStart.setOnClickListener(v -> showDatePicker(termStart));
        termEnd.setOnClickListener(v -> showDatePicker(termEnd));

        // Fetch available years from server
        fetchAllAcademicYears();
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> {
                    String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                    target.setText(formatted);

                    if (target == yearStart || target == yearEnd) {
                        updateYearName();
                    }
                },
                year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void updateYearName() {
        String start = yearStart.getText().toString();
        String end = yearEnd.getText().toString();

        if (!start.isEmpty() && !end.isEmpty()) {
            String startYear = start.split("-")[0];
            String endYear = end.split("-")[0];
            yearName = startYear + "-" + endYear;
        }
    }

    private void fetchAllAcademicYears() {
        String url = getString(R.string.URL) + "academic_year/list.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<String> yearNames = new ArrayList<>();
                        yearIds.clear();

                        if (response.getString("status").equals("success")) {
                            for (int i = 0; i < response.getJSONArray("data").length(); i++) {
                                JSONObject year = response.getJSONArray("data").getJSONObject(i);
                                yearNames.add(year.getString("year_name"));
                                yearIds.add(year.getInt("year_id"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, yearNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            yearSelectSpinner.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse academic years", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching academic years", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void createAcademicYear() {
        String start = yearStart.getText().toString();
        String end = yearEnd.getText().toString();
        String isCurrent = isCurrentSpinner.getSelectedItem().toString();

        int curr = isCurrent.equalsIgnoreCase("true") ? 1 : 0;

        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("year_name", yearName);
            jsonBody.put("start_date", start);
            jsonBody.put("end_date", end);
            jsonBody.put("is_current", curr);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String url = getString(R.string.URL) + "academic_year/create.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            createdYearId = response.getJSONObject("data").getInt("year_id");
                            Toast.makeText(this, "Academic year created", Toast.LENGTH_SHORT).show();
                            fetchAllAcademicYears(); // refresh spinner list
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Request failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void createTerm() {
        String start = termStart.getText().toString();
        String end = termEnd.getText().toString();
        String termName = termTypeSpinner.getSelectedItem().toString();

        int selectedYearIndex = yearSelectSpinner.getSelectedItemPosition();
        if (selectedYearIndex == -1 || selectedYearIndex >= yearIds.size()) {
            Toast.makeText(this, "Please select a valid year", Toast.LENGTH_SHORT).show();
            return;
        }

        int yearId = yearIds.get(selectedYearIndex);

        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please select both term start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("term_name", termName);
            jsonBody.put("year_id", yearId);
            jsonBody.put("start_date", start);
            jsonBody.put("end_date", end);
            jsonBody.put("is_current", 0);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String url = getString(R.string.URL) + "terms/create.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            Toast.makeText(this, "Term created", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid response format", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Request failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
