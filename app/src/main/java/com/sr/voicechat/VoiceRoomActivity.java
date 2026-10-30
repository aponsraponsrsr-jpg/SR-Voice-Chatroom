package com.sr.voicechatroom;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView tvHostName, tvOnline, middleBg, tvName2;
    private TextView seat1, seat2, seat3, seat4, seat5, seat6, seat7, seat8, seat9, seat10;
    private int myCurrentSeat = -1;
    private String myName = "Ham Tum";
    private final int[] bgColors = {0xFFFFE4B5, 0xFFB2DFDB, 0xFFFFCDD2, 0xFFBBDEFB, 0xFF1A1A2E};
    private final String[] bgNames = {"Beach Heart 💖", "Ocean Blue 🌊", "Sunset Pink 🌅", "Sky Blue ☁️", "Dark Night 🌙"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        tvHostName = findViewById(R.id.tvHostName);
        tvOnline = findViewById(R.id.tvOnline);
        middleBg = findViewById(R.id.middleBg);
        tvName2 = findViewById(R.id.tvName2);

        seat1 = findViewById(R.id.seat1);
        seat2 = findViewById(R.id.seat2);
        seat3 = findViewById(R.id.seat3);
        seat4 = findViewById(R.id.seat4);
        seat5 = findViewById(R.id.seat5);
        seat6 = findViewById(R.id.seat6);
        seat7 = findViewById(R.id.seat7);
        seat8 = findViewById(R.id.seat8);
        seat9 = findViewById(R.id.seat9);
        seat10 = findViewById(R.id.seat10);

        tvHostName.setOnClickListener(v -> showNameEditDialog());

        tvOnline.setOnClickListener(v -> {
            String msg = myCurrentSeat == -1 ? "1. Host - Online\nTotal: 1 user" : "1. Host - Online\n2. " + myName + " - Seat " + myCurrentSeat + "\nTotal: 2 users";
            new AlertDialog.Builder(this).setTitle("👥 Online Users").setMessage(msg).setPositiveButton("OK", null).show();
        });

        findViewById(R.id.btnHeaderSetting).setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("🎨 Change Theme").setItems(bgNames, (d, which) -> {
                middleBg.setBackgroundColor(bgColors[which]);
                middleBg.setText(bgNames[which] + "\n🏖️🌈");
                Toast.makeText(this, "Theme: " + bgNames[which], Toast.LENGTH_SHORT).show();
            }).show();
        });

        setupSeat(seat2, 2);
        setupSeat(seat3, 3);
        setupSeat(seat4, 4);
        setupSeat(seat5, 5);
        setupSeat(seat6, 6);
        setupSeat(seat7, 7);
        setupSeat(seat8, 8);
        setupSeat(seat9, 9);
        setupSeat(seat10, 10);
    }

    private void setupSeat(TextView seat, int number) {
        seat.setOnClickListener(v -> {
            if (myCurrentSeat == number) {
                new AlertDialog.Builder(this).setTitle("Leave Seat " + number + "?").setPositiveButton("Leave", (d, w) -> {
                    seat.setText("+");
                    seat.setBackgroundResource(R.drawable.circle_seat_bg);
                    if (number == 2) tvName2.setText("Ham Tum");
                    myCurrentSeat = -1;
                    tvOnline.setText("👥 online 1");
                }).setNegativeButton("Cancel", null).show();
            } else if (myCurrentSeat != -1) {
                new AlertDialog.Builder(this).setTitle("Take Mic?").setMessage("You are on seat " + myCurrentSeat + ". Move to " + number + "?").setPositiveButton("Take Mic", (d, w) -> {
                    clearAllSeats();
                    seat.setText("😊");
                    seat.setBackgroundResource(R.drawable.circle_shape);
                    if (number == 2) tvName2.setText(myName);
                    myCurrentSeat = number;
                }).setNegativeButton("Cancel", null).show();
            } else {
                new AlertDialog.Builder(this).setTitle("Take Mic?").setMessage("Take seat " + number + "?").setPositiveButton("Take Mic", (d, w) -> {
                    seat.setText("😊");
                    seat.setBackgroundResource(R.drawable.circle_shape);
                    if (number == 2) tvName2.setText(myName);
                    myCurrentSeat = number;
                    tvOnline.setText("👥 online 2");
                }).setNegativeButton("Cancel", null).show();
            }
        });
    }

    private void clearAllSeats() {
        TextView[] seats = {seat2, seat3, seat4, seat5, seat6, seat7, seat8, seat9, seat10};
        for (TextView s : seats) {
            s.setText("+");
            s.setBackgroundResource(R.drawable.circle_seat_bg);
        }
        tvName2.setText("Ham Tum");
    }

    private void showNameEditDialog() {
        final EditText input = new EditText(this);
        input.setText(myName);
        input.setPadding(60,40,60,40);
        new AlertDialog.Builder(this).setTitle("Edit Your Name").setView(input).setPositiveButton("Save", (d, w) -> {
            myName = input.getText().toString().trim();
            if(myName.isEmpty()) myName = "Ham Tum";
            tvHostName.setText(myName + " ✏️");
            if (myCurrentSeat == 2) tvName2.setText(myName);
        }).setNegativeButton("Cancel", null).show();
    }
}
