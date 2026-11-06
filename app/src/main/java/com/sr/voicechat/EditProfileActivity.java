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
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgEditProfilePic;
    private EditText editUserName;
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
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Load existing data
        String currentName = prefs.getString("USER_NAME", "SR Apon");
        encodedImage = prefs.getString("USER_PROFILE_IMAGE", "");
        if (editUserName != null) {
            editUserName.setText(currentName);
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
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("USER_NAME", newName);
            if (!encodedImage.isEmpty()) {
                editor.putString("USER_PROFILE_IMAGE", encodedImage);
            }
            boolean isSaved = editor.commit();

            // Also update user's global Firebase profile node so rooms can sync instantly
            String currentUserId = prefs.getString("MY_9_DIGIT_ID", "");
            if (!currentUserId.isEmpty()) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
                userRef.child("name").setValue(newName);
                if (!encodedImage.isEmpty()) {
                    userRef.child("profileImage").setValue(encodedImage);
                }
            }

            if (isSaved) {
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
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
