package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private Button myAbsencesBtn,myAssignmentsBtn,myMarksBtn,mySchduleBtn,myCalendarBtn;
    private TextView txtClassNum,txtStudentName;
    String studentId,studentName;
    String classNum,classBranch,Token;
    ImageView logoutButton,profilebtn;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

       Intent intent = getIntent();
       studentId = intent.getStringExtra("ID");
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
          Token=sharedPreferences.getString("auth_token", "");
        setupViews();
        fetchStudentClassInfo(studentId);
        setScheduleBtn();
        setMarksBtn();
        setAssignmentsBtn();
        setCoursesBtn();
        setCalendarBtn();
        setLogoutButton();
        setProfilebtn();
        fetchStudentClassInfo(studentId);


    }

    private void setupViews(){
        txtStudentName=findViewById(R.id.txtStudentName);
        txtClassNum=findViewById(R.id.txtClassNum);
        mySchduleBtn=findViewById(R.id.mySchduleBtn);
        myMarksBtn=findViewById(R.id.myMarksBtn);
        myAssignmentsBtn=findViewById(R.id.myAssignmentsBtn);
        myAbsencesBtn=findViewById(R.id.myAbsencesBtn);
        myCalendarBtn=findViewById(R.id.myCalendarBtn);
        logoutButton = findViewById(R.id.logoutbtn);
        profilebtn =findViewById(R.id.profilebtn);
    }

    private void setScheduleBtn(){
        mySchduleBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this,studentSchedule.class);
                intent.putExtra("CLASS",classNum);
                intent.putExtra("BRANCH",classBranch);
                startActivity(intent);
            }
        });
    }

    private void setMarksBtn() {
        myMarksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (classNum != null && classBranch != null) {
                    Intent intent = new Intent(HomePage.this, CourseListActivity.class);
                    intent.putExtra("source", "grades");
                    intent.putExtra("ID", studentId);
                    intent.putExtra("CLASS", classNum);
                    intent.putExtra("BRANCH", classBranch);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomePage.this, "Please wait, loading student info...", Toast.LENGTH_SHORT).show();
                }
                Intent intent1 = new Intent(HomePage.this, CourseListActivity.class);
                intent1.putExtra("source", "grades");
                intent1.putExtra("ID",studentId);
                intent1.putExtra("CLASS",classNum);
                intent1.putExtra("BRANCH",classBranch);
                startActivity(intent1);

            }
        });
    }


    private void setAssignmentsBtn(){
        myAssignmentsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (classNum != null && classBranch != null) {
                    Intent intent = new Intent(HomePage.this, CourseListActivity.class);
                    intent.putExtra("source", "assignments");
                    intent.putExtra("ID", studentId);
                    intent.putExtra("CLASS", classNum);
                    intent.putExtra("BRANCH", classBranch);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomePage.this, "Please wait, loading student info...", Toast.LENGTH_SHORT).show();
                }
                Intent intent2 = new Intent(HomePage.this, CourseListActivity.class);
                intent2.putExtra("source", "assignments");
                intent2.putExtra("ID",studentId);
                startActivity(intent2);
            }
        });
    }

    private void setCoursesBtn(){
        myAbsencesBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(HomePage.this,StudentAttendanceReportActivity.class);
                // intent.putExtra("NAME",userName);

                startActivity(intent3);
            }
        });
    }


    private void setCalendarBtn(){
        myCalendarBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent4 = new Intent(HomePage.this,AssessmentsCalendar.class);
                // intent.putExtra("NAME",userName);

                startActivity(intent4);
            }
        });
    }

    private void setLogoutButton(){
        logoutButton.setOnClickListener(v -> {
            // Clear shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Remove all stored user info
            editor.apply();

            // Redirect to LoginActivity
            Intent intent5 = new Intent(HomePage.this, MainActivity.class);
            intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent5);

            Toast.makeText(HomePage.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void setProfilebtn() {
        profilebtn.setOnClickListener(v -> {
            Intent intent6 = new Intent(HomePage.this, Profile.class);
            startActivity(intent6);
        });
    }


    private void fetchStudentClassInfo(String studentId) {
        String url = getString(R.string.URL) + "students/list.php?student_id=" + studentId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.i("RESPONSE", response.toString());
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            if (data.length() > 0) {
                                JSONObject student = data.getJSONObject(0);

                                classNum = student.getString("class_num");
                                classBranch = student.getString("class_branch");
                                studentName = student.getString("name");

                                txtStudentName.setText(studentName);
                                txtClassNum.setText(classNum+" "+classBranch);                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error fetching student info", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + Token);
                return headers;
            }
        };

        queue.add(request);
    }


}