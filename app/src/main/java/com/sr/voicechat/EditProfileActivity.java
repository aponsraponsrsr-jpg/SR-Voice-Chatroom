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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        // ফায়ারবেস থেকে রিয়েল-টাইম ডাটা লোড করার লজিক (যাতে সবসময় লেটেস্ট নাম ও ছবি আসে)
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(permanentId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String dbName = snapshot.child("name").getValue(String.class);
                    String dbGender = snapshot.child("gender").getValue(String.class);
                    String dbCountry = snapshot.child("location").getValue(String.class);
                    String dbImage = snapshot.child("profileImage").getValue(String.class);

                    if (dbName != null && inputName != null) {
                        inputName.setText(dbName);
                        prefs.edit().putString("USER_NAME", dbName).apply();
                    }

                    if (dbGender != null) {
                        for (int i = 0; i < genders.length; i++) {
                            if (genders[i].equals(dbGender)) {
                                spinnerGender.setSelection(i);
                                break;
                            }
                        }
                    }

                    if (dbCountry != null) {
                        for (int i = 0; i < countries.length; i++) {
                            if (countries[i].equals(dbCountry)) {
                                spinnerCountry.setSelection(i);
                                break;
                            }
                        }
                    }

                    if (dbImage != null && !dbImage.isEmpty()) {
                        encodedImage = dbImage;
                        try {
                            byte[] decodedString = Base64.decode(dbImage, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            if (decodedBitmap != null) {
                                imgProfilePic.setImageBitmap(decodedBitmap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // যদি ফায়ারবেসে ডাটা না থাকে তবে লোকাল প্রিফারেন্স থেকে দেখাবে
                    String existingName = prefs.getString("USER_NAME", "");
                    if (existingName != null && !existingName.isEmpty() && inputName != null) {
                        inputName.setText(existingName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

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

            // ফায়ারবেসে পার্মানেন্টলি নতুন ডাটা সেভ ও ওভাররাইট করা
            userRef.setValue(userData).addOnSuccessListener(aVoid -> {
                if (deviceId != null && !deviceId.isEmpty()) {
                    deviceBindingRef.child(deviceId).setValue(bindingData);
                }

                // লোকাল শেয়ার্ড প্রিফারেন্সে আপডেট করা
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
