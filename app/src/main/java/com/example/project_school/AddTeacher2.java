package com.example.project_school;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.*;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTeacher2 extends AppCompatActivity {
    private String name, email, phone, subject, qualification, experience;
    private EditText teacherPassword, teacherAddress, teacherDob, teacherSalary;
    private Spinner teacherGender;
    private Button submitTeacherButton;
    private ImageView backButton;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_teacher2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return insets;
        });

        Intent i = getIntent();
        name = i.getStringExtra("name");
        email = i.getStringExtra("email");
        phone = i.getStringExtra("phone");
        subject = i.getStringExtra("subject");
        qualification = i.getStringExtra("qualification");
        experience = i.getStringExtra("experience");

        teacherPassword = findViewById(R.id.teacherPassword);
        teacherAddress = findViewById(R.id.teacherAddress);
        teacherDob = findViewById(R.id.teacherDob);
        teacherSalary = findViewById(R.id.teachersalary);
        teacherGender = findViewById(R.id.teachergender);
        submitTeacherButton = findViewById(R.id.submitStudentButton);
        backButton = findViewById(R.id.back_button);
        queue = Volley.newRequestQueue(this);

        // Gender spinner
        String[] genders = {"Female", "Male"};
        ArrayAdapter<String> gAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        gAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teacherGender.setAdapter(gAdapter);

        teacherDob.setOnClickListener(v -> {
            Calendar cal=Calendar.getInstance();
            new DatePickerDialog(this, (dp, y,m,d)->{
                teacherDob.setText(String.format(Locale.getDefault(),"%04d-%02d-%02d", y,m+1,d));
            },cal.get(Calendar.YEAR)-30, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        submitTeacherButton.setOnClickListener(v -> submitTeacher());
        backButton.setOnClickListener(v -> finish());
    }

    private void submitTeacher() {
        String password = teacherPassword.getText().toString().trim();
        String address = teacherAddress.getText().toString().trim();
        String dob = teacherDob.getText().toString().trim();
        String salary = teacherSalary.getText().toString().trim();
        String gender = teacherGender.getSelectedItem().toString();

        if(password.isEmpty()){ teacherPassword.setError("Required"); return;}
        if(address.isEmpty()){ teacherAddress.setError("Required"); return;}
        if(dob.isEmpty()){ teacherDob.setError("Required"); return;}
        if(salary.isEmpty()){ teacherSalary.setError("Required"); return;}

        String url = getString(R.string.URL) + "teachers/create.php";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("email", email);
            body.put("subject_specialization", subject);
            body.put("qualification", qualification);
            body.put("experience_years", Integer.parseInt(experience));
            body.put("joining_date", currentDate);
            body.put("salary", Double.parseDouble(salary));
            body.put("date_of_birth", dob);
            body.put("password", password);
            body.put("phone", phone);
            body.put("address", address);
            body.put("gender", gender.toLowerCase());
        }  catch (Exception ex){ ex.printStackTrace(); Toast.makeText(this,"JSON error",Toast.LENGTH_SHORT).show(); return; }

        String token = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE).getString("auth_token","");
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body,
                resp -> {
                    Toast.makeText(this,"Teacher added!",Toast.LENGTH_SHORT).show();
                    try {
                        int teacherId = resp.getJSONObject("data").getInt("teacher_id");
                        Intent intent = new Intent(this, TeacherInfo.class);
                        intent.putExtra("id", teacherId);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("dob", dob);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    }  catch(Exception e){
                        e.printStackTrace(); // Show error in Logcat
                        Toast.makeText(this, "Error loading TeacherInfo", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this,"Submission failed", Toast.LENGTH_SHORT).show()
        ){
            @Override
            public Map<String,String> getHeaders(){
                Map<String,String> h=new HashMap<>();
                h.put("Content-Type", "application/json");
                h.put("Authorization", "Bearer "+token);
                return h;
            }
        };
        queue.add(req);
    }
}
