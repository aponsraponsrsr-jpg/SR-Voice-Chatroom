package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {
    private String roomId, roomOwnerId, myId, myName, myGender, myCountry, myPhoto;
    private boolean amIOwner = false, amIAdmin = false;
    private TextView tvRoomId, tvRoomName, tvMyIdInRoom;
    private TextView roomTitleText, roomInfoText;
    private ImageView roomHeaderProfileImg;
    private Button btnRoomSet, btnRoomEdit;
    private View layoutOwnerTools;
    private RecyclerView rvOnline;
    private LinearLayout chatListContainer;
    private DatabaseReference activeRoomRef, roomMetaRef;
    private SharedPreferences prefs;
    private ImageView dialogRoomPhoto;
    private Uri pickedRoomPhotoUri = null;
    private String selectedTheme = "Default";
    private String currentRoomPhotoUrl = "";

    private ActivityResultLauncher<String> pickRoomPhoto = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                pickedRoomPhotoUri = uri;
                if (dialogRoomPhoto != null) {
                    Glide.with(this).load(uri).circleCrop().into(dialogRoomPhoto);
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = prefs.getString("MY_9_DIGIT_ID", "");
        myName = prefs.getString("USER_NAME", "User");
        myGender = prefs.getString("USER_GENDER", "");
        myCountry = prefs.getString("USER_COUNTRY", "");
        myPhoto = prefs.getString("USER_PHOTO", "");
        if (myPhoto == null || myPhoto.isEmpty()) myPhoto = prefs.getString("PROFILE_PHOTO", "");
        if (myPhoto == null || myPhoto.isEmpty()) myPhoto = prefs.getString("USER_PHOTO_URL", "");
        if (myId == null || myId.isEmpty()) {
            Toast.makeText(this, "ID পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show();
            finish(); return;
        }
        roomId = myId;
        roomOwnerId = myId;
        activeRoomRef = FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId);
        roomMetaRef = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + roomOwnerId).child("meta");
        tvRoomId = findViewById(R.id.tvRoomId);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvMyIdInRoom = findViewById(R.id.tvMyIdInRoom);
        btnRoomSet = findViewById(R.id.btnRoomSet);
        btnRoomEdit = findViewById(R.id.btnRoomEdit);
        layoutOwnerTools = findViewById(R.id.layoutOwnerTools);
        rvOnline = findViewById(R.id.rvOnline);
        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
        chatListContainer = findViewById(R.id.chatListContainer);
        if (rvOnline != null) rvOnline.setLayoutManager(new LinearLayoutManager(this));
        if (roomHeaderProfileImg != null && myPhoto != null && !myPhoto.isEmpty()) {
            Glide.with(this).load(myPhoto).circleCrop().into(roomHeaderProfileImg);
        }
        if (tvRoomId != null) tvRoomId.setText("Room ID: " + roomId);
        if (roomInfoText != null) roomInfoText.setText("ID: " + roomId);
        if (tvMyIdInRoom != null) tvMyIdInRoom.setText("আমার ID: " + myId + " | " + myName);
        if (layoutOwnerTools != null) layoutOwnerTools.setVisibility(View.GONE);
        if (btnRoomSet != null) btnRoomSet.setVisibility(View.GONE);
        if (btnRoomEdit != null) btnRoomEdit.setVisibility(View.GONE);
        checkMyRole();
        loadRoomInfo();
        View btnRoomTools = findViewById(R.id.btnRoomTools);
        if (btnRoomTools != null) {
            btnRoomTools.setOnClickListener(v -> openPremiumSettings());
        }
        if (btnRoomSet != null) {
            btnRoomSet.setOnClickListener(v -> openRoomSetDialog());
        }
        ImageButton btnPower = findViewById(R.id.btnPower);
        if (btnPower != null) {
            btnPower.setOnClickListener(v -> showPowerMenu());
        }
    }

    private void showPowerMenu() {
        String[] options = {"Minimize", "Exit Room"};
        new AlertDialog.Builder(this)
            .setTitle("Room Options")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Minimize = Room theke ber hoye App er Home Page e jabe
                    finish();
                } else {
                    // Exit Room = App theke ekbare ber hoye jabe
                    finishAffinity();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void checkMyRole() {
        amIOwner = myId.equals(roomOwnerId);
        activeRoomRef.child("admins").child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                amIAdmin = snap.exists() && Boolean.TRUE.equals(snap.getValue(Boolean.class));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void loadRoomInfo() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                String rName = snap.child("roomName").getValue(String.class);
                String rPhoto = snap.child("roomPhoto").getValue(String.class);
                String rAnnounce = snap.child("announcement").getValue(String.class);
                String rTheme = snap.child("theme").getValue(String.class);
                if (rName != null) {
                    if (tvRoomName != null) tvRoomName.setText(rName);
                    if (roomTitleText != null) roomTitleText.setText(rName);
                }
                if (rPhoto != null && !rPhoto.isEmpty()) {
                    currentRoomPhotoUrl = rPhoto;
                    if (roomHeaderProfileImg != null) Glide.with(VoiceRoomActivity.this).load(rPhoto).circleCrop().into(roomHeaderProfileImg);
                } else if (myPhoto != null && !myPhoto.isEmpty()) {
                    currentRoomPhotoUrl = myPhoto;
                }
                if (rAnnounce != null && !rAnnounce.isEmpty()) {
                    TextView notice = findViewById(R.id.roomNoticeBarText);
                    if (notice != null) { notice.setVisibility(View.VISIBLE); notice.setText(rAnnounce); }
                }
                if (rTheme != null) selectedTheme = rTheme;
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void openPremiumSettings() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_settings, null);
        dialog.setContentView(view);
        view.findViewById(R.id.btnSettingsRoomSet).setOnClickListener(v -> {
            dialog.dismiss();
            openRoomSetDialog();
        });
        view.findViewById(R.id.btnSettingsClearChat).setOnClickListener(v -> {
            dialog.dismiss();
            clearChat();
        });
        view.findViewById(R.id.btnSettingsClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void clearChat() {
        if (chatListContainer != null) chatListContainer.removeAllViews();
        activeRoomRef.child("chats").removeValue();
        Toast.makeText(this, "Chat Clean হয়ে গেছে", Toast.LENGTH_SHORT).show();
    }

    private void openRoomSetDialog() {
        if (!amIOwner && !amIAdmin) {
            Toast.makeText(this, "Only Room Owner / Admin can change", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_set, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialogRoomPhoto = view.findViewById(R.id.imgRoomPhoto);
        EditText etName = view.findViewById(R.id.etRoomName);
        EditText etAnnounce = view.findViewById(R.id.etRoomAnnouncement);
        TextView tvTheme = view.findViewById(R.id.tvSelectedTheme);
        if (tvRoomName != null) etName.setText(tvRoomName.getText().toString());
        if (currentRoomPhotoUrl != null && !currentRoomPhotoUrl.isEmpty()) {
            Glide.with(this).load(currentRoomPhotoUrl).circleCrop().into(dialogRoomPhoto);
        } else if (myPhoto != null && !myPhoto.isEmpty()) {
            Glide.with(this).load(myPhoto).circleCrop().into(dialogRoomPhoto);
        }
        tvTheme.setText("Theme: " + selectedTheme);
        dialogRoomPhoto.setOnClickListener(v -> pickRoomPhoto.launch("image/*"));
        view.findViewById(R.id.btnThemeDefault).setOnClickListener(v -> { selectedTheme = "Default"; tvTheme.setText("Theme: Default"); });
        view.findViewById(R.id.btnThemeGold).setOnClickListener(v -> { selectedTheme = "Gold"; tvTheme.setText("Theme: Gold"); });
        view.findViewById(R.id.btnThemeNight).setOnClickListener(v -> { selectedTheme = "Night"; tvTheme.setText("Theme: Night"); });
        view.findViewById(R.id.btnSaveRoomSet).setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newAnnounce = etAnnounce.getText().toString().trim();
            if (newName.isEmpty()) { Toast.makeText(this, "Room Name দাও", Toast.LENGTH_SHORT).show(); return; }
            String finalPhoto = currentRoomPhotoUrl;
            if (pickedRoomPhotoUri != null) finalPhoto = pickedRoomPhotoUri.toString();
            Map<String, Object> map = new HashMap<>();
            map.put("roomName", newName);
            map.put("announcement", newAnnounce);
            map.put("theme", selectedTheme);
            map.put("roomPhoto", finalPhoto);
            map.put("roomId", myId);
            map.put("ownerId", myId);
            roomMetaRef.updateChildren(map);
            Map<String, Object> activeMap = new HashMap<>();
            activeMap.put("roomName", newName);
            activeMap.put("roomPhoto", finalPhoto);
            activeRoomRef.updateChildren(activeMap);
            if (tvRoomName != null) tvRoomName.setText(newName);
            if (roomTitleText != null) roomTitleText.setText(newName);
            if (roomHeaderProfileImg != null && finalPhoto != null && !finalPhoto.isEmpty()) {
                Glide.with(this).load(finalPhoto).circleCrop().into(roomHeaderProfileImg);
            }
            currentRoomPhotoUrl = finalPhoto;
            pickedRoomPhotoUri = null;
            Toast.makeText(this, "Room Save হয়ে গেছে", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        view.findViewById(R.id.btnCancelRoomSet).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void makeAdmin(String targetUser9DigitId) {
        if (!amIOwner) { Toast.makeText(this, "শুধু Owner Admin দিতে পারে", Toast.LENGTH_SHORT).show(); return; }
        Map<String,Object> map = new HashMap<>();
        map.put("admins/" + targetUser9DigitId, true);
        activeRoomRef.updateChildren(map);
    }
}
