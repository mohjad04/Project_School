package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private Button myCoursesBtn,myAssignmentsBtn,myMarksBtn,mySchduleBtn;
    private TextView txtClassNum,txtStudentName;
    String studentId,studentName;
    String classNum,classBranch;
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
        setupViews();
        setScheduleBtn();
        setMarksBtn();
        setAssignmentsBtn();
        setCoursesBtn();
        fetchStudentClassInfo(studentId);


    }

    private void setupViews(){
        txtStudentName=findViewById(R.id.txtStudentName);
        txtClassNum=findViewById(R.id.txtClassNum);
        mySchduleBtn=findViewById(R.id.mySchduleBtn);
        myMarksBtn=findViewById(R.id.myMarksBtn);
        myAssignmentsBtn=findViewById(R.id.myAssignmentsBtn);
        myCoursesBtn=findViewById(R.id.myCoursesBtn);
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

    private void setMarksBtn(){
        myMarksBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, CourseListActivity.class);
                intent.putExtra("source", "grades");
                intent.putExtra("ID",studentId);
                intent.putExtra("CLASS",classNum);
                intent.putExtra("BRANCH",classBranch);
                startActivity(intent);

            }
        });
    }

    private void setAssignmentsBtn(){
        myAssignmentsBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, CourseListActivity.class);
                intent.putExtra("source", "assignments");
                intent.putExtra("ID",studentId);
                startActivity(intent);
            }
        });
    }

    private void setCoursesBtn(){
        myCoursesBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this,studentSchedule.class);
                // intent.putExtra("NAME",userName);

                startActivity(intent);
            }
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
                headers.put("Authorization", "Bearer " + MainActivity.token);
                return headers;
            }
        };

        queue.add(request);
    }


}