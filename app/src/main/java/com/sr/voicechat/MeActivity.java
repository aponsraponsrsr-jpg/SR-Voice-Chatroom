package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MeActivity extends AppCompatActivity {

    private ImageView imgMeProfile, btnEditProfile;
    private TextView textMeName, textMeId, txtProfileLevel;
    private LinearLayout btnVipStatus, btnWalletRecharge, btnHostCenter, btnHostPolicy, btnLevelCenter;
    private LinearLayout toolStore, toolDress, toolBirthday;
    
    private SharedPreferences prefs;
    private DatabaseReference userDatabaseRef;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        try {
            prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
            currentUserId = prefs.getString("MY_9_DIGIT_ID", "493859069");
            userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

            imgMeProfile = findViewById(R.id.imgMeProfile);
            textMeName = findViewById(R.id.textMeName);
            textMeId = findViewById(R.id.textMeId);
            txtProfileLevel = findViewById(R.id.txtProfileLevel); 
            btnEditProfile = findViewById(R.id.btnEditProfile);
            btnVipStatus = findViewById(R.id.btnVipStatus);
            btnWalletRecharge = findViewById(R.id.btnWalletRecharge);
            btnHostCenter = findViewById(R.id.btnHostCenter);
            btnHostPolicy = findViewById(R.id.btnHostPolicy);
            btnLevelCenter = findViewById(R.id.btnLevelCenter); 

            toolStore = findViewById(R.id.toolStore);
            toolDress = findViewById(R.id.toolDress);
            toolBirthday = findViewById(R.id.toolBirthday);

            loadUserData();
            syncUserLevelFromFirebase();

            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> {
                    Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                });
            }

            if (toolStore != null) {
                toolStore.setOnClickListener(v -> Toast.makeText(this, "🛍️ Opening Store...", Toast.LENGTH_SHORT).show());
            }
            if (toolDress != null) {
                toolDress.setOnClickListener(v -> Toast.makeText(this, "👕 Opening Dress & Frames...", Toast.LENGTH_SHORT).show());
            }
            if (toolBirthday != null) {
                toolBirthday.setOnClickListener(v -> Toast.makeText(this, "🎂 Opening Birthday Gifts...", Toast.LENGTH_SHORT).show());
            }

            if (btnLevelCenter != null) {
                btnLevelCenter.setOnClickListener(v -> showLevelCenterDialog());
            }

            if (btnVipStatus != null) {
                btnVipStatus.setOnClickListener(v -> showVipDialog());
            }

            if (btnWalletRecharge != null) {
                btnWalletRecharge.setOnClickListener(v -> showWalletRechargeDialog());
            }

            if (btnHostCenter != null) {
                btnHostCenter.setOnClickListener(v -> {
                    new AlertDialog.Builder(MeActivity.this)
                        .setTitle("🎙️ Host Center")
                        .setMessage("Check your streaming targets, live duration, and daily earnings.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
                });
            }

            if (btnHostPolicy != null) {
                btnHostPolicy.setOnClickListener(v -> showPolicyDialog());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncUserLevelFromFirebase() {
        if (currentUserId != null && !currentUserId.isEmpty() && userDatabaseRef != null) {
            try {
                userDatabaseRef.child(currentUserId).child("totalSpentCoins").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            long spentCoins = 0;
                            if (snapshot.exists() && snapshot.getValue(Long.class) != null) {
                                spentCoins = snapshot.getValue(Long.class);
                            }
                            int currentLevel = calculateUserLevel(spentCoins);
                            if (txtProfileLevel != null) {
                                txtProfileLevel.setText("  |  ⭐ Lv." + currentLevel);
                                txtProfileLevel.setTextColor(getLevelColor(currentLevel));
                            }
                        } catch (Exception ignored) {}
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            } catch (Exception ignored) {}
        }
    }

    private int calculateUserLevel(long totalCoins) {
        if (totalCoins < 3000000L) return 1;
        long currentRequirement = 3000000L;
        int level = 1;
        for (int i = 2; i <= 50; i++) {
            currentRequirement += 10000000L;
            if (totalCoins >= currentRequirement) {
                level = i;
            } else {
                break;
            }
        }
        return Math.min(level, 50);
    }

    private int getLevelColor(int level) {
        if (level <= 10) return Color.parseColor("#C0C0C0");
        else if (level <= 20) return Color.parseColor("#29B6F6");
        else if (level <= 35) return Color.parseColor("#AB47BC");
        else return Color.parseColor("#FFD700");
    }

    private void loadUserData() {
        try {
            if (prefs == null) return;
            String name = prefs.getString("USER_NAME", "Sr-Apon");
            String userId = prefs.getString("MY_9_DIGIT_ID", "493859069");
            String encodedImage = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

            if (textMeName != null) textMeName.setText(name);
            if (textMeId != null) textMeId.setText("ID: " + userId);

            if (imgMeProfile != null && encodedImage != null && !encodedImage.isEmpty()) {
                byte[] decodedString = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), decodedByte);
                    circularBitmapDrawable.setCircular(true);
                    imgMeProfile.setImageDrawable(circularBitmapDrawable);
                }
            }
        } catch (Exception ignored) {}
    }

    private void showLevelCenterDialog() {
        try {
            final Dialog levelDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 60, 40, 60);
            layout.setBackgroundColor(Color.parseColor("#120A2A"));

            TextView title = new TextView(this);
            title.setText("🏆 SR Voice Chat - Level Center");
            title.setTextColor(Color.parseColor("#FFD700"));
            title.setTextSize(18);
            title.setGravity(Gravity.CENTER);
            title.setPadding(0, 0, 0, 30);
            layout.addView(title);

            Button btnClose = new Button(this);
            btnClose.setText("Close");
            btnClose.setOnClickListener(v -> levelDialog.dismiss());
            layout.addView(btnClose);

            levelDialog.setContentView(layout);
            levelDialog.show();
        } catch (Exception ignored) {}
    }

    private void showPolicyDialog() {
        try {
            final Dialog policyDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            policyDialog.setContentView(R.layout.dialog_policy);
            policyDialog.show();
        } catch (Exception ignored) {}
    }

    private void showWalletRechargeDialog() {
        try {
            final Dialog rechargeDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            rechargeDialog.setContentView(R.layout.dialog_wallet_recharge);
            rechargeDialog.show();
        } catch (Exception ignored) {}
    }

    private void showVipDialog() {
        try {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_vip_status);
            dialog.show();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}
