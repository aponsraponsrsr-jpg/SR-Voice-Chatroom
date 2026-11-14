package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private TextView btnPlus;
    private ImageView btnSearchRoom, btnCreateRoomTop;
    private Button btnHomeTab, btnMomentTab, btnInboxTab, btnMeTab;
    private GridView roomGridView;

    private ArrayList<RoomModel> roomModelList;
    private RoomGridAdapter roomGridAdapter;

    private DatabaseReference activeRoomsRef;
    private DatabaseReference roomsRef;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        activeRoomsRef = FirebaseDatabase.getInstance().getReference("active_rooms");
        roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        btnPlus = findViewById(R.id.btnPlus);
        btnSearchRoom = findViewById(R.id.btnSearchRoom);
        btnCreateRoomTop = findViewById(R.id.btnCreateRoomTop);
        btnHomeTab = findViewById(R.id.btnHomeTab);
        btnMomentTab = findViewById(R.id.btnMomentTab);
        btnInboxTab = findViewById(R.id.btnInboxTab);
        btnMeTab = findViewById(R.id.btnMeTab);
        roomGridView = findViewById(R.id.roomGridView);

        roomModelList = new ArrayList<>();
        roomGridAdapter = new RoomGridAdapter(this, roomModelList);
        if (roomGridView != null) {
            roomGridView.setAdapter(roomGridAdapter);
        }

        loadActiveRooms();

        View.OnClickListener createRoomListener = v -> checkAndCreateRoom();

        if (btnPlus != null) btnPlus.setOnClickListener(createRoomListener);
        if (btnCreateRoomTop != null) btnCreateRoomTop.setOnClickListener(createRoomListener);

        if (btnSearchRoom != null) {
            btnSearchRoom.setOnClickListener(v -> showSearchRoomDialog());
        }

        if (roomGridView != null) {
            roomGridView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < roomModelList.size()) {
                    RoomModel selectedRoom = roomModelList.get(position);
                    // FIX: ROOM_ID + ROOM_OWNER_ID - ২ টাই পাঠাবো, যাতে VoiceRoom বুঝে কার রুমে ঢুকছি
                    // আমার ID এখানে পাঠাবো না, VoiceRoom নিজে prefs থেকে আমার ID নিবে
                    Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                    intent.putExtra("ROOM_ID", selectedRoom.getRoomOwnerId());
                    intent.putExtra("ROOM_OWNER_ID", selectedRoom.getRoomOwnerId());
                    startActivity(intent);
                }
            });
        }

        if (btnHomeTab != null) {
            btnHomeTab.setOnClickListener(v -> {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnInboxTab != null) {
            btnInboxTab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, InboxActivity.class);
                startActivity(intent);
            });
        }

        if (btnMeTab != null) {
            btnMeTab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }

        if (btnMomentTab != null) {
            btnMomentTab.setOnClickListener(v ->
                Toast.makeText(this, "SR Moments Coming Soon!", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void checkAndCreateRoom() {
        String myId = prefs.getString("MY_9_DIGIT_ID", "");
        // FIX 1: ID খালি থাকলে নতুন ID বানাবো না! Login / Edit এ পাঠাবো
        if (myId == null || myId.isEmpty()) {
            Toast.makeText(this, "Profile পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, EditProfileActivity.class);
            startActivity(i);
            return;
        }

        activeRoomsRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                    intent.putExtra("ROOM_ID", myId);
                    intent.putExtra("ROOM_OWNER_ID", myId);
                    startActivity(intent);
                } else {
                    showCreateRoomDialog();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showCreateRoomDialog();
            }
        });
    }

    private void showCreateRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("🎙️ Create Your Voice Room");

        LinearLayout layout = new LinearLayout(HomeActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputRoomName = new EditText(HomeActivity.this);
        inputRoomName.setHint("Enter Room Name (e.g. My Chat Room)...");
        layout.addView(inputRoomName);

        final EditText inputRoomBio = new EditText(HomeActivity.this);
        inputRoomBio.setHint("Enter Room Notice / Bio...");
        layout.addView(inputRoomBio);

        builder.setView(layout);

        builder.setPositiveButton("CONFIRM", (dialog, which) -> {
            String roomName = inputRoomName.getText().toString().trim();
            String roomBio = inputRoomBio.getText().toString().trim();

            // FIX 2: এখানে কখনো নতুন Random ID বানাবো না - এটাই ছিলো তোমার আসল বাগ
            String myId = prefs.getString("MY_9_DIGIT_ID", "");
            if (myId == null || myId.isEmpty()) {
                Toast.makeText(this, "ID পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show();
                return;
            }

            if (roomName.isEmpty()) {
                roomName = prefs.getString("USER_NAME", "User") + "'s Room";
            }
            if (roomBio.isEmpty()) {
                roomBio = "Welcome to my voice chat room!";
            }

            // FIX 3: ছবির Key ঠিক করলাম - USER_PHOTO / USER_IMAGE ২ টাই চেক করবো
            // EditProfile এ USER_PHOTO নামে সেভ হয়, তাই ওইটাই নিবো
            String userImage = prefs.getString("USER_PHOTO", "");
            if (userImage.isEmpty()) userImage = prefs.getString("USER_IMAGE", "");
            if (userImage.isEmpty()) userImage = prefs.getString("USER_PROFILE_IMAGE", "");

            // RULE: Room ID = আমার ID - সেম, আলাদা না
            DatabaseReference roomMetaRef = roomsRef.child("room_" + myId).child("meta");
            roomMetaRef.child("roomName").setValue(roomName);
            roomMetaRef.child("roomBio").setValue(roomBio);
            roomMetaRef.child("roomOwnerImage").setValue(userImage);
            roomMetaRef.child("roomOwnerId").setValue(myId);
            roomMetaRef.child("roomId").setValue(myId);

            DatabaseReference singleActiveRoomRef = activeRoomsRef.child(myId);
            singleActiveRoomRef.child("roomOwnerId").setValue(myId);
            singleActiveRoomRef.child("roomId").setValue(myId);
            singleActiveRoomRef.child("roomName").setValue(roomName);
            singleActiveRoomRef.child("roomBio").setValue(roomBio);
            singleActiveRoomRef.child("roomOwnerImage").setValue(userImage);

            Toast.makeText(this, "Room Created Successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            intent.putExtra("ROOM_ID", myId);
            intent.putExtra("ROOM_OWNER_ID", myId);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadActiveRooms() {
        activeRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomModelList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String ownerId = child.child("roomOwnerId").getValue(String.class);
                    if (ownerId == null) ownerId = child.getKey(); // Key ই Owner ID
                    String roomName = child.child("roomName").getValue(String.class);
                    String roomBio = child.child("roomBio").getValue(String.class);
                    String roomImage = child.child("roomOwnerImage").getValue(String.class);

                    if (ownerId != null) {
                        RoomModel room = new RoomModel(ownerId, roomName, roomBio, roomImage);
                        roomModelList.add(room);
                    }
                }
                roomGridAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showSearchRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("🔍 Search Room by ID");

        final EditText input = new EditText(HomeActivity.this);
        input.setHint("Enter 9-Digit Owner ID...");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String searchId = input.getText().toString().trim();
            if (!searchId.isEmpty()) {
                activeRoomsRef.child(searchId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String rName = snapshot.child("roomName").getValue(String.class);
                            String rBio = snapshot.child("roomBio").getValue(String.class);

                            AlertDialog.Builder roomPopup = new AlertDialog.Builder(HomeActivity.this);
                            roomPopup.setTitle("🌟 Room Found!");
                            roomPopup.setMessage("Room Name: " + rName + "\nBio: " + rBio + "\nOwner ID: " + searchId);
                            roomPopup.setPositiveButton("Enter Room", (d, w) -> {
                                Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                                intent.putExtra("ROOM_ID", searchId);
                                intent.putExtra("ROOM_OWNER_ID", searchId);
                                startActivity(intent);
                            });
                            roomPopup.setNegativeButton("Cancel", (d, w) -> d.dismiss());
                            roomPopup.show();
                        } else {
                            Toast.makeText(HomeActivity.this, "Room not found or offline!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
