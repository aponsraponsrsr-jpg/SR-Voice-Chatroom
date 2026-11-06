package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class MeActivity extends AppCompatActivity {

    private ImageView imgMeProfile, btnCopyId;
    private TextView textMeName, textMeId;
    private Button btnEditProfile, btnVipStatus, btnWalletRecharge;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        imgMeProfile = findViewById(R.id.imgMeProfile);
        textMeName = findViewById(R.id.textMeName);
        textMeId = findViewById(R.id.textMeId);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnCopyId = findViewById(R.id.btnCopyId);
        btnVipStatus = findViewById(R.id.btnVipStatus);
        btnWalletRecharge = findViewById(R.id.btnWalletRecharge);

        loadUserData();

        // Edit Profile Click
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // Copy ID Click
        if (btnCopyId != null) {
            btnCopyId.setOnClickListener(v -> {
                String userIdText = textMeId.getText().toString().replace("ID: ", "").trim();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("User ID", userIdText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "ID Copied to Clipboard!", Toast.LENGTH_SHORT).show();
            });
        }

        // VIP Status Click -> Coming Soon Dialog
        if (btnVipStatus != null) {
            btnVipStatus.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("👑 VIP Status")
                    .setMessage("Coming Soon")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }

        // Wallet & Recharge Click -> Coming Soon Dialog[span_1](start_span)[span_1](end_span)
        if (btnWalletRecharge != null) {
            btnWalletRecharge.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("💰 Wallet & Recharge")
                    .setMessage("Coming Soon")[span_2](start_span)[span_2](end_span)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        String name = prefs.getString("USER_NAME", "Sr-Apon");
        String userId = prefs.getString("MY_9_DIGIT_ID", "493859069");
        String encodedImage = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

        if (textMeName != null) {
            textMeName.setText(name);
        }
        if (textMeId != null) {
            textMeId.setText("ID: " + userId);
        }

        if (imgMeProfile != null && !encodedImage.isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), decodedByte);
                    circularBitmapDrawable.setCircular(true);
                    imgMeProfile.setImageDrawable(circularBitmapDrawable);
                }
            } catch (Exception e) {
                imgMeProfile.setImageResource(android.R.drawable.ic_menu_my_calendar);
            }
        }
    }
}
