package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button btnOpenMe, btnOpenRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnOpenMe = findViewById(R.id.btnOpenMe);
        btnOpenRoom = findViewById(R.id.btnOpenRoom);

        if (btnOpenMe != null) {
            btnOpenMe.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }

        if (btnOpenRoom != null) {
            btnOpenRoom.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, VoiceroomActivity.class);
                intent.putExtra("ROOM_NAME", "SR Voice Room");
                startActivity(intent);
            });
        }
    }
}
