package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btnOpenRoom = findViewById(R.id.btnOpenRoom);
        btnOpenRoom.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
            startActivity(intent);
        });
    }
}
