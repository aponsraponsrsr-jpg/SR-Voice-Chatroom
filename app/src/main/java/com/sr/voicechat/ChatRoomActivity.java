package com.yourapp.voicechat; // তোমার package name টা এখানে থাকবে, চেঞ্জ করো না

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ChatRoomActivity extends AppCompatActivity {

    GridLayout seatGrid;
    String myUid;
    String roomId = "1024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // --- TOP BAR ---
        TextView txtRoomId = findViewById(R.id.txtRoomId);
        ImageView btnEditRoom = findViewById(R.id.btnEditRoom);
        ImageView btnLeaveRoom = findViewById(R.id.btnLeaveRoom);

        // Room ID - Owner ID শো করবে (1024 এর জায়গায়)
        txtRoomId.setText(roomId); // চাইলে এখানে myUid.substring(0,4) দিতে পারো

        // Edit ON
        btnEditRoom.setOnClickListener(v -> {
            Toast.makeText(this, "Edit Room Open", Toast.LENGTH_SHORT).show();
            // openEditRoomDialog();
        });

        // Power OFF / Exit ON
        btnLeaveRoom.setOnClickListener(v -> {
            leaveRoom();
            finish();
        });

        // --- 10 SEATS - OPEN ---
        seatGrid = findViewById(R.id.seatGrid);
        createSeats();

        // --- BOTTOM BAR - তোমার অর্ডার অনুযায়ী ---
        // 1. Chat ON
        findViewById(R.id.btnChat).setOnClickListener(v -> {
            Toast.makeText(this, "Message Box Open", Toast.LENGTH_SHORT).show();
        });

        // 2. Mic ON
        findViewById(R.id.btnMic).setOnClickListener(v -> {
            Toast.makeText(this, "Mic On/Off", Toast.LENGTH_SHORT).show();
        });

        // 3. Gift OFF / LOCK
        findViewById(R.id.btnGift).setAlpha(0.4f);
        findViewById(R.id.btnGift).setOnClickListener(v -> {
            Toast.makeText(this, "Gift is Locked 🔒", Toast.LENGTH_SHORT).show();
        });

        // 4. Game OFF / LOCK
        findViewById(R.id.btnGame).setAlpha(0.4f);
        findViewById(R.id.btnGame).setOnClickListener(v -> {
            Toast.makeText(this, "Game is Locked 🔒", Toast.LENGTH_SHORT).show();
        });
    }

    private void createSeats() {
        seatGrid.removeAllViews();
        for (int i = 0; i < 10; i++) {
            View seatView = getLayoutInflater().inflate(R.layout.item_seat, seatGrid, false);
            TextView txtName = seatView.findViewById(R.id.txtSeatName);
            txtName.setText("Seat " + (i + 1));

            int index = i;
            seatView.setOnClickListener(v -> onSeatClicked(index));
            seatGrid.addView(seatView);
        }
    }

    private void onSeatClicked(int index) {
        // সিটে বসা / নামা
        new AlertDialog.Builder(this)
                .setTitle("Seat " + (index + 1))
                .setMessage("Take this seat?")
                .setPositiveButton("Take / Mic On", (d, w) -> takeSeat(index))
                .setNegativeButton("Leave / Exit", (d, w) -> leaveSeat(index))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void takeSeat(int index) {
        Toast.makeText(this, "Seat " + (index+1) + " Taken - Mic On", Toast.LENGTH_SHORT).show();
        // Firebase: mDatabase.child("rooms").child(roomId).child("seats").child(String.valueOf(index)).child("userId").setValue(myUid);
    }

    private void leaveSeat(int index) {
        Toast.makeText(this, "Left Seat - Exit Done", Toast.LENGTH_SHORT).show();
        // Firebase: mDatabase.child("rooms").child(roomId).child("seats").child(String.valueOf(index)).removeValue();
    }

    private void leaveRoom() {
        Toast.makeText(this, "Leaving Room...", Toast.LENGTH_SHORT).show();
    }
}
