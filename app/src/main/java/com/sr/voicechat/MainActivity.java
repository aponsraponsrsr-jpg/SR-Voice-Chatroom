package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button createRoomButton, joinRoomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createRoomButton = findViewById(R.id.createRoomButton);
        joinRoomButton = findViewById(R.id.joinRoomButton);

        // নতুন রুম তৈরি করার বাটন (এখানে ওনার হিসেবে রুম ক্রিয়েট হবে)
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                intent.putExtra("isOwner", true);
                startActivity(intent);
            }
        });

        // রুমে প্রবেশ করার বাটন
        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                intent.putExtra("isOwner", false);
                startActivity(intent);
            }
        });
    }
}
