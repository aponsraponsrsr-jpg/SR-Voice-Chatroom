package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
    private ImageView btnSearchRoom;
    private Button btnHomeTab, btnMomentTab, btnMeTab;
    private ListView roomListView;

    private ArrayList<String> roomDisplayList;
    private ArrayList<String> roomOwnerIdList;
    private ArrayAdapter<String> roomAdapter;

    private DatabaseReference activeRoomsRef;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        activeRoomsRef = FirebaseDatabase.getInstance().getReference("active_rooms");

        btnPlus = findViewById(R.id.btnPlus);
        btnSearchRoom = findViewById(R.id.btnSearchRoom);
        btnHomeTab = findViewById(R.id.btnHomeTab);
        btnMomentTab = findViewById(R.id.btnMomentTab);
        btnMeTab = findViewById(R.id.btnMeTab);
        roomListView = findViewById(R.id.roomListView);

        roomDisplayList = new ArrayList<>();
        roomOwnerIdList = new ArrayList<>();
        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roomDisplayList);
        if (roomListView != null) {
            roomListView.setAdapter(roomAdapter);
        }

        // ফায়ারবেস থেকে লাইভ রুম লোড করা
        loadActiveRooms();

        // প্লাস (+) বাটনে ক্লিক করলে নিজের রুমে এন্ট্রি হবে
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                String myId = prefs.getString("MY_9_DIGIT_ID", "777888");
                Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                intent.putExtra("ROOM_OWNER_ID", myId);
                startActivity(intent);
            });
        }

        // সার্চ আইকনে ক্লিক করলে আইডি দিয়ে রুম সার্চ করার ডায়ালগ আসবে
        if (btnSearchRoom != null) {
            btnSearchRoom.setOnClickListener(v -> showSearchRoomDialog());
        }

        // হোম পেজের রুম লিস্টের কোনো কার্ডে ক্লিক করলে সরাসরি সেই রুমে এন্ট্রি হবে
        if (roomListView != null) {
            roomListView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < roomOwnerIdList.size()) {
                    String targetOwnerId = roomOwnerIdList.get(position);
                    Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                    intent.putExtra("ROOM_OWNER_ID", targetOwnerId);
                    startActivity(intent);
                }
            });
        }

        // 'Me' ট্যাবে ক্লিক করলে লকড ওয়ালেট ও প্রিমিয়াম অপশন শো করবে
        if (btnMeTab != null) {
            btnMeTab.setOnClickListener(v -> showMeProfileAndWalletDialog());
        }

        if (btnMomentTab != null) {
            btnMomentTab.setOnClickListener(v -> 
                Toast.makeText(this, "SR Moments Coming Soon!", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void loadActiveRooms() {
        activeRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomDisplayList.clear();
                roomOwnerIdList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String ownerId = child.child("roomOwnerId").getValue(String.class);
                    String roomName = child.child("roomName").getValue(String.class);
                    String roomBio = child.child("roomBio").getValue(String.class);

                    if (ownerId != null) {
                        String displayName = "🌟 " + (roomName != null ? roomName : "SR Voice Room") + 
                                             "\n📝 Bio: " + (roomBio != null ? roomBio : "Welcome!") + 
                                             " (Owner ID: " + ownerId + ")";
                        roomDisplayList.add(displayName);
                        roomOwnerIdList.add(ownerId);
                    }
                }
                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showSearchRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("🔍 Search Room by ID");

        final EditText input = new EditText(HomeActivity.this);
        input.setHint("Enter 9-Digit Owner ID (e.g. 777888)...");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String searchId = input.getText().toString().trim();
            if (!searchId.isEmpty()) {
                // ফায়ারবেসে চেক করা রুমটি আছে কি না
                activeRoomsRef.child(searchId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String rName = snapshot.child("roomName").getValue(String.class);
                            String rBio = snapshot.child("roomBio").getValue(String.class);

                            // স্ক্রিনের ওপর রুম কার্ড ভেসে উঠবে
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

    private void showMeProfileAndWalletDialog() {
        String myName = prefs.getString("USER_NAME", "User");
        String myId = prefs.getString("MY_9_DIGIT_ID", "777888");

        CharSequence[] options = {
            "👤 Profile Name: " + myName,
            "🆔 My Unique ID: " + myId,
            "🔒 Locked Wallet (Coins: 0)",
            "💎 VIP & SVIP Status (Locked)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("👤 My Profile & Wallet");
        builder.setItems(options, (dialog, which) -> {
            if (which == 2) {
                Toast.makeText(this, "Locked Wallet: Recharge feature coming soon!", Toast.LENGTH_SHORT).show();
            } else if (which == 3) {
                Toast.makeText(this, "VIP Feature is currently locked.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
