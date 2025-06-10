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

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {
    private final List<Assessment> assessments;
    private final Context context;
    private final RequestQueue queue;
    private String BASE_URL;
    private String auth_token;
    private SharedPreferences sharedPreferences;


    public AssignmentAdapter(Context context, List<Assessment> assessments) {
        this.context = context;
        this.assessments = assessments;
        this.queue = Volley.newRequestQueue(context);
        sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        auth_token = sharedPreferences.getString("auth_token", "");
        BASE_URL = context.getString(R.string.URL);
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, remarks;
        EditText URL;
        Button saveBtn;

        public AssignmentViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.AssName);
            date = itemView.findViewById(R.id.date);

            remarks = itemView.findViewById(R.id.remarks);
            URL = itemView.findViewById(R.id.remarks_edit);
            saveBtn = itemView.findViewById(R.id.submitBtn);
        }
    }

    @Override
    public AssignmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assigment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AssignmentViewHolder holder, int position) {
        Assessment a = assessments.get(position);

        holder.name.setText(a.name);
        holder.date.setText("Date: " + a.date);
        holder.remarks.setText("Remarks: " + a.remarks);

        holder.saveBtn.setOnClickListener(v -> {
            String newRemarksStr = holder.URL.getText().toString();
            if (newRemarksStr.isEmpty()) {
                Toast.makeText(context, "URL can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            try {

                String url = BASE_URL + "assessments/update.php";
                JSONObject body = new JSONObject();
                body.put("assessment_id", a.assessmentId);
                body.put("remarks", newRemarksStr);

                JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.PUT, url, body,
                        response -> {
                            try {
                                if (response.getString("status").equals("success")) {
                                    Toast.makeText(context, "Submitted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Submit failed", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(context, " error", Toast.LENGTH_SHORT).show();
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

    }

    @Override
    public int getItemCount() {
        return assessments.size();
    }


}