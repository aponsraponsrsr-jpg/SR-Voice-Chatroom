package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

        // প্রোফাইল ছবি বা ক্যামেরা আইকনে ক্লিক করলে গ্যালারি খোলার জন্য
        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQ_CODE);
            });
        }

        // Save & Enter বাটনে ক্লিক করলে ডেটা সেভ হয়ে হোমপেজে যাওয়ার জন্য
        if (btnSaveAndEnter != null) {
            btnSaveAndEnter.setOnClickListener(v -> {
                if (etName == null) return;
                
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    etName.setError("Please enter your name");
                    return;
                }

                String genderText = "Male";
                if (rgGender != null) {
                    int selectedGenderId = rgGender.getCheckedRadioButtonId();
                    if (selectedGenderId != -1) {
                        android.widget.RadioButton selectedGender = findViewById(selectedGenderId);
                        if (selectedGender != null) {
                            genderText = selectedGender.getText().toString();
                        }
                    }
                }

                SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                String savedId = prefs.getString("USER_ID", null);
                if (savedId == null) {
                    int randomId = 1000000 + new Random().nextInt(9000000);
                    savedId = String.valueOf(randomId);
                }

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("USER_NAME", name);
                editor.putString("USER_GENDER", genderText);
                editor.putString("USER_ID", savedId);
                if (imageUri != null) {
                    editor.putString("USER_IMAGE", imageUri.toString());
                }
                editor.putBoolean("IS_LOGGED_IN", true);
                editor.apply();

                Toast.makeText(ProfileActivity.this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ProfileActivity.this, HomeActivity.java);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imgProfile != null && imageUri != null) {
                imgProfile.setImageURI(imageUri);
            }
        }
    }
}
