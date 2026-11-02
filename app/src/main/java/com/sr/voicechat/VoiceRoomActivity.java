package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String roomName = getIntent().getStringExtra("roomName");
        if (roomName == null) roomName = "My Room";
        TextView tv = new TextView(this);
        tv.setText("Voice Room\n" + roomName + "\n\nConnected!");
        tv.setTextSize(22);
        tv.setPadding(50, 300, 50, 50);
        setContentView(tv);
    }
}
