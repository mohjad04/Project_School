package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegHome extends AppCompatActivity {

    TextView nametxt,typetxt;
    private Button addStudentBtn, addTeacherBtn, scheduleBtn,addParentbtn,createYT,classroombtn;
    ImageView logoutButton,profilebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg_home);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initButtons();
        nametxt.setText(getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("name", ""));
        typetxt.setText("Registrar");
        setupNavigation();
    }

    private void initButtons() {
        addStudentBtn = findViewById(R.id.addStudentbtn);
        addTeacherBtn = findViewById(R.id.addTeacherbtn);
        scheduleBtn = findViewById(R.id.schedulebtn);
        addParentbtn = findViewById(R.id.addparentbtn);
        createYT = findViewById(R.id.newyearbtn);
        classroombtn = findViewById(R.id.classroombtn);
        logoutButton = findViewById(R.id.logoutbtn);
        profilebtn = findViewById(R.id.profilebtn);
        nametxt = findViewById(R.id.nametxt);
        typetxt = findViewById(R.id.typetxt);
    }

    private void setupNavigation() {
        addStudentBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, addStudent.class);
            startActivity(intent);
        });

        addTeacherBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, addTeacher.class);
            startActivity(intent);
        });

        scheduleBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, CreateSchedule.class);
            startActivity(intent);
        });

        addParentbtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, AddParent.class);
            startActivity(intent);
        });

        createYT.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, YearTerm.class);
            startActivity(intent);
        });

        classroombtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, CreateClass.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            // Clear shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("auth_token");
            editor.remove("user_id");
            editor.remove("role");
            editor.remove("name");
            editor.remove("email");
            editor.remove("password");
            editor.remove("phone");
            editor.remove("date_of_birth");
            editor.remove("user_image");
            editor.remove("current_term");
            editor.remove("current_year");
            editor.apply();

            // Redirect to LoginActivity
            Intent intent = new Intent(RegHome.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);

            Toast.makeText(RegHome.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        profilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(RegHome.this, Profile.class); // Change to your profile activity
            startActivity(intent);
        });

    }




}
