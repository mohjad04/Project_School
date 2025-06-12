package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {

    private static String BASE_URL;
    private static final int RETRY_TIMEOUT = 10000; // 10 seconds
    private static final int RETRY_MAX_ATTEMPTS = 3;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private LinearLayout errorStateLayout;
    private TextView emptyStateText;
    private TextView errorStateText;
    private MaterialButton retryButton;
    private ImageView emptyStateIcon;
    private ImageView errorStateIcon;

    private StudentAdapter studentAdapter;
    private List<Student> studentList;
    private RequestQueue requestQueue;
    private int termID, yearID;
    private String authToken;
    private SharedPreferences sharedPreferences;
    private ImageView logoutButton,profilebtn;

    private int parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        BASE_URL = getString(R.string.URL);
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        authToken = sharedPreferences.getString("auth_token", "");
        parentId = sharedPreferences.getInt("user_id", 0);
        termID = sharedPreferences.getInt("current_term", -1);
        yearID = sharedPreferences.getInt("current_year", -1);

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        initVolley();
        loadStudents(false);
    }

    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerViewStudents);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        errorStateLayout = findViewById(R.id.errorStateLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        errorStateText = findViewById(R.id.errorStateText);
        retryButton = findViewById(R.id.retryButton);
        emptyStateIcon = findViewById(R.id.emptyStateIcon);
        errorStateIcon = findViewById(R.id.errorStateIcon);
        logoutButton = findViewById(R.id.logoutbtn);
        profilebtn = findViewById(R.id.profilebtn);
    }

    private void setupRecyclerView() {
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(this, studentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent)
        );

        swipeRefreshLayout.setOnRefreshListener(() -> loadStudents(true));

        retryButton.setOnClickListener(v -> loadStudents(false));

        logoutButton.setOnClickListener(v -> {
            // Clear shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Remove all stored user info
            editor.apply();

            // Redirect to LoginActivity
            Intent intent = new Intent(ParentActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);

            Toast.makeText(ParentActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        profilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(ParentActivity.this, Profile.class);
            startActivity(intent);
        });
    }

    private void initVolley() {
        requestQueue = Volley.newRequestQueue(this);
    }

    private void loadStudents(boolean isRefresh) {
        if (!isRefresh) {
            showLoading();
        }

        String url = BASE_URL + "students/listByParent.php?parent_id=" + parentId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (isRefresh) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        handleApiResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isRefresh) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        handleApiError(error);
                    }
                }
        ){
          @Override
          public Map<String, String> getHeaders() {
              Map<String, String> headers = new HashMap<>();
              headers.put("Authorization", "Bearer " + authToken);
              return headers;
          }
        };

        // Set retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                RETRY_TIMEOUT,
                RETRY_MAX_ATTEMPTS,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    private void handleApiResponse(JSONObject response) {
        try {
            String status = response.getString("status");
            String message = response.getString("message");

            if ("success".equals(status)) {
                JSONArray dataArray = response.getJSONArray("data");
                parseStudentData(dataArray);
            } else {
                showErrorState("No students found", message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            showErrorState("Data Error", "Error parsing response data");
        }
    }

    private void parseStudentData(JSONArray dataArray) {
        studentList.clear();

        try {
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject studentObj = dataArray.getJSONObject(i);

                Student student = new Student();
                student.setStudentId(studentObj.getInt("student_id"));

                student.setName(studentObj.getString("name"));
                student.setEmail(studentObj.getString("email"));
                student.setPhone(studentObj.getString("phone"));
                student.setClassNum(studentObj.getInt("class_num"));
                student.setClassBranch(studentObj.getString("class_branch"));
                student.setAdmissionDate(studentObj.getString("admission_date"));
                student.setStatus(studentObj.getString("status"));
                student.setBloodGroup(studentObj.getString("blood_group"));

                studentList.add(student);
            }

            if (studentList.isEmpty()) {
                showEmptyState();
            } else {
                showContent();
                studentAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            showErrorState("Parsing Error", "Error processing student data");
        }
    }

    private void handleApiError(VolleyError error) {
        String errorTitle = "Connection Error";
        String errorMessage = "Please check your internet connection and try again";

        if (error instanceof NoConnectionError) {
            errorTitle = "No Internet";
            errorMessage = "Please check your internet connection";
        } else if (error instanceof TimeoutError) {
            errorTitle = "Request Timeout";
            errorMessage = "Server is taking too long to respond";
        } else if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            switch (statusCode) {
                case 404:
                    errorTitle = "Not Found";
                    errorMessage = "The requested resource was not found";
                    break;
                case 500:
                    errorTitle = "Server Error";
                    errorMessage = "Internal server error occurred";
                    break;
                default:
                    errorTitle = "Error " + statusCode;
                    errorMessage = "An unexpected error occurred";
            }
        }

        showErrorState(errorTitle, errorMessage);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        errorStateLayout.setVisibility(View.GONE);

        emptyStateIcon.setImageResource(R.drawable.ic_school_outline);
        emptyStateText.setText("No students found\nNo children are currently registered under your account");
    }

    private void showErrorState(String title, String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        errorStateLayout.setVisibility(View.VISIBLE);

        errorStateIcon.setImageResource(R.drawable.ic_error_outline);
        errorStateText.setText(title + "\n" + message);
    }

    // StudentAdapter.OnStudentClickListener implementation
    @Override
    public void onCallClick(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEmailClick(String email) {
        if (email != null && !email.isEmpty()) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + email));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Student Information");

            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Email address not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStudentClick(Student student) {
        int student_id = student.getStudentId();
        Intent intent = new Intent(ParentActivity.this, HomePage.class);
        intent.putExtra("ID", student_id+"");
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}