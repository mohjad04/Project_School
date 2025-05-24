package com.example.project_school;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.hash.Hashing;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private Button login;
    private EditText username;
    private EditText password;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.loginButton);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        queue = Volley.newRequestQueue(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString();
                String pass = password.getText().toString();
                String hashedUserID = Hashing.sha256().hashString(user, StandardCharsets.UTF_8).toString();
                String url = getString(R.string.URL)+"getUser.php?user_id=" + hashedUserID;

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    boolean isError = response.getBoolean("error");
                                    if (!isError) {
                                        JSONObject user = response.getJSONObject("user");
                                        String pass_db = user.getString("password");

                                        if (pass.equals(pass_db)) {
                                            String name = user.getString("name");
                                            String role = user.getString("role"); // make sure this is returned from API

                                            Intent intent;

                                            switch (role.toLowerCase()) {
                                                case "student":
                                                    intent = new Intent(MainActivity.this, HomePage.class);
                                                    break;
                                                case "registrar":
                                                    intent = new Intent(MainActivity.this, RegHome.class);
                                                    break;
                                                case "teacher":
                                                    intent = new Intent(MainActivity.this, TeacherHome.class);
                                                    break;
                                                default:
                                                    Log.d("Login", "Unknown role: " + role);
                                                    return;
                                            }

                                            intent.putExtra("name", name);
                                            startActivity(intent);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error", error.toString());
                            }
                        }
                );

                queue.add(request);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
