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
    private EditText inputName;
    private Spinner spinnerGender, spinnerCountry;
    private Button btnSaveProfile, btnLogOut;

    private String userEmail, deviceId, permanentId;
    private String encodedImage = "";
    private SharedPreferences prefs;

    private static final int PICK_IMAGE_REQUEST = 1;

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

    private final String[] genders = {"Select Gender", "Male", "Female", "Other"};

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

        // অ্যাডাপ্টার সেটআপ
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        spinnerCountry.setAdapter(countryAdapter);

        // পার্মানেন্ট আইডি ম্যানেজমেন্ট
        permanentId = prefs.getString("MY_9_DIGIT_ID", "");
        if (permanentId == null || permanentId.isEmpty()) {
            permanentId = generatePermanentId();
            prefs.edit().putString("MY_9_DIGIT_ID", permanentId).apply();
        }

        // যদি আগে থেকেই নাম সেভ করা থাকে তা বক্সে লোড করা
        String existingName = prefs.getString("USER_NAME", "");
        if (existingName != null && !existingName.isEmpty() && inputName != null) {
            inputName.setText(existingName);
        }

        // আগের সেভ করা জেন্ডার ও কান্ট্রি স্পিনারে সিলেক্ট করে রাখার লজিক
        String existingGender = prefs.getString("USER_GENDER", "");
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equals(existingGender)) {
                spinnerGender.setSelection(i);
                break;
            }
        }

        String existingCountry = prefs.getString("USER_COUNTRY", "");
        for (int i = 0; i < countries.length; i++) {
            if (countries[i].equals(existingCountry)) {
                spinnerCountry.setSelection(i);
                break;
            }
        }

        // আগের সেভ করা ছবি (যদি থাকে) লোড করা
        String savedImageBase64 = prefs.getString("USER_PROFILE_IMAGE", "");
        if (savedImageBase64 != null && !savedImageBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(savedImageBase64, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedBitmap != null) {
                    imgProfilePic.setImageBitmap(decodedBitmap);
                    encodedImage = savedImageBase64;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // লগআউট বাটন
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

        // গ্যালারি থেকে ছবি সিলেক্ট করা
        imgProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // সেভ প্রোফাইল বাটন ক্লিক
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

            // ফায়ারবেসে ডেটা আপডেট ও পার্মানেন্টলি সেভ করা
            userRef.setValue(userData).addOnSuccessListener(aVoid -> {
                if (deviceId != null && !deviceId.isEmpty()) {
                    deviceBindingRef.child(deviceId).setValue(bindingData);
                }

                // লোকাল শেয়ার্ড প্রিফারেন্সে সাথে সাথে আপডেট করা যাতে পরবর্তীতে আর লস না হয়
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("IS_LOGGED_IN", true);
                editor.putString("USER_EMAIL", userEmail);
                editor.putString("MY_9_DIGIT_ID", permanentId);
                editor.putString("USER_NAME", name);
                editor.putString("USER_COUNTRY", country);
                editor.putString("USER_GENDER", gender);
                if (!encodedImage.isEmpty()) {
                    editor.putString("USER_PROFILE_IMAGE", encodedImage);
                }
                editor.apply();

                Toast.makeText(this, "Profile Updated Successfully! ID: " + permanentId, Toast.LENGTH_LONG).show();
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
