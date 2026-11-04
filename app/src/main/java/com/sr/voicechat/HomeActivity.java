package com.sr.voicechat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Open Voice Room when create button clicked
        try {
            View btn = findViewById(R.id.btnCreateRoom);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    Intent i = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                    startActivity(i);
                });
            }
        } catch (Exception e) {}
    }
}
