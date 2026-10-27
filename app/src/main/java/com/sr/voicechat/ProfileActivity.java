package com.sr.voicechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private Button selectImageButton, saveProfileButton;
    private EditText nameEditText;
    
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        profileImageView = findViewById(R.id.profileImageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        nameEditText = findViewById(R.id.nameEditText);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        // ছবি সিলেক্ট করার বাটন
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // প্রোফাইল সেভ করার বাটন
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "প্রোফাইল ছবি সিলেক্ট করুন"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveProfileData() {
        final String name = nameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("দয়া করে আপনার নাম লিখুন");
            return;
        }

        if (imageUri != null) {
            // ফায়ারবেস স্টোরেজে ছবি আপলোড করা
            StorageReference fileReference = storageReference.child(mAuth.getCurrentUser().getUid() + ".jpg");

            fileReference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        uploadDataToDatabase(name, imageUrl);
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "ছবি আপলোড ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // ছবি ছাড়া শুধু নাম সেভ করা
            uploadDataToDatabase(name, "default");
        }
    }

    private void uploadDataToDatabase(String name, String imageUrl) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", mAuth.getCurrentUser().getUid());
        userMap.put("name", name);
        userMap.put("image", imageUrl);

        databaseReference.setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "প্রোফাইল সফলভাবে সেভ হয়েছে!", Toast.LENGTH_SHORT).show();
                // সফল হলে মেইন অ্যাক্টিভিটিতে চলে যাবে
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ProfileActivity.this, "ডেটাবেজে সেভ করতে সমস্যা হয়েছে: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
