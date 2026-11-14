package com.sr.voicechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VoiceRoomActivity extends AppCompatActivity {
    private String roomOwnerId = "499506534";
    private String currentUserId = "";
    private String my9DigitId = "";
    private String myDisplayName = "User";
    private String myCountry = "Bangladesh";
    private String myLanguage = "English";
    private String myGender = "Male";
    private String myPhotoBase64 = "";
    private String selectedRoomLabel = "Friends";
    private String galleryMode = "";
    private String pendingRoomPhotoBase64 = "";
    private String currentRoomPhotoBase64 = "";
    private String rankType = "Wealth";
    private String rankPeriod = "Day";
    private long myWalletCoins = 0;
    private boolean giftListenerStarted = false;
    private String selectedGiftEmoji = "🐉";
    private String selectedGiftName = "Dragon";
    private long selectedGiftPrice = 9999;
    private String selectedGiftTargetId = "";
    private String selectedGiftTargetName = "";
    private int selectedGiftIndex = 0;
    private long selectedRechargeCoins = 100000;
    private String selectedRechargePrice = "USD 0.99";
    private SharedPreferences prefs;
    private DatabaseReference databaseReference;
    private DatabaseReference roomMetaRef;
    private DatabaseReference adminsRef;
    private DatabaseReference messagesRef;
    private DatabaseReference userDatabaseRef;
    private TextView roomTitleText;
    private TextView roomNoticeBarText;
    private TextView roomInfoText;
    private TextView textOnlineCount;
    private TextView textRoomCoins;
    private TextView walletTextInGift;
    private TextView roomEditLogoText;
    private ImageView roomHeaderProfileImg;
    private ImageView roomEditLogoView;
    private EditText inputChatMessage;
    private TextView btnChatOption;
    private LinearLayout chatListContainer;
    private LinearLayout chatPopupBar;
    private View bottomBarContainer;
    private ScrollView chatScrollView;
    private List<String> roomAdminList = new ArrayList<>();
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Set<String> lockedSeats = new HashSet<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_voice_room);
        prefs = getSharedPreferences("SR_PREFS", MODE_PRIVATE);
        currentUserId = prefs.getString("USER_UID", "");
        my9DigitId = prefs.getString("USER_9DIGIT_ID", "");
        myDisplayName = prefs.getString("USER_NAME", "User");
        myPhotoBase64 = prefs.getString("USER_PHOTO_BASE64", "");
        myCountry = prefs.getString("USER_COUNTRY", "Bangladesh");
        myLanguage = prefs.getString("USER_LANGUAGE", "English");
        myGender = prefs.getString("USER_GENDER", "Male");
        String rid = getIntent().getStringExtra("ROOM_OWNER_ID");
        if (rid != null && !rid.isEmpty()) roomOwnerId = rid;
        String a = getIntent().getStringExtra("MY_9DIGIT_ID");
        if (a != null && !a.isEmpty()) my9DigitId = a;
        String b = getIntent().getStringExtra("MY_UID");
        if (b != null && !b.isEmpty()) currentUserId = b;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userDatabaseRef = databaseReference.child("users");
        roomMetaRef = databaseReference.child("rooms").child(roomOwnerId).child("meta");
        adminsRef = databaseReference.child("rooms").child(roomOwnerId).child("admins");
        messagesRef = databaseReference.child("rooms").child(roomOwnerId).child("messages");
        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        roomNoticeBarText = findViewById(R.id.roomNoticeBarText);
        textOnlineCount = findViewById(R.id.textOnlineCount);
        textRoomCoins = findViewById(R.id.textRoomCoins);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
        chatListContainer = findViewById(R.id.chatListContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        chatPopupBar = findViewById(R.id.chatPopupBar);
        bottomBarContainer = findViewById(R.id.bottomBarContainer);
        inputChatMessage = findViewById(R.id.inputChatMessage);
        btnChatOption = findViewById(R.id.btnChatOption);
        if (chatPopupBar != null) chatPopupBar.setVisibility(View.GONE);
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == RESULT_OK && r.getData() != null && r.getData().getData() != null) {
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), r.getData().getData());
                    java.io.ByteArrayOutputStream ba = new java.io.ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, ba);
                    String b64 = Base64.encodeToString(ba.toByteArray(), Base64.DEFAULT);
                    if ("ROOM_LOGO".equals(galleryMode)) {
                        pendingRoomPhotoBase64 = b64;
                        if (roomEditLogoView != null) roomEditLogoView.setImageBitmap(bmp);
                        if (roomEditLogoText != null) roomEditLogoText.setVisibility(View.GONE);
                        Toast.makeText(this, "Room Photo Selected", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) { Toast.makeText(this, "Pic nite pari nai", Toast.LENGTH_SHORT).show(); }
            }
        });
        if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(v -> showRoomHeaderProfile());
        if (roomTitleText != null) roomTitleText.setOnClickListener(v -> showRoomHeaderProfile());
        if (roomInfoText != null) roomInfoText.setOnClickListener(v -> showRoomHeaderProfile());
        View bo = findViewById(R.id.btnOnlineList);
        if (bo != null) bo.setOnClickListener(v -> showOnlineListDialog());
        View bp = findViewById(R.id.btnPower);
        if (bp != null) bp.setOnClickListener(v -> showExitDialog());
        View br = findViewById(R.id.btnInRoomRanking);
        if (br != null) br.setOnClickListener(v -> showInRoomRankingDialog());
        View bg = findViewById(R.id.btnGiftBox);
        if (bg != null) bg.setOnClickListener(v -> showNewGiftPanel());
        View bga = findViewById(R.id.btnGame);
        if (bga != null) bga.setOnClickListener(v -> Toast.makeText(this, "Game - Coming Soon", Toast.LENGTH_SHORT).show());
        View bt = findViewById(R.id.btnRoomTools);
        if (bt != null) bt.setOnClickListener(v -> showIconToolsDialog(isAdminOrOwner()));
        View bm = findViewById(R.id.btnMicToggle);
        if (bm != null) bm.setOnClickListener(v -> Toast.makeText(this, "Mic - Coming Soon", Toast.LENGTH_SHORT).show());
        View bw = findViewById(R.id.btnWallet);
        if (bw != null) bw.setOnClickListener(v -> showRechargeDialog());
        setupChatPopup();
        setupSeatInteractions();
        ensureMyIdentity();
        syncLatestUserProfile();
        listenToRoomDetails();
        listenToRoomPhoto();
        listenToRoomAdmins();
        listenToOnlineMembers();
        listenToFirebaseSeats();
        listenToChatRealtime();
        listenToMyWallet();
        listenToLockedSeats();
        listenToGiftEvents();
    }

    private boolean isAdminOrOwner() {
        String me = my9DigitId;
        if (me == null || me.isEmpty()) me = currentUserId;
        if (me != null && me.equals(roomOwnerId)) return true;
        if (me != null && roomAdminList.contains(me)) return true;
        if (currentUserId != null && currentUserId.equals(roomOwnerId)) return true;
        if (currentUserId != null && roomAdminList.contains(currentUserId)) return true;
        return false;
    }

    private void listenToLockedSeats() {
        databaseReference.child("rooms").child(roomOwnerId).child("locked_seats").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                lockedSeats.clear();
                for (DataSnapshot ds : s.getChildren()) if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) lockedSeats.add(ds.getKey());
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void setDialog85(BottomSheetDialog d) {
        Window w = d.getWindow();
        if (w != null) { int sh = getResources().getDisplayMetrics().heightPixels; w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.85)); w.setGravity(Gravity.BOTTOM); }
    }

    private void setDialog75(BottomSheetDialog d) {
        Window w = d.getWindow();
        if (w != null) { int sh = getResources().getDisplayMetrics().heightPixels; w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.75)); w.setGravity(Gravity.BOTTOM); }
    }

    private android.app.Dialog makeFullScreenDialog() {
        android.app.Dialog d = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Window w = d.getWindow();
        if (w != null) { w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); w.setGravity(Gravity.BOTTOM); w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)); }
        return d;
    }

    private String flagForCountry(String c) {
        if (c == null) return "🌍";
        String s = c.toLowerCase();
        if (s.contains("bangladesh") || s.contains("bd")) return "🇧🇩";
        if (s.contains("india")) return "🇮🇳";
        if (s.contains("pakistan")) return "🇵🇰";
        if (s.contains("saudi")) return "🇸🇦";
        if (s.contains("oman")) return "🇴🇲";
        if (s.contains("uae") || s.contains("emirates")) return "🇦🇪";
        if (s.contains("usa") || s.contains("america")) return "🇺🇸";
        if (s.contains("uk") || s.contains("england")) return "🇬🇧";
        return "🌍";
    }

    private String genderIcon(String g) {
        if (g == null) return "👤";
        String s = g.toLowerCase();
        if (s.contains("female") || s.contains("girl") || s.contains("woman")) return "♀️";
        if (s.contains("male") || s.contains("boy") || s.contains("man")) return "♂️";
        return "👤";
    }

    private Bitmap decodeBase64(String b64) {
        if (b64 == null || b64.isEmpty()) return null;
        try { byte[] b = Base64.decode(b64, Base64.DEFAULT); return BitmapFactory.decodeByteArray(b, 0, b.length); } catch (Exception e) { return null; }
    }

    private TextView makeText(String txt, int size, String color, boolean bold, int gravity) {
        TextView t = new TextView(this);
        t.setText(txt); t.setTextSize(size); t.setTextColor(Color.parseColor(color));
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setGravity(gravity); return t;
    }

    private Button makeColorBtn(String txt, int color) {
        Button b = new Button(this);
        b.setText(txt); b.setAllCaps(false); b.setTextSize(16); b.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(60); b.setBackground(g); return b;
    }

    private CardView makeAvatar(String photo, String name, int size) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        card.setLayoutParams(lp); card.setRadius(size / 2); card.setCardElevation(0);
        Bitmap bmp = decodeBase64(photo);
        if (bmp != null) {
            card.setCardBackgroundColor(Color.TRANSPARENT);
            ImageView av = new ImageView(this);
            av.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            av.setScaleType(ImageView.ScaleType.CENTER_CROP); av.setImageBitmap(bmp); card.addView(av);
        } else {
            card.setCardBackgroundColor(Color.parseColor("#FF9800"));
            TextView lt = new TextView(this);
            lt.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            lt.setGravity(Gravity.CENTER); lt.setTextSize(22); lt.setTextColor(Color.WHITE); lt.setTypeface(null, android.graphics.Typeface.BOLD);
            lt.setText(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "U"); card.addView(lt);
        }
        return card;
    }

    private void ensureMyIdentity() {
        if (my9DigitId == null) my9DigitId = "";
        if (my9DigitId.isEmpty()) my9DigitId = prefs.getString("USER_9DIGIT_ID", "");
        if (myDisplayName == null || myDisplayName.equals("User")) myDisplayName = prefs.getString("USER_NAME", "User");
        if (myPhotoBase64 == null || myPhotoBase64.isEmpty()) myPhotoBase64 = prefs.getString("USER_PHOTO_BASE64", "");
        if (!my9DigitId.isEmpty()) {
            userDatabaseRef.orderByChild("my9DigitId").equalTo(my9DigitId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (snap.exists()) {
                        for (DataSnapshot u : snap.getChildren()) {
                            currentUserId = u.getKey();
                            if (u.child("name").getValue() != null) myDisplayName = String.valueOf(u.child("name").getValue());
                            if (u.child("photoBase64").getValue() != null) myPhotoBase64 = String.valueOf(u.child("photoBase64").getValue());
                            if (u.child("country").getValue() != null) myCountry = String.valueOf(u.child("country").getValue());
                            if (u.child("gender").getValue() != null) myGender = String.valueOf(u.child("gender").getValue());
                            prefs.edit().putString("USER_UID", currentUserId).putString("USER_NAME", myDisplayName).putString("USER_PHOTO_BASE64", myPhotoBase64).putString("USER_COUNTRY", myCountry).putString("USER_GENDER", myGender).apply();
                            pushOnlineNow(); break;
                        }
                    } else syncByUidFallback();
                }
                @Override public void onCancelled(@NonNull DatabaseError e) { syncByUidFallback(); }
            });
        } else syncByUidFallback();
    }

    private void syncByUidFallback() {
        if (currentUserId == null || currentUserId.isEmpty()) { pushOnlineNow(); return; }
        userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) {
                    if (s.child("my9DigitId").getValue() != null) my9DigitId = String.valueOf(s.child("my9DigitId").getValue());
                    if (s.child("name").getValue() != null) myDisplayName = String.valueOf(s.child("name").getValue());
                    if (s.child("photoBase64").getValue() != null) myPhotoBase64 = String.valueOf(s.child("photoBase64").getValue());
                    if (s.child("country").getValue() != null) myCountry = String.valueOf(s.child("country").getValue());
                    if (s.child("gender").getValue() != null) myGender = String.valueOf(s.child("gender").getValue());
                    prefs.edit().putString("USER_9DIGIT_ID", my9DigitId).putString("USER_NAME", myDisplayName).putString("USER_PHOTO_BASE64", myPhotoBase64).apply();
                }
                pushOnlineNow();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { pushOnlineNow(); }
        });
    }

    private void syncLatestUserProfile() {
        if (currentUserId.isEmpty() && my9DigitId.isEmpty()) return;
        String key = !currentUserId.isEmpty() ? currentUserId : my9DigitId;
        userDatabaseRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) {
                    String n = s.child("name").getValue(String.class);
                    if (n != null && !n.isEmpty()) { myDisplayName = n; prefs.edit().putString("USER_NAME", n).apply(); }
                    String v = s.child("my9DigitId").getValue(String.class);
                    if (v != null && !v.isEmpty()) { my9DigitId = v; prefs.edit().putString("USER_9DIGIT_ID", v).apply(); }
                    String c = s.child("country").getValue(String.class); if (c != null && !c.isEmpty()) myCountry = c;
                    String l = s.child("language").getValue(String.class); if (l != null && !l.isEmpty()) myLanguage = l;
                    String g = s.child("gender").getValue(String.class); if (g != null && !g.isEmpty()) myGender = g;
                    String p = s.child("photoBase64").getValue(String.class); if (p != null && !p.isEmpty()) { myPhotoBase64 = p; prefs.edit().putString("USER_PHOTO_BASE64", p).apply(); }
                    pushOnlineNow();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void pushOnlineNow() {
        String key = my9DigitId; if (key == null || key.isEmpty()) key = currentUserId; if (key == null || key.isEmpty()) key = roomOwnerId;
        if (key == null || key.isEmpty() || databaseReference == null) return;
        String n = myDisplayName; if (n == null || n.isEmpty()) n = prefs.getString("USER_NAME", "User"); if (n.equals("User") && !key.isEmpty()) n = "User " + key;
        Map<String, Object> m = new HashMap<>();
        m.put("name", n); m.put("photoBase64", myPhotoBase64 != null ? myPhotoBase64 : ""); m.put("level", "0"); m.put("country", myCountry); m.put("gender", myGender); m.put("my9DigitId", key);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).setValue(m);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).onDisconnect().removeValue();
    }

    private void listenToOnlineMembers() {
        pushOnlineNow(); if (databaseReference == null) return;
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { if (textOnlineCount != null && s.exists()) textOnlineCount.setText(String.valueOf(s.getChildrenCount())); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showChatPopup() {
        if (bottomBarContainer != null) bottomBarContainer.setVisibility(View.GONE);
        if (chatPopupBar != null) chatPopupBar.setVisibility(View.VISIBLE);
        if (inputChatMessage != null) {
            inputChatMessage.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(inputChatMessage, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideChatPopup() {
        if (chatPopupBar != null) chatPopupBar.setVisibility(View.GONE);
        if (bottomBarContainer != null) bottomBarContainer.setVisibility(View.VISIBLE);
        if (inputChatMessage != null) {
            inputChatMessage.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(inputChatMessage.getWindowToken(), 0);
        }
    }

    private void setupChatPopup() {
        View b = findViewById(R.id.btnOpenChat);
        if (b != null) b.setOnClickListener(v -> showChatPopup());
        if (inputChatMessage != null) inputChatMessage.setOnEditorActionListener((v, a, e) -> { sendChatMessage(); return true; });
        if (btnChatOption != null) btnChatOption.setOnClickListener(v -> sendChatMessage());
    }

    @Override
    public void onBackPressed() {
        if (chatPopupBar != null && chatPopupBar.getVisibility() == View.VISIBLE) { hideChatPopup(); return; }
        super.onBackPressed();
    }

    private void sendChatMessage() {
        if (inputChatMessage == null) return;
        String msg = inputChatMessage.getText().toString().trim();
        if (msg.isEmpty() || messagesRef == null) return;
        String myId = my9DigitId; if (myId == null || myId.isEmpty()) myId = currentUserId; if (myId == null || myId.isEmpty()) myId = roomOwnerId;
        Map<String, Object> m = new HashMap<>();
        m.put("userId", myId); m.put("userName", myDisplayName); m.put("text", msg); m.put("photoBase64", myPhotoBase64 != null ? myPhotoBase64 : ""); m.put("gender", myGender); m.put("country", myCountry); m.put("level", "0"); m.put("time", System.currentTimeMillis());
        messagesRef.push().setValue(m); hideChatPopup(); pushOnlineNow();
    }

    private void listenToChatRealtime() {
        if (messagesRef == null || chatListContainer == null) return;
        messagesRef.limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                chatListContainer.removeAllViews(); if (!s.exists()) return;
                for (DataSnapshot ds : s.getChildren()) {
                    String uname = ds.child("userName").getValue(String.class); if (uname == null) uname = "User";
                    String txt = ds.child("text").getValue(String.class); if (txt == null) txt = "";
                    String photo = ds.child("photoBase64").getValue(String.class); if (photo == null) photo = "";
                    String co = ds.child("country").getValue(String.class); if (co == null) co = "";
                    String lv = ds.child("level").getValue(String.class); if (lv == null) lv = "0";
                    LinearLayout row = new LinearLayout(VoiceRoomActivity.this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.TOP); row.setPadding(4, 6, 4, 6);
                    CardView cardAv = makeAvatar(photo, uname, 72); cardAv.setRadius(36); row.addView(cardAv);
                    LinearLayout box = new LinearLayout(VoiceRoomActivity.this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(10, 0, 0, 0); box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); row.addView(box);
                    box.addView(makeText(uname + "  " + flagForCountry(co), 12, "#3E2F00", true, Gravity.START));
                    box.addView(makeText("Lv." + lv, 11, "#888888", false, Gravity.START));
                    box.addView(makeText(txt, 14, "#222222", false, Gravity.START));
                    chatListContainer.addView(row);
                }
                if (chatScrollView != null) chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void setupSeatInteractions() {
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            int res = getResources().getIdentifier("seat_" + idx, "id", getPackageName());
            View seat = findViewById(res);
            if (seat != null) seat.setOnClickListener(v -> showSeatPopup(idx));
        }
    }

    private void showSeatPopup(final int idx) {
        String myId = my9DigitId.isEmpty() ? currentUserId : my9DigitId; if (myId == null || myId.isEmpty()) myId = roomOwnerId;
        final String finalMyId = myId;
        databaseReference.child("rooms").child(roomOwnerId).child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot allSeatsSnap) {
                String mySeatKey = null;
                for (DataSnapshot sSnap : allSeatsSnap.getChildren()) { String u = sSnap.child("userId").getValue(String.class); if (u != null && u.equals(finalMyId)) { mySeatKey = sSnap.getKey(); break; } }
                final String existing = mySeatKey;
                DataSnapshot targetSnap = allSeatsSnap.child("seat_" + idx);
                boolean isEmpty = !targetSnap.exists();
                String seatUserId = isEmpty ? null : targetSnap.child("userId").getValue(String.class);
                String seatUserName = isEmpty ? null : targetSnap.child("userName").getValue(String.class);
                boolean isMine = seatUserId != null && (seatUserId.equals(currentUserId) || seatUserId.equals(my9DigitId) || seatUserId.equals(finalMyId));
                boolean admin = isAdminOrOwner();
                boolean locked = lockedSeats.contains("seat_" + idx);
                if (isEmpty) {
                    if (locked && !admin) { Toast.makeText(VoiceRoomActivity.this, "Ei seat lock kora", Toast.LENGTH_SHORT).show(); return; }
                    BottomSheetDialog d1 = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                    LinearLayout r1 = new LinearLayout(VoiceRoomActivity.this); r1.setOrientation(LinearLayout.VERTICAL); r1.setPadding(28, 24, 28, 28); r1.setBackgroundColor(Color.WHITE);
                    r1.addView(makeText("Seat " + (idx + 1), 18, "#222222", true, Gravity.CENTER));
                    Button btnTake = makeColorBtn("Take Mic", Color.parseColor("#FF9800"));
                    btnTake.setOnClickListener(v -> { d1.dismiss(); takeSeat(idx, existing); }); r1.addView(btnTake);
                    if (admin) {
                        Button btnLock = makeColorBtn(locked ? "Unlock" : "Lock", Color.parseColor("#222222"));
                        btnLock.setOnClickListener(v -> { d1.dismiss(); if (locked) databaseReference.child("rooms").child(roomOwnerId).child("locked_seats").child("seat_" + idx).removeValue(); else databaseReference.child("rooms").child(roomOwnerId).child("locked_seats").child("seat_" + idx).setValue(true); });
                        r1.addView(btnLock);
                    }
                    d1.setContentView(r1); setDialog75(d1); d1.show(); return;
                }
                if (isMine) {
                    BottomSheetDialog d2 = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                    LinearLayout r2 = new LinearLayout(VoiceRoomActivity.this); r2.setOrientation(LinearLayout.VERTICAL); r2.setPadding(28, 24, 28, 28); r2.setBackgroundColor(Color.WHITE);
                    Button bLeave = makeColorBtn("Leave the Mic", Color.parseColor("#FF5252"));
                    bLeave.setOnClickListener(v -> { d2.dismiss(); databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_" + idx).removeValue(); }); r2.addView(bLeave);
                    Button bProf = makeColorBtn("Profile", Color.parseColor("#FF9800"));
                    bProf.setOnClickListener(v -> { d2.dismiss(); showUserProfileCard(finalMyId); }); r2.addView(bProf);
                    d2.setContentView(r2); setDialog75(d2); d2.show(); return;
                }
                BottomSheetDialog d3 = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                LinearLayout r3 = new LinearLayout(VoiceRoomActivity.this); r3.setOrientation(LinearLayout.VERTICAL); r3.setPadding(28, 24, 28, 28); r3.setBackgroundColor(Color.WHITE);
                r3.addView(makeText(seatUserName != null ? seatUserName : "User", 18, "#222222", true, Gravity.CENTER));
                String[] otherOpts = {"Profile", "Go to chat", "Let him leave the mic", "Kick out of room"};
                for (String o : otherOpts) {
                    TextView opt = makeText(o, 16, "#222222", false, Gravity.CENTER); opt.setPadding(0, 26, 0, 26);
                    final String oo = o;
                    opt.setOnClickListener(v -> {
                        d3.dismiss();
                        if (oo.equals("Profile")) showUserProfileCard(seatUserId);
                        else if (oo.equals("Go to chat")) showChatPopup();
                        else if (oo.equals("Kick out of room") || oo.equals("Let him leave the mic")) {
                            if (admin) { databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_" + idx).removeValue(); Toast.makeText(VoiceRoomActivity.this, "Removed from mic", Toast.LENGTH_SHORT).show(); }
                            else Toast.makeText(VoiceRoomActivity.this, "Only Owner / Admin", Toast.LENGTH_SHORT).show();
                        }
                    });
                    r3.addView(opt);
                }
                d3.setContentView(r3); setDialog85(d3); d3.show();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void takeSeat(int idx, String existingSeat) {
        String myId = my9DigitId.isEmpty() ? currentUserId : my9DigitId; if (myId == null || myId.isEmpty()) myId = roomOwnerId;
        if (existingSeat != null && !existingSeat.equals("seat_" + idx)) databaseReference.child("rooms").child(roomOwnerId).child("seats").child(existingSeat).removeValue();
        DatabaseReference seatRef = databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_" + idx);
        Map<String, Object> m = new HashMap<>(); m.put("userId", myId); m.put("userName", myDisplayName != null ? myDisplayName : "User"); m.put("photoBase64", myPhotoBase64 != null ? myPhotoBase64 : "");
        seatRef.setValue(m); seatRef.onDisconnect().removeValue();
        Toast.makeText(this, "Seat " + (idx + 1) + " e bosechen", Toast.LENGTH_SHORT).show();
    }

    private void removeMySeat() {
        String myId = my9DigitId.isEmpty() ? currentUserId : my9DigitId; if (myId == null || myId.isEmpty()) myId = roomOwnerId;
        final String finalMyId = myId; if (databaseReference == null) return;
        databaseReference.child("rooms").child(roomOwnerId).child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { for (DataSnapshot s : snap.getChildren()) { String u = s.child("userId").getValue(String.class); if (u != null && u.equals(finalMyId)) databaseReference.child("rooms").child(roomOwnerId).child("seats").child(s.getKey()).removeValue(); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToFirebaseSeats() {
        if (databaseReference == null) return;
        databaseReference.child("rooms").child(roomOwnerId).child("seats").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (int i = 0; i < 10; i++) {
                    View imgView = findViewById(getResources().getIdentifier("seatImg_" + i, "id", getPackageName()));
                    DataSnapshot ss = s.child("seat_" + i);
                    View sl = findViewById(getResources().getIdentifier("seat_" + i, "id", getPackageName()));
                    TextView nameTv = null;
                    if (sl instanceof LinearLayout) { LinearLayout ll = (LinearLayout) sl; if (ll.getChildCount() >= 2 && ll.getChildAt(1) instanceof TextView) nameTv = (TextView) ll.getChildAt(1); }
                    if (ss.exists()) {
                        Bitmap bmp = decodeBase64(ss.child("photoBase64").getValue(String.class)); String nm = ss.child("userName").getValue(String.class);
                        if (imgView instanceof TextView) { TextView tv = (TextView) imgView; if (bmp != null) { Bitmap roundBmp = getCircularBitmap(bmp); tv.setText(""); tv.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), roundBmp)); } else { tv.setText("👤"); tv.setBackgroundResource(R.drawable.circle_seat_bg); } }
                        if (nameTv != null) nameTv.setText(nm != null ? nm : "");
                    } else {
                        if (imgView instanceof TextView) { TextView tv = (TextView) imgView; if (lockedSeats.contains("seat_" + i)) tv.setText("🔒"); else tv.setText("🪑"); tv.setBackgroundResource(R.drawable.circle_seat_bg); }
                        if (nameTv != null) { if (i == 0) nameTv.setText("Host"); else nameTv.setText(""); }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private Bitmap getCircularBitmap(Bitmap src) {
        if (src == null) return null;
        int size = Math.min(src.getWidth(), src.getHeight()); int x = (src.getWidth() - size) / 2; int y = (src.getHeight() - size) / 2;
        Bitmap squared = Bitmap.createBitmap(src, x, y, size, size); Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output); android.graphics.Paint paint = new android.graphics.Paint(); paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0); canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint); paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)); canvas.drawBitmap(squared, 0, 0, paint); return output;
    }

    private void showUserProfileCard(String targetId) {
        if (targetId == null || targetId.isEmpty()) return;
        final String fTargetId = targetId;
        userDatabaseRef.orderByChild("my9DigitId").equalTo(fTargetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                String name = myDisplayName; String photo = myPhotoBase64; String country = myCountry; String gender = myGender;
                boolean isOwner = fTargetId.equals(roomOwnerId);
                if (snap.exists()) { for (DataSnapshot u : snap.getChildren()) { if (u.child("name").getValue() != null) name = String.valueOf(u.child("name").getValue()); if (u.child("photoBase64").getValue() != null) photo = String.valueOf(u.child("photoBase64").getValue()); if (u.child("country").getValue() != null) country = String.valueOf(u.child("country").getValue()); if (u.child("gender").getValue() != null) gender = String.valueOf(u.child("gender").getValue()); break; } }
                showUserProfileCardUI(fTargetId, name, photo, country, gender, isOwner);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { showUserProfileCardUI(fTargetId, myDisplayName, myPhotoBase64, myCountry, myGender, fTargetId.equals(roomOwnerId)); }
        });
    }

    private void showUserProfileCardUI(String targetId, String name, String photo, String country, String gender, boolean isOwner) {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 16, 28, 28); root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView close = makeText("X", 20, "#999999", false, Gravity.END); close.setOnClickListener(v -> d.dismiss()); root.addView(close);
        CardView card = makeAvatar(photo, name, 220); card.setRadius(110); card.setCardElevation(8);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(220, 220); clp.gravity = Gravity.CENTER_HORIZONTAL; clp.setMargins(0, 6, 0, 8); card.setLayoutParams(clp); root.addView(card);
        if (isOwner) { TextView badge = makeText("OWNER", 12, "#FFFFFF", true, Gravity.CENTER); badge.setPadding(22, 8, 22, 8); GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor("#FF9800")); g.setCornerRadius(30); badge.setBackground(g); root.addView(badge); }
        else if (targetId != null && roomAdminList.contains(targetId)) { TextView badge = makeText("ADMIN", 12, "#FFFFFF", true, Gravity.CENTER); badge.setPadding(22, 8, 22, 8); GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor("#7B1FA2")); g.setCornerRadius(30); badge.setBackground(g); root.addView(badge); }
        root.addView(makeText((name != null ? name : "User") + " " + genderIcon(gender) + " " + flagForCountry(country), 20, "#222222", true, Gravity.CENTER));
        root.addView(makeText("ID: " + targetId, 14, "#888888", false, Gravity.CENTER));
        TextView gift = makeText("Gift", 28, "#FF9800", false, Gravity.CENTER); gift.setPadding(0, 18, 0, 8);
        gift.setOnClickListener(v -> { d.dismiss(); showPremiumGiftPanel(targetId, name); }); root.addView(gift);
        LinearLayout btnRow = new LinearLayout(this); btnRow.setOrientation(LinearLayout.HORIZONTAL); btnRow.setWeightSum(2); btnRow.setPadding(0, 14, 0, 0);
        Button addF = new Button(this); addF.setText("Add Friend"); addF.setAllCaps(false); addF.setTextColor(Color.parseColor("#222222"));
        GradientDrawable g1 = new GradientDrawable(); g1.setColor(Color.parseColor("#EEEEEE")); g1.setCornerRadius(60); addF.setBackground(g1); addF.setLayoutParams(new LinearLayout.LayoutParams(0, 120, 1f));
        addF.setOnClickListener(v -> Toast.makeText(this, "Friend Request Sent", Toast.LENGTH_SHORT).show()); btnRow.addView(addF);
        Button follow = makeColorBtn("Follow", Color.parseColor("#FF9800")); LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(0, 120, 1f); flp.setMargins(16, 0, 0, 0); follow.setLayoutParams(flp);
        follow.setOnClickListener(v -> Toast.makeText(this, "Followed", Toast.LENGTH_SHORT).show()); btnRow.addView(follow); root.addView(btnRow);
        d.setContentView(root); setDialog85(d); d.show();
    }

    private void listenToRoomDetails() {
        if (roomMetaRef == null) return;
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String rn = s.child("roomName").getValue(String.class); if (rn != null && !rn.isEmpty() && roomTitleText != null) roomTitleText.setText(rn);
                if (roomInfoText != null) roomInfoText.setText("ID: " + roomOwnerId);
                String ann = s.child("announcement").getValue(String.class);
                if (roomNoticeBarText != null) { if (ann == null || ann.isEmpty() || ann.equals("wlc")) { roomNoticeBarText.setVisibility(View.GONE); roomNoticeBarText.setText(""); } else { roomNoticeBarText.setVisibility(View.VISIBLE); roomNoticeBarText.setText(ann); } }
                String lb = s.child("roomLabel").getValue(String.class); if (lb != null && !lb.isEmpty()) selectedRoomLabel = lb;
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToRoomPhoto() {
        if (roomMetaRef == null) return;
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String rp = s.child("roomPhotoBase64").getValue(String.class);
                if (rp != null && !rp.isEmpty()) { currentRoomPhotoBase64 = rp; Bitmap bmp = decodeBase64(rp); if (bmp != null && roomHeaderProfileImg != null) roomHeaderProfileImg.setImageBitmap(bmp); }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToRoomAdmins() {
        if (adminsRef == null) return;
        adminsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { roomAdminList.clear(); for (DataSnapshot ds : s.getChildren()) if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) roomAdminList.add(ds.getKey()); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToMyWallet() {
        if (currentUserId.isEmpty()) return;
        userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                Long c = s.child("coins").getValue(Long.class); long w = c != null ? c : 0L; myWalletCoins = w;
                if (walletTextInGift != null) walletTextInGift.setText("Coins: " + String.format("%,d", w));
                if (textRoomCoins != null) textRoomCoins.setText(String.format("%,d", w));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showOnlineListDialog() {
        android.app.Dialog d = makeFullScreenDialog();
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(24, 20, 24, 24);
        LinearLayout top = new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL); root.addView(top);
        TextView t = makeText("Online list", 19, "#222222", true, Gravity.START); t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); top.addView(t);
        TextView cl = makeText("X", 22, "#888888", false, Gravity.CENTER); cl.setPadding(10, 10, 10, 10); cl.setOnClickListener(v -> d.dismiss()); top.addView(cl);
        TextView cnt = makeText("", 13, "#888888", false, Gravity.START); root.addView(cnt);
        ScrollView sv = new ScrollView(this); LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); sv.addView(list); root.addView(sv);
        pushOnlineNow();
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                list.removeAllViews(); if (!s.exists()) { cnt.setText("0 online"); return; } cnt.setText(s.getChildrenCount() + " online");
                for (DataSnapshot ds : s.getChildren()) { String k = ds.getKey(); if (k == null) continue; String n = ds.child("name").getValue(String.class); String p = ds.child("photoBase64").getValue(String.class); String co = ds.child("country").getValue(String.class); String lv = ds.child("level").getValue(String.class); addOnlineRow(list, k, n != null ? n : "User", p != null ? p : "", lv != null ? lv : "0", co != null ? co : "", d); }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        d.setContentView(root); d.show();
    }

    private void addOnlineRow(LinearLayout list, String uid, String name, String photo, String level, String country, android.app.Dialog dialog) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(6, 14, 6, 14);
        row.setOnClickListener(v -> { dialog.dismiss(); showUserProfileCard(uid); });
        CardView cardAv = makeAvatar(photo, name, 110); cardAv.setRadius(55); LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(110, 110); cardLp.setMargins(0, 0, 12, 0); cardAv.setLayoutParams(cardLp); row.addView(cardAv);
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(16, 0, 0, 0); box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); row.addView(box);
        box.addView(makeText(name + "  " + flagForCountry(country), 16, "#222222", true, Gravity.START));
        box.addView(makeText("Lv." + level, 13, "#888888", false, Gravity.START));
        if (uid.equals(roomOwnerId)) { TextView badge = makeText("Owner", 11, "#FFFFFF", true, Gravity.CENTER); badge.setPadding(14, 6, 14, 6); GradientDrawable g = new GradientDrawable(); g.setColor(Color.parseColor("#FF9800")); g.setCornerRadius(30); badge.setBackground(g); row.addView(badge); }
        list.addView(row);
    }

    private void showRoomHeaderProfile() {
        android.app.Dialog dialog = makeFullScreenDialog();
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE);
        LinearLayout tabRow = new LinearLayout(this); tabRow.setOrientation(LinearLayout.HORIZONTAL); tabRow.setGravity(Gravity.CENTER); tabRow.setPadding(0, 16, 0, 0); root.addView(tabRow);
        TextView tabProfile = makeText("Profile", 17, "#222222", true, Gravity.CENTER); tabProfile.setPadding(30, 10, 30, 10); tabRow.addView(tabProfile);
        TextView tabMembers = makeText("Members", 17, "#999999", false, Gravity.CENTER); tabMembers.setPadding(30, 10, 30, 10); tabRow.addView(tabMembers);
        TextView cb = makeText("X", 20, "#999999", false, Gravity.CENTER); cb.setPadding(20, 10, 20, 10); cb.setOnClickListener(v -> dialog.dismiss()); tabRow.addView(cb);
        ScrollView svProfile = new ScrollView(this); LinearLayout pb = new LinearLayout(this); pb.setOrientation(LinearLayout.VERTICAL); pb.setPadding(20, 10, 20, 20); svProfile.addView(pb); root.addView(svProfile);
        ScrollView svMembers = new ScrollView(this); LinearLayout mb = new LinearLayout(this); mb.setOrientation(LinearLayout.VERTICAL); mb.setPadding(20, 10, 20, 20); svMembers.addView(mb); svMembers.setVisibility(View.GONE); root.addView(svMembers);
        tabProfile.setOnClickListener(v -> { svProfile.setVisibility(View.VISIBLE); svMembers.setVisibility(View.GONE); tabProfile.setTextColor(Color.parseColor("#222222")); tabMembers.setTextColor(Color.parseColor("#999999")); });
        tabMembers.setOnClickListener(v -> { svProfile.setVisibility(View.GONE); svMembers.setVisibility(View.VISIBLE); tabProfile.setTextColor(Color.parseColor("#999999")); tabMembers.setTextColor(Color.parseColor("#222222")); loadHeaderMembers(mb, dialog); });
        loadHeaderProfileData(pb, dialog);
        dialog.setContentView(root); dialog.show();
    }

    private void loadHeaderProfileData(LinearLayout pb, android.app.Dialog dialog) {
        pb.removeAllViews();
        pb.addView(makeText("Room Profile", 18, "#222222", true, Gravity.CENTER));
        CardView card = makeAvatar(currentRoomPhotoBase64, "Room", 200); card.setRadius(100); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 200); lp.gravity = Gravity.CENTER_HORIZONTAL; card.setLayoutParams(lp); pb.addView(card);
        pb.addView(makeText(roomTitleText != null ? roomTitleText.getText().toString() : "Room", 18, "#222222", true, Gravity.CENTER));
        pb.addView(makeText("ID: " + roomOwnerId, 13, "#888888", false, Gravity.CENTER));
        pb.addView(makeText("Label: " + selectedRoomLabel, 13, "#888888", false, Gravity.CENTER));
        Button giftB = makeColorBtn("Gift Room Owner", Color.parseColor("#FF9800")); giftB.setOnClickListener(v -> { dialog.dismiss(); showPremiumGiftPanel(roomOwnerId, "Room Owner"); }); pb.addView(giftB);
    }

    private void loadHeaderMembers(LinearLayout mb, android.app.Dialog dialog) {
        mb.removeAllViews(); mb.addView(makeText("Members", 16, "#222222", true, Gravity.START));
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { for (DataSnapshot ds : s.getChildren()) { String k = ds.getKey(); String n = ds.child("name").getValue(String.class); String p = ds.child("photoBase64").getValue(String.class); addOnlineRow(mb, k, n != null ? n : "User", p != null ? p : "", "0", "", dialog); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showExitDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 20, 28, 28);
        root.addView(makeText("Leave Room?", 18, "#222222", true, Gravity.CENTER));
        Button b1 = makeColorBtn("Leave Room", Color.parseColor("#FF5252"));
        b1.setOnClickListener(v -> { d.dismiss(); leaveRoomAndFinish(); }); root.addView(b1);
        Button b2 = makeColorBtn("Cancel", Color.parseColor("#222222")); b2.setOnClickListener(v -> d.dismiss()); root.addView(b2);
        d.setContentView(root); setDialog75(d); d.show();
    }

    private void leaveRoomKeepHome() { removeMySeat(); if (databaseReference != null) { String k = my9DigitId.isEmpty() ? currentUserId : my9DigitId; if (k != null && !k.isEmpty()) databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(k).removeValue(); } }
    private void leaveRoomAndFinish() { leaveRoomKeepHome(); finish(); }

    @Override
    protected void onDestroy() { super.onDestroy(); leaveRoomKeepHome(); }

    private void showPremiumGiftPanel(String targetId, String targetName) {
        if (targetId == null || targetId.isEmpty()) return;
        selectedGiftTargetId = targetId; selectedGiftTargetName = targetName != null ? targetName : "User";
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(24, 16, 24, 24);
        root.addView(makeText("Send Gift to " + selectedGiftTargetName, 16, "#222222", true, Gravity.CENTER));
        walletTextInGift = makeText("Coins: " + String.format("%,d", myWalletCoins), 13, "#888888", false, Gravity.CENTER); root.addView(walletTextInGift);
        LinearLayout seatRow = new LinearLayout(this); seatRow.setOrientation(LinearLayout.HORIZONTAL); seatRow.setPadding(0, 12, 0, 8); root.addView(seatRow);
        loadSeatUsersForGift(seatRow, d);
        LinearLayout grid = new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL); root.addView(grid);
        renderGiftGrid(grid);
        LinearLayout bottom = new LinearLayout(this); bottom.setOrientation(LinearLayout.HORIZONTAL); bottom.setGravity(Gravity.CENTER_VERTICAL); bottom.setPadding(0, 12, 0, 0); root.addView(bottom);
        Button rechargeB = makeColorBtn("Recharge", Color.parseColor("#E91E63")); rechargeB.setOnClickListener(v -> { d.dismiss(); showRechargeDialog(); }); bottom.addView(rechargeB);
        Button sendB = makeColorBtn("Send 1", Color.parseColor("#FF9800"));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); slp.setMargins(12, 0, 0, 0); sendB.setLayoutParams(slp);
        sendB.setOnClickListener(v -> { d.dismiss(); sendPremiumGift(); }); bottom.addView(sendB);
        d.setContentView(root); setDialog85(d); d.show();
    }

    private void loadSeatUsersForGift(LinearLayout seatRow, BottomSheetDialog d) {
        databaseReference.child("rooms").child(roomOwnerId).child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                seatRow.removeAllViews();
                for (DataSnapshot ds : s.getChildren()) {
                    String uid = ds.child("userId").getValue(String.class); String nm = ds.child("userName").getValue(String.class); String ph = ds.child("photoBase64").getValue(String.class);
                    if (uid == null) continue;
                    LinearLayout item = new LinearLayout(VoiceRoomActivity.this); item.setOrientation(LinearLayout.VERTICAL); item.setGravity(Gravity.CENTER); item.setPadding(8, 4, 8, 4);
                    boolean isSel = uid.equals(selectedGiftTargetId);
                    item.addView(makeAvatar(ph != null ? ph : "", nm != null ? nm : "User", 96));
                    item.addView(makeText((isSel ? "✅ " : "") + (nm != null ? nm : "User"), 11, isSel ? "#FF9800" : "#222222", isSel, Gravity.CENTER));
                    final String fUid = uid; final String fNm = nm != null ? nm : "User";
                    item.setOnClickListener(v -> { selectedGiftTargetId = fUid; selectedGiftTargetName = fNm; d.dismiss(); showPremiumGiftPanel(fUid, fNm); });
                    seatRow.addView(item);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void renderGiftGrid(LinearLayout grid) {
        grid.removeAllViews();
        String[][] gifts = {{"🐉", "Dragon", "9999"}, {"🦁", "Lion", "4999"}, {"🌹", "Rose", "100"}, {"💎", "Diamond", "1999"}, {"🎁", "Gift Box", "500"}, {"👑", "Crown", "2999"}, {"🚀", "Rocket", "999"}, {"💖", "Heart", "200"}};
        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setWeightSum(4); grid.addView(row);
            for (int c = 0; c < 4; c++) {
                int idx = r * 4 + c; if (idx >= gifts.length) break;
                String em = gifts[idx][0]; String nm = gifts[idx][1]; long pr = Long.parseLong(gifts[idx][2]);
                LinearLayout cell = new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(8, 12, 8, 12); cell.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                boolean isSel = idx == selectedGiftIndex;
                GradientDrawable bg = new GradientDrawable(); if (isSel) { bg.setColor(Color.parseColor("#FFFDE7")); bg.setStroke(2, Color.parseColor("#FFC107")); } else { bg.setColor(Color.WHITE); bg.setStroke(1, Color.parseColor("#EEEEEE")); } bg.setCornerRadius(16); cell.setBackground(bg);
                cell.addView(makeText(em, 28, "#222222", false, Gravity.CENTER));
                cell.addView(makeText((isSel ? "✅ " : "") + nm, 12, isSel ? "#FF9800" : "#222222", isSel, Gravity.CENTER));
                cell.addView(makeText(String.valueOf(pr), 11, "#888888", false, Gravity.CENTER));
                final int fIdx = idx; final String fEm = em; final String fNm = nm; final long fPr = pr;
                cell.setOnClickListener(v -> { selectedGiftIndex = fIdx; selectedGiftEmoji = fEm; selectedGiftName = fNm; selectedGiftPrice = fPr; renderGiftGrid(grid); });
                row.addView(cell);
            }
        }
    }

    private void sendPremiumGift() {
        if (selectedGiftTargetId == null || selectedGiftTargetId.isEmpty()) { Toast.makeText(this, "Target select koro", Toast.LENGTH_SHORT).show(); return; }
        if (myWalletCoins < selectedGiftPrice) { Toast.makeText(this, "Coins kom - Recharge koro", Toast.LENGTH_SHORT).show(); showRechargeDialog(); return; }
        String senderId = my9DigitId.isEmpty() ? currentUserId : my9DigitId;
        final String fSender = senderId; final String fTarget = selectedGiftTargetId; final String fEm = selectedGiftEmoji; final String fNm = selectedGiftName; final long fPrice = selectedGiftPrice;
        userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                Long cur = s.child("coins").getValue(Long.class); long have = cur != null ? cur : 0;
                if (have < fPrice) { Toast.makeText(VoiceRoomActivity.this, "Coins kom", Toast.LENGTH_SHORT).show(); return; }
                userDatabaseRef.child(currentUserId).child("coins").setValue(have - fPrice);
                addCoinsToReceiver(fTarget, fPrice);
                Map<String, Object> g = new HashMap<>(); g.put("senderId", fSender); g.put("senderName", myDisplayName); g.put("receiverId", fTarget); g.put("receiverName", selectedGiftTargetName); g.put("emoji", fEm); g.put("giftName", fNm); g.put("price", fPrice); g.put("time", System.currentTimeMillis());
                databaseReference.child("rooms").child(roomOwnerId).child("gift_events").push().setValue(g);
                Toast.makeText(VoiceRoomActivity.this, fEm + " " + fNm + " Sent", Toast.LENGTH_SHORT).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addCoinsToReceiver(String target9Digit, long price) {
        long receive = (long) (price * 0.30);
        userDatabaseRef.orderByChild("my9DigitId").equalTo(target9Digit).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) { for (DataSnapshot u : snap.getChildren()) { String key = u.getKey(); Long cur = u.child("coins").getValue(Long.class); long have = cur != null ? cur : 0; Long ex = u.child("exchangeCoins").getValue(Long.class); long exHave = ex != null ? ex : 0; Long tot = u.child("totalReceived").getValue(Long.class); long totHave = tot != null ? tot : 0; userDatabaseRef.child(key).child("coins").setValue(have + receive); userDatabaseRef.child(key).child("exchangeCoins").setValue(exHave + receive); userDatabaseRef.child(key).child("totalReceived").setValue(totHave + receive); break; } }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showRechargeDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 20, 28, 28);
        root.addView(makeText("Coins", 18, "#222222", true, Gravity.CENTER));
        root.addView(makeText(String.format("%,d", myWalletCoins), 28, "#222222", true, Gravity.CENTER));
        root.addView(makeText("My Wallet - Original Coins", 13, "#888888", false, Gravity.CENTER));
        TextView bill = makeText("Detailed Bill >", 13, "#888888", false, Gravity.START); bill.setPadding(0, 8, 0, 16); bill.setOnClickListener(v -> showDetailedBill()); root.addView(bill);
        root.addView(makeText("Recharge", 16, "#222222", true, Gravity.START));
        String[][] packs = {{"100000", "USD 0.99"}, {"500000", "USD 4.99"}, {"1000000", "USD 9.99"}, {"5000000", "USD 49.99"}, {"10000000", "USD 99.99"}};
        for (String[] p : packs) {
            String coinStr = p[0]; String priceStr = p[1]; boolean isSel = Long.parseLong(coinStr) == selectedRechargeCoins;
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(20, 20, 20, 20);
            GradientDrawable bg = new GradientDrawable(); if (isSel) { bg.setColor(Color.parseColor("#FFFDE7")); bg.setStroke(2, Color.parseColor("#FFC107")); } else { bg.setColor(Color.WHITE); bg.setStroke(1, Color.parseColor("#EEEEEE")); } bg.setCornerRadius(20); row.setBackground(bg);
            TextView left = makeText(coinStr, 15, "#222222", true, Gravity.START); left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); row.addView(left);
            row.addView(makeText(priceStr, 14, "#888888", false, Gravity.END));
            TextView lock = makeText(isSel ? "✅ 🔒" : "🔒", 14, "#999999", false, Gravity.CENTER); lock.setPadding(12, 0, 0, 0); row.addView(lock);
            row.setOnClickListener(v -> { selectedRechargeCoins = Long.parseLong(coinStr); selectedRechargePrice = priceStr; d.dismiss(); showRechargeDialog(); });
            root.addView(row); View sp = new View(this); sp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 12)); root.addView(sp);
        }
        Button charge = makeColorBtn("Charge - Locked", Color.parseColor("#FFC107")); charge.setTextColor(Color.parseColor("#222222"));
        charge.setOnClickListener(v -> Toast.makeText(this, "Locked - Coming Soon", Toast.LENGTH_LONG).show()); root.addView(charge);
        TextView note = makeText("1 Dollar = 100,000 Coins | 5 Dollar = 500,000 | 10 Dollar = 1,000,000", 11, "#999999", false, Gravity.CENTER); note.setPadding(0, 12, 0, 0); root.addView(note);
        d.setContentView(root); setDialog85(d); d.show();
    }

    private void showDetailedBill() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 20, 28, 28);
        root.addView(makeText("Detailed Bill - My Wallet", 17, "#222222", true, Gravity.CENTER));
        root.addView(makeText("Balance: " + String.format("%,d", myWalletCoins), 15, "#222222", true, Gravity.START));
        TextView e4 = makeText("Gift Send: 100% Katbe | Gift Receive: 30% Pabe", 12, "#999999", false, Gravity.START); e4.setPadding(0, 10, 0, 0); root.addView(e4);
        d.setContentView(root); setDialog75(d); d.show();
    }

    private void showFlyingGift(String senderName, String receiverName, String emoji, String giftName) {
        Toast.makeText(this, senderName + " sent " + emoji + " " + giftName + " to " + receiverName, Toast.LENGTH_LONG).show();
    }

    private void listenToGiftEvents() {
        if (giftListenerStarted) return; giftListenerStarted = true;
        databaseReference.child("rooms").child(roomOwnerId).child("gift_events").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot ds : s.getChildren()) {
                    String sn = ds.child("senderName").getValue(String.class); String rn = ds.child("receiverName").getValue(String.class); String em = ds.child("emoji").getValue(String.class); String gn = ds.child("giftName").getValue(String.class);
                    if (sn != null && rn != null) showFlyingGift(sn, rn, em != null ? em : "🎁", gn != null ? gn : "Gift");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showNewGiftPanel() { showPremiumGiftPanel(roomOwnerId, "Room Owner"); }

    private void showIconToolsDialog(boolean isAdmin) {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 20, 28, 28);
        root.addView(makeText("Tools", 18, "#222222", true, Gravity.CENTER));
        String[] opts = {"Basic", "Other", "Room Password", "Room Edit"};
        for (String o : opts) {
            Button b = makeColorBtn(o, Color.parseColor("#FF9800")); final String oo = o;
            b.setOnClickListener(v -> { d.dismiss(); if (oo.equals("Room Password")) showRoomPasswordDialog(); else if (oo.equals("Room Edit")) { if (isAdmin) showRoomEditDialog(); else Toast.makeText(this, "Only Owner / Admin", Toast.LENGTH_SHORT).show(); } else Toast.makeText(this, oo + " - Coming Soon", Toast.LENGTH_SHORT).show(); });
            root.addView(b);
        }
        d.setContentView(root); setDialog75(d); d.show();
    }

    private void showRoomPasswordDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 20, 28, 28);
        root.addView(makeText("Room Password", 17, "#222222", true, Gravity.CENTER));
        root.addView(makeText("Password - Coming Soon", 13, "#888888", false, Gravity.CENTER));
        d.setContentView(root); setDialog75(d); d.show();
    }

    private void showRoomEditDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 20, 28, 28);
        root.addView(makeText("Room Edit", 17, "#222222", true, Gravity.CENTER));
        LinearLayout logoBox = new LinearLayout(this); logoBox.setOrientation(LinearLayout.VERTICAL); logoBox.setGravity(Gravity.CENTER); logoBox.setPadding(0, 12, 0, 12); root.addView(logoBox);
        roomEditLogoView = new ImageView(this); roomEditLogoView.setLayoutParams(new LinearLayout.LayoutParams(180, 180)); roomEditLogoView.setScaleType(ImageView.ScaleType.CENTER_CROP); logoBox.addView(roomEditLogoView);
        roomEditLogoText = makeText("Tap to change Room Photo", 12, "#888888", false, Gravity.CENTER); logoBox.addView(roomEditLogoText);
        Bitmap bmp = decodeBase64(currentRoomPhotoBase64); if (bmp != null) { roomEditLogoView.setImageBitmap(bmp); roomEditLogoText.setVisibility(View.GONE); }
        logoBox.setOnClickListener(v -> { galleryMode = "ROOM_LOGO"; Intent ii = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); galleryLauncher.launch(ii); });
        Button save = makeColorBtn("Save Room Photo", Color.parseColor("#FF9800"));
        save.setOnClickListener(v -> { if (pendingRoomPhotoBase64 != null && !pendingRoomPhotoBase64.isEmpty()) { roomMetaRef.child("roomPhotoBase64").setValue(pendingRoomPhotoBase64); Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show(); d.dismiss(); } else Toast.makeText(this, "First select photo", Toast.LENGTH_SHORT).show(); });
        root.addView(save);
        d.setContentView(root); setDialog85(d); d.show();
    }

    private void showInRoomRankingDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(28, 16, 28, 28);
        root.addView(makeText("In Room Ranking", 18, "#222222", true, Gravity.CENTER));
        LinearLayout typeRow = new LinearLayout(this); typeRow.setOrientation(LinearLayout.HORIZONTAL); typeRow.setPadding(0, 12, 0, 8); root.addView(typeRow);
        Button bWealth = makeColorBtn("Wealth", rankType.equals("Wealth") ? Color.parseColor("#FF9800") : Color.parseColor("#999999"));
        bWealth.setOnClickListener(v -> { rankType = "Wealth"; d.dismiss(); showInRoomRankingDialog(); }); typeRow.addView(bWealth);
        Button bCharm = makeColorBtn("Charm", rankType.equals("Charm") ? Color.parseColor("#FF9800") : Color.parseColor("#999999"));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); lp2.setMargins(10, 0, 0, 0); bCharm.setLayoutParams(lp2);
        bCharm.setOnClickListener(v -> { rankType = "Charm"; d.dismiss(); showInRoomRankingDialog(); }); typeRow.addView(bCharm);
        LinearLayout periodRow = new LinearLayout(this); periodRow.setOrientation(LinearLayout.HORIZONTAL); root.addView(periodRow);
        String[] periods = {"Day", "Week", "Month"};
        for (String p : periods) { Button bp = makeColorBtn(p, rankPeriod.equals(p) ? Color.parseColor("#222222") : Color.parseColor("#999999")); final String fp = p; bp.setOnClickListener(v -> { rankPeriod = fp; d.dismiss(); showInRoomRankingDialog(); }); periodRow.addView(bp); }
        TextView info = makeText(rankType + " - " + rankPeriod + " Ranking", 13, "#888888", false, Gravity.CENTER); info.setPadding(0, 14, 0, 0); root.addView(info);
        root.addView(makeText("No ranking data yet", 13, "#AAAAAA", false, Gravity.CENTER));
        d.setContentView(root); setDialog85(d); d.show();
    }
}
