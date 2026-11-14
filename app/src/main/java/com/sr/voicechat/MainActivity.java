package com.example.srvoice;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    // ===== PERMANENT - কখনো চেঞ্জ হবে না =====
    public static String myUid; // Firebase Auth UID
    public static String my9DigitId; // 499506534 - Permanent
    public static String myName, myPhoto, myGender, myCountry;

    private DatabaseReference usersRef, roomsRef;
    private RecyclerView rvRooms;
    private RoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();
    private ImageView btnPlus;
    private Button btnResetHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        rvRooms = findViewById(R.id.rvRooms);
        btnPlus = findViewById(R.id.btnPlus);
        btnResetHome = findViewById(R.id.btnResetHome);

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter(roomList, room -> {
            // রুমে ঢোকা - আমার ID চেঞ্জ হবে না, শুধু রুমের ID পাঠাবো
            Intent i = new Intent(MainActivity.this, VoiceRoomActivity.class);
            i.putExtra("ROOM_ID", room.roomId);
            i.putExtra("ROOM_OWNER_ID", room.ownerId);
            startActivity(i);
        });
        rvRooms.setAdapter(adapter);

        loadMyProfile();
        loadRooms();

        // + Plus - আমার ১ টা রুম খোলা
        btnPlus.setOnClickListener(v -> openCreateRoomDialog());

        // Home Reset - নকল রুম সব মুছা
        btnResetHome.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Home Reset করবো?")
                .setMessage("সব নকল রুম মুছে যাবে। তোমার নিজের রুম আবার + Plus দিয়ে খুলতে হবে।")
                .setPositiveButton("হ্যাঁ, Reset", (d, w) -> resetHome())
                .setNegativeButton("না", null).show();
        });
    }

    // ===== ১. আমার প্রোফাইল লোড - ID একবারই বানবে =====
    private void loadMyProfile() {
        usersRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists() && snap.child("my9DigitId").exists()) {
                    // আগে থেকেই ID আছে - নতুন বানাবো না
                    my9DigitId = snap.child("my9DigitId").getValue(String.class);
                    myName = snap.child("name").getValue(String.class);
                    myPhoto = snap.child("photo").getValue(String.class);
                    myGender = snap.child("gender").getValue(String.class);
                    myCountry = snap.child("country").getValue(String.class);
                } else {
                    // প্রথমবার - 1 বার 9-Digit ID বানাও, Firebase এ সেভ করো
                    my9DigitId = generate9DigitId();
                    Map<String, Object> map = new HashMap<>();
                    map.put("my9DigitId", my9DigitId);
                    map.put("uid", myUid);
                    map.put("createdAt", System.currentTimeMillis());
                    usersRef.child(myUid).updateChildren(map);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private String generate9DigitId() {
        Random r = new Random();
        int id = 100000000 + r.nextInt(900000000);
        return String.valueOf(id);
    }

    // ===== ২. Home এ সব রুম দেখা =====
    private void loadRooms() {
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                roomList.clear();
                for (DataSnapshot s : snap.getChildren()) {
                    Room room = s.getValue(Room.class);
                    if (room != null) roomList.add(room);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // ===== ৩. + Plus - রুম খোলা - Popup =====
    private void openCreateRoomDialog() {
        if (my9DigitId == null) {
            Toast.makeText(this, "একটু অপেক্ষা করো, ID লোড হচ্ছে...", Toast.LENGTH_SHORT).show();
            return;
        }
        // আমার রুম আগে থেকেই আছে কিনা চেক করো - থাকলে নতুন বানাবো না
        roomsRef.child(my9DigitId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    // আমার রুম আছে - ওইটাই খুলবো
                    Intent i = new Intent(MainActivity.this, VoiceRoomActivity.class);
                    i.putExtra("ROOM_ID", my9DigitId);
                    i.putExtra("ROOM_OWNER_ID", my9DigitId);
                    startActivity(i);
                } else {
                    showCreateRoomPopup();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showCreateRoomPopup() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_create_room, null);
        EditText etRoomName = v.findViewById(R.id.etRoomName);
        EditText etAnnouncement = v.findViewById(R.id.etAnnouncement);
        // Room Photo - তুমি ImagePicker দিয়ে সেট করবা
        // ImageView ivRoomPhoto = v.findViewById(R.id.ivRoomPhoto);

        new AlertDialog.Builder(this)
            .setTitle("আমার রুম খুলবো")
            .setView(v)
            .setPositiveButton("Save / খুলো", (d, w) -> {
                String roomName = etRoomName.getText().toString().trim();
                String announcement = etAnnouncement.getText().toString().trim();
                if (roomName.isEmpty()) roomName = myName + " Voice Room";

                // RULE: Room ID = আমার ID - সেম
                Map<String, Object> room = new HashMap<>();
                room.put("roomId", my9DigitId);
                room.put("ownerId", my9DigitId);
                room.put("ownerName", myName);
                room.put("ownerPhoto", myPhoto);
                room.put("roomName", roomName);
                room.put("announcement", announcement);
                room.put("roomPhoto", myPhoto);
                room.put("createdAt", System.currentTimeMillis());
                room.put("admins", new HashMap<String, Boolean>());

                roomsRef.child(my9DigitId).setValue(room).addOnSuccessListener(a -> {
                    Intent i = new Intent(MainActivity.this, VoiceRoomActivity.class);
                    i.putExtra("ROOM_ID", my9DigitId);
                    i.putExtra("ROOM_OWNER_ID", my9DigitId);
                    startActivity(i);
                });
            })
            .setNegativeButton("বাতিল", null).show();
    }

    // ===== ৪. Home Reset - সব নকল রুম মুছা =====
    private void resetHome() {
        // সাবধান: এটা শুধু তোমার টেস্টের সময় ব্যবহার করবা
        // সব রুম মুছে দিবে, তারপর তুমি + Plus দিয়ে নতুন করে ১ টা খুলবা
        roomsRef.removeValue().addOnSuccessListener(a -> {
            Toast.makeText(this, "Home Reset হয়ে গেছে। এখন + Plus দিয়ে রুম খুলো", Toast.LENGTH_LONG).show();
        });
    }

    // Room Model
    public static class Room {
        public String roomId, ownerId, ownerName, ownerPhoto, roomName, announcement, roomPhoto;
        public Map<String, Boolean> admins;
        public Room() {}
    }
}
