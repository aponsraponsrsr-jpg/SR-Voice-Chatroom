package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfilePic;
    private EditText inputName, inputLocation;
    private Spinner spinnerGender;
    private Button btnSaveProfile;

    private String userEmail, deviceId;
    private String encodedImage = "";
    private SharedPreferences prefs;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // সঠিক লেআউট ফাইল যুক্ত করা হলো

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        deviceId = getIntent().getStringExtra("DEVICE_ID");

        imgProfilePic = findViewById(R.id.imgProfilePic);
        inputName = findViewById(R.id.inputName);
        inputLocation = findViewById(R.id.inputLocation);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spinnerGender.setAdapter(adapter);

        imgProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSaveProfile.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String location = inputLocation.getText().toString().trim();
            String gender = spinnerGender.getSelectedItem().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (gender.equals("Select Gender")) {
                Toast.makeText(this, "Please select your gender!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (location.isEmpty()) {
                Toast.makeText(this, "Please enter your location!", Toast.LENGTH_SHORT).show();
                return;
            }

            String permanentId = generatePermanentId();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(permanentId);
            DatabaseReference deviceBindingRef = FirebaseDatabase.getInstance().getReference("device_bindings").child(deviceId);

            HashMap<String, Object> userData = new HashMap<>();
            userData.put("userId", permanentId);
            userData.put("email", userEmail);
            userData.put("name", name);
            userData.put("gender", gender);
            userData.put("location", location);
            userData.put("profileImage", encodedImage);
            userData.put("coins", 0);

            HashMap<String, Object> bindingData = new HashMap<>();
            bindingData.put("userId", permanentId);
            bindingData.put("email", userEmail);
            bindingData.put("name", name);

            userRef.setValue(userData).addOnSuccessListener(aVoid -> {
                deviceBindingRef.setValue(bindingData).addOnSuccessListener(aVoid2 -> {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("IS_LOGGED_IN", true);
                    editor.putString("USER_EMAIL", userEmail);
                    editor.putString("MY_9_DIGIT_ID", permanentId);
                    editor.putString("USER_NAME", name);
                    editor.apply();

                    Toast.makeText(this, "Profile Setup Successful! ID: " + permanentId, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                    finish();
                });
            }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private String generatePermanentId() {
        Random random = new Random();
        int id = 100000000 + random.nextInt(900000000);
        return String.valueOf(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imgProfilePic.setImageBitmap(bitmap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] imageBytes = baos.toByteArray();
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            } catch (Exception e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
