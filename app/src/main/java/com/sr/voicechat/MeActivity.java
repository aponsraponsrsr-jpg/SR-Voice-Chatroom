package com.sr.voicechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView btnEditProfile, tvProfileName, tvProfileId, tvProfileGender, tvWalletCoins, tvVipStatus;

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

            // SharedPreferences থেকে ডেটা লোড করা
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "Sr voice");
            String id = prefs.getString("USER_ID", "6533632");
            String gender = prefs.getString("USER_GENDER", "Male");

            if (tvProfileName != null) tvProfileName.setText(name);
            if (tvProfileId != null) tvProfileId.setText("ID: " + id);
            if (tvProfileGender != null) tvProfileGender.setText("Gender: " + gender);
            if (tvWalletCoins != null) tvWalletCoins.setText("🪙 Coins: 1,000");
            if (tvVipStatus != null) tvVipStatus.setText("👑 VIP: Not Active");

            // এডিট প্রొফাইল ক্লিক
            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> 
                    Toast.makeText(this, "Edit Profile coming soon!", Toast.LENGTH_SHORT).show()
                );
            }

            // ওয়ালেট ক্লিক
            if (tvWalletCoins != null) {
                tvWalletCoins.setOnClickListener(v -> 
                    Toast.makeText(this, "Wallet Balance: 1,000 Coins", Toast.LENGTH_SHORT).show()
                );
            }

            // ভিআইপি ক্লিক
            if (tvVipStatus != null) {
                tvVipStatus.setOnClickListener(v -> 
                    Toast.makeText(this, "VIP Status: Standard", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
