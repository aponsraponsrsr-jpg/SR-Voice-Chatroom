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

        // প্রোফাইল বা Me অপশনে ক্লিক করলে MeActivity তে যাওয়ার নিরাপদ লজিক
        View meBtn = findViewById(R.id.meLayout);
        if (meBtn != null) {
            meBtn.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }
    }
}
