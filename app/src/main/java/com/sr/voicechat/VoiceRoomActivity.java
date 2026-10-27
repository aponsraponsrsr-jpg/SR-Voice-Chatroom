package com.sr.voicechat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView roomTitleTextView, seatStatusTextView;
    private Button takeSeatButton, leaveRoomButton;
    
    private FirebaseAuth mAuth;
    private DatabaseReference roomRef;
    private String currentUserId, roomId = "default_room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        
        // ফায়ারবেস ডেটাবেজে রুমের রেফারেন্স
        roomRef = FirebaseDatabase.getInstance().getReference().child("VoiceRooms").child(roomId);

        roomTitleTextView = findViewById(R.id.roomTitleTextView);
        seatStatusTextView = findViewById(R.id.seatStatusTextView);
        takeSeatButton = findViewById(R.id.takeSeatButton);
        leaveRoomButton = findViewById(R.id.leaveRoomButton);

        // সিটে বসার বাটন
        takeSeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeSeat();
            }
        });

        // রুম থেকে বের হওয়ার বাটন
        leaveRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveRoom();
                finish();
            }
        });

        loadRoomDetails();
    }

    private void takeSeat() {
        // ইউজারের আইডি দিয়ে সিট বুক করা
        roomRef.child("Seats").child("Seat_1").setValue(currentUserId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(VoiceRoomActivity.this, "আপনি সফলভাবে সিটে বসেছেন!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(VoiceRoomActivity.this, "সিটে বসা যায়নি!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveRoom() {
        // রুম ছাড়লে সিট খালি করে দেওয়া
        roomRef.child("Seats").child("Seat_1").removeValue();
    }

    private void loadRoomDetails() {
        roomRef.child("roomName").setValue("SR Live Voice Club");
        
        // রিয়েল-টাইমে সিটের অবস্থা মনিটর করা
        roomRef.child("Seats").child("Seat_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String seatedUser = snapshot.getValue(String.class);
                    if (seatedUser != null && seatedUser.equals(currentUserId)) {
                        seatStatusTextView.setText("সিট ১: আপনি বসে আছেন");
                    } else {
                        seatStatusTextView.setText("সিট ১: অন্য ইউজার বসে আছে");
                    }
                } else {
                    seatStatusTextView.setText("সিট ১: খালি আছে");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // কোনো এরর হ্যান্ডেল করতে চাইলে
            }
        });
    }
}
