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

        // ১. বটম মেনুর 'Me' অপশনে ক্লিক করলে MeActivity ওপেন হবে
        TextView tvMe = findViewById(R.id.tvMe);
        if (tvMe != null) {
            tvMe.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }

        // ২. '+' আইকনে ক্লিক করলে VoiceRoomActivity ওপেন হবে
        TextView btnPlus = findViewById(R.id.btnPlus);
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                startActivity(intent);
            });
        }
    }
}
