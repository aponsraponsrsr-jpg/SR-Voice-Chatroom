package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private TextView textUserNameMe, textPermanentIdMe;
    private Button btnEditProfile;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // সাময়িকভাবে ক্র্যাশ এড়াতে সাধারণ এন্ড্রয়েড ডিফল্ট লেআউট বা আমাদের সিম্পল লেআউট ব্যবহার করছি
        setContentView(R.layout.activity_me);

        textUserNameMe = findViewById(R.id.textUserNameMe);
        textPermanentIdMe = findViewById(R.id.textPermanentIdMe);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        String savedName = prefs.getString("USER_NAME", "SR Apon");
        String savedId = prefs.getString("MY_9_DIGIT_ID", "480384559");

        if (textUserNameMe != null) {
            textUserNameMe.setText("Name: " + savedName);
        }
        if (textPermanentIdMe != null) {
            textPermanentIdMe.setText("ID: " + savedId);
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }
    }
}
