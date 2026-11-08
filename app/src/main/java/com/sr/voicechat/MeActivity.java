package com.sr.voicechat;

import android.app.Dialog;
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

        // VIP Status Click -> Custom Dialog with VIP 1 to VIP 7 List
        if (btnVipStatus != null) {
            btnVipStatus.setOnClickListener(v -> showVipDialog());
        }

        // Wallet & Recharge Click -> Full Screen Premium Dialog (Reseller Demo Mode)
        if (btnWalletRecharge != null) {
            btnWalletRecharge.setOnClickListener(v -> showWalletRechargeDialog());
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

    // Full Screen Wallet & Recharge Dialog Method
    private void showWalletRechargeDialog() {
        final Dialog rechargeDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        rechargeDialog.setContentView(R.layout.dialog_wallet_recharge);

        TextView textUserBalance = rechargeDialog.findViewById(R.id.textUserBalance);
        TextView btnCloseRecharge = rechargeDialog.findViewById(R.id.btnCloseRecharge);

        // Fetch current user coin balance (Demo: 411 coins or stored in prefs)
        String currentCoins = prefs.getString("USER_COINS", "411");
        if (textUserBalance != null) {
            textUserBalance.setText(currentCoins + " Coins");
        }

        if (btnCloseRecharge != null) {
            btnCloseRecharge.setOnClickListener(v -> rechargeDialog.dismiss());
        }

        rechargeDialog.show();
    }

    // VIP 1 to VIP 7 Privileges Dialog Method with Updated Coin Amounts
    private void showVipDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_vip_status);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        androidx.recyclerview.widget.RecyclerView recyclerVipList = dialog.findViewById(R.id.recyclerVipList);
        Button btnClose = dialog.findViewById(R.id.btnCloseVipDialog);

        recyclerVipList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // VIP 1 to VIP 7 Data with High Coin Amounts
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
