package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button createRoomButton, joinRoomButton;
    private TextView userIdTextView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createRoomButton = findViewById(R.id.createRoomButton);
        joinRoomButton = findViewById(R.id.joinRoomButton);
        userIdTextView = findViewById(R.id.userIdTextView);
        
        prefs = getSharedPreferences("SR_Prefs", MODE_PRIVATE);

        // --- Step 1: 7 Digit Mixed ID + Permanent Logic ---
        String userId = prefs.getString("user_id", null);
        if (userId == null) {
            // প্রথমবার - নতুন ৭ ডিজিটের মিক্সড ID বানাও
            userId = generateMixed7DigitId();
            prefs.edit().putString("user_id", userId).apply();
        }
        // ID সবসময় দেখাবে - যেমন ID: 7391058
        userIdTextView.setText("ID: " + userId);

        // Button to create a new voice room
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                intent.putExtra("isOwner", true);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        // Button to join an existing room
        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                intent.putExtra("isOwner", false);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }

    // ৭ ডিজিটের মিক্সড ID - সিরিয়াল না, র‍্যান্ডম
    private String generateMixed7DigitId() {
        Random random = new Random();
        int firstDigit = random.nextInt(9) + 1; // 1-9 যাতে 0 দিয়ে শুরু না হয়
        StringBuilder id = new StringBuilder();
        id.append(firstDigit);
        for (int i = 0; i < 6; i++) {
            id.append(random.nextInt(10)); // বাকি 6 টা 0-9
        }
        return id.toString();
    }
}
