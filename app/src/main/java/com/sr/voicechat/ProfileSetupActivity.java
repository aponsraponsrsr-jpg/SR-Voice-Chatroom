package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileSetupActivity extends AppCompatActivity {
    ImageView imgProfile;
    EditText etName;
    Button btnConfirm;
    TextView tvId;
    String savedPath = "";
    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_profile_setup);
        } catch (Exception e) {
            Toast.makeText(this, "Layout Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == RESULT_OK && r.getData() != null && r.getData().getData() != null) {
                Uri uri = r.getData().getData();
                String path = copyFile(uri);
                if (path != null) {
                    savedPath = path;
                    if (imgProfile != null) {
                        try { imgProfile.setImageURI(Uri.fromFile(new File(path))); } catch (Exception e) {}
                    }
                }
            }
        });

        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        tvId = findViewById(R.id.tvId);
        btnConfirm = findViewById(R.id.btnConfirm);

        String uid = IdManager.getOrCreateUserId(this);
        if (tvId != null) tvId.setText("ID: " + uid);

        String oldImg = prefs.getString("userImage", "");
        if (!oldImg.isEmpty()) {
            savedPath = oldImg;
            if (imgProfile != null) {
                try { imgProfile.setImageURI(Uri.fromFile(new File(oldImg))); } catch (Exception e) {}
            }
        }

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                launcher.launch(i);
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    Toast.makeText(this, "Name লিখো", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (savedPath.isEmpty()) {
                    Toast.makeText(this, "Photo সেট করো", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefs.edit()
                    .putString("userName", name)
                    .putString("userImage", savedPath)
                    .putString("userId", uid)
                    .apply();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }
    }

    private String copyFile(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File f = new File(getFilesDir(), "profile.jpg");
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            is.close(); fos.close();
            return f.getAbsolutePath();
        } catch (Exception e) { return null; }
    }
}
