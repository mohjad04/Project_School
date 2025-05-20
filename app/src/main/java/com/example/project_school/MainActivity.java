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

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import com.google.common.hash.Hashing;


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
                String url = "http://10.0.2.2:80/project_school/getUser.php?user_id=" + hashedUserID;
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
                                            Intent intent = new Intent(MainActivity.this, HomePage.class);
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