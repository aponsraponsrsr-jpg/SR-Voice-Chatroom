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
    private Button btnEditProfile, btnVipPrivilege, btnRechargeTopUp, btnStore, btnLevel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        textUserNameMe = findViewById(R.id.textUserNameMe);
        textPermanentIdMe = findViewById(R.id.textPermanentIdMe);
        btnCopyId = findViewById(R.id.btnCopyId);
        imgProfileMe = findViewById(R.id.imgProfileMe);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        
        btnVipPrivilege = findViewById(R.id.btnVipPrivilege);
        btnRechargeTopUp = findViewById(R.id.btnRechargeTopUp);
        btnStore = findViewById(R.id.btnStore);
        btnLevel = findViewById(R.id.btnLevel);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        loadUserProfileData();

        // এডিট প্রোফাইল বাটন সচল
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // আইডি কপি করার ফিচার
        if (btnCopyId != null) {
            btnCopyId.setOnClickListener(v -> {
                String idText = textPermanentIdMe.getText().toString().replace("ID: ", "").trim();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("User ID", idText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "ID Copied: " + idText, Toast.LENGTH_SHORT).show();
            });
        }

        // কামিং সুন লকড বাটনগুলোর অ্যাকশন (যেগুলো প্লে স্টোর লঞ্চের আগে লক থাকবে)
        if (btnVipPrivilege != null) {
            btnVipPrivilege.setOnClickListener(v -> Toast.makeText(this, "SR Voice - VIP Privilege Coming Soon!", Toast.LENGTH_SHORT).show());
        }
        if (btnRechargeTopUp != null) {
            btnRechargeTopUp.setOnClickListener(v -> Toast.makeText(this, "SR Voice - Recharge Top-Up Coming Soon!", Toast.LENGTH_SHORT).show());
        }
        if (btnStore != null) {
            btnStore.setOnClickListener(v -> Toast.makeText(this, "SR Voice - Store Coming Soon!", Toast.LENGTH_SHORT).show());
        }
        if (btnLevel != null) {
            btnLevel.setOnClickListener(v -> Toast.makeText(this, "SR Voice - Level System Coming Soon!", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileData(); // পেজে ব্যাক করলে রিয়েল-টাইমে ডাটা রিফ্রেশ হবে
    }

    private void loadUserProfileData() {
        String savedName = prefs.getString("USER_NAME", "SR Apon");
        String savedId = prefs.getString("MY_9_DIGIT_ID", "480384559");
        String savedImage = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

        if (textUserNameMe != null) {
            textUserNameMe.setText(savedName);
        }
        if (textPermanentIdMe != null) {
            textPermanentIdMe.setText("ID: " + savedId);
        }

        // প্রোফাইল ছবি লোড করার লজিক
        if (imgProfileMe != null && savedImage != null && !savedImage.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(savedImage, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedBitmap != null) {
                    imgProfileMe.setImageBitmap(decodedBitmap);
                    imgProfileMe.setPadding(0, 0, 0, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
