package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private TextView textUserNameMe, textPermanentIdMe, btnCopyId;
    private ImageView imgProfileMe;
    private Button btnEditProfile;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_me);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Layout Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        textUserNameMe = findViewById(R.id.textUserNameMe);
        textPermanentIdMe = findViewById(R.id.textPermanentIdMe);
        btnCopyId = findViewById(R.id.btnCopyId);
        imgProfileMe = findViewById(R.id.imgProfileMe);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        loadUserProfileData();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnCopyId != null) {
            btnCopyId.setOnClickListener(v -> {
                if (textPermanentIdMe != null) {
                    String idText = textPermanentIdMe.getText().toString().replace("ID: ", "").trim();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText("User ID", idText);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "ID Copied: " + idText, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileData();
    }

    private void loadUserProfileData() {
        if (prefs == null) return;
        
        String savedName = prefs.getString("USER_NAME", "SR Apon");
        String savedId = prefs.getString("MY_9_DIGIT_ID", "480384559");
        String savedImage = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

        if (textUserNameMe != null) {
            textUserNameMe.setText(savedName);
        }
        if (textPermanentIdMe != null) {
            textPermanentIdMe.setText("ID: " + savedId);
        }

        if (imgProfileMe != null && savedImage != null && !savedImage.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(savedImage, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedBitmap != null) {
                    imgProfileMe.setImageBitmap(decodedBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
