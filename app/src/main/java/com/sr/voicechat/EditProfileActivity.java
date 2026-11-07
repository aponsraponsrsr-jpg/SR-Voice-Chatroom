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

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgEditProfilePic;
    private EditText editUserName, editUserLocation;
    private Spinner spinnerGender;
    private Button btnSaveProfile;
    private SharedPreferences prefs;
    private String encodedImage = "";
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        imgEditProfilePic = findViewById(R.id.imgEditProfilePic);
        editUserName = findViewById(R.id.editUserName);
        editUserLocation = findViewById(R.id.editUserLocation);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Setup Gender Spinner items
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spinnerGender.setAdapter(adapter);

        // 🌟 চেক করা: লোকেশন, জেন্ডার বা আইডি আগে থেকেই লক করা আছে কি না
        boolean isProfileLocked = prefs.getBoolean("IS_PROFILE_LOCKED", false);
        String savedId = prefs.getString("MY_9_DIGIT_ID", "");

        // Load existing data if available
        String currentName = prefs.getString("USER_NAME", "SR Apon");
        String currentLocation = prefs.getString("USER_LOCATION", "🇧🇩 Bangladesh");
        String currentGender = prefs.getString("USER_GENDER", "");
        encodedImage = prefs.getString("USER_PROFILE_IMAGE", "");

        if (editUserName != null) {
            editUserName.setText(currentName);
        }
        if (editUserLocation != null) {
            editUserLocation.setText(currentLocation);
            if (isProfileLocked) {
                editUserLocation.setEnabled(false); // 🔒 লোকেশন স্থায়ীভাবে লক করে দেওয়া হলো
                editUserLocation.setAlpha(0.7f);
            }
        }

        if (!currentGender.isEmpty()) {
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equals(currentGender)) {
                    spinnerGender.setSelection(i);
                    break;
                }
            }
            if (isProfileLocked) {
                spinnerGender.setEnabled(false); // 🔒 জেন্ডার স্থায়ীভাবে লক করে দেওয়া হলো
                spinnerGender.setAlpha(0.7f);
            }
        }

        if (!encodedImage.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedBitmap != null) {
                    imgEditProfilePic.setImageBitmap(decodedBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Pick Image from Gallery
        imgEditProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Save Profile Data
        btnSaveProfile.setOnClickListener(v -> {
            String newName = editUserName.getText().toString().trim();
            String newLocation = editUserLocation != null ? editUserLocation.getText().toString().trim() : "🇧🇩 Bangladesh";
            String newGender = spinnerGender.getSelectedItem().toString();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newGender.equals("Select Gender")) {
                Toast.makeText(this, "Please select your gender!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🌟 অটোমেটিক ৯-ডিজিট পার্মানেন্ট আইডি জেনারেট করা (যদি আগে না থাকে)
            String permanentId = prefs.getString("MY_9_DIGIT_ID", "");
            if (permanentId.isEmpty()) {
                permanentId = String.valueOf((int)(Math.random() * 900000000) + 100000000);
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("MY_9_DIGIT_ID", permanentId);
            editor.putString("USER_ID", permanentId);
            editor.putString("USER_NAME", newName);
            
            // যদি প্রোফাইল প্রথমবার সেভ হয়, তবে লোকেশন ও জেন্ডার পার্মানেন্টলি লক করে দেব
            if (!isProfileLocked) {
                editor.putString("USER_LOCATION", newLocation);
                editor.putString("USER_GENDER", newGender);
                editor.putBoolean("IS_PROFILE_LOCKED", true); // 🔒 লক স্ট্যাটাস সেভ
            }

            if (!encodedImage.isEmpty()) {
                editor.putString("USER_PROFILE_IMAGE", encodedImage);
            }
            editor.putBoolean("IS_LOGGED_IN", true);
            boolean isSaved = editor.commit();

            // ফায়ারবেসের গ্লোবাল users টেবিলে পার্মানেন্ট ডাটা সিঙ্ক করা
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(permanentId);
            userRef.child("userId").setValue(permanentId);
            userRef.child("name").setValue(newName);
            userRef.child("location").setValue(newLocation);
            userRef.child("gender").setValue(newGender);
            if (!encodedImage.isEmpty()) {
                userRef.child("profileImage").setValue(encodedImage);
            }

            if (isSaved) {
                Toast.makeText(this, "Profile Saved! ID: " + permanentId, Toast.LENGTH_LONG).show();
                
                // সফলভাবে সরাসরি হোম পেজে রিডাইরেক্ট করা
                Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save profile!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                imgEditProfilePic.setImageBitmap(resizedBitmap);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
