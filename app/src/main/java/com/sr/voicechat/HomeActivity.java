package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // হোমপেজের নিচে থাকা 'Me' অপশনের সঠিক আইডি দিয়ে লিংক করা
        // (তোমার লেআউটে Me লেখার আইডি যদি tvMeTab বা tvMe হয়, সেটি এখানে কাজ করবে)
        TextView tvMeTab = findViewById(R.id.tvMeTab);
        if (tvMeTab != null) {
            tvMeTab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }
    }
}
