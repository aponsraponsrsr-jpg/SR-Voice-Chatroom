package com.sr.voicechat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView roomTitleTextView, seatStatusTextView;
    private Button takeSeatButton, leaveRoomButton, changeThemeButton;
    private LinearLayout rootLayout;
    
    private FirebaseAuth mAuth;
    private DatabaseReference roomRef;
    private String currentUserId, roomId = "default_room";
    private boolean isOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        // Safety check to prevent crash if user is not logged in
        if (currentUser == null) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        
        isOwner = getIntent().getBooleanExtra("isOwner", false);

        roomRef = FirebaseDatabase.getInstance().getReference().child("VoiceRooms").child(roomId);

        roomTitleTextView = findViewById(R.id.roomTitleTextView);
        seatStatusTextView = findViewById(R.id.seatStatusTextView);
        takeSeatButton = findViewById(R.id.takeSeatButton);
        leaveRoomButton = findViewById(R.id.leaveRoomButton);
        changeThemeButton = findViewById(R.id.changeThemeButton);
        rootLayout = findViewById(R.id.rootLayout);

        if (!isOwner) {
            changeThemeButton.setVisibility(View.GONE);
        } else {
            changeThemeButton.setVisibility(View.VISIBLE);
        }

        changeThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOwner) {
                    roomRef.child("roomTheme").setValue("dark_blue");
                    Toast.makeText(VoiceRoomActivity.this, "Room theme updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VoiceRoomActivity.this, "You do not have permission to change the theme!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        takeSeatButton.setOnClickListener(v -> takeSeat());
        leaveRoomButton.setOnClickListener(v -> {
            leaveRoom();
            finish();
        });

        loadRoomDetails();
        listenToThemeChanges();
    }

    private void takeSeat() {
        roomRef.child("Seats").child("Seat_1").setValue(currentUserId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(VoiceRoomActivity.this, "You have taken the seat successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(VoiceRoomActivity.this, "Failed to take the seat!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveRoom() {
        roomRef.child("Seats").child("Seat_1").removeValue();
    }

    private void loadRoomDetails() {
        roomRef.child("roomName").setValue("SR Live Voice Club");
        
        roomRef.child("Seats").child("Seat_1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String seatedUser = snapshot.getValue(String.class);
                    if (seatedUser != null && seatedUser.equals(currentUserId)) {
                        seatStatusTextView.setText("Seat 1: You are seated");
                    } else {
                        seatStatusTextView.setText("Seat 1: Occupied by another user");
                    }
                } else {
                    seatStatusTextView.setText("Seat 1: Available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenToThemeChanges() {
        roomRef.child("roomTheme").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String theme = snapshot.getValue(String.class);
                    if ("dark_blue".equals(theme)) {
                        rootLayout.setBackgroundColor(Color.parseColor("#0F2027"));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
