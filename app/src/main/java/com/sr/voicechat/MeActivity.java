package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe, btnEditProfile;
    private TextView tvProfileName, tvProfileId, tvProfileGender, tvWalletCoins, tvVipStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        try {
            imgProfileMe = findViewById(R.id.imgProfileMe);
            btnEditProfile = findViewById(R.id.btnEditProfile);
            tvProfileName = findViewById(R.id.tvProfileName);
            tvProfileId = findViewById(R.id.tvProfileId);
            tvProfileGender = findViewById(R.id.tvProfileGender);
            tvWalletCoins = findViewById(R.id.tvWalletCoins);
            tvVipStatus = findViewById(R.id.tvVipStatus);

            loadUserData();

            // এডিট বা পেন্সিল আইকনে ক্লিক করলে
            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> 
                    Toast.makeText(this, "Edit Profile feature coming soon!", Toast.LENGTH_SHORT).show()
                );
            }

            // কয়েন ওয়ালেট ক্লিক করলে
            if (tvWalletCoins != null) {
                tvWalletCoins.setOnClickListener(v -> 
                    Toast.makeText(this, "Wallet Balance: 1,000 Coins", Toast.LENGTH_SHORT).show()
                );
            }

            // ভিআইপি স্ট্যাটাস ক্লিক করলে
            if (tvVipStatus != null) {
                tvVipStatus.setOnClickListener(v -> 
                    Toast.makeText(this, "VIP Status: Standard", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserData() {
        try {
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "Sr voice");
            String id = prefs.getString("USER_ID", "6533632");
            String gender = prefs.getString("USER_GENDER", "Male");
            String imageUriStr = prefs.getString("USER_IMAGE", null);

            if (tvProfileName != null) tvProfileName.setText(name);
            if (tvProfileId != null) tvProfileId.setText("ID: " + id);
            if (tvProfileGender != null) tvProfileGender.setText("Gender: " + gender);
            if (tvWalletCoins != null) tvWalletCoins.setText("🪙 Coins: 1,000");
            if (tvVipStatus != null) tvVipStatus.setText("👑 VIP: Not Active");

            // সেফলি ইমেজ লোড করার কোড
            if (imgProfileMe != null && imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    Uri imageUri = Uri.parse(imageUriStr);
                    imgProfileMe.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}
