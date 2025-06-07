package com.example.project_school;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder> {
    private final List<Assessment> assessments;
    private final Context context;
    private final RequestQueue queue;
    private String BASE_URL;
    private String auth_token;
    private SharedPreferences sharedPreferences;


    public AssessmentAdapter(Context context, List<Assessment> assessments) {
        this.context = context;
        this.assessments = assessments;
        this.queue = Volley.newRequestQueue(context);
        sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        auth_token = sharedPreferences.getString("auth_token", "");
        BASE_URL = context.getString(R.string.URL);
    }

    public static class AssessmentViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, date, remarks;
        EditText score;
        Button saveBtn;

        public AssessmentViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            type = itemView.findViewById(R.id.type);
            date = itemView.findViewById(R.id.date);
            remarks = itemView.findViewById(R.id.remarks);
            score = itemView.findViewById(R.id.score);
            saveBtn = itemView.findViewById(R.id.saveBtn);
        }
    }

    @Override
    public AssessmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assessment, parent, false);
        return new AssessmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AssessmentViewHolder holder, int position) {
        Assessment a = assessments.get(position);

        holder.name.setText(a.name);
        holder.type.setText("Type: " + a.type);
        holder.date.setText("Date: " + a.date);
        holder.remarks.setText("Remarks: " + a.remarks);
        holder.score.setText(String.valueOf(a.score));

        holder.saveBtn.setOnClickListener(v -> {
            String newScoreStr = holder.score.getText().toString();
            if (newScoreStr.isEmpty()) {
                Toast.makeText(context, "Score can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                double newScore = Double.parseDouble(newScoreStr);
                if (newScore > a.maxScore) {
                    Toast.makeText(context, "Score exceeds max", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = BASE_URL + "assessments/update.php";
                JSONObject body = new JSONObject();
                body.put("assessment_id", a.assessmentId);
                body.put("score", newScore);

                JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.PUT, url, body,
                        response -> {
                            try {
                                if (response.getString("status").equals("success")) {
                                    Toast.makeText(context, "Score updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(context, "Parse error", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                ){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + auth_token);
                        return headers;
                    }
                };
                queue.add(updateRequest);
            } catch (Exception e) {
                Toast.makeText(context, "Invalid score", Toast.LENGTH_SHORT).show();
            }
        });
        int backgroundColor = getBackgroundColorForAssessment(a.type);
        holder.itemView.setBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return assessments.size();
    }

    private int getBackgroundColorForAssessment(String type) {
        if (type == null) return 0xFFE3EDF0;

        switch (type.toLowerCase()) {
            case "quiz":
                return 0xFFFFF3E0; // Light Orange
            case "assignment":
                return 0xFFE8F5E9; // Light Green
            case "midterm":
                return 0xFFFFEBEE; // Light Red
            case "final_exam":
                return 0xFFE3F2FD; // Light Blue
            case "homework":
                return 0xFFF3E5F5; // Light Purple
            default:
                return 0xFFE3EDF0; // Default Light Grey-Blue
        }
    }
}