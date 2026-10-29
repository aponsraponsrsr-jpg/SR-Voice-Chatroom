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

        String userId = prefs.getString("user_id", null);
        if (userId == null) {
            userId = generateMixed7DigitId();
            prefs.edit().putString("user_id", userId).apply();
        }
        userIdTextView.setText("ID: " + userId);

        // FIXED LINE - final করতে হবে
        final String finalUserId = userId;

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                intent.putExtra("isOwner", true);
                intent.putExtra("user_id", finalUserId);
                startActivity(intent);
            }
        });

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                intent.putExtra("isOwner", false);
                intent.putExtra("user_id", finalUserId);
                startActivity(intent);
            }
        });
    }

    private String generateMixed7DigitId() {
        Random random = new Random();
        int firstDigit = random.nextInt(9) + 1;
        StringBuilder id = new StringBuilder();
        id.append(firstDigit);
        for (int i = 0; i < 6; i++) {
            id.append(random.nextInt(10));
        }
        return id.toString();
    }
}
