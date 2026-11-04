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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
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
    private FirebaseFirestore db;
    private DatabaseReference rtdbRef;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private String imageUriString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        rtdbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        profileImage = findViewById(R.id.profileImage);
        editName = findViewById(R.id.editName);
        textUserId = findViewById(R.id.textUserId);
        genderGroup = findViewById(R.id.genderGroup);
        btnSave = findViewById(R.id.btnSave);

        // পার্মানেন্ট ইউনিক আইডি লোড করা
        String uniqueId = prefs.getString("MY_9_DIGIT_ID", "884516806");
        textUserId.setText("ID: " + uniqueId);

        // আগের সেভ করা ছবি বা নাম থাকলে তা লোড করে দেখানো
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

        // লোকাল প্রিফারেন্স বা ফায়ারবেস অথ থেকে নাম সেট করা
        String savedName = prefs.getString("USER_NAME", "");
        if (!savedName.isEmpty()) {
            editName.setText(savedName);
        } else if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null) {
            editName.setText(mAuth.getCurrentUser().getDisplayName());
        }

        // প্রোফাইল ছবির ওপর ক্লিক করলে গ্যালারি ওপেন হবে
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

            // ছবিটিকে Base64 String-এ কনভার্ট করা যাতে গ্লোবালি রিয়েলটাইম ডেটাবেসে পার্মানেন্টলি শো করে
            final String[] imageBase64 = {imageUriString};
            if (!imageUriString.isEmpty() && (imageUriString.startsWith("content://") || imageUriString.startsWith("file://"))) {
                try {
                    Uri uri = Uri.parse(imageUriString);
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        byte[] imageBytes = baos.toByteArray();
                        imageBase64[0] = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // ১. লোকাল SharedPreferences-এ পার্মানেন্টলি সেভ করা
            prefs.edit()
                    .putString("USER_NAME", newName)
                    .putString("USER_GENDER", gender)
                    .putString("USER_IMAGE", imageBase64[0])
                    .putBoolean("PROFILE_COMPLETED", true)
                    .apply();

            // ২. ফায়ারবেস রিয়েলটাইম ডেটাবেস এবং ফায়ারস্টোরে পার্মানেন্ট সিঙ্ক্রোনাইজেশন
            if (mAuth.getCurrentUser() != null) {
                String email = mAuth.getCurrentUser().getEmail();
                if (email != null) {
                    String safeEmailKey = email.replace(".", "_").replace("@", "_");
                    
                    // রিয়েলটাইম ডেটাবেসে ইউজার নোড আপডেট
                    Map<String, Object> rtdbMap = new HashMap<>();
                    rtdbMap.put("userId", uniqueId);
                    rtdbMap.put("userName", newName);
                    rtdbMap.put("userImage", imageBase64[0]);
                    rtdbRef.child("users").child(safeEmailKey).setValue(rtdbMap);
                }

                // ফায়ারস্টোরে আপডেট
                Map<String, Object> update = new HashMap<>();
                update.put("name", newName);
                update.put("gender", gender);
                update.put("uniqueId", uniqueId);
                update.put("profileImage", imageBase64[0]);

                db.collection("users").document(mAuth.getCurrentUser().getUid())
                        .set(update, SetOptions.merge());
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
