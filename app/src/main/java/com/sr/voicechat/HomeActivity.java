package com.sr.voice.chat;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Search button - top right corner
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Toast.makeText(this, "Search: Room ID / User ID - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        // Bottom Nav - Home, Moment, Inbox, Mine (Right to Left as you said)
        // Home is selected by default (Gold color)
        Toast.makeText(this, "SR OFFICIAL Home Loaded", Toast.LENGTH_SHORT).show();
    }
}
