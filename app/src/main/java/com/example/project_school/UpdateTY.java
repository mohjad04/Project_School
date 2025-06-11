package com.example.project_school;

import android.app.DatePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpdateTY extends AppCompatActivity {

    Spinner year1Spinner, yearSpinner, termYearSpinner, termSpinner, termTypeSpinner;
    EditText yearStart, yearEnd, termStart, termEnd;
    Button updateYearBtn, updateTermBtn;
    List<Integer> yearIds = new ArrayList<>();
    List<String> yearNames = new ArrayList<>();
    List<Integer> termIds = new ArrayList<>();
    String yearName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_ty);

        // Initialize views
        year1Spinner = findViewById(R.id.year1_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        termYearSpinner = findViewById(R.id.term_year_spinner);
        termSpinner = findViewById(R.id.term_spinner);
        termTypeSpinner = findViewById(R.id.term_type_spinner);

        yearStart = findViewById(R.id.year_start_date);
        yearEnd = findViewById(R.id.year_end_date);
        termStart = findViewById(R.id.term_start_date);
        termEnd = findViewById(R.id.term_end_date);

        updateYearBtn = findViewById(R.id.createYbtn);
        updateTermBtn = findViewById(R.id.createTbtn);

        // Date pickers
        yearStart.setOnClickListener(v -> showDatePicker(yearStart));
        yearEnd.setOnClickListener(v -> showDatePicker(yearEnd));
        termStart.setOnClickListener(v -> showDatePicker(termStart));
        termEnd.setOnClickListener(v -> showDatePicker(termEnd));

        // Static values for is_current
        ArrayAdapter<String> staticAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"false", "true"});
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        termTypeSpinner.setAdapter(staticAdapter);
        yearSpinner.setAdapter(staticAdapter);

        fetchAllAcademicYears();

        termYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchTermsByYear(yearIds.get(position));
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateYearBtn.setOnClickListener(v -> updateYear());
        updateTermBtn.setOnClickListener(v -> updateTerm());
    }

    private void fetchAllAcademicYears() {
        String url = getString(R.string.URL) + "academic_year/list.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        yearIds.clear();

                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject year = data.getJSONObject(i);
                                yearNames.add(year.getString("year_name"));
                                yearIds.add(year.getInt("year_id"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, yearNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            year1Spinner.setAdapter(adapter);
                            termYearSpinner.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
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

    private void fetchTermsByYear(int yearId) {
        String url = getString(R.string.URL) + "terms/list.php?year_id=" + yearId;
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<String> termNames = new ArrayList<>();
                        termIds.clear();

                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject term = data.getJSONObject(i);
                                termNames.add(term.getString("term_name"));
                                termIds.add(term.getInt("term_id"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, termNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            termSpinner.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse terms", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching terms", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void updateYear() {
        int selectedYearId = yearIds.get(year1Spinner.getSelectedItemPosition());
        int isCurrent = yearSpinner.getSelectedItem().toString().equals("true") ? 1 : 0;

        String startDateStr = yearStart.getText().toString();
        String endDateStr = yearEnd.getText().toString();

        if (!startDateStr.isEmpty() && !endDateStr.isEmpty() && !isValidDateOrder(startDateStr, endDateStr)) {
            Toast.makeText(this, "End date cannot be before start date!", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("year_id", selectedYearId);

            if (yearName != null && !yearName.isEmpty()) json.put("year_name", yearName);
            if (!startDateStr.isEmpty()) json.put("start_date", startDateStr);
            if (!endDateStr.isEmpty()) json.put("end_date", endDateStr);
            json.put("is_current", isCurrent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getString(R.string.URL) + "academic_year/update.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, json,
                response -> {
                    String status = response.optString("status", "error");
                    String msg = response.optString("message", "Unknown error");

                    if (status.equals("success")) {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    } else if (status.equals("error") && msg.contains("already set as current")) {
                        JSONObject data = response.optJSONObject("data");
                        if (data != null) {
                            String conflictingYearName = data.optString("year_name", "Unknown year");
                            Toast.makeText(this,
                                    "Update failed: Another academic year (" + conflictingYearName + ") is already set as current.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Update failed: " + msg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String errorMessage = "Failed to update year";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data);
                            JSONObject errObj = new JSONObject(body);
                            if (errObj.has("message")) {
                                errorMessage = errObj.getString("message");
                                if (errObj.has("data")) {
                                    JSONObject data = errObj.getJSONObject("data");
                                    if (data.has("year_name")) {
                                        errorMessage += ": " + data.getString("year_name");
                                    }
                                }
                            } else {
                                errorMessage += ": " + body;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errorMessage += ": unexpected error format";
                        }
                    } else if (error.getMessage() != null) {
                        errorMessage += ": " + error.getMessage();
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }

        )
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };


        Volley.newRequestQueue(this).add(request);
    }



    private void updateTerm() {
        int selectedTermId = termIds.get(termSpinner.getSelectedItemPosition());
        int selectedYearId = yearIds.get(termYearSpinner.getSelectedItemPosition());
        int isCurrent = termTypeSpinner.getSelectedItem().toString().equals("true") ? 1 : 0;

        String termName = termSpinner.getSelectedItem().toString();
        String startDateStr = termStart.getText().toString();
        String endDateStr = termEnd.getText().toString();

        if (!startDateStr.isEmpty() && !endDateStr.isEmpty() && !isValidDateOrder(startDateStr, endDateStr)) {
            Toast.makeText(this, "End date cannot be before start date!", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("term_id", selectedTermId);
            if (termName != null && !termName.isEmpty()) json.put("term_name", termName);
            json.put("year_id", selectedYearId);
            if (!startDateStr.isEmpty()) json.put("start_date", startDateStr);
            if (!endDateStr.isEmpty()) json.put("end_date", endDateStr);
            json.put("is_current", isCurrent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getString(R.string.URL) + "terms/update.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, json,
                response -> {
                    String status = response.optString("status", "error");
                    String msg = response.optString("message", "Unknown error");

                    if (status.equals("success")) {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String errorMessage = "Unexpected error";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data);
                            JSONObject errObj = new JSONObject(body);

                            Log.d("UpdateTermError", "Error response JSON: " + errObj.toString());

                            String msg = errObj.optString("message", "Unknown error");
                            if (errObj.has("data")) {
                                JSONObject data = errObj.getJSONObject("data");
                                String conflictTerm = data.optString("term_name", "");
                                int yearId = data.optInt("year_id", -1);
                                String yearName = getYearNameById(yearId);

                                if (!conflictTerm.isEmpty() && yearId != -1) {
                                    errorMessage = "Term \"" + conflictTerm + "\" in year " + yearName + " is already current";
                                } else {
                                    errorMessage = msg;
                                }
                            } else {
                                errorMessage = msg;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }



    private boolean isValidDateOrder(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            return !end.before(start);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Invalid format = invalid order
        }
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
                }, year, month, day);
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

    private String getYearNameById(int yearId) {
        for (int i = 0; i < yearIds.size(); i++) {
            if (yearIds.get(i) == yearId) {
                return yearNames.get(i);
            }
        }
        return "Unknown Year";
    }

}
