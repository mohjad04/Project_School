package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button login;
    private EditText username;
    private EditText password;
    private RequestQueue queue;
    public static String token;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String PREFS_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

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

                                editor.putString("auth_token", MainActivity.token);
                                editor.putInt("user_id", userObj.getInt("user_id"));
                                editor.putString("role", role);
                                editor.putString("name", userObj.getString("name"));

                                // GET current term
                                JsonObjectRequest termRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.URL) + "terms/list.php?is_current=1", null,
                                        response1 -> {
                                            try {
                                                JSONArray dataArray = response1.getJSONArray("data");
                                                JSONObject termObj = dataArray.getJSONObject(0);
                                                editor.putInt("current_term", termObj.getInt("term_id"));
                                                editor.putInt("current_year", termObj.getInt("year_id"));
                                                editor.apply();

                                                Intent intent;
                                                switch (role.toLowerCase()) {
                                                    case "student":
                                                        intent = new Intent(MainActivity.this, HomePage.class);
                                                        intent.putExtra("ID",user);

                                                        break;
                                                    case "registrar":
                                                        intent = new Intent(MainActivity.this, RegHome.class); break;
                                                    case "teacher":
                                                        intent = new Intent(MainActivity.this, TeacherDashboardActivity.class); break;
                                                    default:
                                                        return;
                                                }
                                                intent.putExtra("name", userObj.getString("name"));
                                                startActivity(intent);

                                            } catch (JSONException e) {
                                                Toast.makeText(MainActivity.this, "Error getting current term", Toast.LENGTH_SHORT).show();
                                                Log.d("Volley", "Error getting current term: " + e.toString());
                                            }
                                        },
                                        error -> {
                                            Log.e("Volley", "Term fetch error: " + error.toString());
                                            Toast.makeText(MainActivity.this, "Failed to get term data", Toast.LENGTH_SHORT).show();
                                        }
                                ) {
                                    @Override
                                    public Map<String, String> getHeaders() {
                                        Map<String, String> headers = new HashMap<>();
                                        headers.put("Authorization", "Bearer " + MainActivity.token);
                                        return headers;
                                    }
                                };
                                queue.add(termRequest);

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Log.e("Volley", "Login error: " + error.toString());
                            Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
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