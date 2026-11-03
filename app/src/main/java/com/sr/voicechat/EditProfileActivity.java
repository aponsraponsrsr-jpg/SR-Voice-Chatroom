package com.sr.voicechat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    EditText etGroupName, etAnnouncement;
    RadioGroup rgGender;
    Button btnSave;
    ImageView ivProfile;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String base64Image = "";
    int PICK_IMAGE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        etGroupName = findViewById(R.id.etGroupName);
        etAnnouncement = findViewById(R.id.etAnnouncement);
        rgGender = findViewById(R.id.rgGender);
        btnSave = findViewById(R.id.btnSave);
        ivProfile = findViewById(R.id.ivProfile);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // প্রোফাইল পিকচার আইকনে ক্লিক করলে পারমিশন চেক করে গ্যালারি ওপেন হবে
        ivProfile.setOnClickListener(v -> {
            checkPermissionAndOpenGallery();
        });

        btnSave.setOnClickListener(v -> {
            String groupNameStr = etGroupName.getText().toString().trim();
            String announcementStr = etAnnouncement.getText().toString().trim();
            if(groupNameStr.isEmpty()){
                Toast.makeText(this,"Group Name likhun",Toast.LENGTH_SHORT).show();
                return;
            }
            final String finalAnnouncement;
            if(announcementStr.isEmpty()){
                finalAnnouncement = "welcome";
            } else {
                finalAnnouncement = announcementStr;
            }
            final String finalGroupName = groupNameStr;
            
            String gender = "Male";
            int id = rgGender.getCheckedRadioButtonId();
            if(id == R.id.rbFemale) gender = "Female";
            
            if(auth.getCurrentUser() == null) return;
            String uid = auth.getCurrentUser().getUid();
            
            Map<String,Object> user = new HashMap<>();
            user.put("uid", uid);
            user.put("name", finalGroupName);
            user.put("groupName", finalGroupName);
            user.put("announcement", finalAnnouncement);
            user.put("gender", gender);
            user.put("email", auth.getCurrentUser().getEmail());
            user.put("photoUrl", base64Image);
            
            db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(EditProfileActivity.this, VoiceRoomActivity.class);
                    intent2.putExtra("groupName", finalGroupName);
                    intent2.putExtra("announcement", finalAnnouncement);
                    startActivity(intent2);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }

    // গ্যালারি পারমিশন চেক করার মেথড
    private void checkPermissionAndOpenGallery() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
        }
    }

    // গ্যালারি ওপেন করার ইনটেন্ট
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
    }

    // পারমিশন এলাও করার পর রেজাল্ট হ্যান্ডেল করা
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Gallery Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            try {
                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Bitmap resized = Bitmap.createScaledBitmap(original, 500, 500, true);
                ivProfile.setImageBitmap(resized);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                byte[] bytes = baos.toByteArray();
                base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
            } catch (Exception e){ e.printStackTrace(); }
        }
    }
}
