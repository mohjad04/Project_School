package com.example.project_school;


import org.json.JSONException;
import org.json.JSONObject;

public class Assessment {
    public int assessmentId;
    public String name, type, date, remarks;
    public double score, maxScore, percentage;

    public Assessment(JSONObject obj) throws JSONException {
        assessmentId = obj.getInt("assessment_id");
        name = obj.getString("assessment_name");
        type = obj.getString("assessment_type");
        date = obj.getString("date_assessed");
        remarks = obj.getString("remarks");
        score = obj.getDouble("score");
        maxScore = obj.getDouble("max_score");
        percentage = obj.getDouble("percentage");
    }
}
