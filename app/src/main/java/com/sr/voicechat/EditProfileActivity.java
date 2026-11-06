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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfilePic;
    private EditText inputName;
    private Spinner spinnerGender, spinnerCountry;
    private Button btnSaveProfile, btnLogOut;

    private String userEmail, deviceId, permanentId;
    private String encodedImage = "";
    private SharedPreferences prefs;

    private static final int PICK_IMAGE_REQUEST = 1;

    // দেশের তালিকা ফ্ল্যাগসহ
    private final String[] countries = {
        "Select Country", 
        "🇧🇩 Bangladesh", 
        "🇸🇦 Saudi Arabia", 
        "🇦🇪 United Arab Emirates", 
        "🇲🇾 Malaysia", 
        "🇴🇲 Oman", 
        "🇶🇦 Qatar", 
        "🇰🇼 Kuwait", 
        "🇧🇭 Bahrain", 
        "🇮🇳 India", 
        "🌍 Other Country"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        deviceId = getIntent().getStringExtra("DEVICE_ID");

        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = prefs.getString("USER_EMAIL", "user@gmail.com");
        }

        imgProfilePic = findViewById(R.id.imgProfilePic);
        inputName = findViewById(R.id.inputName);
        spinnerCountry = findViewById(R.id.spinnerCountry);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogOut = findViewById(R.id.btnLogOut);

        // জেন্ডার অ্যাডাপ্টার
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spinnerGender.setAdapter(genderAdapter);

        // কান্ট্রি অ্যাডাপ্টার (ফ্ল্যাগসহ)
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        spinnerCountry.setAdapter(countryAdapter);

        // পার্মানেন্ট আইডি চেক করা (আগে থেকে থাকলে সেটাই রাখবে, না থাকলে নতুন তৈরি করে চিরতরে সেভ করে নেবে)
        permanentId = prefs.getString("MY_9_DIGIT_ID", "");
        if (permanentId == null || permanentId.isEmpty()) {
            permanentId = generatePermanentId();
            prefs.edit().putString("MY_9_DIGIT_ID", permanentId).apply();
        }

        // যদি আগে থেকেই নাম বা ডাটা সেভ থাকে তা বক্সে দেখানোর জন্য
        String existingName = prefs.getString("USER_NAME", "");
        if (!existingName.isEmpty() && inputName != null) {
            inputName.setText(existingName);
        }

        // লগআউট বাটন ক্লিক লজিক
        if (btnLogOut != null) {
            btnLogOut.setOnClickListener(v -> {
                prefs.edit().clear().apply();
                Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            });
        }

        imgProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSaveProfile.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String gender = spinnerGender.getSelectedItem().toString();
            String country = spinnerCountry.getSelectedItem().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (gender.equals("Select Gender")) {
                Toast.makeText(this, "Please select your gender!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (country.equals("Select Country")) {
                Toast.makeText(this, "Please select your country!", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(permanentId);
            DatabaseReference deviceBindingRef = FirebaseDatabase.getInstance().getReference("device_bindings");

            HashMap<String, Object> userData = new HashMap<>();
            userData.put("userId", permanentId);
            userData.put("email", userEmail);
            userData.put("name", name);
            userData.put("gender", gender);
            userData.put("location", country);
            userData.put("profileImage", encodedImage);
            userData.put("coins", 0);

            HashMap<String, Object> bindingData = new HashMap<>();
            bindingData.put("userId", permanentId);
            bindingData.put("email", userEmail);
            bindingData.put("name", name);

            // ফায়ারবেসে পার্মানেন্টলি ডাটা সেভ করা
            userRef.setValue(userData).addOnSuccessListener(aVoid -> {
                if (deviceId != null && !deviceId.isEmpty()) {
                    deviceBindingRef.child(deviceId).setValue(bindingData);
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("IS_LOGGED_IN", true);
                editor.putString("USER_EMAIL", userEmail);
                editor.putString("MY_9_DIGIT_ID", permanentId);
                editor.putString("USER_NAME", name);
                editor.putString("USER_COUNTRY", country);
                editor.putString("USER_GENDER", gender);
                editor.apply();

                Toast.makeText(this, "Profile Saved Successfully! ID: " + permanentId, Toast.LENGTH_LONG).show();
                startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                finish();
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                byte[] imageBytes = baos.toByteArray();
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            } catch (Exception e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
