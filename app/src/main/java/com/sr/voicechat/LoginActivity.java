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
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        ivPreview = findViewById(R.id.ivPreview);
        Button btnCreate = findViewById(R.id.btnCreateRoom);
        Button btnJoin = findViewById(R.id.btnJoinRoom);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String oldName = prefs.getString("user_name", "");
        String oldImage = prefs.getString("user_image", "");
        
        if (!oldName.isEmpty()) {
            etName.setText(oldName);
        }
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

        if (ivPreview != null) {
            ivPreview.setOnClickListener(v -> pickImage.launch("image/*"));
        }

        if (btnCreate != null) btnCreate.setOnClickListener(v -> saveAndGo(true));
        if (btnJoin != null) btnJoin.setOnClickListener(v -> saveAndGo(false));
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
        } catch (Exception e) {
            return null;
        }
    }

    void saveAndGo(boolean isCreate) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Name likho", Toast.LENGTH_SHORT).show();
            return;
        }
        if (savedImagePath.isEmpty()) {
            Toast.makeText(this, "Photo choose koro", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", name);
        editor.putString("user_image", savedImagePath);
        editor.putString("room_name", name);
        if (isCreate) {
            String newId = String.valueOf(System.currentTimeMillis() % 100000000);
            editor.putString("group_id", newId);
        }
        editor.apply();
        
        startActivity(new Intent(LoginActivity.this, VoiceRoomActivity.class));
    }
}
