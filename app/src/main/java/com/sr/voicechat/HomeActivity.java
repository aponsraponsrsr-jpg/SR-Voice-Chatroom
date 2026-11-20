package com.sr.voicechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
    private DatabaseReference activeRoomsRef, roomsRef;
    private SharedPreferences prefs;
    private SwipeRefreshLayout swipeRefresh = null;
    private ValueEventListener activeRoomsListener = null;
    private boolean roomsListenerStarted = false;
    private boolean isRefreshingManual = false;

    private BroadcastReceiver profileReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            loadActiveRooms();
        }
    };

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
        // ===== FIX - Hero Fixed - GridView te Adapter bosbe =====
        if (roomGridView != null) roomGridView.setAdapter(roomGridAdapter);
        setupPullToRefresh();
        try {
            IntentFilter filter = new IntentFilter("com.sr.voicechat.PROFILE_UPDATED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(profileReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(profileReceiver, filter);
            }
        } catch (Exception ignored) {}
        loadActiveRooms();
        View.OnClickListener createRoomListener = v -> checkAndCreateRoom();
        if (btnPlus != null) btnPlus.setOnClickListener(createRoomListener);
        if (btnCreateRoomTop != null) btnCreateRoomTop.setOnClickListener(createRoomListener);
        if (btnSearchRoom != null) btnSearchRoom.setOnClickListener(v -> showSearchRoomDialog());
        if (roomGridView != null) {
            roomGridView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < roomModelList.size()) {
                    RoomModel selectedRoom = roomModelList.get(position);
                    Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                    intent.putExtra("ROOM_ID", selectedRoom.getRoomOwnerId());
                    intent.putExtra("ROOM_OWNER_ID", selectedRoom.getRoomOwnerId());
                    startActivity(intent);
                }
            });
        }
        if (btnHomeTab != null) btnHomeTab.setOnClickListener(v -> { doManualRefresh(); Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show(); });
        if (btnInboxTab != null) btnInboxTab.setOnClickListener(v -> { Intent intent = new Intent(HomeActivity.this, InboxActivity.class); startActivity(intent); });
        if (btnMeTab != null) btnMeTab.setOnClickListener(v -> { Intent intent = new Intent(HomeActivity.this, MeActivity.class); startActivity(intent); });
        if (btnMomentTab != null) btnMomentTab.setOnClickListener(v -> Toast.makeText(this, "SR Moments Coming Soon!", Toast.LENGTH_SHORT).show());
    }

    private void setupPullToRefresh(){
        try{
            ViewGroup root = findViewById(android.R.id.content);
            if(root==null || root.getChildCount()==0) return;
            View content = root.getChildAt(0);
            if(content instanceof SwipeRefreshLayout) { swipeRefresh=(SwipeRefreshLayout)content; return; }
            root.removeView(content);
            swipeRefresh = new SwipeRefreshLayout(this);
            swipeRefresh.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            swipeRefresh.setColorSchemeColors(0xFFFFD700, 0xFFFF4081, 0xFF7B1FA2, 0xFF00BCD4);
            swipeRefresh.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            root.addView(swipeRefresh);
            swipeRefresh.setOnRefreshListener(() -> doManualRefresh());
        }catch(Exception ignored){}
    }
    private void doManualRefresh(){
        if(swipeRefresh!=null && !swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(true);
        isRefreshingManual = true;
        activeRoomsRef.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot snapshot){ applyRoomsSnapshot(snapshot); stopRefresh(); }
            @Override public void onCancelled(@NonNull DatabaseError error){ stopRefresh(); }
        });
        if(roomGridView!=null) roomGridView.postDelayed(this::stopRefresh, 3000);
    }
    private void stopRefresh(){
        isRefreshingManual = false;
        if(swipeRefresh!=null && swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(profileReceiver); } catch (Exception ignored) {}
        try { if(activeRoomsRef!=null && activeRoomsListener!=null) activeRoomsRef.removeEventListener(activeRoomsListener); } catch(Exception ignored){}
    }
    @Override protected void onResume(){
        super.onResume();
        if(!isRefreshingManual) loadActiveRooms();
    }
    private void checkAndCreateRoom() {
        String myId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myId == null || myId.isEmpty()) {
            Toast.makeText(this, "Profile পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, EditProfileActivity.class); startActivity(i); return;
        }
        activeRoomsRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                    intent.putExtra("ROOM_ID", myId); intent.putExtra("ROOM_OWNER_ID", myId); startActivity(intent);
                } else showCreateRoomDialog();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { showCreateRoomDialog(); }
        });
    }
    private void showCreateRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("🎙️ Create Your Voice Room");
        LinearLayout layout = new LinearLayout(HomeActivity.this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(50,20,50,20);
        final EditText inputRoomName = new EditText(HomeActivity.this); inputRoomName.setHint("Enter Room Name (e.g. My Chat Room)..."); layout.addView(inputRoomName);
        final EditText inputRoomBio = new EditText(HomeActivity.this); inputRoomBio.setHint("Enter Room Notice / Bio..."); layout.addView(inputRoomBio);
        builder.setView(layout);
        builder.setPositiveButton("CONFIRM", (dialog, which) -> {
            String roomName = inputRoomName.getText().toString().trim();
            String roomBio = inputRoomBio.getText().toString().trim();
            String myId = prefs.getString("MY_9_DIGIT_ID", "");
            if (myId == null || myId.isEmpty()) { Toast.makeText(this, "ID পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show(); return; }
            if (roomName.isEmpty()) roomName = prefs.getString("USER_NAME", "User") + "'s Room";
            if (roomBio.isEmpty()) roomBio = "Welcome to my voice chat room!";
            String userImage = prefs.getString("USER_PHOTO", "");
            if (userImage.isEmpty()) userImage = prefs.getString("PROFILE_PHOTO", "");
            if (userImage.isEmpty()) userImage = prefs.getString("USER_IMAGE", "");
            if (userImage.isEmpty()) userImage = prefs.getString("USER_PROFILE_IMAGE", "");
            DatabaseReference roomMetaRef = roomsRef.child("room_" + myId).child("meta");
            roomMetaRef.child("roomName").setValue(roomName);
            roomMetaRef.child("roomBio").setValue(roomBio);
            roomMetaRef.child("roomOwnerImage").setValue(userImage);
            roomMetaRef.child("roomOwnerId").setValue(myId);
            roomMetaRef.child("roomId").setValue(myId);
            roomMetaRef.child("announcement").setValue(roomBio);
            roomMetaRef.child("theme").setValue("Default");
            roomMetaRef.child("roomLabel").setValue("Friends");
            roomMetaRef.child("roomPhoto").setValue(userImage);
            DatabaseReference singleActiveRoomRef = activeRoomsRef.child(myId);
            singleActiveRoomRef.child("roomOwnerId").setValue(myId);
            singleActiveRoomRef.child("roomId").setValue(myId);
            singleActiveRoomRef.child("roomName").setValue(roomName);
            singleActiveRoomRef.child("roomBio").setValue(roomBio);
            singleActiveRoomRef.child("roomOwnerImage").setValue(userImage);
            Toast.makeText(this, "Room Created Successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            intent.putExtra("ROOM_ID", myId); intent.putExtra("ROOM_OWNER_ID", myId); startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    private void loadActiveRooms() {
        if(roomsListenerStarted) return;
        roomsListenerStarted = true;
        activeRoomsListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) { applyRoomsSnapshot(snapshot); stopRefresh(); }
            @Override public void onCancelled(@NonNull DatabaseError error) { stopRefresh(); }
        };
        activeRoomsRef.addValueEventListener(activeRoomsListener);
    }
    private void applyRoomsSnapshot(DataSnapshot snapshot){
        roomModelList.clear();
        for (DataSnapshot child : snapshot.getChildren()) {
            String ownerId = child.child("roomOwnerId").getValue(String.class);
            if (ownerId == null) ownerId = child.getKey();
            String roomName = child.child("roomName").getValue(String.class);
            String roomBio = child.child("roomBio").getValue(String.class);
            String roomImage = child.child("roomOwnerImage").getValue(String.class);
            if (roomName==null||roomName.isEmpty()) roomName = "Voice Room";
            if (ownerId != null) {
                RoomModel rm = new RoomModel(ownerId, roomName, roomBio, roomImage);
                roomModelList.add(rm);
                refreshRoomImage(rm);
            }
        }
        if(roomGridAdapter!=null) roomGridAdapter.notifyDataSetChanged();
    }
    private void refreshRoomImage(RoomModel rm) {
        try {
            FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("my9DigitId").equalTo(rm.getRoomOwnerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        for (DataSnapshot ds : snap.getChildren()) {
                            String fresh = null;
                            if (ds.child("photoUrl").getValue() != null) fresh = String.valueOf(ds.child("photoUrl").getValue());
                            if ((fresh == null || fresh.isEmpty()) && ds.child("photo").getValue() != null) fresh = String.valueOf(ds.child("photo").getValue());
                            if (fresh != null && !fresh.isEmpty() && !fresh.equals("null") && !fresh.equals(rm.getRoomImage())) {
                                rm.setRoomImage(fresh);
                                if (roomGridAdapter != null) roomGridAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
        } catch (Exception ignored) {}
    }
    private void showSearchRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("🔍 Search Room by ID");
        final EditText input = new EditText(HomeActivity.this); input.setHint("Enter 9-Digit Owner ID..."); builder.setView(input);
        builder.setPositiveButton("Search", (dialog, which) -> {
            String searchId = input.getText().toString().trim();
            if (!searchId.isEmpty()) {
                activeRoomsRef.child(searchId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String rName = snapshot.child("roomName").getValue(String.class);
                            String rBio = snapshot.child("roomBio").getValue(String.class);
                            AlertDialog.Builder roomPopup = new AlertDialog.Builder(HomeActivity.this);
                            roomPopup.setTitle("🌟 Room Found!");
                            roomPopup.setMessage("Room Name: " + rName + "\nBio: " + rBio + "\nOwner ID: " + searchId);
                            roomPopup.setPositiveButton("Enter Room", (d, w) -> {
                                Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                                intent.putExtra("ROOM_ID", searchId); intent.putExtra("ROOM_OWNER_ID", searchId); startActivity(intent);
                            });
                            roomPopup.setNegativeButton("Cancel", (d, w) -> d.dismiss()); roomPopup.show();
                        } else Toast.makeText(HomeActivity.this, "Room not found or offline!", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()); builder.show();
    }
}
