package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.appcompat.app.AlertDialog;
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
import java.util.LinkedHashMap;
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
    private Map<String, Map<String, String>> chatRealMap = new HashMap<>();
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
        if (rid != null && !rid.isEmpty()) {
            roomOwnerId = rid;
        }
        String a = getIntent().getStringExtra("MY_9DIGIT_ID");
        if (a != null && !a.isEmpty()) {
            my9DigitId = a;
        }
        String b = getIntent().getStringExtra("MY_UID");
        if (b != null && !b.isEmpty()) {
            currentUserId = b;
        }
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
        if (chatPopupBar != null) {
            chatPopupBar.setVisibility(View.GONE);
        }
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null && r.getData().getData() != null) {
                    try {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), r.getData().getData());
                        java.io.ByteArrayOutputStream ba = new java.io.ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 70, ba);
                        String b64 = Base64.encodeToString(ba.toByteArray(), Base64.DEFAULT);
                        if ("ROOM_LOGO".equals(galleryMode)) {
                            pendingRoomPhotoBase64 = b64;
                            if (roomEditLogoView != null) {
                                roomEditLogoView.setImageBitmap(bmp);
                            }
                            if (roomEditLogoText != null) {
                                roomEditLogoText.setVisibility(View.GONE);
                            }
                            Toast.makeText(this, "Room Photo Selected ✅", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Pic nite pari nai", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        if (roomHeaderProfileImg != null) {
            roomHeaderProfileImg.setOnClickListener(v -> showRoomHeaderProfile());
        }
        if (roomTitleText != null) {
            roomTitleText.setOnClickListener(v -> showRoomHeaderProfile());
        }
        if (roomInfoText != null) {
            roomInfoText.setOnClickListener(v -> showRoomHeaderProfile());
        }
        View bo = findViewById(R.id.btnOnlineList);
        if (bo != null) {
            bo.setOnClickListener(v -> showOnlineListDialog());
        }
        View bp = findViewById(R.id.btnPower);
        if (bp != null) {
            bp.setOnClickListener(v -> showExitDialog());
        }
        View br = findViewById(R.id.btnInRoomRanking);
        if (br != null) {
            br.setOnClickListener(v -> showInRoomRankingDialog());
        }
        View bg = findViewById(R.id.btnGiftBox);
        if (bg != null) {
            bg.setOnClickListener(v -> showNewGiftPanel());
        }
        View bga = findViewById(R.id.btnGame);
        if (bga != null) {
            bga.setOnClickListener(v -> Toast.makeText(this, "Game - Coming Soon 🎮", Toast.LENGTH_SHORT).show());
        }
        View bt = findViewById(R.id.btnRoomTools);
        if (bt != null) {
            bt.setOnClickListener(v -> showIconToolsDialog(isAdminOrOwner()));
        }
        View bm = findViewById(R.id.btnMicToggle);
        if (bm != null) {
            bm.setOnClickListener(v -> Toast.makeText(this, "Mic - Coming Soon 🎤", Toast.LENGTH_SHORT).show());
        }
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
    }

    private boolean isAdminOrOwner() {
        String me = my9DigitId;
        if (me == null || me.isEmpty()) {
            me = currentUserId;
        }
        if (me != null && me.equals(roomOwnerId)) {
            return true;
        }
        if (me != null && roomAdminList.contains(me)) {
            return true;
        }
        if (currentUserId != null && currentUserId.equals(roomOwnerId)) {
            return true;
        }
        if (currentUserId != null && roomAdminList.contains(currentUserId)) {
            return true;
        }
        return false;
    }

    private void listenToLockedSeats() {
        databaseReference.child("rooms").child(roomOwnerId).child("locked_seats")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot s) {
                    lockedSeats.clear();
                    for (DataSnapshot ds : s.getChildren()) {
                        if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) {
                            lockedSeats.add(ds.getKey());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError e) {
                }
            });
    }

    private void setDialog85(BottomSheetDialog d) {
        Window w = d.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.85));
            w.setGravity(Gravity.BOTTOM);
        }
    }

    private void setDialog75(BottomSheetDialog d) {
        Window w = d.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.75));
            w.setGravity(Gravity.BOTTOM);
        }
    }

    private android.app.Dialog makeFullScreenDialog() {
        android.app.Dialog d = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            w.setGravity(Gravity.BOTTOM);
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
        return d;
    }

    private String flagForCountry(String c) {
        if (c == null) {
            return "🌍";
        }
        String s = c.toLowerCase();
        if (s.contains("bangladesh") || s.contains("bd")) {
            return "🇧🇩";
        }
        if (s.contains("india")) {
            return "🇮🇳";
        }
        if (s.contains("pakistan")) {
            return "🇵🇰";
        }
        if (s.contains("saudi")) {
            return "🇸🇦";
        }
        if (s.contains("oman")) {
            return "🇴🇲";
        }
        if (s.contains("uae") || s.contains("emirates")) {
            return "🇦🇪";
        }
        if (s.contains("usa") || s.contains("america")) {
            return "🇺🇸";
        }
        if (s.contains("uk") || s.contains("england")) {
            return "🇬🇧";
        }
        return "🌍";
    }

    private String genderIcon(String g) {
        if (g == null) {
            return "👤";
        }
        String s = g.toLowerCase();
        if (s.contains("female") || s.contains("girl") || s.contains("woman")) {
            return "♀️";
        }
        if (s.contains("male") || s.contains("boy") || s.contains("man")) {
            return "♂️";
        }
        return "👤";
    }

    private Bitmap decodeBase64(String b64) {
        if (b64 == null || b64.isEmpty()) {
            return null;
        }
        try {
            byte[] b = Base64.decode(b64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } catch (Exception e) {
            return null;
        }
    }

    private TextView makeText(String txt, int size, String color, boolean bold, int gravity) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor(color));
        if (bold) {
            t.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        t.setGravity(gravity);
        return t;
    }

    private Button makeColorBtn(String txt, int color) {
        Button b = new Button(this);
        b.setText(txt);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(60);
        b.setBackground(g);
        return b;
    }

    private CardView makeAvatar(String photo, String name, int size) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        card.setLayoutParams(lp);
        card.setRadius(size / 2);
        card.setCardElevation(0);
        Bitmap bmp = decodeBase64(photo);
        if (bmp != null) {
            card.setCardBackgroundColor(Color.TRANSPARENT);
            ImageView av = new ImageView(this);
            av.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            av.setScaleType(ImageView.ScaleType.CENTER_CROP);
            av.setImageBitmap(bmp);
            card.addView(av);
        } else {
            card.setCardBackgroundColor(Color.parseColor("#FF9800"));
            TextView lt = new TextView(this);
            lt.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            lt.setGravity(Gravity.CENTER);
            lt.setTextSize(22);
            lt.setTextColor(Color.WHITE);
            lt.setTypeface(null, android.graphics.Typeface.BOLD);
            lt.setText(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "U");
            card.addView(lt);
        }
        return card;
    }

    private void ensureMyIdentity() {
        if (my9DigitId == null) {
            my9DigitId = "";
        }
        if (my9DigitId.isEmpty()) {
            my9DigitId = prefs.getString("USER_9DIGIT_ID", "");
        }
        if (myDisplayName == null || myDisplayName.equals("User")) {
            myDisplayName = prefs.getString("USER_NAME", "User");
        }
        if (myPhotoBase64 == null || myPhotoBase64.isEmpty()) {
            myPhotoBase64 = prefs.getString("USER_PHOTO_BASE64", "");
        }
        if (!my9DigitId.isEmpty()) {
            userDatabaseRef.orderByChild("my9DigitId").equalTo(my9DigitId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) {
                            for (DataSnapshot u : snap.getChildren()) {
                                currentUserId = u.getKey();
                                if (u.child("name").getValue() != null) {
                                    myDisplayName = String.valueOf(u.child("name").getValue());
                                }
                                if (u.child("photoBase64").getValue() != null) {
                                    myPhotoBase64 = String.valueOf(u.child("photoBase64").getValue());
                                }
                                if (u.child("country").getValue() != null) {
                                    myCountry = String.valueOf(u.child("country").getValue());
                                }
                                if (u.child("gender").getValue() != null) {
                                    myGender = String.valueOf(u.child("gender").getValue());
                                }
                                prefs.edit().putString("USER_UID", currentUserId).putString("USER_NAME", myDisplayName).putString("USER_PHOTO_BASE64", myPhotoBase64).putString("USER_COUNTRY", myCountry).putString("USER_GENDER", myGender).apply();
                                pushOnlineNow();
                                break;
                            }
                        } else {
                            syncByUidFallback();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        syncByUidFallback();
                    }
                });
        } else {
            syncByUidFallback();
        }
    }

    private void syncByUidFallback() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            pushOnlineNow();
            return;
        }
        userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) {
                    if (s.child("my9DigitId").getValue() != null) {
                        my9DigitId = String.valueOf(s.child("my9DigitId").getValue());
                    }
                    if (s.child("name").getValue() != null) {
                        myDisplayName = String.valueOf(s.child("name").getValue());
                    }
                    if (s.child("photoBase64").getValue() != null) {
                        myPhotoBase64 = String.valueOf(s.child("photoBase64").getValue());
                    }
                    if (s.child("country").getValue() != null) {
                        myCountry = String.valueOf(s.child("country").getValue());
                    }
                    if (s.child("gender").getValue() != null) {
                        myGender = String.valueOf(s.child("gender").getValue());
                    }
                    prefs.edit().putString("USER_9DIGIT_ID", my9DigitId).putString("USER_NAME", myDisplayName).putString("USER_PHOTO_BASE64", myPhotoBase64).apply();
                }
                pushOnlineNow();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                pushOnlineNow();
            }
        });
    }

    private void syncLatestUserProfile() {
        if (currentUserId.isEmpty() && my9DigitId.isEmpty()) {
            return;
        }
        String key = !currentUserId.isEmpty() ? currentUserId : my9DigitId;
        userDatabaseRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) {
                    String n = s.child("name").getValue(String.class);
                    if (n != null && !n.isEmpty()) {
                        myDisplayName = n;
                        prefs.edit().putString("USER_NAME", n).apply();
                    }
                    String v = s.child("my9DigitId").getValue(String.class);
                    if (v != null && !v.isEmpty()) {
                        my9DigitId = v;
                        prefs.edit().putString("USER_9DIGIT_ID", v).apply();
                    }
                    String c = s.child("country").getValue(String.class);
                    if (c != null && !c.isEmpty()) {
                        myCountry = c;
                    }
                    String l = s.child("language").getValue(String.class);
                    if (l != null && !l.isEmpty()) {
                        myLanguage = l;
                    }
                    String g = s.child("gender").getValue(String.class);
                    if (g != null && !g.isEmpty()) {
                        myGender = g;
                    }
                    String p = s.child("photoBase64").getValue(String.class);
                    if (p != null && !p.isEmpty()) {
                        myPhotoBase64 = p;
                        prefs.edit().putString("USER_PHOTO_BASE64", p).apply();
                    }
                    pushOnlineNow();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void pushOnlineNow() {
        String key = my9DigitId;
        if (key == null || key.isEmpty()) {
            key = currentUserId;
        }
        if (key == null || key.isEmpty()) {
            key = roomOwnerId;
        }
        if (key == null || key.isEmpty() || databaseReference == null) {
            return;
        }
        String n = myDisplayName;
        if (n == null || n.isEmpty()) {
            n = prefs.getString("USER_NAME", "User");
        }
        if (n.equals("User") && !key.isEmpty()) {
            n = "User " + key;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("name", n);
        m.put("photoBase64", myPhotoBase64 != null ? myPhotoBase64 : "");
        m.put("level", "0");
        m.put("country", myCountry);
        m.put("gender", myGender);
        m.put("my9DigitId", key);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).setValue(m);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).onDisconnect().removeValue();
    }

    private void listenToOnlineMembers() {
        pushOnlineNow();
        if (databaseReference == null) {
            return;
        }
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                if (textOnlineCount != null && s.exists()) {
                    textOnlineCount.setText(String.valueOf(s.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void showChatPopup() {
        if (bottomBarContainer != null) {
            bottomBarContainer.setVisibility(View.GONE);
        }
        if (chatPopupBar != null) {
            chatPopupBar.setVisibility(View.VISIBLE);
        }
        if (inputChatMessage != null) {
            inputChatMessage.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(inputChatMessage, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideChatPopup() {
        if (chatPopupBar != null) {
            chatPopupBar.setVisibility(View.GONE);
        }
        if (bottomBarContainer != null) {
            bottomBarContainer.setVisibility(View.VISIBLE);
        }
        if (inputChatMessage != null) {
            inputChatMessage.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(inputChatMessage.getWindowToken(), 0);
            }
        }
    }

    private void setupChatPopup() {
        View b = findViewById(R.id.btnOpenChat);
        if (b != null) {
            b.setOnClickListener(v -> showChatPopup());
        }
        if (inputChatMessage != null) {
            inputChatMessage.setOnEditorActionListener((v, a, e) -> {
                sendChatMessage();
                return true;
            });
        }
        if (btnChatOption != null) {
            btnChatOption.setOnClickListener(v -> sendChatMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (chatPopupBar != null && chatPopupBar.getVisibility() == View.VISIBLE) {
            hideChatPopup();
            return;
        }
        super.onBackPressed();
    }

    private void sendChatMessage() {
        if (inputChatMessage == null) {
            return;
        }
        String msg = inputChatMessage.getText().toString().trim();
        if (msg.isEmpty() || messagesRef == null) {
            return;
        }
        String myId = my9DigitId;
        if (myId == null || myId.isEmpty()) {
            myId = currentUserId;
        }
        if (myId == null || myId.isEmpty()) {
            myId = roomOwnerId;
        }
        final String finalId = myId;
        final String finalMsg = msg;
        userDatabaseRef.orderByChild("my9DigitId").equalTo(finalId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String realName = myDisplayName;
                String realPhoto = myPhotoBase64;
                String realCountry = myCountry;
                String realGender = myGender;
                String realLevel = "0";
                if (snap.exists()) {
                    for (DataSnapshot u : snap.getChildren()) {
                        String n = u.child("name").getValue(String.class);
                        if (n != null && !n.isEmpty()) {
                            realName = n;
                        }
                        String p = u.child("photoBase64").getValue(String.class);
                        if (p != null && !p.isEmpty()) {
                            realPhoto = p;
                        }
                        String c = u.child("country").getValue(String.class);
                        if (c != null && !c.isEmpty()) {
                            realCountry = c;
                        }
                        String g = u.child("gender").getValue(String.class);
                        if (g != null && !g.isEmpty()) {
                            realGender = g;
                        }
                        String lv = u.child("level").getValue(String.class);
                        if (lv != null && !lv.isEmpty()) {
                            realLevel = lv;
                        }
                        myDisplayName = realName;
                        myPhotoBase64 = realPhoto;
                        myCountry = realCountry;
                        myGender = realGender;
                        prefs.edit().putString("USER_NAME", realName).putString("USER_PHOTO_BASE64", realPhoto).putString("USER_COUNTRY", realCountry).apply();
                        break;
                    }
                }
                if (realName == null || realName.isEmpty() || realName.equals("User")) {
                    realName = prefs.getString("USER_NAME", "User");
                }
                Map<String, Object> m = new HashMap<>();
                m.put("userId", finalId);
                m.put("userName", realName);
                m.put("text", finalMsg);
                m.put("photoBase64", realPhoto != null ? realPhoto : "");
                m.put("gender", realGender);
                m.put("country", realCountry);
                m.put("level", realLevel);
                m.put("time", System.currentTimeMillis());
                messagesRef.push().setValue(m);
                hideChatPopup();
                pushOnlineNow();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Map<String, Object> m = new HashMap<>();
                m.put("userId", finalId);
                m.put("userName", myDisplayName);
                m.put("text", finalMsg);
                m.put("photoBase64", myPhotoBase64 != null ? myPhotoBase64 : "");
                m.put("gender", myGender);
                m.put("country", myCountry);
                m.put("level", "0");
                m.put("time", System.currentTimeMillis());
                messagesRef.push().setValue(m);
                hideChatPopup();
                pushOnlineNow();
            }
        });
    }

    private void listenToChatRealtime() {
        if (messagesRef == null || chatListContainer == null) {
            return;
        }
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnap) {
                chatRealMap.clear();
                for (DataSnapshot u : usersSnap.getChildren()) {
                    String mid = u.child("my9DigitId").getValue(String.class);
                    if (mid == null || mid.isEmpty()) {
                        mid = u.getKey();
                    }
                    Map<String, String> mm = new HashMap<>();
                    mm.put("name", u.child("name").getValue(String.class) != null ? u.child("name").getValue(String.class) : "");
                    mm.put("photo", u.child("photoBase64").getValue(String.class) != null ? u.child("photoBase64").getValue(String.class) : "");
                    mm.put("country", u.child("country").getValue(String.class) != null ? u.child("country").getValue(String.class) : "");
                    mm.put("level", u.child("level").getValue(String.class) != null ? u.child("level").getValue(String.class) : "0");
                    chatRealMap.put(mid, mm);
                }
                messagesRef.limitToLast(100).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        chatListContainer.removeAllViews();
                        if (!s.exists()) {
                            return;
                        }
                        for (DataSnapshot ds : s.getChildren()) {
                            String uid = ds.child("userId").getValue(String.class);
                            if (uid == null) {
                                uid = "";
                            }
                            String uname = ds.child("userName").getValue(String.class);
                            if (uname == null) {
                                uname = "User";
                            }
                            String txt = ds.child("text").getValue(String.class);
                            if (txt == null) {
                                txt = "";
                            }
                            String photo = ds.child("photoBase64").getValue(String.class);
                            if (photo == null) {
                                photo = "";
                            }
                            String co = ds.child("country").getValue(String.class);
                            if (co == null) {
                                co = "";
                            }
                            String lv = ds.child("level").getValue(String.class);
                            if (lv == null) {
                                lv = "0";
                            }
                            Map<String, String> real = chatRealMap.get(uid);
                            if (real != null) {
                                if (uname.equals("User") || uname.isEmpty()) {
                                    if (real.get("name") != null && !real.get("name").isEmpty()) {
                                        uname = real.get("name");
                                    }
                                }
                                if (photo.isEmpty() && real.get("photo") != null && !real.get("photo").isEmpty()) {
                                    photo = real.get("photo");
                                }
                                if ((co == null || co.isEmpty()) && real.get("country") != null) {
                                    co = real.get("country");
                                }
                            }
                            LinearLayout row = new LinearLayout(VoiceRoomActivity.this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setGravity(Gravity.TOP);
                            row.setPadding(4, 6, 4, 6);
                            CardView cardAv = makeAvatar(photo, uname, 72);
                            cardAv.setRadius(36);
                            row.addView(cardAv);
                            LinearLayout box = new LinearLayout(VoiceRoomActivity.this);
                            box.setOrientation(LinearLayout.VERTICAL);
                            box.setPadding(10, 0, 0, 0);
                            box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                            row.addView(box);
                            TextView tvN = makeText(uname + "  " + flagForCountry(co), 12, "#3E2F00", true, Gravity.START);
                            box.addView(tvN);
                            TextView tvL = makeText("Lv." + lv, 11, "#888888", false, Gravity.START);
                            box.addView(tvL);
                            TextView tvM = makeText(txt, 14, "#222222", false, Gravity.START);
                            box.addView(tvM);
                            chatListContainer.addView(row);
                        }
                        if (chatScrollView != null) {
                            chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void setupSeatInteractions() {
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            int res = getResources().getIdentifier("seat_" + idx, "id", getPackageName());
            View seat = findViewById(res);
            if (seat != null) {
                seat.setOnClickListener(v -> showSeatPopup(idx));
            }
        }
    }

    private void showSeatPopup(final int idx) {
        String myId = my9DigitId.isEmpty() ? currentUserId : my9DigitId;
        if (myId == null || myId.isEmpty()) {
            myId = roomOwnerId;
        }
        final String finalMyId = myId;
        DatabaseReference seatsRootRef = databaseReference.child("rooms").child(roomOwnerId).child("seats");
        seatsRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot allSeatsSnap) {
                String mySeatKey = null;
                for (DataSnapshot sSnap : allSeatsSnap.getChildren()) {
                    String u = sSnap.child("userId").getValue(String.class);
                    if (u != null && u.equals(finalMyId)) {
                        mySeatKey = sSnap.getKey();
                        break;
                    }
                }
                final String existing = mySeatKey;
                DataSnapshot targetSnap = allSeatsSnap.child("seat_" + idx);
                boolean isEmpty = !targetSnap.exists();
                String seatUserId = isEmpty ? null : targetSnap.child("userId").getValue(String.class);
                String seatUserName = isEmpty ? null : targetSnap.child("userName").getValue(String.class);
                boolean isMine = seatUserId != null && (seatUserId.equals(currentUserId) || seatUserId.equals(my9DigitId) || seatUserId.equals(finalMyId));
                boolean admin = isAdminOrOwner();
                boolean locked = lockedSeats.contains("seat_" + idx);
                if (isEmpty) {
                    if (locked && !admin) {
                        Toast.makeText(VoiceRoomActivity.this, "Ei seat lock kora 🔒", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!admin) {
                        BottomSheetDialog dMember = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                        LinearLayout rMember = new LinearLayout(VoiceRoomActivity.this);
                        rMember.setOrientation(LinearLayout.VERTICAL);
                        rMember.setPadding(28, 24, 28, 28);
                        rMember.setBackgroundColor(Color.WHITE);
                        TextView ttMember = makeText("Seat " + (idx + 1), 18, "#222222", true, Gravity.CENTER);
                        ttMember.setPadding(0, 0, 0, 16);
                        rMember.addView(ttMember);
                        Button btnTake = makeColorBtn("🎤 Take Mic", Color.parseColor("#FF9800"));
                        btnTake.setOnClickListener(v -> {
                            dMember.dismiss();
                            takeSeat(idx, existing);
                        });
                        rMember.addView(btnTake);
                        Button btnCancel = new Button(VoiceRoomActivity.this);
                        btnCancel.setText("Cancel");
                        btnCancel.setAllCaps(false);
                        btnCancel.setTextColor(Color.parseColor("#666666"));
                        GradientDrawable gCancel = new GradientDrawable();
                        gCancel.setColor(Color.parseColor("#EEEEEE"));
                        gCancel.setCornerRadius(60);
                        btnCancel.setBackground(gCancel);
                        btnCancel.setOnClickListener(v -> dMember.dismiss());
                        rMember.addView(btnCancel);
                        dMember.setContentView(rMember);
                        setDialog75(dMember);
                        dMember.show();
                        return;
                    }
                    BottomSheetDialog d1 = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                    LinearLayout r1 = new LinearLayout(VoiceRoomActivity.this);
                    r1.setOrientation(LinearLayout.VERTICAL);
                    r1.setPadding(28, 24, 28, 28);
                    r1.setBackgroundColor(Color.WHITE);
                    TextView tt1 = makeText("Seat " + (idx + 1), 18, "#222222", true, Gravity.CENTER);
                    tt1.setPadding(0, 0, 0, 16);
                    r1.addView(tt1);
                    String[] opts = {"🎤 Take Mic", "📩 Invite", locked ? "🔓 Unlock" : "🔒 Lock"};
                    int[] colors = {Color.parseColor("#FF9800"), Color.parseColor("#7B1FA2"), Color.parseColor("#222222")};
                    for (int ii = 0; ii < opts.length; ii++) {
                        String o = opts[ii];
                        Button btn = makeColorBtn(o, colors[ii]);
                        btn.setOnClickListener(v -> {
                            d1.dismiss();
                            if (o.contains("Take Mic")) {
                                takeSeat(idx, existing);
                            } else if (o.contains("Lock")) {
                                databaseReference.child("rooms").child(roomOwnerId).child("locked_seats").child("seat_" + idx).setValue(true);
                                Toast.makeText(VoiceRoomActivity.this, "Seat Locked 🔒", Toast.LENGTH_SHORT).show();
                            } else if (o.contains("Unlock")) {
                                databaseReference.child("rooms").child(roomOwnerId).child("locked_seats").child("seat_" + idx).removeValue();
                                Toast.makeText(VoiceRoomActivity.this, "Seat Unlocked 🔓", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VoiceRoomActivity.this, o + " - Coming Soon", Toast.LENGTH_SHORT).show();
                            }
                        });
                        r1.addView(btn);
                    }
                    d1.setContentView(r1);
                    setDialog75(d1);
                    d1.show();
                    return;
                }
                if (isMine) {
                    BottomSheetDialog d2 = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                    LinearLayout r2 = new LinearLayout(VoiceRoomActivity.this);
                    r2.setOrientation(LinearLayout.VERTICAL);
                    r2.setPadding(28, 24, 28, 28);
                    r2.setBackgroundColor(Color.WHITE);
                    String[] opts2 = {"🚪 Leave the Mic", "🔇 Mute", "👤 Profile"};
                    int[] colors2 = {Color.parseColor("#FF5252"), Color.parseColor("#222222"), Color.parseColor("#FF9800")};
                    for (int ii = 0; ii < opts2.length; ii++) {
                        String o = opts2[ii];
                        Button btn = makeColorBtn(o, colors2[ii]);
                        btn.setOnClickListener(v -> {
                            d2.dismiss();
                            if (o.contains("Leave")) {
                                databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_" + idx).removeValue();
                                Toast.makeText(VoiceRoomActivity.this, "Seat theke uthe gechen 🪑", Toast.LENGTH_SHORT).show();
                            } else if (o.contains("Profile")) {
                                showUserProfileCard(finalMyId);
                            } else {
                                Toast.makeText(VoiceRoomActivity.this, "Mute - Coming Soon", Toast.LENGTH_SHORT).show();
                            }
                        });
                        r2.addView(btn);
                    }
                    d2.setContentView(r2);
                    setDialog75(d2);
                    d2.show();
                    return;
                }
                BottomSheetDialog d3 = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                LinearLayout r3 = new LinearLayout(VoiceRoomActivity.this);
                r3.setOrientation(LinearLayout.VERTICAL);
                r3.setPadding(28, 24, 28, 28);
                r3.setBackgroundColor(Color.WHITE);
                TextView tt3 = makeText(seatUserName != null ? seatUserName : "User", 18, "#222222", true, Gravity.CENTER);
                tt3.setPadding(0, 0, 0, 8);
                r3.addView(tt3);
                String[] otherOpts = {"Let him leave the mic", "Turn off Sound", "Mute", "Kick out of room", "Blacklist", "Go to chat", "Profile", "Set Management"};
                for (String o : otherOpts) {
                    TextView opt = makeText(o, 16, "#222222", false, Gravity.CENTER);
                    opt.setPadding(0, 26, 0, 26);
                    opt.setOnClickListener(v -> {
                        d3.dismiss();
                        if (o.equals("Profile")) {
                            showUserProfileCard(seatUserId);
                        } else if (o.equals("Kick out of room") || o.equals("Let him leave the mic")) {
                            if (admin) {
                                databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_" + idx).removeValue();
                                Toast.makeText(VoiceRoomActivity.this, "Removed from mic", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VoiceRoomActivity.this, "Only Owner / Admin", Toast.LENGTH_SHORT).show();
                            }
                        } else if (o.equals("Set Management")) {
                            if (admin) {
                                showIconToolsDialog(true);
                            } else {
                                Toast.makeText(VoiceRoomActivity.this, "Only Owner / Admin", Toast.LENGTH_SHORT).show();
                            }
                        } else if (o.equals("Go to chat")) {
                            showChatPopup();
                        } else {
                            Toast.makeText(VoiceRoomActivity.this, o + " - Coming Soon", Toast.LENGTH_SHORT).show();
                        }
                    });
                    r3.addView(opt);
                    View line = new View(VoiceRoomActivity.this);
                    line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    line.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    r3.addView(line);
                }
                d3.setContentView(r3);
                setDialog85(d3);
                d3.show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void takeSeat(int idx, String existingSeat) {
        String myId = my9DigitId.isEmpty() ? currentUserId : my9DigitId;
        if (myId == null || myId.isEmpty()) {
            myId = roomOwnerId;
        }
        final String finalMyId = myId;
        final String finalName = (myDisplayName != null && !myDisplayName.isEmpty()) ? myDisplayName : "User";
        final String finalPhoto = (myPhotoBase64 != null) ? myPhotoBase64 : "";
        if (existingSeat != null && !existingSeat.equals("seat_" + idx)) {
            databaseReference.child("rooms").child(roomOwnerId).child("seats").child(existingSeat).removeValue();
        }
        DatabaseReference seatRef = databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_" + idx);
        Map<String, Object> m = new HashMap<>();
        m.put("userId", finalMyId);
        m.put("userName", finalName);
        m.put("photoBase64", finalPhoto);
        seatRef.setValue(m);
        seatRef.onDisconnect().removeValue();
        Toast.makeText(VoiceRoomActivity.this, "Seat " + (idx + 1) + " e bosechen ✅", Toast.LENGTH_SHORT).show();
    }

    private void removeMySeat() {
        String myId = my9DigitId.isEmpty() ? currentUserId : my9DigitId;
        if (myId == null || myId.isEmpty()) {
            myId = roomOwnerId;
        }
        final String finalMyId = myId;
        if (databaseReference == null) {
            return;
        }
        databaseReference.child("rooms").child(roomOwnerId).child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                for (DataSnapshot s : snap.getChildren()) {
                    String u = s.child("userId").getValue(String.class);
                    if (u != null && u.equals(finalMyId)) {
                        databaseReference.child("rooms").child(roomOwnerId).child("seats").child(s.getKey()).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void listenToFirebaseSeats() {
        if (databaseReference == null) {
            return;
        }
        databaseReference.child("rooms").child(roomOwnerId).child("seats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                for (int i = 0; i < 10; i++) {
                    View imgView = findViewById(getResources().getIdentifier("seatImg_" + i, "id", getPackageName()));
                    DataSnapshot ss = s.child("seat_" + i);
                    View sl = findViewById(getResources().getIdentifier("seat_" + i, "id", getPackageName()));
                    TextView nameTv = null;
                    if (sl instanceof LinearLayout) {
                        LinearLayout ll = (LinearLayout) sl;
                        if (ll.getChildCount() >= 2 && ll.getChildAt(1) instanceof TextView) {
                            nameTv = (TextView) ll.getChildAt(1);
                        }
                    }
                    if (ss.exists()) {
                        Bitmap bmp = decodeBase64(ss.child("photoBase64").getValue(String.class));
                        String nm = ss.child("userName").getValue(String.class);
                        if (imgView instanceof TextView) {
                            TextView tv = (TextView) imgView;
                            if (bmp != null) {
                                Bitmap roundBmp = getCircularBitmap(bmp);
                                tv.setText("");
                                tv.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(), roundBmp));
                            } else {
                                tv.setText("👤");
                                tv.setBackgroundResource(R.drawable.circle_seat_bg);
                            }
                        }
                        if (nameTv != null) {
                            nameTv.setText(nm != null ? nm : "");
                        }
                    } else {
                        if (imgView instanceof TextView) {
                            TextView tv = (TextView) imgView;
                            if (lockedSeats.contains("seat_" + i)) {
                                tv.setText("🔒");
                            } else {
                                tv.setText("🪑");
                            }
                            tv.setBackgroundResource(R.drawable.circle_seat_bg);
                        }
                        if (nameTv != null) {
                            if (i == 0) {
                                nameTv.setText("Host");
                            } else {
                                nameTv.setText("");
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private Bitmap getCircularBitmap(Bitmap src) {
        if (src == null) {
            return null;
        }
        int size = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - size) / 2;
        int y = (src.getHeight() - size) / 2;
        Bitmap squared = Bitmap.createBitmap(src, x, y, size, size);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(squared, 0, 0, paint);
        return output;
    }

    private void showUserProfileCard(String targetId) {
        if (targetId == null || targetId.isEmpty()) {
            return;
        }
        final String fTargetId = targetId;
        userDatabaseRef.orderByChild("my9DigitId").equalTo(fTargetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String name = myDisplayName;
                String photo = myPhotoBase64;
                String country = myCountry;
                String gender = myGender;
                String levelSend = "0";
                String levelRecv = "0";
                String agency = "";
                boolean isOwner = fTargetId.equals(roomOwnerId);
                if (snap.exists()) {
                    for (DataSnapshot u : snap.getChildren()) {
                        if (u.child("name").getValue() != null) {
                            name = String.valueOf(u.child("name").getValue());
                        }
                        if (u.child("photoBase64").getValue() != null) {
                            photo = String.valueOf(u.child("photoBase64").getValue());
                        }
                        if (u.child("country").getValue() != null) {
                            country = String.valueOf(u.child("country").getValue());
                        }
                        if (u.child("gender").getValue() != null) {
                            gender = String.valueOf(u.child("gender").getValue());
                        }
                        if (u.child("level").getValue() != null) {
                            levelSend = String.valueOf(u.child("level").getValue());
                        }
                        if (u.child("levelRecv").getValue() != null) {
                            levelRecv = String.valueOf(u.child("levelRecv").getValue());
                        }
                        if (u.child("levelSend").getValue() != null) {
                            levelSend = String.valueOf(u.child("levelSend").getValue());
                        }
                        if (u.child("agency").getValue() != null) {
                            agency = String.valueOf(u.child("agency").getValue());
                        }
                        if (u.child("agencyName").getValue() != null) {
                            agency = String.valueOf(u.child("agencyName").getValue());
                        }
                        break;
                    }
                    showUserProfileCardUI(fTargetId, name, photo, country, gender, levelSend, levelRecv, agency, isOwner);
                } else {
                    showUserProfileCardUI(fTargetId, name, photo, country, gender, levelSend, levelRecv, agency, isOwner);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                showUserProfileCardUI(fTargetId, myDisplayName, myPhotoBase64, myCountry, myGender, "0", "0", "", fTargetId.equals(roomOwnerId));
            }
        });
    }

    private void showUserProfileCardUI(String targetId, String name, String photo, String country, String gender, String levelSend, String levelRecv, String agency, boolean isOwner) {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 16, 28, 28);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView close = makeText("✕", 20, "#999999", false, Gravity.END);
        close.setOnClickListener(v -> d.dismiss());
        root.addView(close);
        CardView card = makeAvatar(photo, name, 220);
        card.setRadius(110);
        card.setCardElevation(8);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(220, 220);
        clp.gravity = Gravity.CENTER_HORIZONTAL;
        clp.setMargins(0, 6, 0, 8);
        card.setLayoutParams(clp);
        root.addView(card);
        if (isOwner) {
            TextView badge = makeText("👑 OWNER", 12, "#FFFFFF", true, Gravity.CENTER);
            badge.setPadding(22, 8, 22, 8);
            GradientDrawable g = new GradientDrawable();
            g.setColor(Color.parseColor("#FF9800"));
            g.setCornerRadius(30);
            badge.setBackground(g);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.gravity = Gravity.CENTER_HORIZONTAL;
            blp.setMargins(0, 0, 0, 8);
            badge.setLayoutParams(blp);
            root.addView(badge);
        } else if (targetId != null && roomAdminList.contains(targetId)) {
            TextView badge = makeText("🛡️ ADMIN", 12, "#FFFFFF", true, Gravity.CENTER);
            badge.setPadding(22, 8, 22, 8);
            GradientDrawable g = new GradientDrawable();
            g.setColor(Color.parseColor("#7B1FA2"));
            g.setCornerRadius(30);
            badge.setBackground(g);
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blp.gravity = Gravity.CENTER_HORIZONTAL;
            blp.setMargins(0, 0, 0, 8);
            badge.setLayoutParams(blp);
            root.addView(badge);
        }
        TextView tn = makeText((name != null ? name : "User") + " " + genderIcon(gender) + " " + flagForCountry(country), 20, "#222222", true, Gravity.CENTER);
        tn.setPadding(0, 4, 0, 2);
        root.addView(tn);
        TextView tid = makeText("ID: " + targetId, 14, "#888888", false, Gravity.CENTER);
        tid.setPadding(0, 0, 0, 10);
        root.addView(tid);
        LinearLayout lvRow = new LinearLayout(this);
        lvRow.setOrientation(LinearLayout.HORIZONTAL);
        lvRow.setGravity(Gravity.CENTER);
        lvRow.setWeightSum(2);
        lvRow.setPadding(0, 6, 0, 6);
        TextView lv1 = makeText("🎁 Send Lv." + levelSend, 14, "#B8860B", true, Gravity.CENTER);
        lv1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        lvRow.addView(lv1);
        TextView lv2 = makeText("💎 Recv Lv." + levelRecv, 14, "#7B1FA2", true, Gravity.CENTER);
        lv2.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        lvRow.addView(lv2);
        root.addView(lvRow);
        TextView ag = makeText(agency != null && !agency.isEmpty() ? "🏢 Agency: " + agency : "🏢 No Agency", 14, agency != null && !agency.isEmpty() ? "#222222" : "#AAAAAA", true, Gravity.CENTER);
        ag.setPadding(0, 6, 0, 6);
        root.addView(ag);
        TextView gift = makeText("🎁  Gift", 28, "#FF9800", false, Gravity.CENTER);
        gift.setPadding(0, 18, 0, 8);
        gift.setOnClickListener(v -> {
            d.dismiss();
            showNewGiftPanel();
        });
        root.addView(gift);
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setWeightSum(2);
        btnRow.setPadding(0, 14, 0, 0);
        Button addF = new Button(this);
        addF.setText("➕ Add Friend");
        addF.setAllCaps(false);
        addF.setTextColor(Color.parseColor("#222222"));
        GradientDrawable g1 = new GradientDrawable();
        g1.setColor(Color.parseColor("#EEEEEE"));
        g1.setCornerRadius(60);
        addF.setBackground(g1);
        addF.setLayoutParams(new LinearLayout.LayoutParams(0, 120, 1f));
        addF.setOnClickListener(v -> Toast.makeText(this, "Friend Request Sent ✅", Toast.LENGTH_SHORT).show());
        btnRow.addView(addF);
        Button follow = makeColorBtn("⭐ Follow", Color.parseColor("#FF9800"));
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(0, 120, 1f);
        flp.setMargins(16, 0, 0, 0);
        follow.setLayoutParams(flp);
        follow.setOnClickListener(v -> Toast.makeText(this, "Followed ✅", Toast.LENGTH_SHORT).show());
        btnRow.addView(follow);
        root.addView(btnRow);
        d.setContentView(root);
        setDialog85(d);
        d.show();
    }

    private void listenToRoomDetails() {
        if (roomMetaRef == null) {
            return;
        }
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                String rn = s.child("roomName").getValue(String.class);
                if (rn != null && !rn.isEmpty() && roomTitleText != null) {
                    roomTitleText.setText(rn);
                }
                if (roomInfoText != null) {
                    roomInfoText.setText("ID: " + roomOwnerId);
                }
                String ann = s.child("announcement").getValue(String.class);
                if (roomNoticeBarText != null) {
                    if (ann == null || ann.isEmpty() || ann.equals("wlc")) {
                        roomNoticeBarText.setVisibility(View.GONE);
                        roomNoticeBarText.setText("");
                    } else {
                        roomNoticeBarText.setVisibility(View.VISIBLE);
                        roomNoticeBarText.setText(ann);
                    }
                }
                String lb = s.child("roomLabel").getValue(String.class);
                if (lb != null && !lb.isEmpty()) {
                    selectedRoomLabel = lb;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void listenToRoomPhoto() {
        if (roomMetaRef == null) {
            return;
        }
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                String rp = s.child("roomPhotoBase64").getValue(String.class);
                if (rp != null && !rp.isEmpty()) {
                    currentRoomPhotoBase64 = rp;
                    Bitmap bmp = decodeBase64(rp);
                    if (bmp != null && roomHeaderProfileImg != null) {
                        roomHeaderProfileImg.setImageBitmap(bmp);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void listenToRoomAdmins() {
        if (adminsRef == null) {
            return;
        }
        adminsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                roomAdminList.clear();
                for (DataSnapshot ds : s.getChildren()) {
                    if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) {
                        roomAdminList.add(ds.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void listenToMyWallet() {
        if (currentUserId.isEmpty()) {
            return;
        }
        userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                Long c = s.child("coins").getValue(Long.class);
                long w = c != null ? c : 0L;
                myWalletCoins = w;
                if (walletTextInGift != null) {
                    walletTextInGift.setText("Coins: " + String.format("%,d", w));
                }
                if (textRoomCoins != null) {
                    textRoomCoins.setText("🪙 " + String.format("%,d", w));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void showOnlineListDialog() {
        android.app.Dialog d = makeFullScreenDialog();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(24, 20, 24, 24);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top);
        TextView t = makeText("Online list", 19, "#222222", true, Gravity.START);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(t);
        TextView cl = makeText("✕", 22, "#888888", false, Gravity.CENTER);
        cl.setPadding(10, 10, 10, 10);
        cl.setOnClickListener(v -> d.dismiss());
        top.addView(cl);
        TextView cnt = makeText("", 13, "#888888", false, Gravity.START);
        cnt.setPadding(0, 4, 0, 10);
        root.addView(cnt);
        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        root.addView(sv);
        pushOnlineNow();
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnap) {
                Map<String, Map<String, String>> realMap = new HashMap<>();
                for (DataSnapshot u : usersSnap.getChildren()) {
                    String mid = u.child("my9DigitId").getValue(String.class);
                    if (mid == null || mid.isEmpty()) {
                        mid = u.getKey();
                    }
                    Map<String, String> m = new HashMap<>();
                    m.put("name", u.child("name").getValue(String.class) != null ? u.child("name").getValue(String.class) : "");
                    m.put("photo", u.child("photoBase64").getValue(String.class) != null ? u.child("photoBase64").getValue(String.class) : "");
                    m.put("country", u.child("country").getValue(String.class) != null ? u.child("country").getValue(String.class) : "");
                    m.put("level", u.child("level").getValue(String.class) != null ? u.child("level").getValue(String.class) : "0");
                    realMap.put(mid, m);
                }
                databaseReference.child("rooms").child(roomOwnerId).child("online_users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        list.removeAllViews();
                        if (!s.exists()) {
                            cnt.setText("0 online");
                            return;
                        }
                        cnt.setText(s.getChildrenCount() + " online");
                        List<DataSnapshot> all = new ArrayList<>();
                        for (DataSnapshot ds : s.getChildren()) {
                            all.add(ds);
                        }
                        all.sort((a, b) -> {
                            if (a.getKey().equals(roomOwnerId)) {
                                return -1;
                            }
                            if (b.getKey().equals(roomOwnerId)) {
                                return 1;
                            }
                            return 0;
                        });
                        for (DataSnapshot ds : all) {
                            String k = ds.getKey();
                            if (k == null) {
                                continue;
                            }
                            Map<String, String> real = realMap.get(k);
                            String n = ds.child("name").getValue(String.class);
                            String p = ds.child("photoBase64").getValue(String.class);
                            String co = ds.child("country").getValue(String.class);
                            String lv = ds.child("level").getValue(String.class);
                            if (real != null) {
                                if (real.get("name") != null && !real.get("name").isEmpty()) {
                                    n = real.get("name");
                                }
                                if (real.get("photo") != null && !real.get("photo").isEmpty()) {
                                    p = real.get("photo");
                                }
                                if (real.get("country") != null && !real.get("country").isEmpty()) {
                                    co = real.get("country");
                                }
                                if (real.get("level") != null && !real.get("level").isEmpty()) {
                                    lv = real.get("level");
                                }
                            }
                            addOnlineRow(list, k, n != null ? n : "User", p != null ? p : "", lv != null ? lv : "0", co != null ? co : "", d);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
        d.setContentView(root);
        d.show();
    }

    private void addOnlineRow(LinearLayout list, String uid, String name, String photo, String level, String country, android.app.Dialog dialog) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(6, 14, 6, 14);
        row.setOnClickListener(v -> {
            dialog.dismiss();
            showUserProfileCard(uid);
        });
        CardView cardAv = makeAvatar(photo, name, 110);
        cardAv.setRadius(55);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(110, 110);
        cardLp.setMargins(0, 0, 12, 0);
        cardAv.setLayoutParams(cardLp);
        row.addView(cardAv);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(16, 0, 0, 0);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(box);
        TextView tn = makeText(name + "  " + flagForCountry(country), 16, "#222222", true, Gravity.START);
        box.addView(tn);
        TextView ti = makeText("Lv." + level, 13, "#888888", false, Gravity.START);
        box.addView(ti);
        if (uid.equals(roomOwnerId)) {
            TextView badge = makeText("Owner", 11, "#FFFFFF", true, Gravity.CENTER);
            badge.setPadding(14, 6, 14, 6);
            GradientDrawable g = new GradientDrawable();
            g.setColor(Color.parseColor("#FF9800"));
            g.setCornerRadius(30);
            badge.setBackground(g);
            row.addView(badge);
        }
        list.addView(row);
    }

    private void showRoomHeaderProfile() {
        android.app.Dialog dialog = makeFullScreenDialog();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabRow.setGravity(Gravity.CENTER);
        tabRow.setPadding(0, 16, 0, 0);
        root.addView(tabRow);
        TextView tabProfile = makeText("Profile", 17, "#222222", true, Gravity.CENTER);
        tabProfile.setPadding(30, 10, 30, 10);
        tabRow.addView(tabProfile);
        TextView tabMembers = makeText("Members", 17, "#999999", false, Gravity.CENTER);
        tabMembers.setPadding(30, 10, 30, 10);
        tabRow.addView(tabMembers);
        TextView cb = makeText("✕", 20, "#999999", false, Gravity.CENTER);
        cb.setPadding(20, 10, 20, 10);
        cb.setOnClickListener(v -> dialog.dismiss());
        tabRow.addView(cb);
        ScrollView svProfile = new ScrollView(this);
        LinearLayout pb = new LinearLayout(this);
        pb.setOrientation(LinearLayout.VERTICAL);
        pb.setPadding(20, 10, 20, 20);
        svProfile.addView(pb);
        root.addView(svProfile);
        ScrollView svMembers = new ScrollView(this);
        LinearLayout mb = new LinearLayout(this);
        mb.setOrientation(LinearLayout.VERTICAL);
        mb.setPadding(20, 10, 20, 20);
        svMembers.addView(mb);
        svMembers.setVisibility(View.GONE);
        root.addView(svMembers);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(Color.parseColor("#FFFDE7"));
        top.setPadding(20, 16, 20, 16);
        pb.addView(top);
        CardView cardRp = new CardView(this);
        LinearLayout.LayoutParams cardRpLp = new LinearLayout.LayoutParams(130, 130);
        cardRpLp.setMargins(0, 0, 12, 0);
        cardRp.setLayoutParams(cardRpLp);
        cardRp.setRadius(65);
        cardRp.setCardElevation(0);
        cardRp.setCardBackgroundColor(Color.TRANSPARENT);
        ImageView rp = new ImageView(this);
        rp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rp.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rp.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap br = decodeBase64(currentRoomPhotoBase64);
        if (br != null) {
            rp.setImageBitmap(br);
        }
        cardRp.addView(rp);
        top.addView(cardRp);
        LinearLayout nb = new LinearLayout(this);
        nb.setOrientation(LinearLayout.VERTICAL);
        nb.setPadding(16, 0, 0, 0);
        nb.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(nb);
        String rn = roomTitleText != null ? roomTitleText.getText().toString() : "Sr Official's Room";
        TextView trn = makeText(rn, 19, "#222222", true, Gravity.START);
        nb.addView(trn);
        TextView tri = makeText("ID: " + roomOwnerId, 13, "#888888", false, Gravity.START);
        nb.addView(tri);
        TextView tvFollowing = makeText("1 Following    1 Fans    104 Visitor", 13, "#666666", false, Gravity.START);
        tvFollowing.setPadding(0, 14, 0, 6);
        pb.addView(tvFollowing);
        LinearLayout banner = new LinearLayout(this);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER);
        banner.setPadding(20, 18, 20, 18);
        GradientDrawable bannerBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.parseColor("#FF9800"), Color.parseColor("#FF5252")});
        bannerBg.setCornerRadius(24);
        banner.setBackground(bannerBg);
        TextView bannerTxt = makeText("🏆  Reward Event", 17, "#FFFFFF", true, Gravity.CENTER);
        banner.addView(bannerTxt);
        pb.addView(banner);
        TextView tvLevel = makeText("Level", 15, "#222222", true, Gravity.START);
        tvLevel.setPadding(0, 18, 0, 6);
        pb.addView(tvLevel);
        TextView tvLevelVal = makeText("Lv.0", 14, "#FF9800", true, Gravity.START);
        tvLevelVal.setPadding(0, 0, 0, 10);
        pb.addView(tvLevelVal);
        TextView tvOwner = makeText("RoomOwner", 15, "#222222", true, Gravity.START);
        tvOwner.setPadding(0, 6, 0, 2);
        pb.addView(tvOwner);
        TextView tvOwnerVal = makeText("Loading... (ID:" + roomOwnerId + ")", 14, "#666666", false, Gravity.START);
        tvOwnerVal.setPadding(0, 0, 0, 10);
        pb.addView(tvOwnerVal);
        TextView tvLang = makeText("Language", 15, "#222222", true, Gravity.START);
        tvLang.setPadding(0, 6, 0, 2);
        pb.addView(tvLang);
        TextView tvLangVal = makeText("English", 14, "#222222", false, Gravity.START);
        tvLangVal.setPadding(0, 0, 0, 10);
        pb.addView(tvLangVal);
        TextView tvCountry = makeText("Country", 15, "#222222", true, Gravity.START);
        tvCountry.setPadding(0, 6, 0, 2);
        pb.addView(tvCountry);
        TextView tvCountryVal = makeText("Bangladesh", 14, "#222222", false, Gravity.START);
        tvCountryVal.setPadding(0, 0, 0, 10);
        pb.addView(tvCountryVal);
        TextView tvTag = makeText("Tag", 15, "#222222", true, Gravity.START);
        tvTag.setPadding(0, 6, 0, 2);
        pb.addView(tvTag);
        TextView tvTagVal = makeText("Friends", 14, "#222222", false, Gravity.START);
        tvTagVal.setPadding(0, 0, 0, 10);
        pb.addView(tvTagVal);
        loadRoomOwnerProfileForHeader(tvFollowing, tvLevelVal, tvOwnerVal, tvLangVal, tvCountryVal, tvTagVal);
        TextView countTv = makeText("Members: 1/5000    Admin: 1/20", 14, "#222222", true, Gravity.START);
        mb.addView(countTv);
        EditText searchEt = new EditText(this);
        searchEt.setHint("Search ID");
        searchEt.setTextSize(14);
        searchEt.setPadding(24, 20, 24, 20);
        searchEt.setBackgroundColor(Color.parseColor("#F5F5F5"));
        mb.addView(searchEt);
        LinearLayout memberList = new LinearLayout(this);
        memberList.setOrientation(LinearLayout.VERTICAL);
        mb.addView(memberList);
        tabProfile.setOnClickListener(v -> {
            tabProfile.setTextColor(Color.parseColor("#222222"));
            tabMembers.setTextColor(Color.parseColor("#999999"));
            svProfile.setVisibility(View.VISIBLE);
            svMembers.setVisibility(View.GONE);
        });
        tabMembers.setOnClickListener(v -> {
            tabMembers.setTextColor(Color.parseColor("#222222"));
            tabProfile.setTextColor(Color.parseColor("#999999"));
            svProfile.setVisibility(View.GONE);
            svMembers.setVisibility(View.VISIBLE);
        });
        loadHeaderProfileData(countTv, memberList, searchEt);
        dialog.setContentView(root);
        dialog.show();
    }

    private void loadRoomOwnerProfileForHeader(TextView tvFollowing, TextView tvLevelVal, TextView tvOwnerVal, TextView tvLangVal, TextView tvCountryVal, TextView tvTagVal) {
        if (userDatabaseRef == null) {
            return;
        }
        userDatabaseRef.orderByChild("my9DigitId").equalTo(roomOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    return;
                }
                for (DataSnapshot u : snap.getChildren()) {
                    String n = u.child("name").getValue(String.class);
                    String lang = u.child("language").getValue(String.class);
                    String country = u.child("country").getValue(String.class);
                    String tag = u.child("tag").getValue(String.class);
                    String level = u.child("level").getValue(String.class);
                    String following = u.child("followingCount").getValue(String.class);
                    String fans = u.child("fansCount").getValue(String.class);
                    String visitor = u.child("visitorCount").getValue(String.class);
                    if (n != null && !n.isEmpty()) {
                        tvOwnerVal.setText(n + "   (ID:" + roomOwnerId + ")");
                    }
                    if (lang != null && !lang.isEmpty()) {
                        tvLangVal.setText(lang);
                    }
                    if (country != null && !country.isEmpty()) {
                        tvCountryVal.setText(country);
                    }
                    if (tag != null && !tag.isEmpty()) {
                        tvTagVal.setText(tag);
                    }
                    if (level != null && !level.isEmpty()) {
                        tvLevelVal.setText("Lv." + level);
                    }
                    String f = following != null ? following : "1";
                    String fa = fans != null ? fans : "1";
                    String vi = visitor != null ? visitor : "104";
                    tvFollowing.setText(f + " Following    " + fa + " Fans    " + vi + " Visitor");
                    break;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void loadHeaderProfileData(TextView countTv, LinearLayout memberList, EditText searchEt) {
        if (databaseReference == null) {
            return;
        }
        databaseReference.child("rooms").child(roomOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot roomSnap) {
                long memberCount = roomSnap.child("members").exists() ? roomSnap.child("members").getChildrenCount() : 0;
                long onlineCount = roomSnap.child("online_users").exists() ? roomSnap.child("online_users").getChildrenCount() : 0;
                long adminCount = roomSnap.child("admins").exists() ? roomSnap.child("admins").getChildrenCount() : 1;
                if (memberCount == 0) {
                    memberCount = Math.max(1, onlineCount);
                }
                if (countTv != null) {
                    countTv.setText("Members: " + memberCount + "/5000    Admin: " + adminCount + "/20");
                }
                Map<String, Map<String, String>> all = new LinkedHashMap<>();
                for (DataSnapshot ds : roomSnap.child("members").getChildren()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("name", ds.child("userName").getValue(String.class) != null ? ds.child("userName").getValue(String.class) : "User");
                    m.put("photo", ds.child("photoBase64").getValue(String.class) != null ? ds.child("photoBase64").getValue(String.class) : "");
                    all.put(ds.getKey(), m);
                }
                for (DataSnapshot ds : roomSnap.child("online_users").getChildren()) {
                    if (!all.containsKey(ds.getKey())) {
                        Map<String, String> m = new HashMap<>();
                        m.put("name", ds.child("name").getValue(String.class) != null ? ds.child("name").getValue(String.class) : "User");
                        m.put("photo", ds.child("photoBase64").getValue(String.class) != null ? ds.child("photoBase64").getValue(String.class) : "");
                        all.put(ds.getKey(), m);
                    }
                }
                final Map<String, Map<String, String>> finalAll = all;
                Runnable render = () -> {
                    memberList.removeAllViews();
                    String q = searchEt.getText().toString().trim();
                    for (Map.Entry<String, Map<String, String>> e : finalAll.entrySet()) {
                        if (q.isEmpty() || e.getKey().contains(q)) {
                            addMemberRow(memberList, e.getKey(), e.getValue().get("name"), e.getValue().get("photo"), e.getKey().equals(roomOwnerId) ? "Owner" : "");
                        }
                    }
                };
                render.run();
                searchEt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int st, int c, int a) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int st, int b, int c) {
                        render.run();
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void addMemberRow(LinearLayout list, String id, String name, String photo, String badge) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(6, 10, 6, 10);
        row.setOnClickListener(v -> showUserProfileCard(id));
        CardView card = makeAvatar(photo, name, 90);
        card.setRadius(45);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(90, 90);
        lp.setMargins(0, 0, 12, 0);
        card.setLayoutParams(lp);
        row.addView(card);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(box);
        TextView tn = makeText(name, 15, "#222222", true, Gravity.START);
        box.addView(tn);
        TextView ti = makeText("ID: " + id, 12, "#888888", false, Gravity.START);
        box.addView(ti);
        if (badge != null && !badge.isEmpty()) {
            TextView tb = makeText(badge, 11, "#FFFFFF", true, Gravity.CENTER);
            tb.setPadding(14, 6, 14, 6);
            GradientDrawable g = new GradientDrawable();
            g.setColor(Color.parseColor("#FF9800"));
            g.setCornerRadius(30);
            tb.setBackground(g);
            row.addView(tb);
        }
        list.addView(row);
    }

    private void showExitDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Exit Room?");
        b.setMessage("Exit room or Minimize?");
        b.setPositiveButton("Exit", (d, w) -> {
            d.dismiss();
            leaveRoomAndFinish();
        });
        b.setNeutralButton("Minimize", (d, w) -> {
            d.dismiss();
            leaveRoomKeepHome();
            Intent home = new Intent(VoiceRoomActivity.this, MainActivity.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            finish();
        });
        b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        b.show();
    }

    private void leaveRoomKeepHome() {
        // MINIMIZE - ONLINE + SEAT 2 TAI THAKBE - KICHU DELETE KORBO NA
    }

    private void leaveRoomAndFinish() {
        String k = my9DigitId.isEmpty() ? currentUserId : my9DigitId;
        if (k != null && !k.isEmpty()) {
            databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(k).removeValue();
        }
        removeMySeat();
        Intent i = new Intent(VoiceRoomActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            String k = my9DigitId.isEmpty() ? currentUserId : my9DigitId;
            if (k != null && !k.isEmpty() && databaseReference != null) {
                databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(k).removeValue();
            }
            removeMySeat();
        }
    }

    private void showNewGiftPanel() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(24, 20, 24, 24);
        r.setBackgroundColor(Color.WHITE);
        TextView t = makeText("🎁 Gift Box", 18, "#222222", true, Gravity.CENTER);
        r.addView(t);
        walletTextInGift = makeText("Coins: " + myWalletCoins, 14, "#B8860B", false, Gravity.CENTER);
        walletTextInGift.setPadding(0, 8, 0, 16);
        r.addView(walletTextInGift);
        String[][] gifts = {{"🌹", "Rose", "10"}, {"💎", "Diamond", "100"}, {"🚗", "Car", "1000"}, {"🏰", "Castle", "10000"}};
        for (String[] gg : gifts) {
            Button btn = new Button(this);
            btn.setText(gg[0] + " " + gg[1] + " - 🪙 " + gg[2]);
            btn.setOnClickListener(v -> {
                Toast.makeText(this, gg[1] + " Sent ✅", Toast.LENGTH_SHORT).show();
                d.dismiss();
            });
            r.addView(btn);
        }
        d.setContentView(r);
        setDialog75(d);
        d.show();
    }

    private void showIconToolsDialog(boolean isOwner) {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(28, 20, 28, 28);
        r.setBackgroundColor(Color.parseColor("#111111"));
        TextView t1 = makeText("Basic Tools", 17, "#FFFFFF", true, Gravity.START);
        t1.setPadding(0, 0, 0, 16);
        r.addView(t1);
        String[][] basic = {{"🔒", "Lock off"}, {"⚙️", "Room set"}, {"✏️", "Room Edit"}, {"🧹", "Clear charm"}, {"🗑️", "Clear chat"}, {"🎤", "Change mic"}, {"🎨", "Mic Theme"}, {"👗", "Dress"}, {"🔔", "Lucky Sound"}, {"🎙️", "Voice User"}, {"🚪", "Kick out record"}};
        r.addView(createToolsGrid(basic, d, true));
        TextView t2 = makeText("Other Tools", 17, "#FFFFFF", true, Gravity.START);
        t2.setPadding(0, 24, 0, 16);
        r.addView(t2);
        String[][] other = {{"▶️", "YouTube Co-wat"}, {"🎈", "Lucky float"}, {"⚔️", "Room PK"}, {"📢", "Broadcast"}};
        r.addView(createToolsGrid(other, d, true));
        d.setContentView(r);
        setDialog85(d);
        d.show();
    }

    private LinearLayout createToolsGrid(String[][] items, BottomSheetDialog dialog, boolean dark) {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < items.length; i += 4) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(4);
            for (int j = 0; j < 4; j++) {
                int idx = i + j;
                LinearLayout cell = new LinearLayout(this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setPadding(4, 14, 4, 14);
                cell.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                if (idx < items.length) {
                    TextView ic = makeText(items[idx][0], 28, "#FFFFFF", false, Gravity.CENTER);
                    cell.addView(ic);
                    TextView lb = makeText(items[idx][1], 11, dark ? "#FFFFFF" : "#333333", false, Gravity.CENTER);
                    lb.setPadding(0, 8, 0, 0);
                    cell.addView(lb);
                    String act = items[idx][1];
                    cell.setOnClickListener(v -> {
                        dialog.dismiss();
                        if (act.equals("Clear chat") && messagesRef != null) {
                            messagesRef.removeValue();
                            Toast.makeText(this, "Chat Cleared!", Toast.LENGTH_SHORT).show();
                        } else if (act.equals("Room set") || act.equals("Room Edit")) {
                            showRoomEditDialog();
                        } else if (act.equals("Lock off")) {
                            showRoomPasswordDialog();
                        } else {
                            Toast.makeText(this, act + " - Coming Soon", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                row.addView(cell);
            }
            grid.addView(row);
        }
        return grid;
    }

    private void showRoomPasswordDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 24, 28, 28);
        root.setBackgroundColor(Color.WHITE);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView tt = makeText("Set room password", 18, "#222222", true, Gravity.CENTER);
        tt.setPadding(0, 0, 0, 20);
        root.addView(tt);
        EditText et = new EditText(this);
        et.setHint("Please enter a 1-6 password");
        et.setTextSize(16);
        et.setGravity(Gravity.CENTER);
        et.setPadding(24, 20, 24, 20);
        et.setBackgroundColor(Color.parseColor("#F5F5F5"));
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        root.addView(et);
        Button confirm = makeColorBtn("Confirm", Color.parseColor("#FFC107"));
        confirm.setTextColor(Color.WHITE);
        confirm.setPadding(0, 20, 0, 20);
        confirm.setOnClickListener(v -> {
            String pw = et.getText().toString().trim();
            if (pw.isEmpty() || pw.length() > 6) {
                Toast.makeText(this, "Please enter 1-6 digit password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (roomMetaRef != null) {
                roomMetaRef.child("roomPassword").setValue(pw);
            }
            d.dismiss();
            Toast.makeText(this, "Operation successful", Toast.LENGTH_SHORT).show();
        });
        root.addView(confirm);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }

    private void showInRoomRankingDialog() {
        android.app.Dialog d = makeFullScreenDialog();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1A0F2E"));
        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabRow.setGravity(Gravity.CENTER);
        tabRow.setPadding(0, 20, 0, 12);
        root.addView(tabRow);
        TextView tabWealth = makeText("Wealth", 18, "#FFFFFF", true, Gravity.CENTER);
        tabWealth.setPadding(30, 10, 30, 10);
        tabRow.addView(tabWealth);
        TextView tabCharm = makeText("Charm", 18, "#999999", false, Gravity.CENTER);
        tabCharm.setPadding(30, 10, 30, 10);
        tabRow.addView(tabCharm);
        TextView close = makeText("✕", 20, "#999999", false, Gravity.CENTER);
        close.setPadding(20, 10, 20, 10);
        close.setOnClickListener(v -> d.dismiss());
        tabRow.addView(close);
        LinearLayout pillRow = new LinearLayout(this);
        pillRow.setOrientation(LinearLayout.HORIZONTAL);
        pillRow.setGravity(Gravity.CENTER);
        pillRow.setPadding(0, 0, 0, 16);
        root.addView(pillRow);
        TextView pillDay = makeText("Day", 14, "#222222", true, Gravity.CENTER);
        pillDay.setPadding(40, 14, 40, 14);
        GradientDrawable gDay = new GradientDrawable();
        gDay.setColor(Color.parseColor("#FFC107"));
        gDay.setCornerRadius(60);
        pillDay.setBackground(gDay);
        pillRow.addView(pillDay);
        TextView pillWeek = makeText("Week", 14, "#FFFFFF", false, Gravity.CENTER);
        pillWeek.setPadding(40, 14, 40, 14);
        GradientDrawable gWeek = new GradientDrawable();
        gWeek.setColor(Color.parseColor("#444444"));
        gWeek.setCornerRadius(60);
        pillWeek.setBackground(gWeek);
        pillRow.addView(pillWeek);
        TextView pillMonth = makeText("Month", 14, "#FFFFFF", false, Gravity.CENTER);
        pillMonth.setPadding(40, 14, 40, 14);
        GradientDrawable gMonth = new GradientDrawable();
        gMonth.setColor(Color.parseColor("#444444"));
        gMonth.setCornerRadius(60);
        pillMonth.setBackground(gMonth);
        pillRow.addView(pillMonth);
        LinearLayout podium = new LinearLayout(this);
        podium.setOrientation(LinearLayout.HORIZONTAL);
        podium.setGravity(Gravity.BOTTOM);
        podium.setWeightSum(3);
        podium.setPadding(20, 10, 20, 0);
        root.addView(podium);
        LinearLayout listWrap = new LinearLayout(this);
        listWrap.setOrientation(LinearLayout.VERTICAL);
        listWrap.setBackgroundColor(Color.WHITE);
        listWrap.setPadding(20, 16, 20, 20);
        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        listWrap.addView(sv);
        root.addView(listWrap);
        Runnable refresh = () -> {
            tabWealth.setTextColor(rankType.equals("Wealth") ? Color.WHITE : Color.parseColor("#999999"));
            tabCharm.setTextColor(rankType.equals("Charm") ? Color.WHITE : Color.parseColor("#999999"));
            pillDay.setTextColor(rankPeriod.equals("Day") ? Color.parseColor("#222222") : Color.WHITE);
            GradientDrawable gd1 = new GradientDrawable();
            gd1.setColor(rankPeriod.equals("Day") ? Color.parseColor("#FFC107") : Color.parseColor("#444444"));
            gd1.setCornerRadius(60);
            pillDay.setBackground(gd1);
            pillWeek.setTextColor(rankPeriod.equals("Week") ? Color.parseColor("#222222") : Color.WHITE);
            GradientDrawable gd2 = new GradientDrawable();
            gd2.setColor(rankPeriod.equals("Week") ? Color.parseColor("#FFC107") : Color.parseColor("#444444"));
            gd2.setCornerRadius(60);
            pillWeek.setBackground(gd2);
            pillMonth.setTextColor(rankPeriod.equals("Month") ? Color.parseColor("#222222") : Color.WHITE);
            GradientDrawable gd3 = new GradientDrawable();
            gd3.setColor(rankPeriod.equals("Month") ? Color.parseColor("#FFC107") : Color.parseColor("#444444"));
            gd3.setCornerRadius(60);
            pillMonth.setBackground(gd3);
            loadRankingDataPremium(list, podium);
        };
        tabWealth.setOnClickListener(v -> {
            rankType = "Wealth";
            refresh.run();
        });
        tabCharm.setOnClickListener(v -> {
            rankType = "Charm";
            refresh.run();
        });
        pillDay.setOnClickListener(v -> {
            rankPeriod = "Day";
            refresh.run();
        });
        pillWeek.setOnClickListener(v -> {
            rankPeriod = "Week";
            refresh.run();
        });
        pillMonth.setOnClickListener(v -> {
            rankPeriod = "Month";
            refresh.run();
        });
        refresh.run();
        d.setContentView(root);
        d.show();
    }

    private LinearLayout makePodiumItem(String name, String photo, int rank, int size, String crown, int bgColor) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView cr = makeText(crown, 26, "#FFFFFF", false, Gravity.CENTER);
        col.addView(cr);
        CardView card = makeAvatar(photo, name, size);
        card.setRadius(size / 2);
        card.setCardElevation(6);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(0, 4, 0, 6);
        card.setLayoutParams(lp);
        col.addView(card);
        TextView nm = makeText(name != null ? name : "", 12, "#FFFFFF", true, Gravity.CENTER);
        col.addView(nm);
        TextView rk = makeText(String.valueOf(rank), 16, "#FFFFFF", true, Gravity.CENTER);
        rk.setPadding(0, 4, 0, 10);
        col.addView(rk);
        return col;
    }

    private void loadRankingDataPremium(LinearLayout list, LinearLayout podium) {
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                list.removeAllViews();
                podium.removeAllViews();
                List<DataSnapshot> all = new ArrayList<>();
                for (DataSnapshot ds : s.getChildren()) {
                    all.add(ds);
                }
                if (all.isEmpty()) {
                    TextView empty = makeText("No ranking data yet", 14, "#888888", false, Gravity.CENTER);
                    empty.setPadding(0, 20, 0, 20);
                    list.addView(empty);
                    return;
                }
                all.sort((a, b) -> {
                    if (a.getKey().equals(roomOwnerId)) {
                        return -1;
                    }
                    if (b.getKey().equals(roomOwnerId)) {
                        return 1;
                    }
                    return 0;
                });
                int showPodium = Math.min(3, all.size());
                List<LinearLayout> cols = new ArrayList<>();
                for (int i = 0; i < showPodium; i++) {
                    DataSnapshot ds = all.get(i);
                    String n = ds.child("name").getValue(String.class);
                    String p = ds.child("photoBase64").getValue(String.class);
                    if (n == null) {
                        n = "User";
                    }
                    if (p == null) {
                        p = "";
                    }
                    if (i == 0) {
                        cols.add(makePodiumItem(n, p, 1, 150, "👑", Color.parseColor("#FF9800")));
                    } else if (i == 1) {
                        cols.add(makePodiumItem(n, p, 2, 120, "🥈", Color.parseColor("#2196F3")));
                    } else {
                        cols.add(makePodiumItem(n, p, 3, 120, "🥉", Color.parseColor("#FF7043")));
                    }
                }
                if (cols.size() == 3) {
                    podium.addView(cols.get(1));
                    podium.addView(cols.get(0));
                    podium.addView(cols.get(2));
                } else {
                    for (LinearLayout c : cols) {
                        podium.addView(c);
                    }
                }
                for (int i = 0; i < all.size(); i++) {
                    DataSnapshot ds = all.get(i);
                    String n = ds.child("name").getValue(String.class);
                    String p = ds.child("photoBase64").getValue(String.class);
                    if (n == null) {
                        n = "User";
                    }
                    if (p == null) {
                        p = "";
                    }
                    LinearLayout row = new LinearLayout(VoiceRoomActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(6, 12, 6, 12);
                    TextView rankTv = makeText(String.valueOf(i + 1), 16, "#222222", true, Gravity.CENTER);
                    rankTv.setLayoutParams(new LinearLayout.LayoutParams(70, ViewGroup.LayoutParams.WRAP_CONTENT));
                    row.addView(rankTv);
                    CardView card = makeAvatar(p, n, 90);
                    card.setRadius(45);
                    LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(90, 90);
                    clp.setMargins(0, 0, 12, 0);
                    card.setLayoutParams(clp);
                    row.addView(card);
                    LinearLayout box = new LinearLayout(VoiceRoomActivity.this);
                    box.setOrientation(LinearLayout.VERTICAL);
                    box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(box);
                    TextView tn = makeText(n, 15, "#222222", true, Gravity.START);
                    box.addView(tn);
                    TextView ti = makeText("ID: " + ds.getKey(), 12, "#888888", false, Gravity.START);
                    box.addView(ti);
                    TextView coin = makeText(rankType.equals("Wealth") ? "🪙 0" : "❤️ 0", 14, "#B8860B", true, Gravity.CENTER);
                    row.addView(coin);
                    list.addView(row);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }

    private void showRoomEditDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 16, 28, 28);
        root.setBackgroundColor(Color.parseColor("#FFFFFF"));
        TextView tt = makeText("Room set", 18, "#222222", true, Gravity.CENTER);
        root.addView(tt);
        EditText etName = new EditText(this);
        etName.setHint("Room Name");
        etName.setText(roomTitleText != null ? roomTitleText.getText().toString() : "");
        root.addView(etName);
        EditText etAnn = new EditText(this);
        etAnn.setHint("Announcement");
        root.addView(etAnn);
        Button save = makeColorBtn("Save", Color.parseColor("#FF9800"));
        save.setOnClickListener(v -> {
            String nm = etName.getText().toString().trim();
            if (!nm.isEmpty() && roomMetaRef != null) {
                roomMetaRef.child("roomName").setValue(nm);
            }
            String an = etAnn.getText().toString().trim();
            if (roomMetaRef != null) {
                roomMetaRef.child("announcement").setValue(an);
            }
            if (pendingRoomPhotoBase64 != null && !pendingRoomPhotoBase64.isEmpty() && roomMetaRef != null) {
                roomMetaRef.child("roomPhotoBase64").setValue(pendingRoomPhotoBase64);
            }
            Toast.makeText(this, "Saved ✅", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        root.addView(save);
        dialog.setContentView(root);
        setDialog85(dialog);
        dialog.show();
    }
}
