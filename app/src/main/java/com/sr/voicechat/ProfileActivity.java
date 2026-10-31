package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText etName;
    private RadioGroup rgGender;
    private Button btnSaveAndEnter;
    private static final int GALLERY_REQ_CODE = 100;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        rgGender = findViewById(R.id.rgGender);
        btnSaveAndEnter = findViewById(R.id.btnSaveAndEnter);

        // Open Gallery to choose profile picture
        imgProfile.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQ_CODE);
        });

        btnSaveAndEnter.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            int selectedGenderId = rgGender.getCheckedRadioButtonId();

            if (name.isEmpty()) {
                etName.setError("Please enter your name");
                return;
            }

            if (selectedGenderId == -1) {
                Toast.makeText(ProfileActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedGender = findViewById(selectedGenderId);
            String genderText = selectedGender.getText().toString();

            // Generate 7-digit unique permanent ID if not exists
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String savedId = prefs.getString("USER_ID", null);
            if (savedId == null) {
                int randomId = 1000000 + new Random().nextInt(9000000);
                savedId = String.valueOf(randomId);
            }

            // Save data permanently using SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("USER_NAME", name);
            editor.putString("USER_GENDER", genderText);
            editor.putString("USER_ID", savedId);
            if (imageUri != null) {
                editor.putString("USER_IMAGE", imageUri.toString());
            }
            editor.putBoolean("IS_LOGGED_IN", true);
            editor.apply();

            // Navigate to HomeActivity
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }
}
