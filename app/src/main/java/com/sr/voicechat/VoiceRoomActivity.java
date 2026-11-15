package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {
    private String roomId, roomOwnerId, myId, myName, myGender, myCountry, myPhoto, myLevel = "0";
    private boolean amIOwner = false, amIAdmin = false;
    private TextView tvRoomId, tvRoomName, tvMyIdInRoom;
    private TextView roomTitleText, roomInfoText;
    private ImageView roomHeaderProfileImg;
    private Button btnRoomSet, btnRoomEdit;
    private View layoutOwnerTools;
    private RecyclerView rvOnline;
    private android.widget.LinearLayout chatListContainer;
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

    private String getMyPhotoFromAllPlaces() {
        if (prefs == null) return "";
        String[] keys = {"USER_PHOTO","PROFILE_PHOTO","USER_PHOTO_URL","PHOTO_URL","photo","profile_photo","USER_IMAGE","PROFILE_IMAGE","userPhoto","imageUrl","avatar","profileImage"};
        for (String k : keys) {
            String v = prefs.getString(k, "");
            if (v != null && !v.trim().isEmpty() && !v.equals("null")) return v.trim();
        }
        if (myPhoto != null && !myPhoto.trim().isEmpty()) return myPhoto.trim();
        return "";
    }

    // FIX - লিংক হলে Glide, Base64 হলে পুরানো নিয়ম
    private void loadPhotoInto(ImageView iv, String url) {
        if (iv == null) return;
        if (url == null || url.trim().isEmpty() || url.equals("null")) {
            iv.setImageResource(android.R.drawable.sym_def_app_icon);
            return;
        }
        try {
            if (url.trim().startsWith("http")) {
                Glide.with(this).load(url).circleCrop()
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(iv);
            } else {
                byte[] bytes = Base64.decode(url.trim(), Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) iv.setImageBitmap(bmp);
                else iv.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        } catch (Exception e) {
            iv.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    private void syncMyPhotoFromFirebase() {
        // FIX - users এর চাবি firebaseUid, তাই my9DigitId দিয়ে খুঁজতে হবে
        FirebaseDatabase.getInstance().getReference("users")
            .orderByChild("my9DigitId").equalTo(myId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                for (DataSnapshot ds : snap.getChildren()) {
                    String p = null;
                    if (ds.child("photoUrl").getValue() != null) p = String.valueOf(ds.child("photoUrl").getValue());
                    if ((p == null || p.isEmpty()) && ds.child("photo").getValue() != null) p = String.valueOf(ds.child("photo").getValue());
                    if ((p == null || p.isEmpty()) && ds.child("photoBase64").getValue() != null) p = String.valueOf(ds.child("photoBase64").getValue());
                    if (p != null && !p.isEmpty()) {
                        myPhoto = p;
                        prefs.edit().putString("USER_PHOTO", p).putString("PROFILE_PHOTO", p).apply();
                        if (roomHeaderProfileImg != null) loadPhotoInto(roomHeaderProfileImg, myPhoto);
                        markMeOnline();
                    }
                    break;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = prefs.getString("MY_9_DIGIT_ID", "");
        myName = prefs.getString("USER_NAME", "User");
        myGender = prefs.getString("USER_GENDER", "");
        myCountry = prefs.getString("USER_COUNTRY", "Bangladesh");
        myPhoto = getMyPhotoFromAllPlaces();
        myLevel = prefs.getString("USER_LEVEL", "0");
        if (myLevel == null) myLevel = "0";
        if (myId == null || myId.isEmpty()) {
            Toast.makeText(this, "ID পাওয়া যায় নাই, আবার Login করো", Toast.LENGTH_LONG).show();
            finish(); return;
        }
        // FIX - Intent থেকে আসল রুম নিবো, নিজের ID দিয়ে না
        String intentRoomId = getIntent().getStringExtra("ROOM_ID");
        String intentOwnerId = getIntent().getStringExtra("ROOM_OWNER_ID");
        if (intentRoomId != null && !intentRoomId.isEmpty()) roomId = intentRoomId; else roomId = myId;
        if (intentOwnerId != null && !intentOwnerId.isEmpty()) roomOwnerId = intentOwnerId; else roomOwnerId = roomId;
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
        if (roomHeaderProfileImg != null) loadPhotoInto(roomHeaderProfileImg, myPhoto);
        if (tvRoomId != null) tvRoomId.setText("Room ID: " + roomId);
        if (roomInfoText != null) roomInfoText.setText("ID: " + roomId);
        if (tvMyIdInRoom != null) tvMyIdInRoom.setText("আমার ID: " + myId + " | " + myName);
        if (layoutOwnerTools != null) layoutOwnerTools.setVisibility(View.GONE);
        if (btnRoomSet != null) btnRoomSet.setVisibility(View.GONE);
        if (btnRoomEdit != null) btnRoomEdit.setVisibility(View.GONE);
        checkMyRole();
        loadRoomInfo();
        syncMyPhotoFromFirebase();
        markMeOnline();
        View btnOpenChat = findViewById(R.id.btnOpenChat);
        if (btnOpenChat != null) btnOpenChat.setOnClickListener(v -> openChatInput());
        View btnOnlineList = findViewById(R.id.btnOnlineList);
        if (btnOnlineList != null) btnOnlineList.setOnClickListener(v -> openOnlineList());
        View tvOnlineCount = findViewById(R.id.textOnlineCount);
        if (tvOnlineCount != null) tvOnlineCount.setOnClickListener(v -> openOnlineList());
        View btnRoomTools = findViewById(R.id.btnRoomTools);
        if (btnRoomTools != null) btnRoomTools.setOnClickListener(v -> openPremiumSettings());
        if (btnRoomSet != null) btnRoomSet.setOnClickListener(v -> openRoomSetDialog());
        ImageButton btnPower = findViewById(R.id.btnPower);
        if (btnPower != null) btnPower.setOnClickListener(v -> showPowerMenu());
        listenChatRealtime();
        listenOnlineCount();
    }

    private String getFlag(String country) {
        if (country == null) return "🌍";
        String c = country.toLowerCase();
        if (c.contains("bangladesh")) return "🇧🇩";
        if (c.contains("india")) return "🇮🇳";
        if (c.contains("pakistan")) return "🇵🇰";
        if (c.contains("nepal")) return "🇳🇵";
        if (c.contains("sri lanka")) return "🇱🇰";
        if (c.contains("usa") || c.contains("america")) return "🇺🇸";
        if (c.contains("uk") || c.contains("england")) return "🇬🇧";
        if (c.contains("saudi")) return "🇸🇦";
        if (c.contains("uae") || c.contains("emirates")) return "🇦🇪";
        if (c.contains("qatar")) return "🇶🇦";
        if (c.contains("kuwait")) return "🇰🇼";
        if (c.contains("malaysia")) return "🇲🇾";
        if (c.contains("indonesia")) return "🇮🇩";
        if (c.contains("philippines")) return "🇵🇭";
        if (c.contains("turkey")) return "🇹🇷";
        if (c.contains("canada")) return "🇨🇦";
        if (c.contains("australia")) return "🇦🇺";
        return "🌍";
    }

    private void markMeOnline() {
        myPhoto = getMyPhotoFromAllPlaces();
        Map<String, Object> m = new HashMap<>();
        m.put("name", myName);
        m.put("photo", myPhoto == null ? "" : myPhoto);
        m.put("photoUrl", myPhoto == null ? "" : myPhoto);
        m.put("country", myCountry);
        m.put("level", myLevel);
        m.put("id", myId);
        activeRoomRef.child("online_users").child(myId).setValue(m);
        activeRoomRef.child("online_users").child(myId).onDisconnect().removeValue();
    }

    private void listenOnlineCount() {
        activeRoomRef.child("online_users").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                TextView tvC = findViewById(R.id.textOnlineCount);
                if (tvC != null) tvC.setText(String.valueOf(snap.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void openChatInput() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_room_chat, null);
        d.setContentView(v);
        EditText et = v.findViewById(R.id.etChatInput);
        et.requestFocus();
        if (d.getWindow() != null) d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        d.setOnShowListener(dialog -> {
            et.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(et, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });
        v.findViewById(R.id.btnSendChat).setOnClickListener(x -> {
            String msg = et.getText().toString().trim();
            if (msg.isEmpty()) return;
            Map<String, Object> m = new HashMap<>();
            m.put("senderId", myId);
            m.put("senderName", myName);
            m.put("senderPhoto", getMyPhotoFromAllPlaces());
            m.put("senderCountry", myCountry);
            m.put("senderLevel", myLevel);
            m.put("msg", msg);
            m.put("time", System.currentTimeMillis());
            activeRoomRef.child("chats").push().setValue(m);
            d.dismiss();
        });
        d.show();
    }

    private void listenChatRealtime() {
        activeRoomRef.child("chats").limitToLast(50).addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot snap, String p) {
                if (snap == null || !snap.exists()) return;
                String name = snap.child("senderName").getValue(String.class);
                String photo = snap.child("senderPhoto").getValue(String.class);
                String country = snap.child("senderCountry").getValue(String.class);
                String level = snap.child("senderLevel").getValue(String.class);
                String msg = snap.child("msg").getValue(String.class);
                if (name == null) name = "User";
                if (msg == null) msg = "";
                if (country == null) country = "";
                if (level == null) level = "0";
                addChatToScreen(name, photo, country, level, msg);
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {
                if (chatListContainer != null) chatListContainer.removeAllViews();
            }
            @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addChatToScreen(String name, String photo, String country, String level, String msg) {
        if (chatListContainer == null) return;
        View v = LayoutInflater.from(this).inflate(R.layout.item_chat, chatListContainer, false);
        ImageView iv = v.findViewById(R.id.imgChatPhoto);
        TextView tvH = v.findViewById(R.id.tvChatHeader);
        TextView tvM = v.findViewById(R.id.tvChatMsg);
        loadPhotoInto(iv, photo);
        tvH.setText(name + "  Lv." + level + "  " + getFlag(country));
        tvM.setText(msg);
        chatListContainer.addView(v);
    }

    private void openOnlineList() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_online_list, null);
        d.setContentView(v);
        RecyclerView rv = v.findViewById(R.id.rvOnlineList);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Map<String, String>> list = new ArrayList<>();
        RecyclerView.Adapter adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup p, int t) {
                View item = LayoutInflater.from(p.getContext()).inflate(R.layout.item_user, p, false);
                return new RecyclerView.ViewHolder(item) {};
            }
            @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                Map<String, String> u = list.get(pos);
                ImageView iv = h.itemView.findViewById(R.id.imgChatPhoto);
                TextView tvH = h.itemView.findViewById(R.id.userName);
                TextView tvM = h.itemView.findViewById(R.id.tvChatMsg);
                loadPhotoInto(iv, u.get("photo"));
                tvH.setText(u.get("name") + "  Lv." + u.get("level") + "  " + getFlag(u.get("country")));
                tvM.setText("ID: " + u.get("id"));
            }
            @Override public int getItemCount() { return list.size(); }
        };
        rv.setAdapter(adapter);
        activeRoomRef.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                list.clear();
                for (DataSnapshot c : snap.getChildren()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("name", c.child("name").getValue(String.class) == null ? "User" : c.child("name").getValue(String.class));
                    String ph = c.child("photo").getValue(String.class);
                    if (ph == null || ph.isEmpty()) ph = c.child("photoUrl").getValue(String.class);
                    m.put("photo", ph == null ? "" : ph);
                    m.put("country", c.child("country").getValue(String.class) == null ? "" : c.child("country").getValue(String.class));
                    Object lvlObj = c.child("level").getValue();
                    m.put("level", lvlObj == null ? "0" : String.valueOf(lvlObj));
                    m.put("id", c.getKey());
                    list.add(m);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        v.findViewById(R.id.btnCloseOnline).setOnClickListener(x -> d.dismiss());
        d.show();
    }

    private void showPowerMenu() {
        String[] options = {"Minimize", "Exit Room"};
        new AlertDialog.Builder(this).setTitle("Room Options").setItems(options, (dialog, which) -> {
            if (which == 0) finish(); else finishAffinity();
        }).setNegativeButton("Cancel", null).show();
    }

    private void checkMyRole() {
        amIOwner = myId.equals(roomOwnerId);
        activeRoomRef.child("admins").child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { amIAdmin = snap.exists() && Boolean.TRUE.equals(snap.getValue(Boolean.class)); }
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
                    if (roomHeaderProfileImg != null) loadPhotoInto(roomHeaderProfileImg, rPhoto);
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
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_tools, null);
        dialog.setContentView(view);
        view.findViewById(R.id.toolRoomSet).setOnClickListener(v -> { dialog.dismiss(); openRoomSetDialog(); });
        view.findViewById(R.id.toolRoomEdit).setOnClickListener(v -> { dialog.dismiss(); openRoomSetDialog(); });
        view.findViewById(R.id.toolClearChat).setOnClickListener(v -> { dialog.dismiss(); clearChat(); });
        view.findViewById(R.id.toolLock).setOnClickListener(v -> Toast.makeText(this, "Lock on - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolClearCharm).setOnClickListener(v -> Toast.makeText(this, "Clear charm - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolChangeMic).setOnClickListener(v -> Toast.makeText(this, "Change mic - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolMicTheme).setOnClickListener(v -> Toast.makeText(this, "Mic Theme - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolDress).setOnClickListener(v -> Toast.makeText(this, "Dress - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolLuckySound).setOnClickListener(v -> Toast.makeText(this, "Lucky Sound - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolVoiceUser).setOnClickListener(v -> Toast.makeText(this, "Voice User - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolKickRecord).setOnClickListener(v -> Toast.makeText(this, "Kick out record - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolYoutube).setOnClickListener(v -> Toast.makeText(this, "YouTube Co-watch - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolLuckyFloat).setOnClickListener(v -> Toast.makeText(this, "Lucky float - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolRoomPK).setOnClickListener(v -> Toast.makeText(this, "Room PK - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolBroadcast).setOnClickListener(v -> Toast.makeText(this, "Broadcast - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnToolsClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void clearChat() {
        if (chatListContainer != null) chatListContainer.removeAllViews();
        activeRoomRef.child("chats").removeValue();
        Toast.makeText(this, "Chat Clean হয়ে গেছে", Toast.LENGTH_SHORT).show();
    }

    private void openRoomSetDialog() {
        if (!amIOwner && !amIAdmin) { Toast.makeText(this, "Only Room Owner / Admin can change", Toast.LENGTH_SHORT).show(); return; }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_set, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialogRoomPhoto = view.findViewById(R.id.imgRoomPhoto);
        EditText etName = view.findViewById(R.id.etRoomName);
        EditText etAnnounce = view.findViewById(R.id.etRoomAnnouncement);
        TextView tvTheme = view.findViewById(R.id.tvSelectedTheme);
        if (tvRoomName != null) etName.setText(tvRoomName.getText().toString());
        if (currentRoomPhotoUrl != null && !currentRoomPhotoUrl.isEmpty()) loadPhotoInto(dialogRoomPhoto, currentRoomPhotoUrl);
        else if (myPhoto != null && !myPhoto.isEmpty()) loadPhotoInto(dialogRoomPhoto, myPhoto);
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
            if (roomHeaderProfileImg != null && finalPhoto != null && !finalPhoto.isEmpty()) loadPhotoInto(roomHeaderProfileImg, finalPhoto);
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

    @Override protected void onDestroy() {
        super.onDestroy();
        try { activeRoomRef.child("online_users").child(myId).removeValue(); } catch (Exception e) {}
    }
}
