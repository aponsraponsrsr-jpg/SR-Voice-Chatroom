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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class MeActivity extends AppCompatActivity {

    private ImageView imgMeProfile;
    private TextView textMeName, textMeId;
    private Button btnEditProfile;
    private LinearLayout btnVipStatus, btnWalletRecharge, btnHostCenter, btnHostPolicy;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // Binding UI elements
        imgMeProfile = findViewById(R.id.imgMeProfile);
        textMeName = findViewById(R.id.textMeName);
        textMeId = findViewById(R.id.textMeId);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnVipStatus = findViewById(R.id.btnVipStatus);
        btnWalletRecharge = findViewById(R.id.btnWalletRecharge);
        btnHostCenter = findViewById(R.id.btnHostCenter);
        btnHostPolicy = findViewById(R.id.btnHostPolicy);

        loadUserData();

        // Edit Profile Click
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // VIP Status Click -> Dialog
        if (btnVipStatus != null) {
            btnVipStatus.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("👑 VIP Status")
                    .setMessage("Unlock exclusive VIP perks, badges, and special privileges soon!")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }

        // Wallet & Recharge Click -> Dialog
        if (btnWalletRecharge != null) {
            btnWalletRecharge.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("💰 Wallet & Recharge")
                    .setMessage("Manage your coins, diamonds, and fast recharge options here.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }

        // Host Center Click -> Dialog
        if (btnHostCenter != null) {
            btnHostCenter.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("🎙️ Host Center")
                    .setMessage("Check your streaming targets, live duration, and daily earnings.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }

        // Host Policy Click -> Dialog
        if (btnHostPolicy != null) {
            btnHostPolicy.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("📜 Host Policy & Guidelines")
                    .setMessage("1. Maintain respectful communication.\n2. No vulgar or abusive language.\n3. Follow agency and app streaming rules strictly to ensure the best service.")
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
