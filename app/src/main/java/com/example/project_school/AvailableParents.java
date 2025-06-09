package com.example.project_school;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AvailableParents extends AppCompatActivity {

    ListView listView;
    ArrayList<Parent> parentList;
    ParentAdapter adapter;
    ImageView backButton;
    TextView title;
    String studentId, studentName,parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_parents);

        Intent intent = getIntent();
        studentId = intent.getStringExtra("student_id");
        studentName = intent.getStringExtra("student_name");

        listView = findViewById(R.id.Parents_list_view);
        backButton = findViewById(R.id.back_button);
        title = findViewById(R.id.title_text);

        parentList = new ArrayList<>();
        adapter = new ParentAdapter(this, parentList);
        listView.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        fetchParents();
    }

    private void fetchParents() {
        String url = getString(R.string.URL) + "parents/list.php";
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            parentList.clear();

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                parentId = obj.getString("parent_id");
                                String name = obj.getString("name");
                                String email = obj.optString("email", "");
                                parentList.add(new Parent(parentId, name, email));
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No parents found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Request failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    public void submitAssignment(String parentIdStr) {


        if (parentIdStr.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "Parent ID and Student ID are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("student_id", Integer.parseInt(studentId));
            jsonBody.put("parent_id", Integer.parseInt(parentIdStr));
            jsonBody.put("relationship", "father");

            String url = getString(R.string.URL) + "parents/assign.php";
            String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("auth_token", "");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            new AlertDialog.Builder(this)
                                    .setTitle("Success")
                                    .setMessage("Parent assigned successfully")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dialog.dismiss();
                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .setCancelable(false)
                                    .show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Invalid response format", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Request failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request body", Toast.LENGTH_SHORT).show();
        }
    }
}
