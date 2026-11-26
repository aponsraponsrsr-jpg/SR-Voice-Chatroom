package com.sr.voicechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private boolean isSyncingActive = false;

    private ImageView dialogCreatePhoto = null;
    private String pickedCreatePhotoBase64 = null;
    private ActivityResultLauncher<String> pickCreatePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (is != null) is.close();
                if (bmp != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap scaled = Bitmap.createScaledBitmap(bmp, 400, 400, true);
                    scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    pickedCreatePhotoBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                }
            } catch (Exception e) { pickedCreatePhotoBase64 = null; }
            if (dialogCreatePhoto != null) Glide.with(this).load(uri).circleCrop().into(dialogCreatePhoto);
        }
    });

    private BroadcastReceiver profileReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            loadActiveRooms();
        }
    };

    private void openVoiceRoomPermanent(String ownerId){
        if(ownerId==null||ownerId.isEmpty()) return;
        Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
        intent.putExtra("ROOM_ID", ownerId);
        intent.putExtra("ROOM_OWNER_ID", ownerId);
        startActivity(intent);
    }

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
                    openVoiceRoomPermanent(selectedRoom.getRoomOwnerId());
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

    // ===== FIX 1 - Purono Room thakle Active e abar likhe Home e anbe, tarpor Open korbe =====
    private void checkAndCreateRoom() {
        String myId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myId == null || myId.isEmpty()) {
            Toast.makeText(this, "Profile পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, EditProfileActivity.class); startActivity(i); return;
        }
        roomsRef.child("room_" + myId).child("meta").addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot metaSnap){
                if(metaSnap.exists()){
                    // Purono Room ache - Active e na thakle abar baniye dao, jate Home e dekha jay
                    Map<String,Object> active = new HashMap<>();
                    String n = metaSnap.child("roomName").getValue(String.class);
                    String b = metaSnap.child("roomBio").getValue(String.class);
                    if(b==null||b.isEmpty()) b = metaSnap.child("announcement").getValue(String.class);
                    String p = metaSnap.child("roomPhoto").getValue(String.class);
                    if(p==null||p.isEmpty()) p = metaSnap.child("roomOwnerImage").getValue(String.class);
                    active.put("roomOwnerId", myId);
                    active.put("roomId", myId);
                    if(n!=null) active.put("roomName", n);
                    if(b!=null) { active.put("roomBio", b); active.put("announcement", b); }
                    if(p!=null) { active.put("roomPhoto", p); active.put("roomOwnerImage", p); }
                    String th = metaSnap.child("theme").getValue(String.class);
                    String lb = metaSnap.child("roomLabel").getValue(String.class);
                    if(th!=null) active.put("theme", th);
                    if(lb!=null) active.put("roomLabel", lb);
                    activeRoomsRef.child(myId).updateChildren(active).addOnCompleteListener(t -> openVoiceRoomPermanent(myId));
                } else {
                    activeRoomsRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                openVoiceRoomPermanent(myId);
                            } else showCreateRoomDialog();
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { showCreateRoomDialog(); }
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){ showCreateRoomDialog(); }
        });
    }

    private void showCreateRoomDialog() {
        pickedCreatePhotoBase64 = null;
        dialogCreatePhoto = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("🎙️ Create Your Voice Room");
        LinearLayout layout = new LinearLayout(HomeActivity.this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(50,20,50,20); layout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        dialogCreatePhoto = new ImageView(HomeActivity.this);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(220, 220);
        imgLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        imgLp.bottomMargin = 20;
        dialogCreatePhoto.setLayoutParams(imgLp);
        String myPhotoForCreate = prefs.getString("USER_PHOTO", "");
        if (myPhotoForCreate.isEmpty()) myPhotoForCreate = prefs.getString("PROFILE_PHOTO", "");
        if (myPhotoForCreate.isEmpty()) myPhotoForCreate = prefs.getString("USER_IMAGE", "");
        if (myPhotoForCreate != null && !myPhotoForCreate.isEmpty() && !myPhotoForCreate.equals("null")) {
            try {
                if (myPhotoForCreate.startsWith("http")) Glide.with(this).load(myPhotoForCreate).circleCrop().into(dialogCreatePhoto);
                else { byte[] b = Base64.decode(myPhotoForCreate, Base64.DEFAULT); Glide.with(this).load(b).circleCrop().into(dialogCreatePhoto); }
            } catch (Exception e) { dialogCreatePhoto.setImageResource(R.mipmap.ic_launcher); }
        } else dialogCreatePhoto.setImageResource(R.mipmap.ic_launcher);
        dialogCreatePhoto.setOnClickListener(v -> pickCreatePhoto.launch("image/*"));
        layout.addView(dialogCreatePhoto);
        TextView tvTapPhoto = new TextView(HomeActivity.this);
        tvTapPhoto.setText("👆 Tap Photo to Change Header Photo");
        tvTapPhoto.setGravity(android.view.Gravity.CENTER);
        tvTapPhoto.setTextSize(13);
        tvTapPhoto.setPadding(0, 0, 0, 20);
        tvTapPhoto.setOnClickListener(v -> pickCreatePhoto.launch("image/*"));
        layout.addView(tvTapPhoto);
        final EditText inputRoomName = new EditText(HomeActivity.this); inputRoomName.setHint("Enter Room Name (e.g. My Chat Room)..."); inputRoomName.setText(prefs.getString("USER_NAME", "User") + "'s Room"); layout.addView(inputRoomName);
        final EditText inputRoomBio = new EditText(HomeActivity.this); inputRoomBio.setHint("Enter Room Notice / Bio..."); inputRoomBio.setText("Welcome to my voice chat room!"); layout.addView(inputRoomBio);
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
            String finalPhoto = userImage;
            if (pickedCreatePhotoBase64 != null && !pickedCreatePhotoBase64.isEmpty()) finalPhoto = pickedCreatePhotoBase64;
            String finalRoomName = roomName;
            String finalRoomBio = roomBio;
            String finalUserImage = finalPhoto;
            roomsRef.child("room_" + myId).child("meta").addListenerForSingleValueEvent(new ValueEventListener(){
                @Override public void onDataChange(@NonNull DataSnapshot snap){
                    if(snap.exists()){
                        openVoiceRoomPermanent(myId);
                        return;
                    }
                    Map<String,Object> meta = new HashMap<>();
                    meta.put("roomName", finalRoomName);
                    meta.put("roomBio", finalRoomBio);
                    meta.put("announcement", finalRoomBio);
                    meta.put("roomOwnerImage", finalUserImage);
                    meta.put("roomPhoto", finalUserImage);
                    meta.put("roomOwnerId", myId);
                    meta.put("roomId", myId);
                    meta.put("ownerId", myId);
                    meta.put("theme", "Default");
                    meta.put("roomLabel", "Friends");
                    Map<String,Object> active = new HashMap<>();
                    active.put("roomOwnerId", myId);
                    active.put("roomId", myId);
                    active.put("roomName", finalRoomName);
                    active.put("roomBio", finalRoomBio);
                    active.put("announcement", finalRoomBio);
                    active.put("roomOwnerImage", finalUserImage);
                    active.put("roomPhoto", finalUserImage);
                    active.put("theme", "Default");
                    active.put("roomLabel", "Friends");
                    // FIX 2 - Age lekha sesh hobe, tarpor Room khulbe
                    roomsRef.child("room_" + myId).child("meta").setValue(meta).addOnCompleteListener(t1 -> {
                        activeRoomsRef.child(myId).setValue(active).addOnCompleteListener(t2 -> {
                            prefs.edit().putString("HEADER_"+myId+"_NAME", finalRoomName)
                                .putString("HEADER_"+myId+"_PHOTO", finalUserImage)
                                .putString("HEADER_"+myId+"_ANN", finalRoomBio)
                                .putString("HEADER_"+myId+"_THEME", "Default")
                                .putString("HEADER_"+myId+"_LABEL", "Friends").apply();
                            Toast.makeText(HomeActivity.this, "Room Created Successfully!", Toast.LENGTH_SHORT).show();
                            openVoiceRoomPermanent(myId);
                        });
                    });
                }
                @Override public void onCancelled(@NonNull DatabaseError e){}
            });
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
            if(roomBio==null||roomBio.isEmpty()) roomBio = child.child("announcement").getValue(String.class);
            String roomImage = child.child("roomPhoto").getValue(String.class);
            if(roomImage==null||roomImage.isEmpty()) roomImage = child.child("roomOwnerImage").getValue(String.class);
            if (roomName==null||roomName.isEmpty()) roomName = "Voice Room";
            if (ownerId != null) {
                RoomModel rm = new RoomModel(ownerId, roomName, roomBio, roomImage);
                roomModelList.add(rm);
                syncMetaToList(rm);
            }
        }
        if(roomGridAdapter!=null) roomGridAdapter.notifyDataSetChanged();
    }
    private void syncMetaToList(RoomModel rm){
        if(isSyncingActive) return;
        String ownerId = rm.getRoomOwnerId();
        if(ownerId==null) return;
        roomsRef.child("room_"+ownerId).child("meta").addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot snap){
                if(!snap.exists()) return;
                String mName = snap.child("roomName").getValue(String.class);
                String mBio = snap.child("roomBio").getValue(String.class);
                if(mBio==null||mBio.isEmpty()) mBio = snap.child("announcement").getValue(String.class);
                String mPhoto = snap.child("roomPhoto").getValue(String.class);
                if(mPhoto==null||mPhoto.isEmpty()) mPhoto = snap.child("roomOwnerImage").getValue(String.class);
                boolean changed=false;
                if(mName!=null && !mName.isEmpty() && !mName.equals(rm.getRoomName())){ rm.setRoomName(mName); changed=true; }
                if(mBio!=null && !mBio.isEmpty()){ rm.setRoomBio(mBio); changed=true; }
                if(mPhoto!=null && !mPhoto.isEmpty() && !mPhoto.equals("null") && !mPhoto.equals(rm.getRoomImage())){ rm.setRoomImage(mPhoto); changed=true; }
                if(changed && roomGridAdapter!=null) roomGridAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
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
                                openVoiceRoomPermanent(searchId);
                            });
                            roomPopup.setNegativeButton("Cancel", (d, w) -> d.dismiss()); roomPopup.show();
                        } else {
                            // Active e na thakle Meta te khujo - purono Room holeo pabe
                            roomsRef.child("room_"+searchId).child("meta").addListenerForSingleValueEvent(new ValueEventListener(){
                                @Override public void onDataChange(@NonNull DataSnapshot ms){
                                    if(ms.exists()){
                                        String rName2 = ms.child("roomName").getValue(String.class);
                                        String rBio2 = ms.child("roomBio").getValue(String.class);
                                        AlertDialog.Builder roomPopup = new AlertDialog.Builder(HomeActivity.this);
                                        roomPopup.setTitle("🌟 Room Found!");
                                        roomPopup.setMessage("Room Name: " + rName2 + "\nBio: " + rBio2 + "\nOwner ID: " + searchId);
                                        roomPopup.setPositiveButton("Enter Room", (d, w) -> openVoiceRoomPermanent(searchId));
                                        roomPopup.setNegativeButton("Cancel", (d, w) -> d.dismiss()); roomPopup.show();
                                    } else Toast.makeText(HomeActivity.this, "Room not found or offline!", Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onCancelled(@NonNull DatabaseError e){}
                            });
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel()); builder.show();
    }
}
