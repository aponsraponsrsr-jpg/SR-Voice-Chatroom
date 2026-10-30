package com.sr.voicechat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView tvHostName, tvOnline, tvRoomBg, tvChatLog;
    private TextView seat1, seat2, seat3, seat4, seat5, seat6, seat7, seat8, seat9, seat10;
    private TextView tvName1, tvName2, tvName3, tvName4, tvName5, tvName6, tvName7, tvName8, tvName9, tvName10;
    private EditText etMessage;
    private int myCurrentSeat = -1;
    private String myName = "Ham Tum";
    private final int[] profileColors = {0xFFE91E63, 0xFF9C27B0, 0xFF3F51B5, 0xFF009688, 0xFFFF9800, 0xFF795548};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        tvHostName = findViewById(R.id.tvHostName);
        tvOnline = findViewById(R.id.tvOnline);
        tvRoomBg = findViewById(R.id.tvRoomBg);
        tvChatLog = findViewById(R.id.tvChatLog);
        tvName1 = findViewById(R.id.tvName1); tvName2 = findViewById(R.id.tvName2); tvName3 = findViewById(R.id.tvName3); tvName4 = findViewById(R.id.tvName4); tvName5 = findViewById(R.id.tvName5);
        tvName6 = findViewById(R.id.tvName6); tvName7 = findViewById(R.id.tvName7); tvName8 = findViewById(R.id.tvName8); tvName9 = findViewById(R.id.tvName9); tvName10 = findViewById(R.id.tvName10);
        etMessage = findViewById(R.id.etMessage);
        seat1 = findViewById(R.id.seat1); seat2 = findViewById(R.id.seat2); seat3 = findViewById(R.id.seat3); seat4 = findViewById(R.id.seat4); seat5 = findViewById(R.id.seat5);
        seat6 = findViewById(R.id.seat6); seat7 = findViewById(R.id.seat7); seat8 = findViewById(R.id.seat8); seat9 = findViewById(R.id.seat9); seat10 = findViewById(R.id.seat10);

        tvHostName.setOnClickListener(v -> showNameEditDialog());
        tvOnline.setOnClickListener(v -> {
            String s = myCurrentSeat==-1 ? "You are not on seat" : "You are on Seat " + myCurrentSeat;
            new AlertDialog.Builder(this).setTitle("👥 Online " + (myCurrentSeat==-1?1:2)).setMessage(s).setPositiveButton("OK", null).show();
        });
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            String m = etMessage.getText().toString().trim();
            if(!m.isEmpty()){ tvChatLog.append("\n" + myName + ": " + m); etMessage.setText(""); }
        });
        findViewById(R.id.btnHeaderSetting).setOnClickListener(v -> Toast.makeText(this, "Theme coming", Toast.LENGTH_SHORT).show());

        setup(seat2, tvName2, 2); setup(seat3, tvName3, 3); setup(seat4, tvName4, 4); setup(seat5, tvName5, 5); setup(seat6, tvName6, 6);
        setup(seat7, tvName7, 7); setup(seat8, tvName8, 8); setup(seat9, tvName9, 9); setup(seat10, tvName10, 10);
    }

    private void setup(TextView seat, TextView nameView, int num){
        seat.setOnClickListener(v -> {
            if(myCurrentSeat==num){
                new AlertDialog.Builder(this).setTitle("Seat " + num).setMessage("Leave this seat?").setPositiveButton("Leave", (d,w)->{
                    seat.setText("+"); seat.setBackgroundResource(R.drawable.circle_seat_bg); seat.setTextColor(0xFFFFFFFF);
                    nameView.setText(""); myCurrentSeat=-1; findViewById(R.id.tvOnline); ((TextView)findViewById(R.id.tvOnline)).setText("👥 online 1");
                    tvChatLog.append("\nSystem: left Seat " + num);
                }).setNegativeButton("Cancel", null).show();
            } else {
                new AlertDialog.Builder(this).setTitle("Take Seat " + num + "?").setMessage("Join as " + myName).setPositiveButton("Take Mic", (d,w)->{
                    clearAll();
                    String letter = myName.isEmpty() ? "?" : String.valueOf(myName.charAt(0)).toUpperCase();
                    seat.setText(letter);
                    seat.setBackgroundResource(R.drawable.circle_shape);
                    // profile color
                    seat.setBackgroundTintList(android.content.res.ColorStateList.valueOf(profileColors[num % profileColors.length]));
                    nameView.setText(myName);
                    myCurrentSeat=num;
                    ((TextView)findViewById(R.id.tvOnline)).setText("👥 online 2");
                    tvChatLog.append("\nSystem: " + myName + " joined Seat " + num);
                }).setNegativeButton("Cancel", null).show();
            }
        });
    }

    private void clearAll(){
        TextView[] seats = {seat2, seat3, seat4, seat5, seat6, seat7, seat8, seat9, seat10};
        TextView[] names = {tvName2, tvName3, tvName4, tvName5, tvName6, tvName7, tvName8, tvName9, tvName10};
        for(int i=0;i<seats.length;i++){ seats[i].setText("+"); seats[i].setBackgroundResource(R.drawable.circle_seat_bg); seats[i].setBackgroundTintList(null); names[i].setText(""); }
    }

    private void showNameEditDialog(){
        final EditText input = new EditText(this); input.setText(myName); input.setPadding(60,40,60,40);
        new AlertDialog.Builder(this).setTitle("Edit Name").setView(input).setPositiveButton("Save", (d,w)->{
            myName = input.getText().toString().trim(); if(myName.isEmpty()) myName="Ham Tum";
            tvHostName.setText(myName + " ✏️");
            if(myCurrentSeat!=-1){
                TextView curSeat = findViewById(getResources().getIdentifier("seat"+myCurrentSeat,"id",getPackageName()));
                TextView curName = findViewById(getResources().getIdentifier("tvName"+myCurrentSeat,"id",getPackageName()));
                curSeat.setText(String.valueOf(myName.charAt(0)).toUpperCase());
                curName.setText(myName);
            }
        }).setNegativeButton("Cancel", null).show();
    }
}
