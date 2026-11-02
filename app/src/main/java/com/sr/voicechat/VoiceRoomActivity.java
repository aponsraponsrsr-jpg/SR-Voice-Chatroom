package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    TextView tvRoomName, tvRoomId, tvWelcome;
    ImageView btnMic, btnGift, btnGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room); // তোমার নতুন 10 সিটের XML

        // XML এর id গুলা
        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomId = findViewById(R.id.tvRoomId);
        tvWelcome = findViewById(R.id.tvWelcome);
        btnMic = findViewById(R.id.btnMic);
        btnGift = findViewById(R.id.btnGift);
        btnGame = findViewById(R.id.btnGame);

        // STEP 1 থেকে আসা Group Name + Announcement
        String groupName = getIntent().getStringExtra("groupName");
        String announcement = getIntent().getStringExtra("announcement");
        String roomNameExtra = getIntent().getStringExtra("roomName");

        // groupName যদি থাকে সেটা দেখাবে, না হলে roomName, না হলে My Room
        if (groupName != null && !groupName.isEmpty()) {
            tvRoomName.setText(groupName);
        } else if (roomNameExtra != null && !roomNameExtra.isEmpty()) {
            tvRoomName.setText(roomNameExtra);
        } else {
            tvRoomName.setText("Sr voice chat");
        }

        if (announcement != null && !announcement.isEmpty()) {
            tvWelcome.setText("Notice: " + announcement + " - স্বাগতম সবাইকে");
        } else {
            tvWelcome.setText("Notice: welcome - স্বাগতম সবাইকে, বাংলাদেশ অফিসিয়াল ম্যানেজমেন্ট");
        }

        tvRoomId.setText("ID: 121");

        // Bottom Buttons Click
        btnMic.setOnClickListener(v -> {
            Toast.makeText(this, "Mic On/Off", Toast.LENGTH_SHORT).show();
        });

        btnGift.setOnClickListener(v -> {
            Toast.makeText(this, "Gift Box 🎁", Toast.LENGTH_SHORT).show();
        });

        btnGame.setOnClickListener(v -> {
            Toast.makeText(this, "Game 🎮 Starting...", Toast.LENGTH_SHORT).show();
        });

        // 10 Seat Click
        int[] seatIds = {R.id.seat1, R.id.seat2, R.id.seat3, R.id.seat4, R.id.seat5,
                         R.id.seat6, R.id.seat7, R.id.seat8, R.id.seat9, R.id.seat10};
        
        for (int i = 0; i < seatIds.length; i++) {
            int seatNumber = i + 1;
            ImageView seat = findViewById(seatIds[i]);
            seat.setOnClickListener(v -> {
                Toast.makeText(this, "Seat " + seatNumber + " clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
