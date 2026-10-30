package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);

        String userId = prefs.getString("userId", "SR123");
        
        // + Create Button -> Room On
        findViewById(R.id.navCreate).setOnClickListener(v -> {
            Intent i = new Intent(this, VoiceRoomActivity.class);
            i.putExtra("ROOM_ID", userId);
            i.putExtra("IS_HOST", true);
            startActivity(i);
        });
        
        findViewById(R.id.btnCreateFirst).setOnClickListener(v -> {
            Intent i = new Intent(this, VoiceRoomActivity.class);
            i.putExtra("ROOM_ID", userId);
            i.putExtra("IS_HOST", true);
            startActivity(i);
        });

        findViewById(R.id.bannerArea).setOnClickListener(v -> 
            Toast.makeText(this, "Banner Area - Future Ad Space", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navMe).setOnClickListener(v -> 
            startActivity(new Intent(this, ProfileActivity.class))
        );
    }
}
