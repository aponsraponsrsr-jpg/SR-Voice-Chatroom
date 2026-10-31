package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView tvProfileName, tvProfileId, tvProfileGender, tvWalletCoins, tvVipStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        imgProfileMe = findViewById(R.id.imgProfileMe);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileId = findViewById(R.id.tvProfileId);
        tvProfileGender = findViewById(R.id.tvProfileGender);
        tvWalletCoins = findViewById(R.id.tvWalletCoins);
        tvVipStatus = findViewById(R.id.tvVipStatus);

        loadUserData();

        // কয়েন ওয়ালেট ক্লিক করলে মেসেজ বা রিচার্জ অপশন
        if (tvWalletCoins != null) {
            tvWalletCoins.setOnClickListener(v -> 
                Toast.makeText(this, "Coin Wallet: 1000 Coins", Toast.LENGTH_SHORT).show()
            );
        }

        // ভিআইপি স্ট্যাটাস ক্লিক করলে
        if (tvVipStatus != null) {
            tvVipStatus.setOnClickListener(v -> 
                Toast.makeText(this, "VIP Status: Regular Member", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void loadUserData() {
        try {
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "User Name");
            String id = prefs.getString("USER_ID", "12345678");
            String gender = prefs.getString("USER_GENDER", "Male");
            String imageUriStr = prefs.getString("USER_IMAGE", null);

            if (tvProfileName != null) tvProfileName.setText(name);
            if (tvProfileId != null) tvProfileId.setText("ID: " + id);
            if (tvProfileGender != null) tvProfileGender.setText("Gender: " + gender);
            if (tvWalletCoins != null) tvWalletCoins.setText("🪙 Coins: 1,000");
            if (tvVipStatus != null) tvVipStatus.setText("👑 VIP: Not Active");

            // ক্র্যাশ রোধ করার জন্য ট্রাই-ক্যাচ ও সেফ চেক সহ ইমেজ লোডিং
            if (imgProfileMe != null && imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    Uri imageUri = Uri.parse(imageUriStr);
                    imgProfileMe.setImageURI(imageUri);
                } catch (SecurityException se) {
                    // যদি পারমিশন ইস্যু থাকে তবে ডিফল্ট ব্যাকগ্রাউন্ডে থাকবে, ক্র্যাশ করবে না
                    se.printStackTrace();
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
        loadUserData(); // পেজে ফিরে আসলে ডেটা রিফ্রেশ হবে
    }
}
