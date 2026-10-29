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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etName;
    private ImageView ivPreview;
    private String savedImagePath = "";
    private ActivityResultLauncher<String> pickImage;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // পার্মানেন্ট লগইন চেক
        prefs = getSharedPreferences("SR_Fixed_Database", MODE_PRIVATE);
        boolean isRegistered = prefs.getBoolean("is_registered", false);

        if (isRegistered) {
            startActivity(new Intent(LoginActivity.this, VoiceRoomActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        ivPreview = findViewById(R.id.ivPreview);
        Button btnCreate = findViewById(R.id.btnCreateRoom);

        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                String path = copyImageToInternal(uri);
                if (path != null) {
                    savedImagePath = path;
                    ivPreview.setImageURI(Uri.parse(path));
                }
            }
        });

        ivPreview.setOnClickListener(v -> pickImage.launch("image/*"));

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "দয়া করে নাম লিখুন", Toast.LENGTH_SHORT).show();
                return;
            }
            if (savedImagePath.isEmpty()) {
                Toast.makeText(this, "দয়া করে প্রোফাইল ছবি সিলেক্ট করুন", Toast.LENGTH_SHORT).show();
                return;
            }

            // ইউনিক আইডি একবারই জেনারেট হয়ে পার্মানেন্টলি সেভ থাকবে
            Random random = new Random();
            String generatedId = String.valueOf(10000000 + random.nextInt(90000000));

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("is_registered", true);
            editor.putString("user_name", name);
            editor.putString("user_image", savedImagePath);
            editor.putString("room_name", name + "'s Room");
            editor.putString("group_id", generatedId);
            editor.apply();

            startActivity(new Intent(LoginActivity.this, VoiceRoomActivity.class));
            finish();
        });
    }

    private String copyImageToInternal(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "permanent_profile.jpg");
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
            input.close();
            output.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            return null;
        }
    }
}
