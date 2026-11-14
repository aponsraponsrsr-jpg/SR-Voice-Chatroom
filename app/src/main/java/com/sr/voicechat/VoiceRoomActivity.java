package com.sr.voicechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {

    // ===== ৩ টা আলাদা ID - কখনো মিক্স হবে না =====
    private String roomId; // এই রুমের ID - যেই রুমে ঢুকছি
    private String roomOwnerId; // এই রুমের মালিকের ID
    private String myId; // আমার ID - prefs থেকে নিবো, কখনো চেঞ্জ হবে না
    private String myName, myGender, myCountry;

    private boolean amIOwner = false;
    private boolean amIAdmin = false;

    private TextView tvRoomId, tvRoomName, tvMyIdInRoom;
    private Button btnRoomSet, btnRoomEdit;
    private View layoutOwnerTools;
    private RecyclerView rvOnline;

    private DatabaseReference activeRoomRef;
    private DatabaseReference roomMetaRef;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // ===== FIX: আমার ID prefs থেকে নিবো, Intent থেকে না =====
        // Intent থেকে শুধু রুমের ID নিবো
        roomId = getIntent().getStringExtra("ROOM_ID");
        roomOwnerId = getIntent().getStringExtra("ROOM_OWNER_ID");

        // যদি ROOM_ID না আসে, ROOM_OWNER_ID ই ROOM_ID
        if (roomId == null || roomId.isEmpty()) roomId = roomOwnerId;
        if (roomOwnerId == null || roomOwnerId.isEmpty()) roomOwnerId = roomId;

        myId = prefs.getString("MY_9_DIGIT_ID", "");
        myName = prefs.getString("USER_NAME", "User");
        myGender = prefs.getString("USER_GENDER", "");
        myCountry = prefs.getString("USER_COUNTRY", "");

        // যদি আমার ID খালি থাকে, তাহলে রুমে ঢুকবো না - Login এ পাঠাবো
        if (myId == null || myId.isEmpty()) {
            Toast.makeText(this, "ID পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // তোমার HomeActivity এর Structure অনুযায়ী:
        // active_rooms / {ownerId} আর rooms / room_{ownerId} / meta
        activeRoomRef = FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId);
        roomMetaRef = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + roomOwnerId).child("meta");

        tvRoomId = findViewById(R.id.tvRoomId);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvMyIdInRoom = findViewById(R.id.tvMyIdInRoom);
        btnRoomSet = findViewById(R.id.btnRoomSet);
        btnRoomEdit = findViewById(R.id.btnRoomEdit);
        layoutOwnerTools = findViewById(R.id.layoutOwnerTools);
        rvOnline = findViewById(R.id.rvOnline);
        if (rvOnline != null) rvOnline.setLayoutManager(new LinearLayoutManager(this));

        // উপরে রুমের ID দেখাবে, নিচে আমার ID দেখাবে - ২ টা আলাদা
        if (tvRoomId != null) tvRoomId.setText("Room ID: " + roomId);
        if (tvMyIdInRoom != null) tvMyIdInRoom.setText("আমার ID: " + myId + " | " + myName + " | " + myGender + " | " + myCountry);

        checkMyRole();
        loadRoomInfo();

        if (btnRoomSet != null) {
            btnRoomSet.setOnClickListener(v -> {
                if (!amIOwner && !amIAdmin) {
                    Toast.makeText(this, "Only Room Owner / Admin can change", Toast.LENGTH_SHORT).show();
                    return;
                }
                openRoomSet();
            });
        }
    }

    private void checkMyRole() {
        // RULE: আমি Owner কিনা? - আমার ID == রুমের মালিকের ID হলেই Owner
        amIOwner = myId.equals(roomOwnerId);

        // Admin কিনা? - Firebase থেকে চেক করবো
        // active_rooms / {ownerId} / admins / {myId}
        activeRoomRef.child("admins").child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                amIAdmin = snap.exists() && Boolean.TRUE.equals(snap.getValue(Boolean.class));
                updateToolsVisibility();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { updateToolsVisibility(); }
        });
        updateToolsVisibility();
    }

    private void updateToolsVisibility() {
        if (amIOwner) {
            if (layoutOwnerTools != null) layoutOwnerTools.setVisibility(View.VISIBLE);
            if (btnRoomSet != null) btnRoomSet.setVisibility(View.VISIBLE);
            if (btnRoomEdit != null) btnRoomEdit.setVisibility(View.VISIBLE);
        } else if (amIAdmin) {
            if (layoutOwnerTools != null) layoutOwnerTools.setVisibility(View.VISIBLE);
            if (btnRoomSet != null) btnRoomSet.setVisibility(View.VISIBLE);
            if (btnRoomEdit != null) btnRoomEdit.setVisibility(View.GONE);
        } else {
            // সাধারণ Member - কোনো Tools দেখবে না, Owner লেখাও আসবে না
            if (layoutOwnerTools != null) layoutOwnerTools.setVisibility(View.GONE);
            if (btnRoomSet != null) btnRoomSet.setVisibility(View.GONE);
            if (btnRoomEdit != null) btnRoomEdit.setVisibility(View.GONE);
        }
    }

    private void loadRoomInfo() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                String rName = snap.child("roomName").getValue(String.class);
                if (tvRoomName != null) tvRoomName.setText(rName != null ? rName : "Voice Room");
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Fallback: meta না থাকলে active_rooms থেকে নিবো
        activeRoomRef.child("roomName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists() && tvRoomName != null) {
                    String rName = snap.getValue(String.class);
                    if (rName != null) tvRoomName.setText(rName);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void openRoomSet() {
        Toast.makeText(this, "Room Set খুলছে...", Toast.LENGTH_SHORT).show();
        // এখানে তোমার Room Set Dialog / Activity খুলবে
    }

    // Owner কাউকে Admin বানাবে - শুধু Owner পারবে
    private void makeAdmin(String targetUser9DigitId) {
        if (!amIOwner) {
            Toast.makeText(this, "শুধু Owner Admin দিতে পারে", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("admins/" + targetUser9DigitId, true);
        activeRoomRef.updateChildren(map);
    }

    // Online List এর জন্য নিয়ম - তোমার Adapter এ এইটা ব্যবহার করবা:
    // if (memberId.equals(roomOwnerId)) -> "Owner" দেখাও
    // else if (admins.contains(memberId)) -> "Admin" দেখাও
    // else -> "Member" দেখাও - Owner লেখা না
}
