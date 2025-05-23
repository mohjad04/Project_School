package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {

    private Button myCoursesBtn,myAssignmentsBtn,myMarksBtn,mySchduleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

       Intent intent = getIntent();

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

    }

    private void setupViews(){
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
               // intent.putExtra("NAME",userName);
               
                startActivity(intent);
            }
        });
    }

    private void setMarksBtn(){
        mySchduleBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this,studentSchedule.class);
                // intent.putExtra("NAME",userName);

                startActivity(intent);
            }
        });
    }

    private void setAssignmentsBtn(){
        mySchduleBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this,studentSchedule.class);
                // intent.putExtra("NAME",userName);

                startActivity(intent);
            }
        });
    }

    private void setCoursesBtn(){
        mySchduleBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this,studentSchedule.class);
                // intent.putExtra("NAME",userName);

                startActivity(intent);
            }
        });
    }
    
}