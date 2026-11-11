package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {
    private ImageButton btnPower;
    private TextView btnMicToggle, btnChatOption, btnGiftBox, btnGame, btnRoomTools;
    private TextView roomTitleText, roomInfoText, textOnlineCount, roomNoticeBarText, textRoomCoins;
    private TextView tabAll, tabChat, tabGift, tabNotice, textChatEmpty;
    private EditText inputChatMessage;
    private LinearLayout btnInRoomRanking, chatListContainer;
    private ScrollView chatScrollView;
    private ImageView btnOnlineList, roomHeaderProfileImg;
    private boolean isMuted = false;
    private boolean chatListenerStarted = false;
    private SharedPreferences prefs;
    private DatabaseReference databaseReference, roomMetaRef, userDatabaseRef, messagesRef, adminsRef;
    private FirebaseAuth mAuth;
    private String roomOwnerId = "", currentUserId = "";
    private String currentThemeName = "Night Purple";
    private List<String> roomAdminList = new ArrayList<>();
    private long myWalletCoins = 0L;
    private TextView walletTextInGift = null;
    private String myDisplayName = "User";
    private String myCountry = "Bangladesh";
    private String myGender = "Male";
    private String currentChatFilter = "All";
    private List<DataSnapshot> allChatSnapshots = new ArrayList<>();

    private String getMyDisplayName() {
        String n = prefs.getString("USER_NAME", "");
        if (n != null && !n.isEmpty()) return n;
        if (myDisplayName != null && !myDisplayName.isEmpty()) return myDisplayName;
        return "User";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        currentUserId = prefs.getString("MY_9_DIGIT_ID", "");
        if (currentUserId.isEmpty()) {
            int r = (int) (Math.random() * 900000000) + 100000000;
            currentUserId = String.valueOf(r);
            prefs.edit().putString("MY_9_DIGIT_ID", currentUserId).apply();
        }
        String saved = prefs.getString("USER_NAME", "");
        if (saved != null && !saved.isEmpty()) myDisplayName = saved;
        String savedC = prefs.getString("USER_COUNTRY", "");
        if (savedC != null && !savedC.isEmpty()) myCountry = savedC;
        String savedG = prefs.getString("USER_GENDER", "");
        if (savedG != null && !savedG.isEmpty()) myGender = savedG;

        roomOwnerId = getIntent().getStringExtra("ROOM_OWNER_ID");
        if (roomOwnerId == null || roomOwnerId.isEmpty()) roomOwnerId = currentUserId;

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + roomOwnerId);
        roomMetaRef = databaseReference.child("meta");
        messagesRef = databaseReference.child("messages");
        adminsRef = databaseReference.child("admins");
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        syncLatestUserProfile();
        listenToRoomDetails();
        listenToRoomAdmins();
        listenToRoomTheme();
        listenToMyWallet();
        // CHAT - EKTA MATRO LISTENER, BAR BAR LAGBE NA - REALTIME
        listenToChatMessagesRealtime();

        btnPower = findViewById(R.id.btnPower);
        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        textOnlineCount = findViewById(R.id.textOnlineCount);
        btnOnlineList = findViewById(R.id.btnOnlineList);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
        btnInRoomRanking = findViewById(R.id.btnInRoomRanking);
        roomNoticeBarText = findViewById(R.id.roomNoticeBarText);
        btnRoomTools = findViewById(R.id.btnRoomTools);
        textRoomCoins = findViewById(R.id.textRoomCoins);
        inputChatMessage = findViewById(R.id.inputChatMessage);
        chatListContainer = findViewById(R.id.chatListContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        textChatEmpty = findViewById(R.id.textChatEmpty);
        tabAll = findViewById(R.id.tabAll);
        tabChat = findViewById(R.id.tabChat);
        tabGift = findViewById(R.id.tabGift);
        tabNotice = findViewById(R.id.tabNotice);
        btnGame = findViewById(R.id.btnGame);

        if (btnPower != null) btnPower.setOnClickListener(v -> showExitDialog());

        if (btnRoomTools != null) {
            btnRoomTools.setOnClickListener(v -> {
                boolean isOwner = currentUserId.equals(roomOwnerId);
                boolean isAdmin = roomAdminList.contains(currentUserId);
                if (!isOwner && !isAdmin) {
                    Toast.makeText(this, "Only Owner / Admin can access Tools!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showIconToolsDialog(isOwner);
            });
        }
        if (btnInRoomRanking != null) btnInRoomRanking.setOnClickListener(v -> showInRoomRankingDialog());
        if (btnOnlineList != null) btnOnlineList.setOnClickListener(v -> showOnlineListDialog());
        // HEADER CLICK - FUL BORO POPUP - PROFILE + MEMBERS
        if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(v -> showHeaderProfileMembersDialog());

        setupChatTabs();
        setupBottomChatSend();

        if (btnGame != null) btnGame.setOnClickListener(v -> Toast.makeText(this, "Games Coming Soon 🎮", Toast.LENGTH_SHORT).show());

        setupSeatInteractions();
        listenToFirebaseSeats();
        listenToOnlineMembers();

        btnMicToggle = findViewById(R.id.btnMicToggle);
        if (btnMicToggle != null) btnMicToggle.setOnClickListener(v -> {
            isMuted = !isMuted;
            btnMicToggle.setText(isMuted ? "🔇" : "🎤");
            btnMicToggle.setBackgroundColor(isMuted ? Color.parseColor("#616161") : Color.parseColor("#FF9800"));
            Toast.makeText(this, isMuted ? "Mic Muted" : "Mic On", Toast.LENGTH_SHORT).show();
        });

        btnChatOption = findViewById(R.id.btnChatOption);
        if (btnChatOption != null) btnChatOption.setOnClickListener(v -> {
            if (inputChatMessage != null && !inputChatMessage.getText().toString().trim().isEmpty()) sendChatFromInput();
            else showChatInputPopup();
        });

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) btnGiftBox.setOnClickListener(v -> showNewGiftPanel());
    }

    @Override public void onBackPressed() { showExitDialog(); }

    private void setDialog75(BottomSheetDialog dialog) {
        Window w = dialog.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.85));
            w.setGravity(Gravity.BOTTOM);
        }
    }

    private void setupChatTabs() {
        if (tabAll != null) tabAll.setOnClickListener(v -> setChatFilter("All"));
        if (tabChat != null) tabChat.setOnClickListener(v -> setChatFilter("Chat"));
        if (tabGift != null) tabGift.setOnClickListener(v -> setChatFilter("Gift"));
        if (tabNotice != null) tabNotice.setOnClickListener(v -> setChatFilter("Notice"));
    }

    private void setChatFilter(String filter) {
        currentChatFilter = filter;
        if (tabAll != null) tabAll.setTextColor(filter.equals("All") ? Color.WHITE : Color.parseColor("#AAAAAA"));
        if (tabChat != null) tabChat.setTextColor(filter.equals("Chat") ? Color.WHITE : Color.parseColor("#AAAAAA"));
        if (tabGift != null) tabGift.setTextColor(filter.equals("Gift") ? Color.WHITE : Color.parseColor("#AAAAAA"));
        if (tabNotice != null) tabNotice.setTextColor(filter.equals("Notice") ? Color.WHITE : Color.parseColor("#AAAAAA"));
        if (tabAll != null) tabAll.setTypeface(null, filter.equals("All") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        if (tabChat != null) tabChat.setTypeface(null, filter.equals("Chat") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        if (tabGift != null) tabGift.setTypeface(null, filter.equals("Gift") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        if (tabNotice != null) tabNotice.setTypeface(null, filter.equals("Notice") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        // FILTER SUDHU SCREEN E - LISTENER AR LAGBE NA
        redrawChatList();
    }

    private void setupBottomChatSend() {
        if (inputChatMessage == null) return;
        inputChatMessage.setOnEditorActionListener((v, actionId, event) -> { sendChatFromInput(); return true; });
        // TYPE BOX E CLICK KORLEI BORO CHAT POPUP - ONNO APP ER MOTO
        inputChatMessage.setOnClickListener(v -> showChatInputPopup());
    }

    // ===== CHAT POPUP - TYPE YOUR WORDS HERE - ONNO APP ER MOTO =====
    private void showChatInputPopup() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 16, 24, 24);
        root.setBackgroundColor(Color.parseColor("#FFFFFF"));
        TextView hint = new TextView(this);
        hint.setText("Type your words here!");
        hint.setTextColor(Color.parseColor("#888888"));
        hint.setTextSize(13);
        hint.setPadding(0, 0, 0, 8);
        root.addView(hint);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(row);
        EditText bigInput = new EditText(this);
        bigInput.setHint("Type...");
        bigInput.setTextColor(Color.parseColor("#222222"));
        bigInput.setTextSize(15);
        bigInput.setBackgroundColor(Color.parseColor("#F2F2F2"));
        bigInput.setPadding(20, 20, 20, 20);
        bigInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (inputChatMessage != null) bigInput.setText(inputChatMessage.getText().toString());
        row.addView(bigInput);
        TextView btnSend = new TextView(this);
        btnSend.setText("➤");
        btnSend.setTextSize(22);
        btnSend.setTextColor(Color.WHITE);
        btnSend.setGravity(Gravity.CENTER);
        btnSend.setBackgroundColor(Color.parseColor("#FF9800"));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(110, 110);
        sp.setMargins(12, 0, 0, 0);
        btnSend.setLayoutParams(sp);
        btnSend.setOnClickListener(v -> {
            String t = bigInput.getText().toString().trim();
            if (!t.isEmpty()) { sendChatMessage(t); dialog.dismiss(); if (inputChatMessage != null) inputChatMessage.setText(""); }
        });
        row.addView(btnSend);
        LinearLayout emojiRow = new LinearLayout(this);
        emojiRow.setOrientation(LinearLayout.HORIZONTAL);
        emojiRow.setPadding(0, 16, 0, 0);
        String[] emojis = {"😊","❤️","😂","🔥","👏","🎉","😍","👍"};
        for (String e : emojis) {
            TextView tv = new TextView(this);
            tv.setText(e); tv.setTextSize(24); tv.setPadding(12, 0, 12, 0);
            tv.setOnClickListener(v -> bigInput.append(e));
            emojiRow.addView(tv);
        }
        root.addView(emojiRow);
        dialog.setContentView(root);
        dialog.show();
        bigInput.requestFocus();
    }

    private void sendChatFromInput() {
        if (inputChatMessage == null) return;
        String msg = inputChatMessage.getText().toString().trim();
        if (msg.isEmpty()) return;
        sendChatMessage(msg);
        inputChatMessage.setText("");
    }

    // SEND - SOBAI PARBE - SOB ROOM E - SOB COUNTRY TE
    private void sendChatMessage(String msg) {
        if (messagesRef == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUserId);
        data.put("userName", getMyDisplayName());
        data.put("text", msg);
        data.put("type", "Chat");
        data.put("time", System.currentTimeMillis());
        messagesRef.push().setValue(data).addOnFailureListener(e -> Toast.makeText(this, "Send failed, try again", Toast.LENGTH_SHORT).show());
    }

    // REALTIME CHAT - EKTA LISTENER - SOBAR MESSAGE SATHE SATHE ASBE
    private void listenToChatMessagesRealtime() {
        if (messagesRef == null || chatListenerStarted) return;
        chatListenerStarted = true;
        messagesRef.limitToLast(50).addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                allChatSnapshots.add(ds);
                if (allChatSnapshots.size() > 50) allChatSnapshots.remove(0);
                addOneChatToScreen(ds);
            }
            @Override public void onChildChanged(@NonNull DataSnapshot ds, @Nullable String s) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot ds) { redrawChatList(); }
            @Override public void onChildMoved(@NonNull DataSnapshot ds, @Nullable String s) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addOneChatToScreen(DataSnapshot ds) {
        if (chatListContainer == null) return;
        String type = ds.child("type").getValue(String.class);
        if (type == null) type = "Chat";
        if (!currentChatFilter.equals("All") && !currentChatFilter.equals(type)) return;
        String uname = ds.child("userName").getValue(String.class);
        String txt = ds.child("text").getValue(String.class);
        if (txt == null) txt = "";
        if (uname == null) uname = "User";
        // wlc khali thakle soray dao
        if (textChatEmpty != null && textChatEmpty.getParent() != null) ((ViewGroup) textChatEmpty.getParent()).removeView(textChatEmpty);
        TextView tv = new TextView(VoiceRoomActivity.this);
        tv.setText(uname + ": " + txt);
        tv.setTextColor(type.equals("Gift") ? Color.parseColor("#FFD700") : Color.WHITE);
        tv.setTextSize(13);
        tv.setPadding(0, 4, 0, 4);
        chatListContainer.addView(tv);
        // AUTO SCROLL NICHE
        if (chatScrollView != null) chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void redrawChatList() {
        if (chatListContainer == null) return;
        chatListContainer.removeAllViews();
        boolean hasAny = false;
        for (DataSnapshot ds : allChatSnapshots) {
            String type = ds.child("type").getValue(String.class);
            if (type == null) type = "Chat";
            if (!currentChatFilter.equals("All") && !currentChatFilter.equals(type)) continue;
            String uname = ds.child("userName").getValue(String.class);
            String txt = ds.child("text").getValue(String.class);
            if (txt == null) txt = "";
            if (uname == null) uname = "User";
            TextView tv = new TextView(this);
            tv.setText(uname + ": " + txt);
            tv.setTextColor(type.equals("Gift") ? Color.parseColor("#FFD700") : Color.WHITE);
            tv.setTextSize(13);
            tv.setPadding(0, 4, 0, 4);
            chatListContainer.addView(tv);
            hasAny = true;
        }
        if (!hasAny) {
            TextView empty = new TextView(this);
            empty.setText("wlc");
            empty.setTextColor(Color.parseColor("#AAAAAA"));
            empty.setTextSize(13);
            chatListContainer.addView(empty);
        }
        if (chatScrollView != null) chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void showOnlineListDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 25, 35, 35);
        root.setBackgroundColor(Color.parseColor("#FFF8E1"));
        TextView title = new TextView(this);
        title.setText("Online Members");
        title.setTextColor(Color.parseColor("#FF9800"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);
        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        root.addView(sv);
        databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                list.removeAllViews();
                for (DataSnapshot ds : s.getChildren()) {
                    String name = ds.getValue(String.class);
                    if (name == null) name = "User";
                    TextView tv = new TextView(VoiceRoomActivity.this);
                    tv.setText("• " + name + "  (ID: " + ds.getKey() + ")");
                    tv.setTextColor(Color.parseColor("#222222"));
                    tv.setTextSize(14);
                    tv.setPadding(10, 10, 10, 10);
                    tv.setOnClickListener(v -> { dialog.dismiss(); showMiniProfileCard(ds.getKey()); });
                    list.addView(tv);
                }
                if (s.getChildrenCount() == 0) {
                    TextView tv = new TextView(VoiceRoomActivity.this);
                    tv.setText("No one online");
                    tv.setTextColor(Color.parseColor("#AAAAAA"));
                    list.addView(tv);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        dialog.setContentView(root);
        setDialog75(dialog);
        dialog.show();
    }

    private void showExitDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Exit Room?");
        b.setMessage("Exit room or Minimize to Home?");
        b.setPositiveButton("Exit Room", (d, w) -> { d.dismiss(); leaveRoomAndFinish(); });
        b.setNeutralButton("Minimize", (d, w) -> {
            d.dismiss(); leaveRoomKeepHome();
            Intent home = new Intent(VoiceRoomActivity.this, MainActivity.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home); finish();
        });
        b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        b.show();
    }

    private void leaveRoomKeepHome() {
        if (currentUserId != null && !currentUserId.isEmpty()) {
            databaseReference.child("online_users").child(currentUserId).removeValue();
            for (int i = 0; i < 10; i++) {
                final int idx = i;
                databaseReference.child("seats").child("seat_" + idx).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s) {
                        String uid = s.child("userId").getValue(String.class);
                        if (uid != null && uid.equals(currentUserId)) databaseReference.child("seats").child("seat_" + idx).removeValue();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            }
        }
    }

    private void leaveRoomAndFinish() {
        leaveRoomKeepHome();
        Intent intent = new Intent(VoiceRoomActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent); finish();
    }

    private void listenToMyWallet() {
        if (currentUserId.isEmpty()) return;
        userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                Long coins = s.child("coins").getValue(Long.class);
                long w = (coins != null) ? coins : 0L;
                myWalletCoins = w;
                if (walletTextInGift != null) walletTextInGift.setText("Coins: " + String.format("%,d", w));
                if (textRoomCoins != null) textRoomCoins.setText("🪙 " + String.format("%,d", w));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToRoomTheme() {
        if (roomMetaRef == null) return;
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String n = s.child("roomTheme").getValue(String.class);
                if (n != null && !n.isEmpty()) currentThemeName = n;
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // ===== SETTINGS - BORO POPUP - ICON + LEKHA =====
    private void showIconToolsDialog(boolean isOwner) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 20, 28, 28);
        root.setBackgroundColor(Color.parseColor("#FFF8E1"));
        TextView title = new TextView(this);
        title.setText("Room Settings");
        title.setTextColor(Color.parseColor("#222222"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 16);
        root.addView(title);
        TextView basic = new TextView(this);
        basic.setText("Basic Tools");
        basic.setTextColor(Color.parseColor("#888888"));
        basic.setTextSize(13);
        basic.setTypeface(null, android.graphics.Typeface.BOLD);
        basic.setPadding(4, 0, 0, 8);
        root.addView(basic);
        // 4 COLUMN GRID
        root.addView(createToolsGrid(new String[][]{
            {"🔒","Lock off"},{"⚙️","Room set"},{"✏️","Room Edit"},{"🧹","Clear charm"},
            {"🗑️","Clear chat"},{"🎤","Change mic"},{"🎨","Mic Theme"},{"👗","Dress"},
            {"🔔","Lucky Sound"},{"🎙️","Voice User"},{"🚪","Kick out record"}
        }, dialog));
        TextView other = new TextView(this);
        other.setText("Other Tools");
        other.setTextColor(Color.parseColor("#888888"));
        other.setTextSize(13);
        other.setTypeface(null, android.graphics.Typeface.BOLD);
        other.setPadding(4, 20, 0, 8);
        root.addView(other);
        root.addView(createToolsGrid(new String[][]{
            {"▶️","YouTube"},{"🍀","Lucky float"},{"⚔️","Room PK"},{"📡","Broadcast"}
        }, dialog));
        dialog.setContentView(root);
        setDialog75(dialog);
        dialog.show();
    }

    private LinearLayout createToolsGrid(String[][] items, BottomSheetDialog dialog) {
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
                cell.setPadding(4, 12, 4, 12);
                cell.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                if (idx < items.length) {
                    TextView icon = new TextView(this);
                    icon.setText(items[idx][0]);
                    icon.setTextSize(26);
                    icon.setGravity(Gravity.CENTER);
                    cell.addView(icon);
                    TextView label = new TextView(this);
                    label.setText(items[idx][1]);
                    label.setTextColor(Color.parseColor("#333333"));
                    label.setTextSize(11);
                    label.setGravity(Gravity.CENTER);
                    label.setPadding(0, 6, 0, 0);
                    cell.addView(label);
                    String action = items[idx][1];
                    cell.setOnClickListener(v -> {
                        dialog.dismiss();
                        if (action.equals("Clear chat") && messagesRef != null) { messagesRef.removeValue(); allChatSnapshots.clear(); redrawChatList(); Toast.makeText(this, "Chat Cleared!", Toast.LENGTH_SHORT).show(); }
                        else Toast.makeText(this, action + " - Coming Soon", Toast.LENGTH_SHORT).show();
                    });
                }
                row.addView(cell);
            }
            grid.addView(row);
        }
        return grid;
    }

    private Button createToolButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14);
        btn.setAllCaps(false);
        btn.setBackgroundColor(Color.parseColor("#2A1B4E"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
        params.setMargins(0, 10, 0, 10);
        btn.setLayoutParams(params);
        return btn;
    }

    private void showInRoomRankingDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 25, 35, 25);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);
        TextView title = new TextView(this);
        title.setText("🏆 Contribution Ranking");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);
        ScrollView sv = new ScrollView(this);
        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        sv.addView(listLayout);
        root.addView(sv);
        databaseReference.child("contributions").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                listLayout.removeAllViews();
                if (!s.exists() || s.getChildrenCount() == 0) {
                    TextView empty = new TextView(VoiceRoomActivity.this);
                    empty.setText("No contributions yet\nSend gift to be Top 1 🏆");
                    empty.setTextColor(Color.parseColor("#AAAAAA"));
                    empty.setTextSize(14);
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(0, 40, 0, 40);
                    listLayout.addView(empty);
                    return;
                }
                List<DataSnapshot> list = new ArrayList<>();
                for (DataSnapshot ds : s.getChildren()) list.add(ds);
                Collections.sort(list, (a, b) -> {
                    Long ca = a.child("coins").getValue(Long.class); if (ca == null) ca = 0L;
                    Long cb = b.child("coins").getValue(Long.class); if (cb == null) cb = 0L;
                    return Long.compare(cb, ca);
                });
                int rank = 1;
                for (DataSnapshot ds : list) {
                    String uname = ds.child("userName").getValue(String.class);
                    if (uname == null || uname.isEmpty()) uname = "User";
                    Long c = ds.child("coins").getValue(Long.class);
                    long coins = (c != null) ? c : 0;
                    LinearLayout row = new LinearLayout(VoiceRoomActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(20, 15, 20, 15);
                    row.setBackgroundColor(Color.parseColor("#140B26"));
                    LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rp.setMargins(0, 6, 0, 6);
                    row.setLayoutParams(rp);
                    TextView tvRank = new TextView(VoiceRoomActivity.this);
                    tvRank.setText(rank <= 3 ? "🏆 " + rank : String.valueOf(rank));
                    tvRank.setTextColor(rank <= 3 ? Color.parseColor("#FFD700") : Color.parseColor("#B0BEC5"));
                    tvRank.setTextSize(14);
                    tvRank.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvRank.setLayoutParams(new LinearLayout.LayoutParams(90, ViewGroup.LayoutParams.WRAP_CONTENT));
                    row.addView(tvRank);
                    TextView tvName = new TextView(VoiceRoomActivity.this);
                    tvName.setText(uname);
                    tvName.setTextColor(Color.WHITE);
                    tvName.setTextSize(14);
                    tvName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    row.addView(tvName);
                    TextView tvCoins = new TextView(VoiceRoomActivity.this);
                    tvCoins.setText(String.format("%,d Coins", coins));
                    tvCoins.setTextColor(Color.parseColor("#00E676"));
                    tvCoins.setTextSize(13);
                    row.addView(tvCoins);
                    listLayout.addView(row);
                    rank++;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        dialog.setContentView(root);
        setDialog75(dialog);
        dialog.show();
    }

    private void showNewGiftPanel() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 20, 30, 20);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);
        TextView title = new TextView(this);
        title.setText("Gift Center");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 12);
        root.addView(title);
        TextView wallet = new TextView(this);
        wallet.setText("Coins: " + String.format("%,d", myWalletCoins));
        wallet.setTextColor(Color.WHITE);
        wallet.setTextSize(14);
        wallet.setGravity(Gravity.CENTER);
        wallet.setPadding(0, 0, 0, 12);
        root.addView(wallet);
        walletTextInGift = wallet;
        TextView info = new TextView(this);
        info.setText("Full Gift Store Coming Soon 🎁");
        info.setTextColor(Color.parseColor("#AAAAAA"));
        info.setTextSize(13);
        info.setGravity(Gravity.CENTER);
        root.addView(info);
        dialog.setContentView(root);
        setDialog75(dialog);
        dialog.show();
    }

    private void showChatDialog() { showChatInputPopup(); }

    private void listenToRoomAdmins() {
        adminsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                roomAdminList.clear();
                for (DataSnapshot ds : s.getChildren()) if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) roomAdminList.add(ds.getKey());
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void syncLatestUserProfile() {
        if (currentUserId.isEmpty()) return;
        userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) {
                    String dbName = s.child("name").getValue(String.class);
                    if (dbName != null && !dbName.isEmpty()) { myDisplayName = dbName; prefs.edit().putString("USER_NAME", dbName).apply(); }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToRoomDetails() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String rn = s.child("roomName").getValue(String.class);
                if (rn != null && !rn.isEmpty() && roomTitleText != null) roomTitleText.setText(rn);
                if (roomInfoText != null) roomInfoText.setText("ID: " + roomOwnerId);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToOnlineMembers() {
        String un = getMyDisplayName();
        databaseReference.child("online_users").child(currentUserId).setValue(un);
        databaseReference.child("online_users").child(currentUserId).onDisconnect().removeValue();
        databaseReference.child("online_users").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                if (textOnlineCount != null) textOnlineCount.setText(String.valueOf(s.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void setupSeatInteractions() {
        for (int i = 0; i < 10; i++) {
            int resId = getResources().getIdentifier("seat_" + i, "id", getPackageName());
            if (resId == 0) continue;
            View seat = findViewById(resId);
            if (seat == null) continue;
            final int idx = i;
            seat.setOnClickListener(v -> {
                databaseReference.child("seats").child("seat_" + idx).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s) {
                        String uid = s.child("userId").getValue(String.class);
                        if (uid != null && !uid.isEmpty()) {
                            if (uid.equals(currentUserId)) showSeatActionSheet(idx, true);
                            else showMiniProfileCard(uid);
                        } else showSeatActionSheet(idx, false);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            });
        }
    }

    private void showSeatActionSheet(int seatIndex, boolean isMySeat) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 20, 40, 40);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FFFFFF"));
        bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0});
        root.setBackground(bg);
        TextView txtSeatNo = new TextView(this);
        txtSeatNo.setText("Seat " + (seatIndex + 1));
        txtSeatNo.setTextColor(Color.parseColor("#888888")); txtSeatNo.setTextSize(13); txtSeatNo.setGravity(Gravity.CENTER); txtSeatNo.setPadding(0, 0, 0, 15);
        root.addView(txtSeatNo);
        Button btnAction = new Button(this);
        btnAction.setText(isMySeat ? "Leave Mic" : "Take Mic");
        btnAction.setTextColor(Color.WHITE); btnAction.setTextSize(16); btnAction.setAllCaps(false);
        btnAction.setTypeface(null, android.graphics.Typeface.BOLD);
        GradientDrawable btnBg = new GradientDrawable(); btnBg.setColor(Color.parseColor(isMySeat ? "#FF5252" : "#FF9800")); btnBg.setCornerRadius(60);
        btnAction.setBackground(btnBg);
        btnAction.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 130));
        btnAction.setOnClickListener(v -> { dialog.dismiss(); if (isMySeat) leaveSeat(seatIndex); else takeSeat(seatIndex); });
        root.addView(btnAction);
        Button btnCancel = new Button(this);
        btnCancel.setText("Cancel"); btnCancel.setTextColor(Color.parseColor("#888888")); btnCancel.setTextSize(14); btnCancel.setAllCaps(false);
        btnCancel.setBackgroundColor(Color.TRANSPARENT);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        root.addView(btnCancel);
        dialog.setContentView(root);
        dialog.show();
    }

    private void takeSeat(int seatIndex) {
        for (int j = 0; j < 10; j++) {
            final int sj = j;
            databaseReference.child("seats").child("seat_" + sj).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot ss) {
                    String ouid = ss.child("userId").getValue(String.class);
                    if (ouid != null && ouid.equals(currentUserId)) databaseReference.child("seats").child("seat_" + sj).removeValue();
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
        Map<String, Object> seatData = new HashMap<>();
        seatData.put("userId", currentUserId);
        seatData.put("userName", getMyDisplayName());
        String myPhoto = prefs.getString("USER_PHOTO_BASE64", "");
        if (myPhoto != null && !myPhoto.isEmpty()) seatData.put("photoBase64", myPhoto);
        databaseReference.child("seats").child("seat_" + seatIndex).setValue(seatData);
        Toast.makeText(this, "Seated Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void leaveSeat(int seatIndex) {
        databaseReference.child("seats").child("seat_" + seatIndex).removeValue();
        Toast.makeText(this, "Left Mic", Toast.LENGTH_SHORT).show();
    }

    private void listenToFirebaseSeats() {
        databaseReference.child("seats").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (int i = 0; i < 10; i++) {
                    int resId = getResources().getIdentifier("seat_" + i, "id", getPackageName());
                    if (resId == 0) continue;
                    View seat = findViewById(resId);
                    if (seat == null) continue;
                    String uid = s.child("seat_" + i).child("userId").getValue(String.class);
                    String uname = s.child("seat_" + i).child("userName").getValue(String.class);
                    String photo = s.child("seat_" + i).child("photoBase64").getValue(String.class);
                    updatePremiumSeatView(seat, i, uid, uname, photo);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void updatePremiumSeatView(View seat, int seatIndex, String uid, String uname, String photoBase64) {
        boolean isOccupied = uid != null && !uid.isEmpty();
        boolean isMine = isOccupied && uid.equals(currentUserId);
        if (seat instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) seat;
            for (int c = 0; c < vg.getChildCount(); c++) {
                View child = vg.getChildAt(c);
                if (child instanceof ImageView) {
                    ImageView iv = (ImageView) child;
                    GradientDrawable imgBg = new GradientDrawable();
                    imgBg.setShape(GradientDrawable.OVAL);
                    if (isOccupied) { imgBg.setColor(Color.parseColor("#FFFFFF")); imgBg.setStroke(4, Color.parseColor(isMine ? "#FF9800" : "#FFFFFF")); }
                    else { imgBg.setColor(Color.parseColor("#40000000")); imgBg.setStroke(3, Color.parseColor("#FFFFFF")); }
                    iv.setBackground(imgBg); iv.setPadding(4, 4, 4, 4);
                    if (isOccupied) {
                        boolean set = false;
                        if (photoBase64 != null && !photoBase64.isEmpty()) {
                            try { byte[] bytes = Base64.decode(photoBase64, Base64.DEFAULT); Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); if (b != null) { iv.setImageBitmap(b); set = true; } } catch (Exception ignored) {}
                        }
                        if (!set) iv.setImageResource(android.R.drawable.ic_menu_gallery);
                    } else {
                        int sofaId = getResources().getIdentifier("seat_sofa", "drawable", getPackageName());
                        if (sofaId != 0) iv.setImageResource(sofaId); else iv.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    Object tag = tv.getTag();
                    if (tag != null && "seat_number".equals(String.valueOf(tag))) { tv.setText(String.valueOf(seatIndex + 1)); tv.setVisibility(View.VISIBLE); tv.setTextColor(Color.WHITE); }
                    else { if (isOccupied && uname != null && !uname.isEmpty()) { tv.setText(uname); tv.setVisibility(View.VISIBLE); } else { tv.setText(""); tv.setVisibility(View.GONE); } }
                }
            }
        }
    }

    // ===== HEADER - FUL BORO POPUP - PROFILE + MEMBERS =====
    private void showHeaderProfileMembersDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#FFF8E1"));
        root.setPadding(0, 0, 0, 0);

        // TOP TABS - Profile / Members
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(24, 18, 24, 8);
        tabs.setWeightSum(2);
        TextView tabProfile = new TextView(this);
        tabProfile.setText("Profile");
        tabProfile.setTextSize(16); tabProfile.setTypeface(null, android.graphics.Typeface.BOLD);
        tabProfile.setTextColor(Color.parseColor("#222222")); tabProfile.setGravity(Gravity.CENTER);
        tabProfile.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView tabMembers = new TextView(this);
        tabMembers.setText("Members");
        tabMembers.setTextSize(16); tabMembers.setTextColor(Color.parseColor("#999999")); tabMembers.setGravity(Gravity.CENTER);
        tabMembers.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tabs.addView(tabProfile); tabs.addView(tabMembers);
        root.addView(tabs);

        LinearLayout profileBox = new LinearLayout(this);
        profileBox.setOrientation(LinearLayout.VERTICAL);
        profileBox.setPadding(24, 8, 24, 16);
        root.addView(profileBox);

        LinearLayout membersBox = new LinearLayout(this);
        membersBox.setOrientation(LinearLayout.VERTICAL);
        membersBox.setPadding(24, 8, 24, 16);
        membersBox.setVisibility(View.GONE);
        root.addView(membersBox);

        tabProfile.setOnClickListener(v -> { tabProfile.setTextColor(Color.parseColor("#222222")); tabMembers.setTextColor(Color.parseColor("#999999")); profileBox.setVisibility(View.VISIBLE); membersBox.setVisibility(View.GONE); });
        tabMembers.setOnClickListener(v -> { tabMembers.setTextColor(Color.parseColor("#222222")); tabProfile.setTextColor(Color.parseColor("#999999")); profileBox.setVisibility(View.GONE); membersBox.setVisibility(View.VISIBLE); loadMembersInto(membersBox, dialog); });

        // PROFILE CONTENT - OWNER ER TA
        userDatabaseRef.orderByChild("my9DigitId").equalTo(roomOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                String name = roomTitleText != null ? roomTitleText.getText().toString() : "Room Owner";
                String country = myCountry, gender = "Male", photoBase64 = "";
                if (snap.exists()) {
                    for (DataSnapshot ds : snap.getChildren()) {
                        if (ds.child("name").getValue() != null) name = String.valueOf(ds.child("name").getValue());
                        if (ds.child("country").getValue() != null) country = String.valueOf(ds.child("country").getValue());
                        if (ds.child("gender").getValue() != null) gender = String.valueOf(ds.child("gender").getValue());
                        if (ds.child("photoBase64").getValue() != null) photoBase64 = String.valueOf(ds.child("photoBase64").getValue());
                        break;
                    }
                }
                profileBox.removeAllViews();
                LinearLayout top = new LinearLayout(VoiceRoomActivity.this);
                top.setOrientation(LinearLayout.HORIZONTAL);
                top.setGravity(Gravity.CENTER_VERTICAL);
                profileBox.addView(top);
                ImageView avatar = new ImageView(VoiceRoomActivity.this);
                avatar.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
                avatar.setImageResource(android.R.drawable.ic_menu_gallery);
                if (!photoBase64.isEmpty()) {
                    try { byte[] bts = Base64.decode(photoBase64, Base64.DEFAULT); Bitmap b = BitmapFactory.decodeByteArray(bts, 0, bts.length); if (b != null) avatar.setImageBitmap(b); } catch (Exception ignored) {}
                }
                top.addView(avatar);
                LinearLayout nameBox = new LinearLayout(VoiceRoomActivity.this);
                nameBox.setOrientation(LinearLayout.VERTICAL);
                nameBox.setPadding(24, 0, 0, 0);
                nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                top.addView(nameBox);
                TextView tvName = new TextView(VoiceRoomActivity.this);
                tvName.setText(name);
                tvName.setTextColor(Color.parseColor("#222222")); tvName.setTextSize(19); tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                nameBox.addView(tvName);
                TextView tvId = new TextView(VoiceRoomActivity.this);
                tvId.setText("ID: " + roomOwnerId + "  •  Level: 1");
                tvId.setTextColor(Color.parseColor("#888888")); tvId.setTextSize(13);
                nameBox.addView(tvId);
                TextView tvCountry = new TextView(VoiceRoomActivity.this);
                tvCountry.setText(country + "  •  " + gender);
                tvCountry.setTextColor(Color.parseColor("#FF9800")); tvCountry.setTextSize(13);
                nameBox.addView(tvCountry);
                // FOLLOW / FANS ROW
                TextView tvFollow = new TextView(VoiceRoomActivity.this);
                tvFollow.setText("1 Following   •   1 Fans   •   70 Visitor");
                tvFollow.setTextColor(Color.parseColor("#666666")); tvFollow.setTextSize(12);
                tvFollow.setPadding(0, 12, 0, 0);
                profileBox.addView(tvFollow);
                // BANNER - REWARD ER JONNO - EKHON DEMO
                TextView banner = new TextView(VoiceRoomActivity.this);
                banner.setText("🏆  Reward Event  •  Weekly Contribution  •  Coming Soon");
                banner.setTextColor(Color.WHITE); banner.setTextSize(14); banner.setTypeface(null, android.graphics.Typeface.BOLD);
                banner.setGravity(Gravity.CENTER);
                banner.setBackgroundColor(Color.parseColor("#FF9800"));
                banner.setPadding(16, 24, 16, 24);
                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bp.setMargins(0, 16, 0, 8);
                banner.setLayoutParams(bp);
                profileBox.addView(banner);
                TextView tvLevel = new TextView(VoiceRoomActivity.this);
                tvLevel.setText("Level: Lv0    •    RoomOwner: " + name);
                tvLevel.setTextColor(Color.parseColor("#333333")); tvLevel.setTextSize(13);
                profileBox.addView(tvLevel);
                TextView tvLang = new TextView(VoiceRoomActivity.this);
                tvLang.setText("Language: English    •    Country: " + country + "    •    Tag: Friends");
                tvLang.setTextColor(Color.parseColor("#666666")); tvLang.setTextSize(12);
                tvLang.setPadding(0, 8, 0, 0);
                profileBox.addView(tvLang);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        dialog.setContentView(root);
        setDialog75(dialog);
        dialog.show();
    }

    private void loadMembersInto(LinearLayout box, BottomSheetDialog dialog) {
        box.removeAllViews();
        TextView title = new TextView(this);
        title.setText("Members");
        title.setTextColor(Color.parseColor("#888888")); title.setTextSize(13); title.setTypeface(null, android.graphics.Typeface.BOLD);
        box.addView(title);
        EditText search = new EditText(this);
        search.setHint("Search ID");
        search.setTextColor(Color.parseColor("#222222"));
        search.setBackgroundColor(Color.parseColor("#FFFFFF"));
        search.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.setMargins(0, 12, 0, 12);
        search.setLayoutParams(sp);
        box.addView(search);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        box.addView(list);
        databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                list.removeAllViews();
                // 1. OWNER AGE
                addMemberRow(list, roomOwnerId, getMyDisplayName().equals("") ? "Owner" : "Owner", "👑 Owner", dialog);
                // 2. ADMIN RA SERIAL E
                for (String adminId : roomAdminList) {
                    if (!adminId.equals(roomOwnerId)) addMemberRow(list, adminId, "Admin " + adminId, "🛡️ Admin", dialog);
                }
                // 3. MEMBERS
                for (DataSnapshot ds : s.getChildren()) {
                    String uid = ds.getKey();
                    if (uid == null) continue;
                    if (uid.equals(roomOwnerId) || roomAdminList.contains(uid)) continue;
                    String nm = ds.getValue(String.class);
                    if (nm == null) nm = "User";
                    addMemberRow(list, uid, nm, "👤 Member", dialog);
                }
                if (s.getChildrenCount() == 0) {
                    TextView tv = new TextView(VoiceRoomActivity.this);
                    tv.setText("No members yet");
                    tv.setTextColor(Color.parseColor("#AAAAAA"));
                    list.addView(tv);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addMemberRow(LinearLayout list, String uid, String name, String role, BottomSheetDialog dialog) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(12, 14, 12, 14);
        row.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rp.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rp);
        TextView tvAvatar = new TextView(this);
        tvAvatar.setText("👤");
        tvAvatar.setTextSize(22);
        tvAvatar.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
        row.addView(tvAvatar);
        LinearLayout txtBox = new LinearLayout(this);
        txtBox.setOrientation(LinearLayout.VERTICAL);
        txtBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(txtBox);
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#222222")); tvName.setTextSize(14); tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        txtBox.addView(tvName);
        TextView tvId = new TextView(this);
        tvId.setText("ID: " + uid + "  •  " + role);
        tvId.setTextColor(Color.parseColor("#888888")); tvId.setTextSize(12);
        txtBox.addView(tvId);
        row.setOnClickListener(v -> { dialog.dismiss(); showMiniProfileCard(uid); });
        list.addView(row);
    }

    private void showMiniProfileCard(String target9DigitId) {
        userDatabaseRef.orderByChild("my9DigitId").equalTo(target9DigitId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                String name = "User", country = myCountry, gender = "Male", photoBase64 = "";
                String targetId = target9DigitId; String level = "1", giftCount = "0";
                if (snap.exists()) {
                    for (DataSnapshot ds : snap.getChildren()) {
                        if (ds.child("name").getValue() != null) name = String.valueOf(ds.child("name").getValue());
                        if (ds.child("country").getValue() != null) country = String.valueOf(ds.child("country").getValue());
                        if (ds.child("gender").getValue() != null) gender = String.valueOf(ds.child("gender").getValue());
                        if (ds.child("photoBase64").getValue() != null) photoBase64 = String.valueOf(ds.child("photoBase64").getValue());
                        if (ds.child("my9DigitId").getValue() != null) targetId = String.valueOf(ds.child("my9DigitId").getValue());
                        if (ds.child("level").getValue() != null) level = String.valueOf(ds.child("level").getValue());
                        if (ds.child("giftCount").getValue() != null) giftCount = String.valueOf(ds.child("giftCount").getValue());
                        break;
                    }
                }
                UserModel um = new UserModel(); um.name = name; um.country = country; um.gender = gender;
                BottomSheetDialog dialog = new BottomSheetDialog(VoiceRoomActivity.this);
                LinearLayout root = new LinearLayout(VoiceRoomActivity.this);
                root.setOrientation(LinearLayout.VERTICAL);
                root.setPadding(40, 30, 40, 40);
                GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#FFF8E1"), Color.parseColor("#FFFFFF")});
                bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0});
                root.setBackground(bg);
                LinearLayout topRow = new LinearLayout(VoiceRoomActivity.this);
                topRow.setOrientation(LinearLayout.HORIZONTAL); topRow.setGravity(Gravity.CENTER_VERTICAL);
                root.addView(topRow);
                ImageView avatar = new ImageView(VoiceRoomActivity.this);
                avatar.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
                avatar.setImageResource(android.R.drawable.ic_menu_gallery);
                if (!photoBase64.isEmpty()) {
                    try { byte[] bytes = Base64.decode(photoBase64, Base64.DEFAULT); Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); if (b != null) avatar.setImageBitmap(b); } catch (Exception ignored) {}
                }
                topRow.addView(avatar);
                LinearLayout nameBox = new LinearLayout(VoiceRoomActivity.this);
                nameBox.setOrientation(LinearLayout.VERTICAL); nameBox.setPadding(30, 0, 0, 0);
                nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                topRow.addView(nameBox);
                boolean isMale = gender.equalsIgnoreCase("Male");
                String flag = um.getFlag(); String genderIcon = um.getGenderIcon();
                int genderColor = isMale ? Color.parseColor("#2196F3") : Color.parseColor("#E91E63");
                TextView txtName = new TextView(VoiceRoomActivity.this);
                txtName.setText(name + "  " + flag + "  " + genderIcon);
                txtName.setTextColor(Color.parseColor("#222222")); txtName.setTextSize(18); txtName.setTypeface(null, android.graphics.Typeface.BOLD);
                nameBox.addView(txtName);
                TextView txtId = new TextView(VoiceRoomActivity.this);
                txtId.setText("ID: " + targetId + "   Level: " + level);
                txtId.setTextColor(Color.parseColor("#777777")); txtId.setTextSize(13);
                nameBox.addView(txtId);
                TextView txtCountry = new TextView(VoiceRoomActivity.this);
                txtCountry.setText(country + "  •  Gift: " + giftCount);
                txtCountry.setTextColor(genderColor); txtCountry.setTextSize(13);
                nameBox.addView(txtCountry);
                LinearLayout btnRow = new LinearLayout(VoiceRoomActivity.this);
                btnRow.setOrientation(LinearLayout.HORIZONTAL); btnRow.setPadding(0, 30, 0, 0);
                root.addView(btnRow);
                Button btnFollow = new Button(VoiceRoomActivity.this);
                btnFollow.setText(target9DigitId.equals(currentUserId) ? "My Profile" : "+ Follow");
                btnFollow.setTextColor(Color.WHITE); btnFollow.setAllCaps(false); btnFollow.setBackgroundColor(Color.parseColor("#FF9800"));
                btnFollow.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                String finalName = name, finalId = targetId;
                btnFollow.setOnClickListener(v -> {
                    dialog.dismiss();
                    Intent intent = new Intent(VoiceRoomActivity.this, PublicProfileActivity.class);
                    intent.putExtra("TARGET_9DIGIT_ID", finalId); intent.putExtra("TARGET_NAME", finalName);
                    try { startActivity(intent); } catch (Exception e) { Toast.makeText(VoiceRoomActivity.this, "Full Profile Coming Soon", Toast.LENGTH_SHORT).show(); }
                });
                btnRow.addView(btnFollow);
                Button btnGift = new Button(VoiceRoomActivity.this);
                btnGift.setText("🎁 Gift"); btnGift.setTextColor(Color.WHITE); btnGift.setAllCaps(false); btnGift.setBackgroundColor(Color.parseColor("#E91E63"));
                btnGift.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                btnGift.setOnClickListener(v -> { dialog.dismiss(); showNewGiftPanel(); });
                btnRow.addView(btnGift);
                dialog.setContentView(root);
                setDialog75(dialog);
                dialog.show();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
