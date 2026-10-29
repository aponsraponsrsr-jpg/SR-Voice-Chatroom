package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    ImageView ivProfile;
    EditText etName;
    Uri selectedImageUri = null;

    ActivityResultLauncher<String> pickImage = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if(uri != null){
                selectedImageUri = uri;
                ivProfile.setImageURI(uri);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfile = findViewById(R.id.ivProfile);
        etName = findViewById(R.id.etName);
        Button btnSave = findViewById(R.id.btnSaveProfile);

        // লোগোতে টাচ করলেই গ্যালারি খুলবে
        ivProfile.setOnClickListener(v -> pickImage.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if(name.isEmpty()){
                Toast.makeText(this,"Nick name দাও বন্ধু",Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit()
                .putString("user_name", name)
                .putString("user_image", selectedImageUri != null ? selectedImageUri.toString() : "")
                .putBoolean("profile_saved", true)
                .apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
