package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private TextView btnEditProfile, tvProfileName, tvProfileId, tvProfileGender, tvWalletCoins, tvVipStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_me);

            tvProfileName = findViewById(R.id.tvProfileName);
            tvProfileId = findViewById(R.id.tvProfileId);
            tvProfileGender = findViewById(R.id.tvProfileGender);
            tvWalletCoins = findViewById(R.id.tvWalletCoins);
            tvVipStatus = findViewById(R.id.tvVipStatus);
            btnEditProfile = findViewById(R.id.btnEditProfile);

            if (tvProfileName != null) tvProfileName.setText("Sr voice");
            if (tvProfileId != null) tvProfileId.setText("ID: 6533632");
            if (tvProfileGender != null) tvProfileGender.setText("Gender: Male");
            if (tvWalletCoins != null) tvWalletCoins.setText("🪙 Coins: 1,000");
            if (tvVipStatus != null) tvVipStatus.setText("👑 VIP: Not Active");

            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> 
                    Toast.makeText(this, "Edit profile coming soon", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
