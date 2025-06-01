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
    public static String token;

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

                String url = getString(R.string.URL) + "auth/login.php";
                JSONObject body = new JSONObject();
                try {
                    body.put("user_id", user);
                    body.put("password", pass);
                } catch (Exception e) { e.printStackTrace(); }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                        response -> {
                            try {
                                JSONObject data = response.getJSONObject("data");
                                JSONObject userObj = data.getJSONObject("user");
                                String role = userObj.getString("role");
                                MainActivity.token = data.getString("token");

                                Intent intent;
                                switch (role.toLowerCase()) {
                                    case "student":
                                        intent = new Intent(MainActivity.this, HomePage.class); break;
                                    case "registrar":
                                        intent = new Intent(MainActivity.this, RegHome.class); break;
                                    case "teacher":
                                        intent = new Intent(MainActivity.this, TeacherHome.class); break;
                                    default:
                                        return;
                                }
                                intent.putExtra("name", userObj.getString("name"));
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.e("Volley", error.toString())
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
