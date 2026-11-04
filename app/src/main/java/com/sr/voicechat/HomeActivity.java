package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView btnPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // এক্সএমএল লেআউটের প্লাস (+) বাটনটি ইনিশিয়ালাইজেশন
        btnPlus = findViewById(R.id.btnPlus);

        // প্লাস (+) বাটনে ক্লিক করলে সরাসরি ভয়েস রুম ওপেন হবে
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                startActivity(intent);
            });
        }
    }
}
