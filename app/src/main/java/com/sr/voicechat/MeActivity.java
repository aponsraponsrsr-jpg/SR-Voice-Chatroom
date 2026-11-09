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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
                userDatabaseRef.child(currentUserId).child("sendingCoins").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            long spentCoins = 0;
                            if (snapshot.exists() && snapshot.getValue(Long.class) != null) {
                                spentCoins = snapshot.getValue(Long.class);
                            }
                            int currentLevel = LevelUtils.calculateLevel(spentCoins);
                            if (txtProfileLevel != null) {
                                txtProfileLevel.setText("  |  ⭐ Lv." + currentLevel);
                                txtProfileLevel.setTextColor(LevelUtils.getLevelColor(currentLevel));
                            }
                        } catch (Exception ignored) {}
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            } catch (Exception ignored) {}
        }
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

    // নিখুঁত লেভেল সেন্টার ডায়ালগ (এখন জেনারেল স্টোরের লেভেল আইকনে ক্লিক করলেই ১ থেকে ১৫০ লেভেলের লিস্ট শো করবে)
    private void showLevelCenterDialog() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(35, 35, 35, 35);

            GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
            );
            bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
            root.setBackground(bg);

            LinearLayout topBar = new LinearLayout(this);
            topBar.setOrientation(LinearLayout.HORIZONTAL);
            topBar.setGravity(Gravity.CENTER_VERTICAL);
            topBar.setPadding(0, 0, 0, 20);

            TextView title = new TextView(this);
            title.setText("🏆 SR Level Center (Lv.1 - Lv.150)");
            title.setTextColor(Color.parseColor("#FFD700"));
            title.setTextSize(18);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            topBar.addView(title);

            Button btnClose = new Button(this);
            btnClose.setText("✕");
            btnClose.setBackgroundColor(Color.TRANSPARENT);
            btnClose.setTextColor(Color.WHITE);
            btnClose.setTextSize(16);
            btnClose.setOnClickListener(v -> dialog.dismiss());
            topBar.addView(btnClose);

            root.addView(topBar);

            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1400));
            scrollView.setFillViewport(true);

            LinearLayout listContainer = new LinearLayout(this);
            listContainer.setOrientation(LinearLayout.VERTICAL);

            // ১ থেকে ১৫০ লেভেলের ডাইনামিক রো জেনারেশন
            for (int lvl = 1; lvl <= 150; lvl++) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(25, 20, 25, 20);
                row.setGravity(Gravity.CENTER_VERTICAL);

                GradientDrawable rBg = new GradientDrawable();
                rBg.setColor(Color.parseColor("#2A1B4E"));
                rBg.setCornerRadius(20);
                row.setBackground(rBg);

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 0, 0, 14);
                row.setLayoutParams(p);

                int levelColor = LevelUtils.getLevelColor(lvl);
                String badgeText = LevelUtils.getLevelBadgeText(lvl);
                String targetCoins = LevelUtils.getLevelTargetCoinsFormatted(lvl);

                TextView badge = new TextView(this);
                badge.setText(badgeText);
                badge.setTextColor(Color.WHITE);
                badge.setTextSize(14);
                badge.setTypeface(null, android.graphics.Typeface.BOLD);
                badge.setGravity(Gravity.CENTER);
                badge.setPadding(20, 12, 20, 12);

                GradientDrawable bBg = new GradientDrawable();
                bBg.setColor(levelColor);
                bBg.setCornerRadius(25);
                badge.setBackground(bBg);
                badge.setMinWidth(110);
                row.addView(badge);

                LinearLayout textStack = new LinearLayout(this);
                textStack.setOrientation(LinearLayout.VERTICAL);
                textStack.setPadding(20, 0, 0, 0);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                textStack.setLayoutParams(tp);

                TextView titleTxt = new TextView(this);
                titleTxt.setText("Level Badge Tier " + lvl);
                titleTxt.setTextColor(Color.WHITE);
                titleTxt.setTextSize(15);
                titleTxt.setTypeface(null, android.graphics.Typeface.BOLD);
                textStack.addView(titleTxt);

                TextView targetTxt = new TextView(this);
                targetTxt.setText("Target: " + targetCoins);
                targetTxt.setTextColor(Color.parseColor("#00E676"));
                targetTxt.setTextSize(12);
                textStack.addView(targetTxt);

                row.addView(textStack);
                listContainer.addView(row);
            }

            scrollView.addView(listContainer);
            root.addView(scrollView);

            dialog.setContentView(root);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1600);
                window.setGravity(Gravity.BOTTOM);
            }
            dialog.show();
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
            WalletRechargeDialog rechargeDialog = new WalletRechargeDialog();
            rechargeDialog.show(getSupportFragmentManager(), "WalletRechargeDialog");
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
