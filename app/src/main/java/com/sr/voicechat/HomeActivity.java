package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // সেফ নেভিগেশন লজিক
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }
    }
}
