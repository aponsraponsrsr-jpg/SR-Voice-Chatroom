package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private EditText editName;
    private TextView textUserId;
    private RadioGroup genderGroup;
    private Button btnSave;
    private DatabaseReference rtdbRef;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private String imageUriString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        rtdbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        profileImage = findViewById(R.id.profileImage);
        editName = findViewById(R.id.editName);
        textUserId = findViewById(R.id.textUserId);
        genderGroup = findViewById(R.id.genderGroup);
        btnSave = findViewById(R.id.btnSave);

        // পার্মানেন্ট ইউনিক আইডি লোড বা জেনারেট করা
        String tempId = prefs.getString("MY_9_DIGIT_ID", "");
        if (tempId.isEmpty()) {
            tempId = String.valueOf((int)(Math.random() * 900000000) + 100000000);
            prefs.edit().putString("MY_9_DIGIT_ID", tempId).apply();
        }
        textUserId.setText("ID: " + tempId);
        final String finalUniqueId = tempId;

        // আগের সেভ করা ছবি লোড করা
        String savedImage = prefs.getString("USER_IMAGE", "");
        if (!savedImage.isEmpty()) {
            imageUriString = savedImage;
            try {
                if (savedImage.startsWith("content://") || savedImage.startsWith("file://")) {
                    profileImage.setImageURI(Uri.parse(savedImage));
                } else {
                    byte[] decodedString = Base64.decode(savedImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (decodedByte != null) {
                        profileImage.setImageBitmap(decodedByte);
                    }
                }
                profileImage.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String savedName = prefs.getString("USER_NAME", "");
        if (!savedName.isEmpty()) {
            editName.setText(savedName);
        } else if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null) {
            editName.setText(mAuth.getCurrentUser().getDisplayName());
        }

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            String gender = (selectedGenderId == R.id.radioFemale) ? "Female" : "Male";

            String processedImageBase64 = imageUriString;
            if (!imageUriString.isEmpty() && (imageUriString.startsWith("content://") || imageUriString.startsWith("file://"))) {
                try {
                    Uri uri = Uri.parse(imageUriString);
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                        byte[] imageBytes = baos.toByteArray();
                        processedImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // লোকাল প্রিফারেন্সে সেভ
            prefs.edit()
                    .putString("USER_NAME", newName)
                    .putString("USER_GENDER", gender)
                    .putString("USER_IMAGE", processedImageBase64)
                    .putBoolean("PROFILE_COMPLETED", true)
                    .apply();

            // ফায়ারবেস রিয়েলটাইম ডেটাবেসে পার্মানেন্ট সিঙ্ক
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                String email = mAuth.getCurrentUser().getEmail();
                String safeEmailKey = email.replace(".", "_").replace("@", "_");

                Map<String, Object> rtdbMap = new HashMap<>();
                rtdbMap.put("userId", finalUniqueId);
                rtdbMap.put("userName", newName);
                rtdbMap.put("userImage", processedImageBase64);
                rtdbRef.child("users").child(safeEmailKey).setValue(rtdbMap);
            }

            Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageUriString = imageUri.toString();
            try {
                profileImage.setImageURI(imageUri);
                profileImage.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
