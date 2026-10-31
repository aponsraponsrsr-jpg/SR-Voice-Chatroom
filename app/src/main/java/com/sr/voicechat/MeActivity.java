package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView tvProfileName, tvProfileId, tvProfileGender, btnWallet, btnVip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_me);

            // লেআউট থেকে আইডিগুলো ইনিশিয়ালাইজ করা
            imgProfileMe = findViewById(R.id.imgProfileMe);
            tvProfileName = findViewById(R.id.tvProfileName);
            tvProfileId = findViewById(R.id.tvProfileId);
            tvProfileGender = findViewById(R.id.tvProfileGender);
            btnWallet = findViewById(R.id.btnWallet);
            btnVip = findViewById(R.id.btnVip);

            // SharedPreferences থেকে ইউজারের সেভ করা ডেটা লোড করা
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "SR User");
            String id = prefs.getString("USER_ID", "1000000");
            String gender = prefs.getString("USER_GENDER", "Male");
            String imageUriStr = prefs.getString("USER_IMAGE", null);

            // ভিউগুলোতে ডেটা সেট করা
            if (tvProfileName != null) {
                tvProfileName.setText(name);
            }
            if (tvProfileId != null) {
                tvProfileId.setText("ID: " + id);
            }
            if (tvProfileGender != null) {
                tvProfileGender.setText("Gender: " + gender);
            }

            if (imgProfileMe != null && imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    imgProfileMe.setImageURI(Uri.parse(imageUriStr));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Wallet Click Action
            if (btnWallet != null) {
                btnWallet.setOnClickListener(v -> {
                    Toast.makeText(MeActivity.this, "Wallet opening...", Toast.LENGTH_SHORT).show();
                });
            }

            // VIP Click Action
            if (btnVip != null) {
                btnVip.setOnClickListener(v -> {
                    Toast.makeText(MeActivity.this, "VIP section opening...", Toast.LENGTH_SHORT).show();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
