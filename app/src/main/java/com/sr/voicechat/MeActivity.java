package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe, btnEditProfileIcon;
    private TextView textUserNameMe, textPermanentIdMe;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        // সেফলি ভিউ ইনিশিয়ালাইজেশন
        imgProfileMe = findViewById(R.id.imgProfileMe);
        btnEditProfileIcon = findViewById(R.id.btnEditProfileIcon);
        textUserNameMe = findViewById(R.id.textUserNameMe);
        textPermanentIdMe = findViewById(R.id.textPermanentIdMe);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // সেভ করা নাম ও আইডি লোড করা (নাল চেকসহ)
        String savedName = prefs.getString("USER_NAME", "SR Apon");
        String savedPermanentId = prefs.getString("MY_9_DIGIT_ID", "480384559");

        if (textUserNameMe != null) {
            textUserNameMe.setText(savedName);
        }
        if (textPermanentIdMe != null) {
            textPermanentIdMe.setText("ID: " + savedPermanentId);
        }

        // এডিট প্রোফাইল আইকনে ক্লিক হ্যান্ডলার
        if (btnEditProfileIcon != null) {
            btnEditProfileIcon.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
