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

public class LoginActivity extends AppCompatActivity {

    EditText etName;
    ImageView ivPreview;
    String savedImagePath = "";
    ActivityResultLauncher<String> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // অ্যাপ ওপেন হওয়ার সাথে সাথেই চেক করবে ইউজার আগে থেকে নাম সেভ করে রেখেছে কি না
        SharedPreferences checkPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String savedName = checkPrefs.getString("user_name", "");
        
        if (!savedName.isEmpty()) {
            // যদি নাম আগে থেকেই সেভ করা থাকে, সরাসরি ভয়েস রুমে চলে যাবে (লগইন পেজ আর আসবে না)
            startActivity(new Intent(LoginActivity.this, VoiceRoomActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        ivPreview = findViewById(R.id.ivPreview);
        Button btnCreate = findViewById(R.id.btnCreateRoom);
        Button btnJoin = findViewById(R.id.btnJoinRoom);

        // আগের কোনো ডেটা থাকলে তা প্রিভিউতে দেখাবে
        String oldImage = checkPrefs.getString("user_image", "");
        if (!oldImage.isEmpty()) {
            savedImagePath = oldImage;
            try { ivPreview.setImageURI(Uri.parse(oldImage)); } catch (Exception e) {}
        }

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
        btnCreate.setOnClickListener(v -> saveAndGo());
        btnJoin.setOnClickListener(v -> saveAndGo());
    }

    String copyImageToInternal(Uri uri) {
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "profile.jpg");
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > 0) output.write(buffer, 0, len);
            input.close(); output.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) { return null; }
    }

    void saveAndGo() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "দয়া করে আপনার নাম লিখুন", Toast.LENGTH_SHORT).show(); 
            return;
        }
        if (savedImagePath.isEmpty()) {
            Toast.makeText(this, "দয়া করে একটি প্রোফাইল ছবি সিলেক্ট করুন", Toast.LENGTH_SHORT).show(); 
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String existingGroupId = prefs.getString("group_id", "");
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", name);
        editor.putString("user_image", savedImagePath);
        editor.putString("room_name", name + " Room");
        
        // প্রথমবার ৭ ডিজিটের ইউনিক রুম আইডি তৈরি হবে যা পার্মানেন্টলি সেভ থাকবে
        if (existingGroupId.isEmpty()) {
            String generatedId = String.valueOf(1000000 + (long)(Math.random() * 9000000));
            editor.putString("group_id", generatedId);
        }
        
        editor.apply();
        
        // সরাসরি ভয়েস রুমে প্রবেশ করবে
        startActivity(new Intent(LoginActivity.this, VoiceRoomActivity.class));
        finish();
    }
}
