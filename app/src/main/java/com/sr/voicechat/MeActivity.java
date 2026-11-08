package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class MeActivity extends AppCompatActivity {

    private ImageView imgMeProfile, btnEditProfile;
    private TextView textMeName, textMeId;
    private LinearLayout btnVipStatus, btnWalletRecharge, btnHostCenter, btnHostPolicy;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // Binding UI elements
        imgMeProfile = findViewById(imgMeProfile);
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

        // Host Policy Click -> Opens Dynamic Policy Dialog
        if (btnHostPolicy != null) {
            btnHostPolicy.setOnClickListener(v -> showPolicyDialog());
        }
    }

    // Role-Based Dynamic Policy Dialog Method
    private void showPolicyDialog() {
        final Dialog policyDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        policyDialog.setContentView(R.layout.dialog_policy);

        TextView btnClosePolicy = policyDialog.findViewById(R.id.btnClosePolicy);
        TextView textPolicyRoleBanner = policyDialog.findViewById(R.id.textPolicyRoleBanner);
        TextView headerAgency = policyDialog.findViewById(R.id.headerAgency);
        TextView headerTotal = policyDialog.findViewById(R.id.headerTotal);

        // Check if current user is an Agency/Admin or regular Host
        boolean isAgencyUser = prefs.getBoolean("IS_AGENCY_ROLE", true); // Default true for demo/owner view

        if (!isAgencyUser) {
            // Regular Host View: Hide Agency commission and Total payout columns description
            if (headerAgency != null) headerAgency.setVisibility(View.GONE);
            if (headerTotal != null) headerTotal.setVisibility(View.GONE);
            if (textPolicyRoleBanner != null) {
                textPolicyRoleBanner.setText("🎙️ Host Salary Target Chart (Level 1-22)");
            }
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
