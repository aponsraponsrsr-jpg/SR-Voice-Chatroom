package com.sr.voicechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileId, tvProfileGender, tvWalletCoins, tvVipStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        try {
            tvProfileName = findViewById(R.id.tvProfileName);
            tvProfileId = findViewById(R.id.tvProfileId);
            tvProfileGender = findViewById(R.id.tvProfileGender);
            tvWalletCoins = findViewById(R.id.tvWalletCoins);
            tvVipStatus = findViewById(R.id.tvVipStatus);

            // SharedPreferences থেকে সেফলি ডেটা লোড করা
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "SR User");
            String id = prefs.getString("USER_ID", "12345678");
            String gender = prefs.getString("USER_GENDER", "Male");

            if (tvProfileName != null) tvProfileName.setText(name);
            if (tvProfileId != null) tvProfileId.setText("ID: " + id);
            if (tvProfileGender != null) tvProfileGender.setText("Gender: " + gender);
            if (tvWalletCoins != null) tvWalletCoins.setText("🪙 Coins: 1,000");
            if (tvVipStatus != null) tvVipStatus.setText("👑 VIP: Not Active");

            // ওয়ালেট ক্লিক করলে
            if (tvWalletCoins != null) {
                tvWalletCoins.setOnClickListener(v -> 
                    Toast.makeText(this, "Wallet Balance: 1,000 Coins", Toast.LENGTH_SHORT).show()
                );
            }

            // ভিআইপি ক্লিক করলে
            if (tvVipStatus != null) {
                tvVipStatus.setOnClickListener(v -> 
                    Toast.makeText(this, "VIP Status: Standard", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        }
    }
}
