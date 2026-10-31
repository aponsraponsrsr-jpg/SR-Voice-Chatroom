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

        // Me পেজে যাওয়ার জন্য সিম্পল ক্লিক লজিক
        View meBtn = findViewById(R.id.meLayout);
        if (meBtn != null) {
            meBtn.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }
    }
}
