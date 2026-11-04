package com.sr.voicechat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    private int currentSeat = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        findViewById(R.id.btnPower).setOnClickListener(v -> showPowerDialog());

        for (int i = 1; i <= 10; i++) {
            int resId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            View seat = findViewById(resId);
            int seatNum = i;
            if (seat != null) {
                seat.setOnClickListener(v -> onSeatClicked(seatNum));
            }
        }

        findViewById(R.id.btnMic).setOnClickListener(v -> Toast.makeText(this, "Mic On/Off 🎙️", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnChat).setOnClickListener(v -> Toast.makeText(this, "Chat Open 💬", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnGift).setOnClickListener(v -> Toast.makeText(this, "Gift Box 🎁", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnGame).setOnClickListener(v -> Toast.makeText(this, "Game Zone 🎮", Toast.LENGTH_SHORT).show());
    }

    private void onSeatClicked(int seatNum) {
        if (currentSeat == seatNum) {
            new AlertDialog.Builder(this)
                    .setTitle("Seat " + seatNum)
                    .setItems(new String[]{"Leave Mic", "Close"}, (dialog, which) -> {
                        if (which == 0) {
                            currentSeat = -1;
                            Toast.makeText(this, "Left Seat " + seatNum + " ✅", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Seat " + seatNum)
                    .setItems(new String[]{"Take Mic", "View Profile"}, (dialog, which) -> {
                        if (which == 0) {
                            if (currentSeat != -1) {
                                Toast.makeText(this, "Moved from " + currentSeat + " to " + seatNum + " ✅", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Joined Seat " + seatNum + " 🎙️", Toast.LENGTH_SHORT).show();
                            }
                            currentSeat = seatNum;
                        }
                    }).show();
        }
    }

    private void showPowerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Room?")
                .setItems(new String[]{"Minimize", "Leave Room", "Exit App"}, (dialog, which) -> {
                    if (which == 0) {
                        moveTaskToBack(true);
                    } else if (which == 1) {
                        finish();
                    } else {
                        finishAffinity();
                    }
                }).show();
    }
}
