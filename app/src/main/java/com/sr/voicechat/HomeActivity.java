package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        btnCreateRoomTop = findViewById(R.id.btnCreateRoomTop); // ওপরের প্লাস আইকন
        btnHomeTab = findViewById(R.id.btnHomeTab);
        btnMomentTab = findViewById(R.id.btnMomentTab);
        btnInboxTab = findViewById(R.id.btnInboxTab); // ইনবক্স ট্যাব
        btnMeTab = findViewById(R.id.btnMeTab);
        roomGridView = findViewById(R.id.roomGridView);

        roomModelList = new ArrayList<>();
        roomGridAdapter = new RoomGridAdapter(this, roomModelList);
        if (roomGridView != null) {
            roomGridView.setAdapter(roomGridAdapter);
        }

        loadActiveRooms();

        // 🌟 স্মার্ট প্লাস (+) আইকন লজিক (নিচের ও ওপরের দুটোর জন্যই কাজ করবে)
        View.OnClickListener createRoomListener = v -> checkAndCreateRoom();

        if (btnPlus != null) {
            btnPlus.setOnClickListener(createRoomListener);
        }
        if (btnCreateRoomTop != null) {
            btnCreateRoomTop.setOnClickListener(createRoomListener);
        }

        if (btnSearchRoom != null) {
            btnSearchRoom.setOnClickListener(v -> showSearchRoomDialog());
        }

        if (roomGridView != null) {
            roomGridView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < roomModelList.size()) {
                    RoomModel selectedRoom = roomModelList.get(position);
                    Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                    intent.putExtra("ROOM_OWNER_ID", selectedRoom.getRoomOwnerId());
                    startActivity(intent);
                }
            });
        }

        // ইনবক্স ট্যাব ক্লিক
        if (btnInboxTab != null) {
            btnInboxTab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, InboxActivity.class);
                startActivity(intent);
            });
        }

        // সরাসরি প্রিমিয়াম MeActivity ওপেন করার লজিক
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
        if (myId.isEmpty()) {
            showCreateRoomDialog();
        } else {
            activeRoomsRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
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

            String myId = prefs.getString("MY_9_DIGIT_ID", "");
            if (myId == null || myId.isEmpty()) {
                myId = String.valueOf((int)(Math.random() * 900000000) + 100000000);
                prefs.edit().putString("MY_9_DIGIT_ID", myId).apply();
            }

            if (roomName.isEmpty()) {
                roomName = prefs.getString("USER_NAME", "User") + "'s Room";
            }
            if (roomBio.isEmpty()) {
                roomBio = "Welcome to my voice chat room!";
            }

            String userImage = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

            DatabaseReference roomMetaRef = roomsRef.child("room_" + myId).child("meta");
            roomMetaRef.child("roomName").setValue(roomName);
            roomMetaRef.child("roomBio").setValue(roomBio);
            roomMetaRef.child("roomOwnerImage").setValue(userImage);

            DatabaseReference singleActiveRoomRef = activeRoomsRef.child(myId);
            singleActiveRoomRef.child("roomOwnerId").setValue(myId);
            singleActiveRoomRef.child("roomName").setValue(roomName);
            singleActiveRoomRef.child("roomBio").setValue(roomBio);
            singleActiveRoomRef.child("roomOwnerImage").setValue(userImage);

            Toast.makeText(this, "Room Created Successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
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
