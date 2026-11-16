package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomActivity extends AppCompatActivity {

    Button btnMic, btnGame;
    boolean isMicOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        btnMic = findViewById(R.id.btnMic);
        btnGame = findViewById(R.id.btnGame);

        // Mic - এখন শুধু On/Off দেখাবে, Voice পরে লাগাবো
        btnMic.setOnClickListener(v -> {
            isMicOn = !isMicOn;
            if(isMicOn){
                btnMic.setText("🎤 Mic ON");
                Toast.makeText(this, "Mic On", Toast.LENGTH_SHORT).show();
            } else {
                btnMic.setText("🎤 Mic OFF");
                Toast.makeText(this, "Mic Off", Toast.LENGTH_SHORT).show();
            }
        });

        // Game - Room থেকে Out হবে না, Room এর উপরেই খুলবে
        btnGame.setOnClickListener(v -> {
            // FruitPartyActivity খুলবে, Back দিলে আবার এই Room এ ফিরে আসবে
            Intent intent = new Intent(ChatRoomActivity.this, FruitPartyActivity.class);
            startActivity(intent);
        });
    }
}
