package com.example.project_school;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.bumptech.glide.Glide;

import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface; // Add this at the top

public class Profile extends AppCompatActivity {

    EditText idTxt, nameTxt, emailTxt, passwordTxt, phoneTxt;
    Button saveBtn, cancelBtn;

    // Store original values for cancel
    int originalUserId;
    String originalName, originalEmail, originalPassword, originalPhone;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    ImageView profilePicture;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;


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

        profilePicture.setOnClickListener(v -> openFileChooser());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePicture.setImageURI(imageUri); // Update image view immediately

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                bitmap = rotateImageIfRequired(bitmap, imageUri); // NEW
                uploadProfilePicture(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        InputStream input = getContentResolver().openInputStream(selectedImage);
        ExifInterface ei = new ExifInterface(input);

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void uploadProfilePicture(Bitmap bitmap) {
        String url = getString(R.string.URL) + "users/upload_image.php"; // Your PHP API endpoint

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", prefs.getInt("user_id", 0));
            jsonObject.put("image_data", encodedImage); // Base64 string
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONObject object = response.getJSONObject("data");
                            String newImageName = object.getString("image");
                            editor.putString("user_image", newImageName);
                            editor.apply();

                            Toast.makeText(Profile.this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Profile.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Profile.this, "Unexpected response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(Profile.this, "Network error", Toast.LENGTH_SHORT).show();
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



    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    private void loadUserDataFromPreferences() {
        originalUserId = prefs.getInt("user_id", 0);
        originalName = prefs.getString("name", "");
        originalEmail = prefs.getString("email", "");
        originalPassword = prefs.getString("password", "");
        originalPhone = prefs.getString("phone", "");

        String imageName = prefs.getString("user_image", ""); // e.g., abc123.jpg

        idTxt.setText(String.valueOf(originalUserId));
        idTxt.setEnabled(false);
        nameTxt.setText(originalName);
        emailTxt.setText(originalEmail);
        passwordTxt.setText(originalPassword);
        phoneTxt.setText(originalPhone);

        // Load the profile image from server
        if (imageName != null && !imageName.isEmpty() && !imageName.equals("null")) {
            String imageUrl = getString(R.string.URL).replace("api", "uploads") + imageName;

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.profile) // optional
                    .error(R.drawable.profile)       // fallback on error
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.profile);
        }
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
                        String status = response.getString("status");
                        if (status.equals("success")) {
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
