package com.sr.voice.chat;

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

        // Safe check - if buttons exist, make them work
        try {
            View btnCreateRoom = findViewById(R.id.btnCreateRoom);
            if (btnCreateRoom != null) {
                btnCreateRoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openVoiceRoom();
                    }
                });
            }

            // If you have any room click, open voice room
            View anyRoom = findViewById(R.id.roomCard);
            if (anyRoom != null) {
                anyRoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openVoiceRoom();
                    }
                });
            }

        } catch (Exception e) {
            // No crash - just open home
        }
    }

    private void openVoiceRoom() {
        try {
            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice Room Opening Soon 💜", Toast.LENGTH_SHORT).show();
        }
    }
}
