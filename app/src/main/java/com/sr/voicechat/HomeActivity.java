package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Search button - Top Right
        View btnSearch = findViewById(R.id.btnSearch);
        if(btnSearch != null){
            btnSearch.setOnClickListener(v -> {
                Toast.makeText(this, "SR Official Search - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        // 4 Rooms Click
        View room1 = findViewById(R.id.room1);
        View room2 = findViewById(R.id.room2);
        View room3 = findViewById(R.id.room3);
        View room4 = findViewById(R.id.room4);

        View.OnClickListener roomClick = v -> {
            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            startActivity(intent);
        };

        if(room1 != null) room1.setOnClickListener(roomClick);
        if(room2 != null) room2.setOnClickListener(roomClick);
        if(room3 != null) room3.setOnClickListener(roomClick);
        if(room4 != null) room4.setOnClickListener(roomClick);

        // PLUS BUTTON - MIDDLE - ROOM OPEN
        View btnPlus = findViewById(R.id.btnPlus);
        if(btnPlus != null){
            btnPlus.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                startActivity(intent);
                Toast.makeText(this, "SR Official Room Opening...", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
