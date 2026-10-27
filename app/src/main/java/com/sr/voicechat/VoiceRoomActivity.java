package com.sr.voicechat;

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
        currentUserId = mAuth.getCurrentUser().getUid();
        
        // MainActivity থেকে পাঠানো isOwner ভ্যালু রিসিভ করা
        isOwner = getIntent().getBooleanExtra("isOwner", false);

        roomRef = FirebaseDatabase.getInstance().getReference().child("VoiceRooms").child(roomId);

        roomTitleTextView = findViewById(R.id.roomTitleTextView);
        seatStatusTextView = findViewById(R.id.seatStatusTextView);
        takeSeatButton = findViewById(R.id.takeSeatButton);
        leaveRoomButton = findViewById(R.id.leaveRoomButton);
        changeThemeButton = findViewById(R.id.changeThemeButton);
        rootLayout = findViewById(R.id.rootLayout); // লেআউটের রুট আইডি

        // যদি ইউজার ওনার না হয়, তবে থিম চেঞ্জ বাটন লুকিয়ে রাখা
        if (!isOwner) {
            changeThemeButton.setVisibility(View.GONE);
        } else {
            changeThemeButton.setVisibility(View.VISIBLE);
        }

        // থিম বা ব্যাকগ্রাউন্ড পরিবর্তনের বাটন (শুধু ওনার কাজ করবে)
        changeThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOwner) {
                    // ফায়ারবেসে থিম বা ব্যাকগ্রাউন্ড আপডেট করা
                    roomRef.child("roomTheme").setValue("dark_blue");
                    Toast.makeText(VoiceRoomActivity.this, "রুমের থিম সফলভাবে পরিবর্তন করা হয়েছে!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VoiceRoomActivity.this, "আপনার রুমের থিম পরিবর্তনের অনুমতি নেই!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(VoiceRoomActivity.this, "আপনি সফলভাবে সিটে বসেছেন!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(VoiceRoomActivity.this, "সিটে বসা যায়নি!", Toast.LENGTH_SHORT).show();
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
                        seatStatusTextView.setText("সিট ১: আপনি বসে আছেন");
                    } else {
                        seatStatusTextView.setText("সিট ১: অন্য ইউজার বসে আছে");
                    }
                } else {
                    seatStatusTextView.setText("সিট ১: খালি আছে");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // রিয়েল-টাইমে থিম পরিবর্তন মনিটর করা
    private void listenToThemeChanges() {
        roomRef.child("roomTheme").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String theme = snapshot.getValue(String.class);
                    if ("dark_blue".equals(theme)) {
                        rootLayout.setBackgroundColor(Color.parseColor("#0F2027")); // প্রিমিয়াম ডার্ক ব্লু থিম
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
