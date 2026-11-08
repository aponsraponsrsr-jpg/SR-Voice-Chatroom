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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private SharedPreferences prefs;
    private DatabaseReference userDatabaseRef;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        currentUserId = prefs.getString("MY_9_DIGIT_ID", "493859069");
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Binding UI elements
        imgMeProfile = findViewById(R.id.imgMeProfile);
        textMeName = findViewById(R.id.textMeName);
        textMeId = findViewById(R.id.textMeId);
        txtProfileLevel = findViewById(R.id.txtProfileLevel); // আপনার লেভেল দেখানোর TextView (যদি লেআউটে না থাকে, নিচে কোড সেফ রাখা হয়েছে)
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnVipStatus = findViewById(R.id.btnVipStatus);
        btnWalletRecharge = findViewById(R.id.btnWalletRecharge);
        btnHostCenter = findViewById(R.id.btnHostCenter);
        btnHostPolicy = findViewById(R.id.btnHostPolicy);
        btnLevelCenter = findViewById(R.id.btnLevelCenter); // লেভেল সেন্টার অপশন (যদি XML-এ আইডি না থাকে, নিচে নিরাপদ হ্যান্ডেল করা হয়েছে)

        loadUserData();
        syncUserLevelFromFirebase();

        // Edit Profile Click
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // 🌟 Level Center Click -> Opens 1 to 150 Level Details Chart Dialog
        if (btnLevelCenter != null) {
            btnLevelCenter.setOnClickListener(v -> showLevelCenterDialog());
        }

        // VIP Status Click
        if (btnVipStatus != null) {
            btnVipStatus.setOnClickListener(v -> showVipDialog());
        }

        // Wallet & Recharge Click
        if (btnWalletRecharge != null) {
            btnWalletRecharge.setOnClickListener(v -> showWalletRechargeDialog());
        }

        // Host Center Click
        if (btnHostCenter != null) {
            btnHostCenter.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("🎙️ Host Center")
                    .setMessage("Check your streaming targets, live duration, and daily earnings.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }

        // Host Policy Click -> Opens Policy Dialog
        if (btnHostPolicy != null) {
            btnHostPolicy.setOnClickListener(v -> showPolicyDialog());
        }
    }

    // 🌟 ১ থেকে ১৫০ লেভেল ক্যালকুলেটর লজিক
    private int calculateUserLevel(long totalCoins) {
        if (totalCoins < 3000000L) {
            return 1;
        }

        long currentRequirement = 3000000L;
        int level = 1;

        for (int i = 2; i <= 150; i++) {
            if (i <= 20) {
                currentRequirement += 5000000L; 
            } else if (i <= 50) {
                currentRequirement += 20000000L; 
            } else if (i <= 100) {
                currentRequirement += 25000000L; 
            } else {
                currentRequirement += 35000000L; 
            }

            if (totalCoins >= currentRequirement) {
                level = i;
            } else {
                break;
            }
        }
        return Math.min(level, 150);
    }

    private int getLevelColor(int level) {
        if (level <= 10) {
            return Color.parseColor("#C0C0C0"); // Silver
        } else if (level <= 20) {
            return Color.parseColor("#29B6F6"); // Light Blue
        } else if (level <= 50) {
            return Color.parseColor("#AB47BC"); // Royal Purple
        } else if (level <= 100) {
            return Color.parseColor("#FF4081"); // Diamond Pink
        } else {
            return Color.parseColor("#FFD700"); // Glowing Gold (101-150)
        }
    }

    // 🌟 ফায়ারবেস থেকে কয়েন এনে লেভেল ও কালার সেট করা
    private void syncUserLevelFromFirebase() {
        if (!currentUserId.isEmpty()) {
            userDatabaseRef.child(currentUserId).child("totalSpentCoins").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long spentCoins = 0;
                    if (snapshot.exists() && snapshot.getValue(Long.class) != null) {
                        spentCoins = snapshot.getValue(Long.class);
                    }
                    int currentLevel = calculateUserLevel(spentCoins);
                    if (txtProfileLevel != null) {
                        txtProfileLevel.setText("⭐ Lv." + currentLevel);
                        txtProfileLevel.setTextColor(getLevelColor(currentLevel));
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // 🌟 Level Center Dialog (১ থেকে ১৫০ লেভেল চার্ট দেখানোর জন্য)
    private void showLevelCenterDialog() {
        final Dialog levelDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 60);
        
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#2A1B4E"), Color.parseColor("#120A2A")}
        );
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("🏆 SR Voice Chat - Level Center (Lv.1 - Lv.150)");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("• Spend 1 coin = 1 experience point\n• Level 1 to 20: Progressive target scaling\n• Level 21 to 150: Advanced milestone scaling with 20M+ coin gaps\n• Unlock glowing gold, diamond pink & royal purple entry effects!");
        info.setTextColor(Color.WHITE);
        info.setTextSize(14);
        info.setPadding(20, 20, 20, 30);
        layout.addView(info);

        Button btnClose = new Button(this);
        btnClose.setText("Close");
        btnClose.setBackgroundColor(Color.parseColor("#FF4081"));
        btnClose.setTextColor(Color.WHITE);
        btnClose.setOnClickListener(v -> levelDialog.dismiss());
        layout.addView(btnClose);

        levelDialog.setContentView(layout);
        levelDialog.show();
    }

    // Policy Dialog Method
    private void showPolicyDialog() {
        final Dialog policyDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        policyDialog.setContentView(R.layout.dialog_policy);

        TextView btnClosePolicy = policyDialog.findViewById(R.id.btnClosePolicy);
        TextView textPolicyRoleBanner = policyDialog.findViewById(R.id.textPolicyRoleBanner);

        boolean isAgencyUser = prefs.getBoolean("IS_AGENCY_ROLE", true);

        if (!isAgencyUser && textPolicyRoleBanner != null) {
            textPolicyRoleBanner.setText("🎙️ Host Salary Target Chart (Level 1-22)");
        }

        if (btnClosePolicy != null) {
            btnClosePolicy.setOnClickListener(v -> policyDialog.dismiss());
        }

        policyDialog.show();
    }

    // Full Screen Wallet & Recharge Dialog Method
    private void showWalletRechargeDialog() {
        final Dialog rechargeDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        rechargeDialog.setContentView(R.layout.dialog_wallet_recharge);

        TextView textUserBalance = rechargeDialog.findViewById(R.id.textUserBalance);
        TextView btnCloseRecharge = rechargeDialog.findViewById(R.id.btnCloseRecharge);

        String currentCoins = prefs.getString("USER_COINS", "0");
        if (textUserBalance != null) {
            textUserBalance.setText(currentCoins + " Coins");
        }

        if (btnCloseRecharge != null) {
            btnCloseRecharge.setOnClickListener(v -> rechargeDialog.dismiss());
        }

        View.OnClickListener packageClick = v -> {
            new AlertDialog.Builder(MeActivity.this)
                .setTitle("💎 Coin Recharge")
                .setMessage("Please contact SR Voice Chat Room CEO for coin recharge.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        };

        LinearLayout pkg1 = rechargeDialog.findViewById(R.id.pkg1);
        LinearLayout pkg2 = rechargeDialog.findViewById(R.id.pkg2);
        LinearLayout pkg3 = rechargeDialog.findViewById(R.id.pkg3);
        LinearLayout pkg4 = rechargeDialog.findViewById(R.id.pkg4);

        if (pkg1 != null) pkg1.setOnClickListener(packageClick);
        if (pkg2 != null) pkg2.setOnClickListener(packageClick);
        if (pkg3 != null) pkg3.setOnClickListener(packageClick);
        if (pkg4 != null) pkg4.setOnClickListener(packageClick);

        rechargeDialog.show();
    }

    // VIP 1 to VIP 7 Privileges Dialog Method
    private void showVipDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_vip_status);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        androidx.recyclerview.widget.RecyclerView recyclerVipList = dialog.findViewById(R.id.recyclerVipList);
        Button btnClose = dialog.findViewById(R.id.btnCloseVipDialog);

        recyclerVipList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        java.util.List<VipModel> vipList = new java.util.ArrayList<>();
        vipList.add(new VipModel("VIP 1", "2,000,000 Coins", 0, 0, 0, "Basic Sparkle"));
        vipList.add(new VipModel("VIP 2", "5,000,000 Coins", 0, 0, 0, "Glow Ring"));
        vipList.add(new VipModel("VIP 3", "8,000,000 Coins", 0, 0, 0, "Silver Wings"));
        vipList.add(new VipModel("VIP 4", "15,000,000 Coins", 0, 0, 0, "Golden Crown"));
        vipList.add(new VipModel("VIP 5", "20,000,000 Coins", 0, 0, 0, "Fire Tiger Entry"));
        vipList.add(new VipModel("VIP 6", "30,000,000 Coins", 0, 0, 0, "Royal Dragon"));
        vipList.add(new VipModel("VIP 7", "50,000,000 Coins", 0, 0, 0, "Supreme Lord Effect"));

        VipAdapter adapter = new VipAdapter(this, vipList);
        recyclerVipList.setAdapter(adapter);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        syncUserLevelFromFirebase();
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
