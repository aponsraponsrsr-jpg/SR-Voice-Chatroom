package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {
    private String roomId, roomOwnerId, myId, myName, myGender, myCountry, myPhoto, myLevel = "0";
    private boolean amIOwner = false, amIAdmin = false;
    private TextView tvRoomId, tvRoomName, tvMyIdInRoom;
    private TextView roomTitleText, roomInfoText;
    private ImageView roomHeaderProfileImg, roomHeaderHeadwearImg;
    private Button btnRoomSet, btnRoomEdit;
    private View layoutOwnerTools;
    private RecyclerView rvOnline;
    private android.widget.LinearLayout chatListContainer;
    private DatabaseReference activeRoomRef, roomMetaRef;
    private SharedPreferences prefs;
    private ImageView dialogRoomPhoto;
    private Uri pickedRoomPhotoUri = null;
    private String pickedRoomPhotoBase64 = null;
    private String selectedTheme = "Default";
    private String currentRoomPhotoUrl = "";
    private String rankingTab = "daily";
    private String selectedGiftName = "Rose";
    private int selectedGiftPrice = 10;
    private View[] seatViews = new View[10];
    private ImageView[] seatImgViews = new ImageView[10];
    private TextView[] seatLetterViews = new TextView[10];
    private TextView[] seatNameViews = new TextView[10];
    private Map<String, String> seatUserId = new HashMap<>();
    private Map<String, String> seatUserName = new HashMap<>();
    private Map<String, String> seatUserPhoto = new HashMap<>();
    private Map<String, Boolean> seatMuted = new HashMap<>();
    private Map<String, Boolean> seatLocked = new HashMap<>();
    private long joinTime = 0;
    private ChildEventListener chatListener = null;
    private Query chatQuery = null;
    private boolean isExiting = false;

    private ActivityResultLauncher<String> pickRoomPhoto = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                pickedRoomPhotoUri = uri;
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();
                    if (bmp != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Bitmap scaled = Bitmap.createScaledBitmap(bmp, 400, 400, true);
                        scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        pickedRoomPhotoBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    }
                } catch (Exception e) { pickedRoomPhotoBase64 = null; }
                if (dialogRoomPhoto != null) Glide.with(this).load(uri).circleCrop().into(dialogRoomPhoto);
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
    private String getMyHeadwearFromAllPlaces() {
        if (prefs == null) return "";
        String[] keys = {"HEADWEAR_IMAGE","headwearImage","HEADWEAR_URL","headwearUrl","HEADWEAR","headwear","HEADWEAR_PHOTO"};
        for (String k : keys) {
            String v = prefs.getString(k, "");
            if (v != null && !v.trim().isEmpty() && !v.equals("null")) return v.trim();
        }
        return "";
    }
    private void loadPhotoInto(ImageView iv, String url) {
        if (iv == null) return;
        if (url == null || url.trim().isEmpty() || url.equals("null")) { iv.setImageDrawable(null); return; }
        try {
            if (url.trim().startsWith("http")) Glide.with(this).load(url).circleCrop().into(iv);
            else if (url.trim().startsWith("content://") || url.trim().startsWith("file://")) Glide.with(this).load(Uri.parse(url.trim())).circleCrop().into(iv);
            else { byte[] bytes = Base64.decode(url.trim(), Base64.DEFAULT); Glide.with(this).load(bytes).circleCrop().into(iv); }
        } catch (Exception e) {}
    }
    private void loadSeatPhoto(ImageView iv, TextView letterTv, String photoUrl, String name) {
        if (iv == null || letterTv == null) return;
        if (photoUrl != null && !photoUrl.trim().isEmpty() && !photoUrl.equals("null")) {
            letterTv.setVisibility(View.GONE); iv.setVisibility(View.VISIBLE);
            try {
                if (photoUrl.trim().startsWith("http")) Glide.with(this).load(photoUrl.trim()).circleCrop().into(iv);
                else if (photoUrl.trim().startsWith("content://") || photoUrl.trim().startsWith("file://")) Glide.with(this).load(Uri.parse(photoUrl.trim())).circleCrop().into(iv);
                else { byte[] b = Base64.decode(photoUrl.trim(), Base64.DEFAULT); Glide.with(this).load(b).circleCrop().into(iv); }
            } catch (Exception e) { iv.setVisibility(View.GONE); letterTv.setVisibility(View.VISIBLE); letterTv.setText(name.isEmpty() ? "U" : name.substring(0,1).toUpperCase()); }
        } else { iv.setVisibility(View.GONE); iv.setImageDrawable(null); letterTv.setVisibility(View.VISIBLE); letterTv.setText(name.isEmpty() ? "U" : name.substring(0,1).toUpperCase()); }
    }
    private void loadRoomPhotoInto(ImageView iv) {
        if (iv == null) return;
        String url = currentRoomPhotoUrl;
        if (url == null || url.trim().isEmpty() || url.equals("null") || url.trim().startsWith("content://") || url.trim().startsWith("file://")) {
            try { iv.setImageResource(R.mipmap.ic_launcher); } catch (Exception e) { iv.setImageDrawable(null); }
            return;
        }
        loadPhotoInto(iv, url);
    }
    private void loadHeadwearInto(ImageView iv, String url) {
        if (iv == null) return;
        if (url == null || url.trim().isEmpty() || url.equals("null")) { iv.setVisibility(View.GONE); return; }
        iv.setVisibility(View.VISIBLE);
        try {
            if (url.trim().startsWith("http") || url.trim().startsWith("content://") || url.trim().startsWith("file://")) Glide.with(this).load(url.trim()).into(iv);
            else { byte[] bytes = Base64.decode(url.trim(), Base64.DEFAULT); Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); if (bmp != null) iv.setImageBitmap(bmp); else iv.setVisibility(View.GONE); }
        } catch (Exception e) { iv.setVisibility(View.GONE); }
    }

    private void setupSeats() {
        for (int i = 0; i < 10; i++) {
            int seatId = getResources().getIdentifier("seat_" + i, "id", getPackageName());
            int imgId = getResources().getIdentifier("seatImg_" + i, "id", getPackageName());
            int letterId = getResources().getIdentifier("seatLetter_" + i, "id", getPackageName());
            View seat = findViewById(seatId); ImageView img = findViewById(imgId); TextView letter = findViewById(letterId);
            seatViews[i] = seat; seatImgViews[i] = img; seatLetterViews[i] = letter;
            if (seat instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout ll = (android.widget.LinearLayout) seat;
                if (ll.getChildCount() >= 2 && ll.getChildAt(1) instanceof TextView) seatNameViews[i] = (TextView) ll.getChildAt(1);
            }
            final int seatIndex = i;
            if (seat != null) seat.setOnClickListener(v -> onSeatClicked(seatIndex));
            if (img != null) img.setOnClickListener(v -> onSeatClicked(seatIndex));
            if (letter != null) letter.setOnClickListener(v -> onSeatClicked(seatIndex));
        }
        listenSeats();
    }
    private void listenSeats() {
        activeRoomRef.child("seats").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                seatUserId.clear(); seatUserName.clear(); seatUserPhoto.clear(); seatMuted.clear(); seatLocked.clear();
                for (DataSnapshot c : snap.getChildren()) {
                    String key = c.getKey(); if (key == null) continue;
                    String uid = c.child("userId").getValue(String.class);
                    if (uid == null || uid.isEmpty()) { Boolean l2 = c.child("locked").getValue(Boolean.class); if (l2 != null && l2) seatLocked.put(key, true); continue; }
                    seatUserId.put(key, uid);
                    String n = c.child("name").getValue(String.class); seatUserName.put(key, n == null ? "User" : n);
                    String ph = c.child("photo").getValue(String.class); seatUserPhoto.put(key, ph == null ? "" : ph);
                    Boolean m = c.child("muted").getValue(Boolean.class); seatMuted.put(key, m != null && m);
                    Boolean l = c.child("locked").getValue(Boolean.class); seatLocked.put(key, l != null && l);
                }
                updateAllSeatsUI();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void updateAllSeatsUI() {
        for (int i = 0; i < 10; i++) {
            String key = "seat_" + i; ImageView img = seatImgViews[i]; TextView letter = seatLetterViews[i]; TextView nameTv = seatNameViews[i];
            if (letter == null) continue;
            String uid = seatUserId.get(key);
            if (uid == null || uid.isEmpty()) {
                boolean locked = seatLocked.get(key) != null && seatLocked.get(key);
                if (img != null) { img.setVisibility(View.GONE); img.setImageDrawable(null); }
                letter.setVisibility(View.VISIBLE); letter.setText(locked ? "🔒" : "🪑");
                if (nameTv != null) nameTv.setText(i == 0 ? "Host" : (locked ? "Locked" : ""));
            } else {
                String name = seatUserName.get(key); if (name == null) name = "User";
                boolean muted = seatMuted.get(key) != null && seatMuted.get(key);
                String photo = seatUserPhoto.get(key); if (photo == null) photo = "";
                loadSeatPhoto(img, letter, photo, name);
                if (nameTv != null) nameTv.setText(name + (muted ? " 🔇" : ""));
            }
        }
    }
    private int mySeatIndex() { for (int i = 0; i < 10; i++) { String uid = seatUserId.get("seat_" + i); if (myId.equals(uid)) return i; } return -1; }

    private void onSeatClicked(int seatIndex) {
        String key = "seat_" + seatIndex; String occupantId = seatUserId.get(key);
        boolean isEmpty = occupantId == null || occupantId.isEmpty();
        boolean locked = seatLocked.get(key) != null && seatLocked.get(key);
        boolean isMine = myId.equals(occupantId); boolean iAmOwnerOrAdmin = amIOwner || amIAdmin;
        List<String> options = new ArrayList<>(); List<Runnable> actions = new ArrayList<>();
        if (isEmpty) {
            if (locked && !iAmOwnerOrAdmin) { Toast.makeText(this, "This seat is Locked", Toast.LENGTH_SHORT).show(); return; }
            if (mySeatIndex() == -1) { options.add("On Mic"); final int f1 = seatIndex; actions.add(() -> takeSeat(f1)); }
            else { options.add("Change Seat"); final int f2 = seatIndex; final int fMy = mySeatIndex(); actions.add(() -> changeSeat(fMy, f2)); }
            if (iAmOwnerOrAdmin) { options.add("Invite"); final int f3 = seatIndex; actions.add(() -> inviteToSeat(f3)); final boolean fL = locked; final int f4 = seatIndex; options.add(fL ? "Unlock Seat" : "Lock Seat"); actions.add(() -> toggleLockSeat(f4)); }
        } else if (isMine) {
            boolean muted = seatMuted.get(key) != null && seatMuted.get(key);
            final int f5 = seatIndex; options.add("Leave the Mic"); actions.add(() -> leaveSeat(f5));
            final int f6 = seatIndex; options.add(muted ? "Unmute" : "Mute"); actions.add(() -> toggleMuteSeat(f6));
            final String fName = seatUserName.get(key) == null ? "User" : seatUserName.get(key);
            final String fPhoto = seatUserPhoto.get(key) == null ? "" : seatUserPhoto.get(key);
            options.add("Profile"); actions.add(() -> openUserProfile(myId, fName, fPhoto));
        } else {
            final String finalOccupantId = occupantId;
            final String finalOccName = seatUserName.get(key) == null ? "User" : seatUserName.get(key);
            final String finalOccPhoto = seatUserPhoto.get(key) == null ? "" : seatUserPhoto.get(key);
            boolean muted = seatMuted.get(key) != null && seatMuted.get(key); final boolean fMuted = muted;
            if (iAmOwnerOrAdmin) {
                options.add("Profile"); actions.add(() -> openUserProfile(finalOccupantId, finalOccName, finalOccPhoto));
                final int f7 = seatIndex; options.add(fMuted ? "Unmute" : "Mute"); actions.add(() -> toggleMuteSeat(f7));
                final int f8 = seatIndex; options.add("Kick"); actions.add(() -> kickSeat(f8));
                final boolean fL2 = locked; final int f9 = seatIndex; options.add(fL2 ? "Unlock Seat" : "Lock Seat"); actions.add(() -> toggleLockSeat(f9));
            } else { options.add("Profile"); actions.add(() -> openUserProfile(finalOccupantId, finalOccName, finalOccPhoto)); }
        }
        if (options.isEmpty()) return;
        String title = "Seat " + (seatIndex + 1) + (isEmpty ? " - Empty" : " - " + seatUserName.get(key));
        new AlertDialog.Builder(this).setTitle(title).setItems(options.toArray(new String[0]), (d, w) -> actions.get(w).run()).setNegativeButton("Cancel", null).show();
    }
    private void takeSeat(int seatIndex) {
        String key = "seat_" + seatIndex;
        if (seatLocked.get(key) != null && seatLocked.get(key) && !amIOwner && !amIAdmin) { Toast.makeText(this, "This seat is Locked", Toast.LENGTH_SHORT).show(); return; }
        if (mySeatIndex() != -1 && mySeatIndex() != seatIndex) { changeSeat(mySeatIndex(), seatIndex); return; }
        Map<String, Object> m = new HashMap<>(); m.put("userId", myId); m.put("name", myName); m.put("photo", getMyPhotoFromAllPlaces()); m.put("muted", false); m.put("locked", seatLocked.get(key) != null && seatLocked.get(key));
        activeRoomRef.child("seats").child(key).setValue(m); Toast.makeText(this, "You are on Seat " + (seatIndex + 1), Toast.LENGTH_SHORT).show();
    }
    private void changeSeat(int oldSeatIndex, int newSeatIndex) {
        String oldKey = "seat_" + oldSeatIndex; String newKey = "seat_" + newSeatIndex;
        if (seatLocked.get(newKey) != null && seatLocked.get(newKey) && !amIOwner && !amIAdmin) { Toast.makeText(this, "This seat is Locked", Toast.LENGTH_SHORT).show(); return; }
        Map<String, Object> updates = new HashMap<>(); updates.put("seats/" + oldKey, null);
        Map<String, Object> m = new HashMap<>(); m.put("userId", myId); m.put("name", myName); m.put("photo", getMyPhotoFromAllPlaces()); m.put("muted", false); m.put("locked", seatLocked.get(newKey) != null && seatLocked.get(newKey));
        updates.put("seats/" + newKey, m); activeRoomRef.updateChildren(updates); Toast.makeText(this, "Moved to Seat " + (newSeatIndex + 1), Toast.LENGTH_SHORT).show();
    }
    private void leaveSeat(int seatIndex) { activeRoomRef.child("seats").child("seat_" + seatIndex).removeValue(); Toast.makeText(this, "Left the Mic", Toast.LENGTH_SHORT).show(); }
    private void kickSeat(int seatIndex) { activeRoomRef.child("seats").child("seat_" + seatIndex).removeValue(); Toast.makeText(this, "Kicked from Seat", Toast.LENGTH_SHORT).show(); }
    private void toggleMuteSeat(int seatIndex) { String key = "seat_" + seatIndex; boolean cur = seatMuted.get(key) != null && seatMuted.get(key); activeRoomRef.child("seats").child(key).child("muted").setValue(!cur); Toast.makeText(this, !cur ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show(); }
    private void toggleLockSeat(int seatIndex) {
        String key = "seat_" + seatIndex; boolean cur = seatLocked.get(key) != null && seatLocked.get(key);
        if (seatUserId.get(key) == null) {
            if (!cur) { Map<String, Object> m = new HashMap<>(); m.put("userId", ""); m.put("name", ""); m.put("photo", ""); m.put("muted", false); m.put("locked", true); activeRoomRef.child("seats").child(key).setValue(m); }
            else activeRoomRef.child("seats").child(key).removeValue();
        } else activeRoomRef.child("seats").child(key).child("locked").setValue(!cur);
        Toast.makeText(this, !cur ? "Seat Locked" : "Seat Unlocked", Toast.LENGTH_SHORT).show();
    }
    private void inviteToSeat(final int seatIndex) {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_online_list, null); d.setContentView(v);
        RecyclerView rv = v.findViewById(R.id.rvOnlineList); if (rv != null) rv.setLayoutManager(new LinearLayoutManager(this));
        List<Map<String, String>> list = new ArrayList<>();
        RecyclerView.Adapter adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup p, int t) { View item = LayoutInflater.from(p.getContext()).inflate(R.layout.item_user, p, false); return new RecyclerView.ViewHolder(item) {}; }
            @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                Map<String, String> u = list.get(pos); ImageView iv = h.itemView.findViewById(R.id.imgChatPhoto); TextView tvH = h.itemView.findViewById(R.id.userName); TextView tvM = h.itemView.findViewById(R.id.tvChatMsg);
                loadPhotoInto(iv, u.get("photo")); if (tvH != null) tvH.setText(u.get("name") + "  Lv." + u.get("level")); if (tvM != null) tvM.setText("ID: " + u.get("id") + " - Tap to Invite");
                h.itemView.setOnClickListener(x -> { Map<String, Object> m = new HashMap<>(); m.put("userId", u.get("id")); m.put("name", u.get("name")); m.put("photo", u.get("photo")); m.put("muted", false); m.put("locked", false); activeRoomRef.child("seats").child("seat_" + seatIndex).setValue(m); Toast.makeText(VoiceRoomActivity.this, u.get("name") + " invited to Seat", Toast.LENGTH_SHORT).show(); d.dismiss(); });
            }
            @Override public int getItemCount() { return list.size(); }
        };
        if (rv != null) rv.setAdapter(adapter);
        activeRoomRef.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { list.clear(); for (DataSnapshot c : snap.getChildren()) { Map<String, String> m = new HashMap<>(); m.put("name", c.child("name").getValue(String.class) == null ? "User" : c.child("name").getValue(String.class)); String ph = c.child("photo").getValue(String.class); if (ph == null || ph.isEmpty()) ph = c.child("photoUrl").getValue(String.class); m.put("photo", ph == null ? "" : ph); Object lvl = c.child("level").getValue(); m.put("level", lvl == null ? "0" : String.valueOf(lvl)); m.put("id", c.getKey()); if (!c.getKey().equals(myId)) list.add(m); } adapter.notifyDataSetChanged(); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        View btnClose = v.findViewById(R.id.btnCloseOnline); if (btnClose != null) btnClose.setOnClickListener(x -> d.dismiss()); d.show();
    }

    private void openUserProfile(String targetId, String targetName, String targetPhoto) { openMiniProfile(targetId, targetName, targetPhoto); }

    private void openMiniProfile(String targetId, String targetName, String targetPhoto) {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_mini_profile, null);
        d.setContentView(v);
        ImageView img = v.findViewById(R.id.imgMiniPhoto);
        TextView tvName = v.findViewById(R.id.txtMiniName);
        TextView tvGenderIcon = v.findViewById(R.id.txtMiniGenderIcon);
        TextView tvFlag = v.findViewById(R.id.txtMiniFlag);
        TextView tvGenderCountry = v.findViewById(R.id.txtMiniGenderCountry);
        TextView tvLevel = v.findViewById(R.id.txtMiniLevel);
        TextView tvSending = v.findViewById(R.id.txtMiniSending);
        TextView tvReceiving = v.findViewById(R.id.txtMiniReceiving);
        TextView tvId = v.findViewById(R.id.txtMiniId);
        View layoutAgency = v.findViewById(R.id.layoutMiniAgency);
        TextView tvAgency = v.findViewById(R.id.txtMiniAgency);
        View layoutHost = v.findViewById(R.id.layoutMiniHost);
        TextView tvHost = v.findViewById(R.id.txtMiniHost);
        View btnAdd = v.findViewById(R.id.btnMiniAddLeft);
        View btnGift = v.findViewById(R.id.btnMiniGift);
        View btnFollow = v.findViewById(R.id.btnMiniFollowRight);
        View root = v.findViewById(R.id.layoutMiniRoot);
        final String fName = targetName == null ? "User" : targetName;
        if (tvName != null) tvName.setText(fName);
        if (tvId != null) tvId.setText("ID: " + targetId + "  📋");
        if (img != null && targetPhoto != null && !targetPhoto.isEmpty()) loadPhotoInto(img, targetPhoto);
        if (tvId != null) tvId.setOnClickListener(x -> { ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("ID", targetId)); Toast.makeText(this, "ID Copied", Toast.LENGTH_SHORT).show(); });
        if (root != null) root.setOnClickListener(x -> { d.dismiss(); try { android.content.Intent i = new android.content.Intent(this, PublicProfileActivity.class); i.putExtra("TARGET_9DIGIT_ID", targetId); i.putExtra("TARGET_NAME", fName); startActivity(i); } catch (Exception e) { Toast.makeText(this, "Profile cannot be opened", Toast.LENGTH_SHORT).show(); } });
        if (btnAdd != null) btnAdd.setOnClickListener(x -> Toast.makeText(this, "Friend Request Sent to " + fName, Toast.LENGTH_SHORT).show());
        if (btnFollow != null) btnFollow.setOnClickListener(x -> Toast.makeText(this, "Followed! ❤️", Toast.LENGTH_SHORT).show());
        if (btnGift != null) btnGift.setOnClickListener(x -> { d.dismiss(); openGiftDialog(targetId, fName); });
        FirebaseDatabase.getInstance().getReference("users").orderByChild("my9DigitId").equalTo(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                for (DataSnapshot ds : snap.getChildren()) {
                    String name = ds.child("name").getValue() == null ? fName : String.valueOf(ds.child("name").getValue());
                    String gender = ds.child("gender").getValue() == null ? "" : String.valueOf(ds.child("gender").getValue());
                    String country = ds.child("country").getValue() == null ? "" : String.valueOf(ds.child("country").getValue());
                    String level = ds.child("level").getValue() == null ? "0" : String.valueOf(ds.child("level").getValue());
                    String photo = "";
                    if (ds.child("photoUrl").getValue() != null) photo = String.valueOf(ds.child("photoUrl").getValue());
                    if (photo.isEmpty() && ds.child("photo").getValue() != null) photo = String.valueOf(ds.child("photo").getValue());
                    if (photo.isEmpty() && ds.child("photoBase64").getValue() != null) photo = String.valueOf(ds.child("photoBase64").getValue());
                    String sending = ds.child("sending").getValue() == null ? (ds.child("totalSending").getValue() == null ? "0" : String.valueOf(ds.child("totalSending").getValue())) : String.valueOf(ds.child("sending").getValue());
                    String receiving = ds.child("receiving").getValue() == null ? (ds.child("totalReceiving").getValue() == null ? "0" : String.valueOf(ds.child("totalReceiving").getValue())) : String.valueOf(ds.child("receiving").getValue());
                    if (tvName != null) tvName.setText(name);
                    String gLow = gender.toLowerCase();
                    boolean isFemale = gLow.contains("female");
                    boolean isMale = !isFemale && gLow.contains("male");
                    String gIcon = isFemale ? "♀" : (isMale ? "♂" : "⚧");
                    if (tvGenderIcon != null) {
                        tvGenderIcon.setText(gIcon);
                        if (isFemale) tvGenderIcon.setTextColor(0xFFFF69B4);
                        else if (isMale) tvGenderIcon.setTextColor(0xFF00B0FF);
                        else tvGenderIcon.setTextColor(0xFFFFFFFF);
                    }
                    if (tvFlag != null) tvFlag.setText(getFlag(country));
                    if (tvGenderCountry != null) tvGenderCountry.setText(gIcon + "  " + getFlag(country));
                    if (tvLevel != null) tvLevel.setText("⭐ Lv." + level);
                    if (tvSending != null) tvSending.setText("⬆️ Sending: " + sending);
                    if (tvReceiving != null) tvReceiving.setText("⬇️ Receiving: " + receiving);
                    if (!photo.isEmpty() && img != null) loadPhotoInto(img, photo);
                    String agency = "";
                    if (ds.child("agencyName").getValue() != null) agency = String.valueOf(ds.child("agencyName").getValue());
                    if (agency.isEmpty() && ds.child("agency").getValue() != null) agency = String.valueOf(ds.child("agency").getValue());
                    if (agency.isEmpty() && ds.child("familyName").getValue() != null) agency = String.valueOf(ds.child("familyName").getValue());
                    if (!agency.isEmpty() && layoutAgency != null && tvAgency != null) { layoutAgency.setVisibility(View.VISIBLE); tvAgency.setText(agency); }
                    String host = "";
                    if (ds.child("hostName").getValue() != null) host = String.valueOf(ds.child("hostName").getValue());
                    if (host.isEmpty() && ds.child("isHost").getValue() != null && Boolean.TRUE.equals(ds.child("isHost").getValue(Boolean.class))) host = "Official Host";
                    if (host.isEmpty() && ds.child("host").getValue() != null) host = String.valueOf(ds.child("host").getValue());
                    if (!host.isEmpty() && layoutHost != null && tvHost != null) { layoutHost.setVisibility(View.VISIBLE); tvHost.setText(host); }
                    break;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        FirebaseDatabase.getInstance().getReference("agencies").child(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { if (s.exists() && s.child("name").getValue() != null && layoutAgency != null && tvAgency != null) { layoutAgency.setVisibility(View.VISIBLE); tvAgency.setText(String.valueOf(s.child("name").getValue())); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        FirebaseDatabase.getInstance().getReference("hosts").child(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { if (s.exists() && layoutHost != null && tvHost != null) { layoutHost.setVisibility(View.VISIBLE); String hn = s.child("name").getValue() == null ? "Official Host" : String.valueOf(s.child("name").getValue()); tvHost.setText(hn); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        d.show();
    }

    private void openGiftDialog(String targetId, String targetName) {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v;
        try { v = LayoutInflater.from(this).inflate(R.layout.dialog_gift_panel, null); } catch (Exception e) { Toast.makeText(this, "Gift - Coming Soon", Toast.LENGTH_SHORT).show(); return; }
        d.setContentView(v);
        EditText etTarget = v.findViewById(R.id.editTargetSeat);
        if (etTarget != null) etTarget.setText(targetName + " (ID: " + targetId + ")");
        TextView tvBal = v.findViewById(R.id.txtUserCoinBalance);
        if (tvBal != null) tvBal.setText("💰 " + prefs.getString("USER_COINS", "0") + " Coins");
        View btnSend = v.findViewById(R.id.btnSendGift);
        if (btnSend != null) btnSend.setOnClickListener(x -> { Toast.makeText(this, "Gift " + selectedGiftName + " sent to " + targetName + " 🎁", Toast.LENGTH_SHORT).show(); d.dismiss(); });
        RecyclerView rv = v.findViewById(R.id.rvGifts);
        if (rv != null) {
            rv.setLayoutManager(new GridLayoutManager(this, 4));
            String[] giftNames = {"Rose", "Heart", "Cake", "Diamond", "Car", "Crown", "Plane", "Castle"};
            String[] giftEmojis = {"🌹", "❤️", "🎂", "💎", "🚗", "👑", "✈️", "🏰"};
            int[] giftPrices = {10, 50, 200, 100, 500, 1000, 2000, 5000};
            selectedGiftName = giftNames[0];
            selectedGiftPrice = giftPrices[0];
            RecyclerView.Adapter a = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup p, int t) { TextView tv = new TextView(p.getContext()); tv.setLayoutParams(new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 160)); tv.setGravity(android.view.Gravity.CENTER); tv.setTextColor(0xFFFFFFFF); tv.setTextSize(13); tv.setPadding(8, 8, 8, 8); tv.setBackgroundColor(0xFF2A1B4E); return new RecyclerView.ViewHolder(tv) {}; }
                @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                    TextView tv = (TextView) h.itemView;
                    tv.setText(giftEmojis[pos] + "\n" + giftNames[pos] + "\n" + giftPrices[pos] + " Coins");
                    tv.setBackgroundColor(giftNames[pos].equals(selectedGiftName) ? 0xFFFF4081 : 0xFF2A1B4E);
                    tv.setOnClickListener(x -> { selectedGiftName = giftNames[pos]; selectedGiftPrice = giftPrices[pos]; notifyDataSetChanged(); Toast.makeText(VoiceRoomActivity.this, "Selected: " + selectedGiftName, Toast.LENGTH_SHORT).show(); });
                }
                @Override public int getItemCount() { return giftNames.length; }
            };
            rv.setAdapter(a);
        }
        d.show();
    }

    private void openRoomHeaderPopup() {
        BottomSheetDialog d = new BottomSheetDialog(this);
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_user_profile, null);
        d.setContentView(v);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ImageView ivProfile = v.findViewById(R.id.imgUserProfile);
        TextView tvName = v.findViewById(R.id.txtUserName);
        TextView tvGroup = v.findViewById(R.id.txtGroupName);
        TextView tvId = v.findViewById(R.id.txtUserId);
        TextView tvLevel = v.findViewById(R.id.txtUserLevel);
        TextView tvOwner = v.findViewById(R.id.txtRoomOwner);
        TextView tvLang = v.findViewById(R.id.txtHeaderLanguage);
        TextView tvCountry = v.findViewById(R.id.txtHeaderCountry);
        TextView tvTag = v.findViewById(R.id.txtHeaderTag);
        View rewardBanner = v.findViewById(R.id.layoutRewardBanner);
        RecyclerView rvMembers = v.findViewById(R.id.rvHeaderMembersList);
        TextView tvMembersCount = v.findViewById(R.id.txtMembersCount);
        View btnFollow = v.findViewById(R.id.btnFollow);
        if (btnFollow != null) btnFollow.setVisibility(View.GONE);
        TextView txtProfileTab = v.findViewById(R.id.txtProfileTab);
        TextView txtMembersTopTab = v.findViewById(R.id.txtMembersTopTab);
        View layoutHeaderInfo = v.findViewById(R.id.layoutHeaderInfo);
        View layoutMembersSection = v.findViewById(R.id.layoutMembersSection);
        TextView txtMembersTab = v.findViewById(R.id.txtMembersTab);
        TextView txtAdminTab = v.findViewById(R.id.txtAdminTab);
        if (layoutHeaderInfo != null) layoutHeaderInfo.setVisibility(View.VISIBLE);
        if (rewardBanner != null) rewardBanner.setVisibility(View.VISIBLE);
        if (layoutMembersSection != null) layoutMembersSection.setVisibility(View.VISIBLE);
        if (txtProfileTab != null && txtMembersTopTab != null) {
            txtProfileTab.setOnClickListener(x -> {
                txtProfileTab.setTextColor(0xFF000000);
                txtProfileTab.setTypeface(null, android.graphics.Typeface.BOLD);
                txtMembersTopTab.setTextColor(0xFF999999);
                txtMembersTopTab.setTypeface(null, android.graphics.Typeface.NORMAL);
                if (layoutHeaderInfo != null) layoutHeaderInfo.setVisibility(View.VISIBLE);
                if (rewardBanner != null) rewardBanner.setVisibility(View.VISIBLE);
            });
            txtMembersTopTab.setOnClickListener(x -> {
                txtMembersTopTab.setTextColor(0xFF000000);
                txtMembersTopTab.setTypeface(null, android.graphics.Typeface.BOLD);
                txtProfileTab.setTextColor(0xFF999999);
                txtProfileTab.setTypeface(null, android.graphics.Typeface.NORMAL);
                if (layoutHeaderInfo != null) layoutHeaderInfo.setVisibility(View.GONE);
                if (rewardBanner != null) rewardBanner.setVisibility(View.GONE);
                if (layoutMembersSection != null) layoutMembersSection.setVisibility(View.VISIBLE);
            });
        }
        if (txtMembersTab != null && txtAdminTab != null) {
            txtMembersTab.setOnClickListener(x -> {
                txtMembersTab.setTextColor(0xFF000000);
                txtMembersTab.setTypeface(null, android.graphics.Typeface.BOLD);
                txtAdminTab.setTextColor(0xFF666666);
                txtAdminTab.setTypeface(null, android.graphics.Typeface.NORMAL);
            });
            txtAdminTab.setOnClickListener(x -> {
                txtAdminTab.setTextColor(0xFF000000);
                txtAdminTab.setTypeface(null, android.graphics.Typeface.BOLD);
                txtMembersTab.setTextColor(0xFF666666);
                txtMembersTab.setTypeface(null, android.graphics.Typeface.NORMAL);
            });
        }
        if (tvName != null) tvName.setText(roomTitleText != null ? roomTitleText.getText().toString() : "Room");
        if (tvGroup != null) tvGroup.setText(roomTitleText != null ? roomTitleText.getText().toString() : "");
        if (tvId != null) tvId.setText("ID: " + roomOwnerId + " 📋");
        if (tvLevel != null) tvLevel.setText("⭐ Level " + myLevel);
        if (tvOwner != null) tvOwner.setText("Owner (ID:" + roomOwnerId + ")");
        if (tvLang != null) tvLang.setText(prefs.getString("USER_LANGUAGE", "English"));
        if (tvCountry != null) tvCountry.setText(myCountry);
        if (tvTag != null) tvTag.setText("Friends");
        if (ivProfile != null) loadRoomPhotoInto(ivProfile);
        if (tvId != null) tvId.setOnClickListener(x -> { ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("Room ID", roomOwnerId)); Toast.makeText(this, "ID Copied", Toast.LENGTH_SHORT).show(); });
        if (rewardBanner != null) rewardBanner.setOnClickListener(x -> openRewardEvent());
        if (rvMembers != null) {
            rvMembers.setLayoutManager(new LinearLayoutManager(this));
            List<Map<String, String>> members = new ArrayList<>();
            RecyclerView.Adapter adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup p, int t) { View item = LayoutInflater.from(p.getContext()).inflate(R.layout.item_user, p, false); return new RecyclerView.ViewHolder(item) {}; }
                @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) { Map<String, String> u = members.get(pos); ImageView iv = h.itemView.findViewById(R.id.imgChatPhoto); TextView tvH = h.itemView.findViewById(R.id.userName); TextView tvM = h.itemView.findViewById(R.id.tvChatMsg); loadPhotoInto(iv, u.get("photo")); if (tvH != null) tvH.setText(u.get("name") + "  Lv." + u.get("level")); if (tvM != null) tvM.setText("ID: " + u.get("id") + "  " + u.get("role")); }
                @Override public int getItemCount() { return members.size(); }
            };
            rvMembers.setAdapter(adapter);
            activeRoomRef.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) { members.clear(); List<Map<String, String>> temp = new ArrayList<>(); for (DataSnapshot c : snap.getChildren()) { Map<String, String> m = new HashMap<>(); m.put("name", c.child("name").getValue(String.class) == null ? "User" : c.child("name").getValue(String.class)); String ph = c.child("photo").getValue(String.class); if (ph == null || ph.isEmpty()) ph = c.child("photoUrl").getValue(String.class); m.put("photo", ph == null ? "" : ph); Object lvl = c.child("level").getValue(); m.put("level", lvl == null ? "0" : String.valueOf(lvl)); m.put("id", c.getKey()); m.put("role", "Member"); temp.add(m); } for (Map<String, String> m : temp) if (m.get("id").equals(roomOwnerId)) { m.put("role", "Owner"); members.add(m); } activeRoomRef.child("admins").addListenerForSingleValueEvent(new ValueEventListener() { @Override public void onDataChange(@NonNull DataSnapshot aSnap) { for (Map<String, String> m : temp) { if (!m.get("id").equals(roomOwnerId) && aSnap.hasChild(m.get("id"))) { m.put("role", "Admin"); members.add(m); } } for (Map<String, String> m : temp) { if (!m.get("id").equals(roomOwnerId) && !aSnap.hasChild(m.get("id"))) members.add(m); } if (tvMembersCount != null) tvMembersCount.setText("Members: " + members.size() + "/1000    Admin: " + aSnap.getChildrenCount() + "/20"); adapter.notifyDataSetChanged(); } @Override public void onCancelled(@NonNull DatabaseError e) {} }); }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
        d.show();
    }
    private void openRewardEvent() { String msg = "🏆 Room Reward Event\n\nWeekly Contribution\nEvery Monday 00:00 (GMT+3)\nClaim directly to Wallet\n\n3.0M Sending = 150K Reward\n6.0M Sending = 300K Reward\n10.0M Sending = 500K Reward\n14.0M Sending = 700K Reward\n20.0M Sending = 1.0M Reward\n30.0M Sending = 2.0M Reward\n50.0M Sending = 4.0M Reward\n100.0M Sending = 9.0M Reward\n200.0M Sending = 19.0M Reward\n500.0M Sending = 49.0M Reward\n\nRule: 3M - 20M = 5%, After 20M = 10%"; new AlertDialog.Builder(this).setTitle("Reward Event").setMessage(msg).setPositiveButton("OK", null).show(); }
    private void openRanking() {
        BottomSheetDialog d = new BottomSheetDialog(this); View v = LayoutInflater.from(this).inflate(R.layout.dialog_room_ranking, null); d.setContentView(v);
        android.widget.LinearLayout container = v.findViewById(R.id.rankingListContainer); Button bDaily = v.findViewById(R.id.btnTabDaily); Button bWeekly = v.findViewById(R.id.btnTabWeekly); Button bMonthly = v.findViewById(R.id.btnTabMonthly); View btnClose = v.findViewById(R.id.btnCloseRanking); if (btnClose != null) btnClose.setOnClickListener(x -> d.dismiss());
        Runnable load = () -> {
            if (bDaily != null) { bDaily.setBackgroundTintList(android.content.res.ColorStateList.valueOf(rankingTab.equals("daily") ? 0xFFFFD700 : 0xFF2A1B4E)); bDaily.setTextColor(rankingTab.equals("daily") ? 0xFF000000 : 0xFFFFFFFF); }
            if (bWeekly != null) { bWeekly.setBackgroundTintList(android.content.res.ColorStateList.valueOf(rankingTab.equals("weekly") ? 0xFFFFD700 : 0xFF2A1B4E)); bWeekly.setTextColor(rankingTab.equals("weekly") ? 0xFF000000 : 0xFFFFFFFF); }
            if (bMonthly != null) { bMonthly.setBackgroundTintList(android.content.res.ColorStateList.valueOf(rankingTab.equals("monthly") ? 0xFFFFD700 : 0xFF2A1B4E)); bMonthly.setTextColor(rankingTab.equals("monthly") ? 0xFF000000 : 0xFFFFFFFF); }
            activeRoomRef.child("ranking").child(rankingTab).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (container != null) container.removeAllViews(); List<DataSnapshot> list = new ArrayList<>(); for (DataSnapshot c : snap.getChildren()) list.add(c);
                    list.sort((a,b) -> Long.compare(b.child("coins").getValue(Long.class) == null ? 0 : b.child("coins").getValue(Long.class), a.child("coins").getValue(Long.class) == null ? 0 : a.child("coins").getValue(Long.class)));
                    for (int i=0;i<Math.min(3,list.size());i++) { DataSnapshot c = list.get(i); String n = c.child("name").getValue(String.class); Long co = c.child("coins").getValue(Long.class); String name = n == null ? "User" : n; long coins = co == null ? 0 : co; String ph = c.child("photo").getValue(String.class); if (ph == null) ph = ""; if (i==0) { TextView tn = v.findViewById(R.id.txtTop1Name); TextView tc = v.findViewById(R.id.txtTop1Coins); ImageView iv = v.findViewById(R.id.imgTop1Photo); if (tn!=null) tn.setText(name); if (tc!=null) tc.setText(coins+" 🪙"); loadPhotoInto(iv, ph); } if (i==1) { TextView tn = v.findViewById(R.id.txtTop2Name); TextView tc = v.findViewById(R.id.txtTop2Coins); ImageView iv = v.findViewById(R.id.imgTop2Photo); if (tn!=null) tn.setText(name); if (tc!=null) tc.setText(coins+" 🪙"); loadPhotoInto(iv, ph); } if (i==2) { TextView tn = v.findViewById(R.id.txtTop3Name); TextView tc = v.findViewById(R.id.txtTop3Coins); ImageView iv = v.findViewById(R.id.imgTop3Photo); if (tn!=null) tn.setText(name); if (tc!=null) tc.setText(coins+" 🪙"); loadPhotoInto(iv, ph); } }
                    for (int i=3;i<list.size();i++) { DataSnapshot c = list.get(i); View row; try { row = LayoutInflater.from(VoiceRoomActivity.this).inflate(R.layout.item_ranking, container, false); } catch (Exception e) { row = new TextView(VoiceRoomActivity.this); ((TextView)row).setText((i+1)+". "+c.child("name").getValue(String.class)+" - "+(c.child("coins").getValue(Long.class)==null?0:c.child("coins").getValue(Long.class))+" 🪙"); ((TextView)row).setTextColor(0xFFFFFFFF); container.addView(row); continue; } TextView tvRank = row.findViewById(R.id.txtRankNumber); TextView tvName = row.findViewById(R.id.txtRankName); TextView tvCoin = row.findViewById(R.id.txtRankCoins); ImageView iv = row.findViewById(R.id.imgRankPhoto); if (tvRank != null) tvRank.setText(String.valueOf(i+1)); if (tvName != null) tvName.setText(c.child("name").getValue(String.class)); if (tvCoin != null) tvCoin.setText((c.child("coins").getValue(Long.class)==null?0:c.child("coins").getValue(Long.class))+" 🪙"); String ph = c.child("photo").getValue(String.class); loadPhotoInto(iv, ph==null?"":ph); container.addView(row); }
                } @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        };
        if (bDaily != null) bDaily.setOnClickListener(x -> { rankingTab="daily"; load.run(); }); if (bWeekly != null) bWeekly.setOnClickListener(x -> { rankingTab="weekly"; load.run(); }); if (bMonthly != null) bMonthly.setOnClickListener(x -> { rankingTab="monthly"; load.run(); }); load.run(); d.show();
    }
    private void syncMyPhotoFromFirebase() {
        FirebaseDatabase.getInstance().getReference("users").orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { if (!snap.exists()) return; for (DataSnapshot ds : snap.getChildren()) { String p = null; if (ds.child("photoUrl").getValue() != null) p = String.valueOf(ds.child("photoUrl").getValue()); if ((p == null || p.isEmpty()) && ds.child("photo").getValue() != null) p = String.valueOf(ds.child("photo").getValue()); if ((p == null || p.isEmpty()) && ds.child("photoBase64").getValue() != null) p = String.valueOf(ds.child("photoBase64").getValue()); if (p != null && !p.isEmpty()) { myPhoto = p; prefs.edit().putString("USER_PHOTO", p).putString("PROFILE_PHOTO", p).apply(); markMeOnline(); } String hw = null; if (ds.child("headwearImage").getValue() != null) hw = String.valueOf(ds.child("headwearImage").getValue()); if ((hw == null || hw.isEmpty()) && ds.child("headwearUrl").getValue() != null) hw = String.valueOf(ds.child("headwearUrl").getValue()); if ((hw == null || hw.isEmpty()) && ds.child("headwearBase64").getValue() != null) hw = String.valueOf(ds.child("headwearBase64").getValue()); if ((hw == null || hw.isEmpty()) && ds.child("HEADWEAR_IMAGE").getValue() != null) hw = String.valueOf(ds.child("HEADWEAR_IMAGE").getValue()); if (hw != null && !hw.isEmpty()) { prefs.edit().putString("HEADWEAR_IMAGE", hw).apply(); if (roomHeaderHeadwearImg != null) loadHeadwearInto(roomHeaderHeadwearImg, hw); } break; } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);
        joinTime = System.currentTimeMillis();
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = prefs.getString("MY_9_DIGIT_ID", ""); myName = prefs.getString("USER_NAME", "User"); myGender = prefs.getString("USER_GENDER", ""); myCountry = prefs.getString("USER_COUNTRY", "Bangladesh"); myPhoto = getMyPhotoFromAllPlaces(); myLevel = prefs.getString("USER_LEVEL", "0"); if (myLevel == null) myLevel = "0";
        if (myId == null || myId.isEmpty()) { Toast.makeText(this, "ID not found, Login again", Toast.LENGTH_LONG).show(); finish(); return; }
        String intentRoomId = getIntent().getStringExtra("ROOM_ID"); String intentOwnerId = getIntent().getStringExtra("ROOM_OWNER_ID");
        if (intentRoomId != null && !intentRoomId.isEmpty()) roomId = intentRoomId; else roomId = myId;
        if (intentOwnerId != null && !intentOwnerId.isEmpty()) roomOwnerId = intentOwnerId; else roomOwnerId = roomId;
        activeRoomRef = FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId);
        roomMetaRef = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + roomOwnerId).child("meta");
        tvRoomId = findViewById(R.id.tvRoomId); tvRoomName = findViewById(R.id.tvRoomName); tvMyIdInRoom = findViewById(R.id.tvMyIdInRoom);
        btnRoomSet = findViewById(R.id.btnRoomSet); btnRoomEdit = findViewById(R.id.btnRoomEdit); layoutOwnerTools = findViewById(R.id.layoutOwnerTools);
        rvOnline = findViewById(R.id.rvOnline); roomTitleText = findViewById(R.id.roomTitleText); roomInfoText = findViewById(R.id.roomInfoText);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg); roomHeaderHeadwearImg = findViewById(R.id.roomHeaderHeadwearImg);
        chatListContainer = findViewById(R.id.chatListContainer);
        if (rvOnline != null) rvOnline.setLayoutManager(new LinearLayoutManager(this));
        if (roomHeaderProfileImg != null) loadRoomPhotoInto(roomHeaderProfileImg);
        if (roomHeaderHeadwearImg != null) loadHeadwearInto(roomHeaderHeadwearImg, getMyHeadwearFromAllPlaces());
        if (tvRoomId != null) tvRoomId.setText("Room ID: " + roomId);
        if (roomInfoText != null) roomInfoText.setText("ID: " + roomId);
        if (tvMyIdInRoom != null) tvMyIdInRoom.setText("My ID: " + myId + " | " + myName);
        if (layoutOwnerTools != null) layoutOwnerTools.setVisibility(View.GONE);
        if (btnRoomSet != null) btnRoomSet.setVisibility(View.GONE);
        if (btnRoomEdit != null) btnRoomEdit.setVisibility(View.GONE);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { showPowerMenu(); }
        });
        checkMyRole(); loadRoomInfo(); syncMyPhotoFromFirebase(); markMeOnline(); setupSeats();
        if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(v -> openRoomHeaderPopup());
        if (roomTitleText != null) roomTitleText.setOnClickListener(v -> openRoomHeaderPopup());
        if (roomInfoText != null) roomInfoText.setOnClickListener(v -> openRoomHeaderPopup());
        View btnRanking = findViewById(R.id.btnInRoomRanking); if (btnRanking != null) btnRanking.setOnClickListener(v -> openRanking());
        View btnOpenChat = findViewById(R.id.btnOpenChat); if (btnOpenChat != null) btnOpenChat.setOnClickListener(v -> openChatInput());
        View btnOnlineList = findViewById(R.id.btnOnlineList); if (btnOnlineList != null) btnOnlineList.setOnClickListener(v -> openOnlineList());
        View tvOnlineCount = findViewById(R.id.textOnlineCount); if (tvOnlineCount != null) tvOnlineCount.setOnClickListener(v -> openOnlineList());
        View btnRoomTools = findViewById(R.id.btnRoomTools); if (btnRoomTools != null) btnRoomTools.setOnClickListener(v -> openPremiumSettings());
        if (btnRoomSet != null) btnRoomSet.setOnClickListener(v -> openRoomSetDialog());
        ImageButton btnPower = findViewById(R.id.btnPower); if (btnPower != null) btnPower.setOnClickListener(v -> showPowerMenu());
        listenChatRealtime(); listenOnlineCount();
    }

    private String getFlag(String country) {
        if (country == null) return "🌍"; String c = country.toLowerCase();
        if (c.contains("bangladesh")) return "🇧🇩"; if (c.contains("india")) return "🇮🇳"; if (c.contains("pakistan")) return "🇵🇰"; if (c.contains("nepal")) return "🇳🇵"; if (c.contains("sri lanka")) return "🇱🇰";
        if (c.contains("usa") || c.contains("america")) return "🇺🇸"; if (c.contains("uk") || c.contains("england")) return "🇬🇧"; if (c.contains("saudi")) return "🇸🇦"; if (c.contains("uae") || c.contains("emirates")) return "🇦🇪";
        if (c.contains("qatar")) return "🇶🇦"; if (c.contains("kuwait")) return "🇰🇼"; if (c.contains("malaysia")) return "🇲🇾"; if (c.contains("indonesia")) return "🇮🇩"; if (c.contains("philippines")) return "🇵🇭"; if (c.contains("turkey")) return "🇹🇷"; if (c.contains("canada")) return "🇨🇦"; if (c.contains("australia")) return "🇦🇺"; return "🌍";
    }
    private void markMeOnline() {
        myPhoto = getMyPhotoFromAllPlaces();
        Map<String, Object> m = new HashMap<>(); m.put("name", myName); m.put("photo", myPhoto == null ? "" : myPhoto); m.put("photoUrl", myPhoto == null ? "" : myPhoto); m.put("country", myCountry); m.put("level", myLevel); m.put("id", myId);
        activeRoomRef.child("online_users").child(myId).setValue(m);
        activeRoomRef.child("online_users").child(myId).onDisconnect().removeValue();
    }
    private void listenOnlineCount() { activeRoomRef.child("online_users").addValueEventListener(new ValueEventListener() { @Override public void onDataChange(@NonNull DataSnapshot snap) { TextView tvC = findViewById(R.id.textOnlineCount); if (tvC != null) tvC.setText(String.valueOf(snap.getChildrenCount())); } @Override public void onCancelled(@NonNull DatabaseError e) {} }); }
    private void openChatInput() {
        BottomSheetDialog d = new BottomSheetDialog(this); View v = LayoutInflater.from(this).inflate(R.layout.dialog_room_chat, null); d.setContentView(v);
        EditText et = v.findViewById(R.id.etChatInput); et.requestFocus();
        if (d.getWindow() != null) d.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        d.setOnShowListener(dialog -> { et.requestFocus(); android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); if (imm != null) imm.showSoftInput(et, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT); });
        v.findViewById(R.id.btnSendChat).setOnClickListener(x -> { String msg = et.getText().toString().trim(); if (msg.isEmpty()) return; Map<String, Object> m = new HashMap<>(); m.put("senderId", myId); m.put("senderName", myName); m.put("senderPhoto", getMyPhotoFromAllPlaces()); m.put("senderCountry", myCountry); m.put("senderLevel", myLevel); m.put("msg", msg); m.put("time", System.currentTimeMillis()); activeRoomRef.child("chats").push().setValue(m); d.dismiss(); });
        d.show();
    }
    private void listenChatRealtime() {
        if (chatListContainer != null) chatListContainer.removeAllViews();
        if (chatListener != null && chatQuery != null) { try { chatQuery.removeEventListener(chatListener); } catch (Exception e) {} }
        chatQuery = activeRoomRef.child("chats").orderByChild("time").startAt(joinTime + 1);
        chatListener = new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot snap, String p) { if (snap == null || !snap.exists()) return; Long t = snap.child("time").getValue(Long.class); if (t == null || t <= joinTime) return; String name = snap.child("senderName").getValue(String.class); String photo = snap.child("senderPhoto").getValue(String.class); String country = snap.child("senderCountry").getValue(String.class); String level = snap.child("senderLevel").getValue(String.class); String msg = snap.child("msg").getValue(String.class); if (name == null) name = "User"; if (msg == null) msg = ""; if (country == null) country = ""; if (level == null) level = "0"; addChatToScreen(name, photo, country, level, msg); }
            @Override public void onChildChanged(@NonNull DataSnapshot s, String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) { if (chatListContainer != null) chatListContainer.removeAllViews(); }
            @Override public void onChildMoved(@NonNull DataSnapshot s, String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        chatQuery.addChildEventListener(chatListener);
    }
    private void addChatToScreen(String name, String photo, String country, String level, String msg) {
        if (chatListContainer == null) return;
        View v = LayoutInflater.from(this).inflate(R.layout.item_chat, chatListContainer, false);
        ImageView iv = v.findViewById(R.id.imgChatPhoto); TextView tvH = v.findViewById(R.id.tvChatHeader); TextView tvM = v.findViewById(R.id.tvChatMsg);
        loadPhotoInto(iv, photo); tvH.setText(name + "  Lv." + level + "  " + getFlag(country)); tvM.setText(msg); chatListContainer.addView(v);
    }
    private void openOnlineList() {
        BottomSheetDialog d = new BottomSheetDialog(this); View v = LayoutInflater.from(this).inflate(R.layout.dialog_online_list, null); d.setContentView(v);
        RecyclerView rv = v.findViewById(R.id.rvOnlineList); rv.setLayoutManager(new LinearLayoutManager(this)); List<Map<String, String>> list = new ArrayList<>();
        RecyclerView.Adapter adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup p, int t) { View item = LayoutInflater.from(p.getContext()).inflate(R.layout.item_user, p, false); return new RecyclerView.ViewHolder(item) {}; }
            @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                Map<String, String> u = list.get(pos); ImageView iv = h.itemView.findViewById(R.id.imgChatPhoto); TextView tvH = h.itemView.findViewById(R.id.userName); TextView tvM = h.itemView.findViewById(R.id.tvChatMsg);
                loadPhotoInto(iv, u.get("photo")); tvH.setText(u.get("name") + "  Lv." + u.get("level") + "  " + getFlag(u.get("country"))); tvM.setText("ID: " + u.get("id"));
                h.itemView.setOnClickListener(x -> openMiniProfile(u.get("id"), u.get("name"), u.get("photo")));
            }
            @Override public int getItemCount() { return list.size(); }
        };
        rv.setAdapter(adapter);
        activeRoomRef.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { list.clear(); for (DataSnapshot c : snap.getChildren()) { Map<String, String> m = new HashMap<>(); m.put("name", c.child("name").getValue(String.class) == null ? "User" : c.child("name").getValue(String.class)); String ph = c.child("photo").getValue(String.class); if (ph == null || ph.isEmpty()) ph = c.child("photoUrl").getValue(String.class); m.put("photo", ph == null ? "" : ph); m.put("country", c.child("country").getValue(String.class) == null ? "" : c.child("country").getValue(String.class)); Object lvlObj = c.child("level").getValue(); m.put("level", lvlObj == null ? "0" : String.valueOf(lvlObj)); m.put("id", c.getKey()); list.add(m); } adapter.notifyDataSetChanged(); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        v.findViewById(R.id.btnCloseOnline).setOnClickListener(x -> d.dismiss()); d.show();
    }
    private void showPowerMenu() {
        String[] options = {"Minimize", "Exit Room"};
        new AlertDialog.Builder(this).setTitle("Room Options").setItems(options, (dialog, which) -> {
            if (which == 0) { isExiting = false; finish(); }
            else { isExiting = true; finishAffinity(); }
        }).setNegativeButton("Cancel", null).show();
    }
    private void checkMyRole() {
        amIOwner = myId.equals(roomOwnerId);
        activeRoomRef.child("admins").child(myId).addListenerForSingleValueEvent(new ValueEventListener() { @Override public void onDataChange(@NonNull DataSnapshot snap) { amIAdmin = snap.exists() && Boolean.TRUE.equals(snap.getValue(Boolean.class)); } @Override public void onCancelled(@NonNull DatabaseError e) {} });
    }
    private void loadRoomInfo() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                String rName = snap.child("roomName").getValue(String.class); String rPhoto = snap.child("roomPhoto").getValue(String.class); String rAnnounce = snap.child("announcement").getValue(String.class); String rTheme = snap.child("theme").getValue(String.class);
                if (rName != null) { if (tvRoomName != null) tvRoomName.setText(rName); if (roomTitleText != null) roomTitleText.setText(rName); }
                if (rPhoto != null && !rPhoto.isEmpty() && !rPhoto.equals("null") && !rPhoto.startsWith("content://") && !rPhoto.startsWith("file://")) currentRoomPhotoUrl = rPhoto; else currentRoomPhotoUrl = "";
                if (roomHeaderProfileImg != null) loadRoomPhotoInto(roomHeaderProfileImg);
                if (rAnnounce != null && !rAnnounce.isEmpty()) { TextView notice = findViewById(R.id.roomNoticeBarText); if (notice != null) { notice.setVisibility(View.VISIBLE); notice.setText(rAnnounce); } }
                if (rTheme != null) selectedTheme = rTheme;
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void openPremiumSettings() {
        BottomSheetDialog dialog = new BottomSheetDialog(this); View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_tools, null); dialog.setContentView(view);
        view.findViewById(R.id.toolRoomSet).setOnClickListener(v -> { dialog.dismiss(); openRoomSetDialog(); });
        view.findViewById(R.id.toolRoomEdit).setOnClickListener(v -> { dialog.dismiss(); openRoomSetDialog(); });
        view.findViewById(R.id.toolClearChat).setOnClickListener(v -> { dialog.dismiss(); clearChat(); });
        view.findViewById(R.id.toolLock).setOnClickListener(v -> Toast.makeText(this, "Lock on - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolClearCharm).setOnClickListener(v -> Toast.makeText(this, "Clear charm - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolChangeMic).setOnClickListener(v -> Toast.makeText(this, "Change mic - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolDress).setOnClickListener(v -> Toast.makeText(this, "Dress - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolLuckySound).setOnClickListener(v -> Toast.makeText(this, "Lucky Sound - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolVoiceUser).setOnClickListener(v -> Toast.makeText(this, "Voice User - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolKickRecord).setOnClickListener(v -> Toast.makeText(this, "Kick out record - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolYoutube).setOnClickListener(v -> Toast.makeText(this, "YouTube Co-watch - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolLuckyFloat).setOnClickListener(v -> Toast.makeText(this, "Lucky Float - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolRoomPK).setOnClickListener(v -> Toast.makeText(this, "Room PK - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.toolBroadcast).setOnClickListener(v -> Toast.makeText(this, "Broadcast - Coming Soon", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnToolsClose).setOnClickListener(v -> dialog.dismiss()); dialog.show();
    }
    private void clearChat() { if (chatListContainer != null) chatListContainer.removeAllViews(); activeRoomRef.child("chats").removeValue(); Toast.makeText(this, "Chat Cleaned", Toast.LENGTH_SHORT).show(); }
    private void openRoomSetDialog() {
        if (!amIOwner && !amIAdmin) { Toast.makeText(this, "Only Room Owner / Admin can change", Toast.LENGTH_SHORT).show(); return; }
        pickedRoomPhotoUri = null; pickedRoomPhotoBase64 = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this); View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_set, null); builder.setView(view); AlertDialog dialog = builder.create();
        dialogRoomPhoto = view.findViewById(R.id.imgRoomPhoto); EditText etName = view.findViewById(R.id.etRoomName); EditText etAnnounce = view.findViewById(R.id.etRoomAnnouncement); TextView tvTheme = view.findViewById(R.id.tvSelectedTheme);
        if (tvRoomName != null) etName.setText(tvRoomName.getText().toString());
        if (dialogRoomPhoto != null) loadRoomPhotoInto(dialogRoomPhoto);
        tvTheme.setText("Theme: " + selectedTheme);
        dialogRoomPhoto.setOnClickListener(v -> pickRoomPhoto.launch("image/*"));
        view.findViewById(R.id.btnThemeDefault).setOnClickListener(v -> { selectedTheme = "Default"; tvTheme.setText("Theme: Default"); });
        view.findViewById(R.id.btnThemeGold).setOnClickListener(v -> { selectedTheme = "Gold"; tvTheme.setText("Theme: Gold"); });
        view.findViewById(R.id.btnThemeNight).setOnClickListener(v -> { selectedTheme = "Night"; tvTheme.setText("Theme: Night"); });
        view.findViewById(R.id.btnSaveRoomSet).setOnClickListener(v -> {
            String newName = etName.getText().toString().trim(); String newAnnounce = etAnnounce.getText().toString().trim();
            if (newName.isEmpty()) { Toast.makeText(this, "Enter Room Name", Toast.LENGTH_SHORT).show(); return; }
            String finalPhoto = currentRoomPhotoUrl; if (pickedRoomPhotoBase64 != null && !pickedRoomPhotoBase64.isEmpty()) finalPhoto = pickedRoomPhotoBase64;
            Map<String, Object> map = new HashMap<>(); map.put("roomName", newName); map.put("announcement", newAnnounce); map.put("theme", selectedTheme); map.put("roomPhoto", finalPhoto == null ? "" : finalPhoto); map.put("roomId", roomOwnerId); map.put("ownerId", roomOwnerId); roomMetaRef.updateChildren(map);
            Map<String, Object> activeMap = new HashMap<>(); activeMap.put("roomName", newName); activeMap.put("roomPhoto", finalPhoto == null ? "" : finalPhoto); activeRoomRef.updateChildren(activeMap);
            currentRoomPhotoUrl = finalPhoto == null ? "" : finalPhoto;
            if (tvRoomName != null) tvRoomName.setText(newName); if (roomTitleText != null) roomTitleText.setText(newName);
            if (roomHeaderProfileImg != null) loadRoomPhotoInto(roomHeaderProfileImg);
            pickedRoomPhotoUri = null; pickedRoomPhotoBase64 = null; Toast.makeText(this, "Room Saved", Toast.LENGTH_SHORT).show(); dialog.dismiss();
        });
        view.findViewById(R.id.btnCancelRoomSet).setOnClickListener(v -> dialog.dismiss()); dialog.show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { if (chatListener != null && chatQuery != null) chatQuery.removeEventListener(chatListener); } catch (Exception e) {}
        try { if (chatListContainer != null) chatListContainer.removeAllViews(); } catch (Exception e) {}
        if (isExiting) {
            try { int mySeat = mySeatIndex(); if (mySeat != -1) activeRoomRef.child("seats").child("seat_" + mySeat).removeValue(); } catch (Exception e) {}
            try { activeRoomRef.child("online_users").child(myId).removeValue(); } catch (Exception e) {}
        }
    }
}
