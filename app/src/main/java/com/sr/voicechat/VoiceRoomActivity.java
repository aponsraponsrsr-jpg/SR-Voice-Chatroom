package com.sr.voicechat;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    SharedPreferences prefs;
    String myName = "Ham Tum";
    int mySeat = -1;
    int onlineCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        prefs = getSharedPreferences("SRVoicePrefs", MODE_PRIVATE);
        myName = prefs.getString("userName", "Ham Tum");

        TextView tvHostName = findViewById(R.id.tvHostName);
        TextView tvOnline = findViewById(R.id.tvOnline);
        
        tvHostName.setText(myName + " ✏️");
        tvOnline.setText("👥 online " + onlineCount);

        // এডিট অপশন - নামে টাচ করলেই যে যার নাম লাগাতে পারবে
        tvHostName.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Edit Your Name");
            final EditText input = new EditText(this);
            input.setText(myName);
            builder.setView(input);
            builder.setPositiveButton("Save", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    myName = newName;
                    prefs.edit().putString("userName", myName).apply();
                    tvHostName.setText(myName + " ✏️");
                    if (mySeat != -1) {
                        int id = getResources().getIdentifier("seat" + mySeat, "id", getPackageName());
                        ((TextView) findViewById(id)).setText(myName.substring(0,1));
                    }
                    Toast.makeText(this, "Name saved: " + myName, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        // সিটে বসা - + এ টাচ করলে মাইকে বসবে
        for (int i = 2; i <= 10; i++) {
            int id = getResources().getIdentifier("seat" + i, "id", getPackageName());
            int num = i;
            findViewById(id).setOnClickListener(v -> {
                if (mySeat != -1) {
                    Toast.makeText(this, "You are already on seat " + mySeat, Toast.LENGTH_SHORT).show();
                    return;
                }
                mySeat = num;
                ((TextView) v).setText(myName.substring(0,1).toUpperCase());
                ((TextView) v).setBackgroundColor(0xFF4CAF50);
                onlineCount++;
                tvOnline.setText("👥 online " + onlineCount);
                Toast.makeText(this, "Sat on seat " + num, Toast.LENGTH_SHORT).show();
            });
        }

        findViewById(R.id.seat1).setOnClickListener(v -> 
            Toast.makeText(this, "Host: " + myName + " 👑", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnMic).setOnClickListener(v -> Toast.makeText(this, "Mic On/Off", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnGiftBottom).setOnClickListener(v -> Toast.makeText(this, "Gift 🎁", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnGame).setOnClickListener(v -> Toast.makeText(this, "Game 🎮", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnMore).setOnClickListener(v -> Toast.makeText(this, "More Options", Toast.LENGTH_SHORT).show());
    }
}
