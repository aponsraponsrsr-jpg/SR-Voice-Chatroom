package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe, btnEditProfileIcon;
    private TextView textUserNameMe, textPermanentIdMe;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        // ইনিশিয়ালাইজেশন
        imgProfileMe = findViewById(R.id.imgProfileMe);
        btnEditProfileIcon = findViewById(R.id.btnEditProfileIcon);
        textUserNameMe = findViewById(R.id.textUserNameMe);
        textPermanentIdMe = findViewById(R.id.textPermanentIdMe);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // সেভ করা ইউজার নাম এবং পার্মানেন্ট আইডি লোড করা
        String savedName = prefs.getString("USER_NAME", "Muktadul Rahaman");
        String savedPermanentId = prefs.getString("MY_9_DIGIT_ID", "480384559");

        textUserNameMe.setText(savedName);
        textPermanentIdMe.setText("ID: " + savedPermanentId);

        // এডিট প্রোফাইল পেন্সিল আইকনে ক্লিক হ্যান্ডলার
        btnEditProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // অন্যান্য প্রিমিয়াম মেনু অপশনগুলোতে ক্লিক করলে কামিং সুন পপআপ দেখানোর লজিক
        setupMenuClickListeners();
    }

    private void setupMenuClickListeners() {
        // মেনু অপশনগুলো টাস্ক বা ক্লিকের জন্য এখানে টোস্ট বা ডায়লগ সেট করা যেতে পারে
        // যেমন: VIP Center, Wallet, Store, Level, Family ইত্যাদি
    }
}
