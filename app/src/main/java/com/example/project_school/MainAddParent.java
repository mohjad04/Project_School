// MainAddParent.java
package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainAddParent extends AppCompatActivity {
    Button availableParentsbtn, addParentsbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_add_parent);

        Intent intent = getIntent();
        String studentId = intent.getStringExtra("student_id");
        String studentName = intent.getStringExtra("student_name");

        Toast.makeText(this, "Selected: " + studentName + " (ID: " + studentId + ")", Toast.LENGTH_SHORT).show();

        availableParentsbtn = findViewById(R.id.chooseParent);
        addParentsbtn = findViewById(R.id.addParentbtn);

        availableParentsbtn.setOnClickListener(v -> {
            Intent intent1 = new Intent(MainAddParent.this, AvailableParents.class);
            intent1.putExtra("student_id", studentId);
            intent1.putExtra("student_name", studentName);
            startActivity(intent1);
        });

        addParentsbtn.setOnClickListener(v -> {
            Intent intent2 = new Intent(MainAddParent.this, ParentsForm.class);
            intent2.putExtra("student_id", studentId);
            intent2.putExtra("student_name", studentName);
            startActivity(intent2);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
