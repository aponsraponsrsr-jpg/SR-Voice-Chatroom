package com.srvoiceapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class VoiceRoomActivity extends AppCompatActivity {

    private boolean isMicOn = true;
    private TextView tvOnlineCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        tvOnlineCount = findViewById(R.id.tvOnlineCount);
        ImageButton btnPower = findViewById(R.id.btnPower);
        Button btnMic = findViewById(R.id.btnMicToggle);
        Button btnChat = findViewById(R.id.btnChatOption);
        Button btnGift = findViewById(R.id.btnGiftBox);
        Button btnGame = findViewById(R.id.btnGameBox);

        // Online Count Random Update
        updateOnlineCount();

        // 1. Power Button - Exit Room
        btnPower.setOnClickListener(v -> {
            Toast.makeText(this, "Room থেকে বের হচ্ছো বন্ধু!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // 2. Mic Toggle
        btnMic.setOnClickListener(v -> {
            isMicOn = !isMicOn;
            if(isMicOn){
                btnMic.setText("🎙️ Mic ON");
                Toast.makeText(this, "Mic চালু বন্ধু!", Toast.LENGTH_SHORT).show();
            } else {
                btnMic.setText("🔇 Mic OFF");
                Toast.makeText(this, "Mic বন্ধ বন্ধু!", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Chat
        btnChat.setOnClickListener(v -> {
            Toast.makeText(this, "Chat Box খুলছে বন্ধু!", Toast.LENGTH_SHORT).show();
        });

        // 4. Gift
        btnGift.setOnClickListener(v -> {
            Toast.makeText(this, "🎁 Gift Store আসছে বন্ধু!", Toast.LENGTH_SHORT).show();
        });

        // 5. Game
        btnGame.setOnClickListener(v -> {
            Toast.makeText(this, "🎮 Game Box আসছে বন্ধু!", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateOnlineCount(){
        int count = new Random().nextInt(200) + 50;
        tvOnlineCount.setText("Online: " + count + " | 🕌 Mosque Theme Active");
    }
}
