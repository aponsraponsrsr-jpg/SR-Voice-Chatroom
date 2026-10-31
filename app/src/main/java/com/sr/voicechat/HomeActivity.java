package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvMeTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // অথবা তোমার হোম লেআউট

        // নিচের মেনুর 'Me' বাটন খুঁজে বের করা (তোমার লেআউটের আইডি অনুযায়ী এটি মিলিয়ে নেবে)
        tvMeTab = findViewById(R.id.tvMeTab); // লেআউটে আইডি না থাকলে নিচে আইডি সহ লেআউট দেওয়া হলো

        if (tvMeTab != null) {
            tvMeTab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }
    }
}
