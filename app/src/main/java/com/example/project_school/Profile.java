package com.example.project_school;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    EditText idTxt, nameTxt, emailTxt, passwordTxt, phoneTxt;
    Button saveBtn, cancelBtn;

    // Store original values for cancel
    int originalUserId;
    String originalName, originalEmail, originalPassword, originalPhone;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        idTxt = findViewById(R.id.idtxt);
        nameTxt = findViewById(R.id.nametxt);
        emailTxt = findViewById(R.id.emailtxt);
        passwordTxt = findViewById(R.id.passwordtxt);
        phoneTxt = findViewById(R.id.phonetxt);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.canselBtn);
        profilePicture = findViewById(R.id.profilePicture);

        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = prefs.edit();

        loadUserDataFromPreferences();

        saveBtn.setOnClickListener(view -> {
            int userId = originalUserId;
            String name = nameTxt.getText().toString().trim();
            String email = emailTxt.getText().toString().trim();
            String password = passwordTxt.getText().toString().trim();
            String phone = phoneTxt.getText().toString().trim();

            // Update in DB (you need to implement this method to call your API)
            updateProfileInDatabase(userId, name, email, password, phone);

            // Update SharedPreferences
            editor.putString("name", name);
            editor.putString("email", email);
            editor.putString("password", password);
            editor.putString("phone", phone);
            editor.apply();
        });

        cancelBtn.setOnClickListener(view -> resetFormToOriginalValues());

        profilePicture.setOnClickListener(v -> {

        });
    }

    private void loadUserDataFromPreferences() {
        originalUserId = prefs.getInt("user_id", 0);
        originalName = prefs.getString("name", "");
        originalEmail = prefs.getString("email", "");
        originalPassword = prefs.getString("password", "");
        originalPhone = prefs.getString("phone", "");

        idTxt.setText(String.valueOf(originalUserId));
        idTxt.setEnabled(false); // prevent editing
        nameTxt.setText(originalName);
        emailTxt.setText(originalEmail);
        passwordTxt.setText(originalPassword);
        phoneTxt.setText(originalPhone);
    }

    private void resetFormToOriginalValues() {
        nameTxt.setText(originalName);
        emailTxt.setText(originalEmail);
        passwordTxt.setText(originalPassword);
        phoneTxt.setText(originalPhone);
    }

    // You need to replace this with actual API call code
    private void updateProfileInDatabase(int userId, String name, String email, String password, String phone) {
        String url = getString(R.string.URL) + "users/update.php";

        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("name", name);
            body.put("email", email);
            body.put("password", password);
            body.put("phone", phone);
            body.put("date_of_birth", prefs.getString("date_of_birth", "").equals("null")? null : prefs.getString("date_of_birth", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            Toast.makeText(Profile.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Profile.this, "Update failed. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Profile.this, "Unexpected response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(Profile.this, "Network error. Update failed.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + prefs.getString("auth_token", ""));
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

}
