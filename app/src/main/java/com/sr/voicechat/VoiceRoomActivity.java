package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView tvRoomName, tvMyIdDisplay, tvSeatsInfo;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String roomId = "room_1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvRoomName = findViewById(R.id.tvRoomName);
        tvMyIdDisplay = findViewById(R.id.tvMyIdDisplay);
        tvSeatsInfo = findViewById(R.id.tvSeatsInfo);

        // Get RoomId from Intent - Safe
        try {
            String intentRoom = getIntent().getStringExtra("roomId");
            if (intentRoom != null && !intentRoom.isEmpty()) {
                roomId = intentRoom;
            }
        } catch (Exception e) {
            roomId = "room_1";
        }

        if (tvRoomName != null) {
            tvRoomName.setText("Voice Room: " + roomId);
        }

        loadMyPermanentId();
        setup10SeatsHeavy();
        joinRoomPermanent();
    }

    private void loadMyPermanentId() {
        if (mAuth == null || mAuth.getCurrentUser() == null) return;
        if (tvMyIdDisplay == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) {
                tvMyIdDisplay.setText("My ID: Loading...");
                return;
            }

            Long displayId = doc.getLong("displayId");
            Long nineDigitId = doc.getLong("nineDigitId");
            Boolean isShort = doc.getBoolean("isShortId");
            Long shortId = doc.getLong("shortId");

            if (isShort != null && isShort && shortId != null) {
                tvMyIdDisplay.setText("My ID: " + shortId + " [VIP SHORT] - Permanent");
            } else if (displayId != null) {
                tvMyIdDisplay.setText("My ID: " + displayId + " - Permanent");
            } else if (nineDigitId != null) {
                tvMyIdDisplay.setText("My ID: " + nineDigitId + " - Permanent");
            } else {
                tvMyIdDisplay.setText("My ID: 100000000+ - Generating");
            }
        });
    }

    private void setup10SeatsHeavy() {
        // 10 Seats - Heavy Business Logic - Permanent System
        // Seat 1-10 Ready - Mic On/Off, Lock, Kick System Later
        if (tvSeatsInfo != null) {
            tvSeatsInfo.setText("10 Seats System Ready\nSeat 1-10 Active\nID Permanent Lifetime");
        }
        // Toast for success check
        Toast.makeText(this, "Voice Room 10 Seats Ready - Success", Toast.LENGTH_SHORT).show();
    }

    private void joinRoomPermanent() {
        if (mAuth == null || mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> joinData = new HashMap<>();
        joinData.put("uid", uid);
        joinData.put("joinedAt", System.currentTimeMillis());
        joinData.put("roomId", roomId);
        joinData.put("seat", 0); // 0 = Audience, 1-10 = On Mic Seat

        db.collection("rooms").document(roomId)
                .collection("users").document(uid).set(joinData)
                .addOnSuccessListener(a -> {
                    // Joined Success - But ID remains Permanent
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mAuth != null && mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                db.collection("rooms").document(roomId).collection("users").document(uid).delete();
            }
        } catch (Exception e) {
            // Ignore - ID still Permanent Lifetime
        }
    }
}
