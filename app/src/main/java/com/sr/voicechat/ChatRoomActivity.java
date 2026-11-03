package com.sr.voicechat;

import com.sr.voicechat.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatRoomActivity extends AppCompatActivity {

    GridLayout seatGrid;
    String myUid;
    String roomId = "room_1";
    DatabaseReference roomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        seatGrid = findViewById(R.id.seatGrid);
        
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            myUid = "guest_" + System.currentTimeMillis();
        }

        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId).child("seats");
        
        loadSeats();
    }

    private void loadSeats() {
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                seatGrid.removeAllViews();
                for (int i = 0; i < 8; i++) {
                    View seatView = LayoutInflater.from(ChatRoomActivity.this).inflate(R.layout.item_seat, null);
                    
                    ImageView imgAvatar = seatView.findViewById(R.id.imgSeatAvatar);
                    TextView txtName = seatView.findViewById(R.id.txtSeatName);

                    String seatUid = null;
                    if (snapshot.hasChild(String.valueOf(i))) {
                        seatUid = snapshot.child(String.valueOf(i)).getValue(String.class);
                    }

                    if (seatUid != null && !seatUid.isEmpty()) {
                        txtName.setText("User " + (i+1));
                    } else {
                        txtName.setText("Empty Seat " + (i+1));
                    }

                    int seatIndex = i;
                    String finalSeatUid = seatUid;
                    seatView.setOnClickListener(v -> {
                        if (finalSeatUid == null) {
                            joinSeat(seatIndex);
                        } else if (finalSeatUid.equals(myUid)) {
                            leaveSeat(seatIndex);
                        } else {
                            Toast.makeText(ChatRoomActivity.this, "Seat busy", Toast.LENGTH_SHORT).show();
                        }
                    });

                    GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                    param.width = GridLayout.LayoutParams.WRAP_CONTENT;
                    param.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    param.setMargins(10, 10, 10, 10);
                    seatView.setLayoutParams(param);
                    
                    seatGrid.addView(seatView);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinSeat(int index) {
        roomRef.child(String.valueOf(index)).setValue(myUid);
        Toast.makeText(this, "Joined Seat " + (index+1), Toast.LENGTH_SHORT).show();
    }

    private void leaveSeat(int index) {
        new AlertDialog.Builder(this)
                .setTitle("Leave Seat?")
                .setMessage("Do you want to leave this seat?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    roomRef.child(String.valueOf(index)).removeValue();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
