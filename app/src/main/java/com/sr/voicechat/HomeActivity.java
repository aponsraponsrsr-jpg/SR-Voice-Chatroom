package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button btnCreateRoom, btnJoinRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        btnJoinRoom = findViewById(R.id.btnJoinRoom);

        btnCreateRoom.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            startActivity(intent);
        });

        btnJoinRoom.setOnClickListener(v -> {
            Toast.makeText(this, "Join Room Coming Soon! 🎙️", Toast.LENGTH_SHORT).show();
        });
    }
}
