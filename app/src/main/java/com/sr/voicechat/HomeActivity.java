package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnPlus).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            startActivity(intent);
        });
    }
}
