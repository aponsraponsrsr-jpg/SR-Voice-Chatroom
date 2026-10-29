package com.sr.voicechat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView roomIdText, roomNameText;
    private CircleImageView[] seatImages = new CircleImageView[10];
    private TextView[] seatNames = new TextView[10];
    private boolean[] isSeatLocked = new boolean[10];
    private boolean[] isSeatTaken = new boolean[10];
    private String myUserId;
    private boolean isOwner = true; // for test, you are owner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        // Get my ID from IdManager - now 7/8/9 digit mixed and unique
        myUserId = IdManager.getOrCreateUserId(this);

        roomIdText = findViewById(R.id.roomIdText);
        roomNameText = findViewById(R.id.roomNameText);

        roomIdText.setText("Room ID: " + myUserId);
        roomNameText.setText("Room: SR Voice Room");

        initSeats();
    }

    private void initSeats() {
        int[] imageIds = {
                R.id.seat1Image, R.id.seat2Image, R.id.seat3Image, R.id.seat4Image, R.id.seat5Image,
                R.id.seat6Image, R.id.seat7Image, R.id.seat8Image, R.id.seat9Image, R.id.seat10Image
        };
        int[] nameIds = {
                R.id.seat1Name, R.id.seat2Name, R.id.seat3Name, R.id.seat4Name, R.id.seat5Name,
                R.id.seat6Name, R.id.seat7Name, R.id.seat8Name, R.id.seat9Name, R.id.seat10Name
        };

        for (int i = 0; i < 10; i++) {
            seatImages[i] = findViewById(imageIds[i]);
            seatNames[i] = findViewById(nameIds[i]);
            isSeatLocked[i] = false;
            isSeatTaken[i] = false;
            seatNames[i].setText("No." + (i + 1));

            final int index = i;
            seatImages[i].setOnClickListener(v -> onSeatClicked(index));
        }
        
        // Owner auto take seat 1
        takeSeat(0);
    }

    private void onSeatClicked(int index) {
        if (isSeatLocked[index]) {
            Toast.makeText(this, "Mic is Locked", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSeatTaken[index]) {
            showSeatOptions(index);
        } else {
            showTakeMicDialog(index);
        }
    }

    private void showTakeMicDialog(int index) {
        new AlertDialog.Builder(this)
                .setTitle("Seat No." + (index + 1))
                .setItems(new String[]{"Take The Mic", "Lock this mic", "Cancel"}, (dialog, which) -> {
                    if (which == 0) takeSeat(index);
                    else if (which == 1) lockSeat(index);
                })
                .show();
    }

    private void showSeatOptions(int index) {
        if (isOwner) {
            new AlertDialog.Builder(this)
                    .setTitle("Manage Seat No." + (index + 1))
                    .setItems(new String[]{"Mute this mic", "Lock this mic", "Kick from mic", "Invite", "Cancel"}, (dialog, which) -> {
                        if (which == 0) Toast.makeText(this, "Muted Seat " + (index+1), Toast.LENGTH_SHORT).show();
                        else if (which == 1) lockSeat(index);
                        else if (which == 2) leaveSeat(index);
                    })
                    .show();
        } else {
            Toast.makeText(this, seatNames[index].getText(), Toast.LENGTH_SHORT).show();
        }
    }

    private void takeSeat(int index) {
        // leave previous seat
        for(int i=0;i<10;i++){
            if(seatNames[i].getText().toString().contains(myUserId)){
                leaveSeat(i);
            }
        }
        isSeatTaken[index] = true;
        seatNames[index].setText("User: " + myUserId);
        seatImages[index].setBorderColor(getResources().getColor(android.R.color.holo_green_light));
        seatImages[index].setBorderWidth(4);
        Toast.makeText(this, "You took seat No." + (index + 1), Toast.LENGTH_SHORT).show();
    }

    private void leaveSeat(int index) {
        isSeatTaken[index] = false;
        seatNames[index].setText("No." + (index + 1));
        seatImages[index].setBorderWidth(0);
    }

    private void lockSeat(int index) {
        isSeatLocked[index] = true;
        seatImages[index].setImageResource(android.R.drawable.ic_lock_lock);
        Toast.makeText(this, "Seat No." + (index+1) + " Locked", Toast.LENGTH_SHORT).show();
    }
}
