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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
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
    private ImageButton btnPower;
    private Button btnMicToggle, btnChatOption, btnGiftBox, btnGame, btnRoomTools;
    private TextView roomTitleText, roomInfoText, textOnlineCount, roomNoticeBarText, textRoomCoins;
    private TextView tabAll, tabChat, tabGift, tabNotice, textChatEmpty;
    private EditText inputChatMessage;
    private LinearLayout btnInRoomRanking, chatListContainer;
    private ImageView btnOnlineList, roomHeaderProfileImg;
    private boolean isMuted = false;
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
        listenToChatMessages();

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

        if (btnInRoomRanking != null) {
            btnInRoomRanking.setOnClickListener(v -> showInRoomRankingDialog());
        }

        if (btnOnlineList != null) {
            btnOnlineList.setOnClickListener(v -> showOnlineListDialog());
        }

        if (roomHeaderProfileImg != null) {
            roomHeaderProfileImg.setOnClickListener(v -> showMiniProfileCard(currentUserId));
        }

        setupChatTabs();
        setupBottomChatSend();

        if (btnGame != null) {
            btnGame.setOnClickListener(v -> Toast.makeText(this, "Games Coming in Step 3 🎮", Toast.LENGTH_SHORT).show());
        }

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
            if (inputChatMessage != null && !inputChatMessage.getText().toString().trim().isEmpty()) {
                sendChatFromInput();
            } else {
                showChatDialog();
            }
        });

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) btnGiftBox.setOnClickListener(v -> showNewGiftPanel());
    }

    @Override public void onBackPressed() { showExitDialog(); }

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
        listenToChatMessages();
    }

    private void setupBottomChatSend() {
        if (inputChatMessage == null) return;
        inputChatMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendChatFromInput();
            return true;
        });
    }

    private void sendChatFromInput() {
        if (inputChatMessage == null) return;
        String msg = inputChatMessage.getText().toString().trim();
        if (msg.isEmpty()) return;
        sendChatMessage(msg);
        inputChatMessage.setText("");
    }

    private void sendChatMessage(String msg) {
        if (messagesRef == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUserId);
        data.put("userName", getMyDisplayName());
        data.put("text", msg);
        data.put("type", "Chat");
        data.put("time", System.currentTimeMillis());
        messagesRef.push().setValue(data);
    }

    private void listenToChatMessages() {
        if (messagesRef == null || chatListContainer == null) return;
        messagesRef.limitToLast(30).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                chatListContainer.removeAllViews();
                boolean hasAny = false;
                for (DataSnapshot ds : s.getChildren()) {
                    String type = ds.child("type").getValue(String.class);
                    if (type == null) type = "Chat";
                    if (!currentChatFilter.equals("All") && !currentChatFilter.equals(type)) continue;
                    String uname = ds.child("userName").getValue(String.class);
                    String txt = ds.child("text").getValue(String.class);
                    if (txt == null) txt = "";
                    if (uname == null) uname = "User";
                    TextView tv = new TextView(VoiceRoomActivity.this);
                    tv.setText(uname + ": " + txt);
                    tv.setTextColor(type.equals("Gift") ? Color.parseColor("#FFD700") : Color.WHITE);
                    tv.setTextSize(13);
                    tv.setPadding(0, 4, 0, 4);
                    chatListContainer.addView(tv);
                    hasAny = true;
                }
                if (!hasAny) {
                    TextView empty = new TextView(VoiceRoomActivity.this);
                    empty.setText("wlc");
                    empty.setTextColor(Color.parseColor("#AAAAAA"));
                    empty.setTextSize(13);
                    chatListContainer.addView(empty);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showOnlineListDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 25, 35, 35);
        root.setBackgroundColor(Color.parseColor("#1B1035"));
        TextView title = new TextView(this);
        title.setText("Online Members");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);
        databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (DataSnapshot ds : s.getChildren()) {
                    String name = ds.getValue(String.class);
                    if (name == null) name = "User";
                    TextView tv = new TextView(VoiceRoomActivity.this);
                    tv.setText("• " + name + "  (ID: " + ds.getKey() + ")");
                    tv.setTextColor(Color.WHITE);
                    tv.setTextSize(14);
                    tv.setPadding(10, 10, 10, 10);
                    root.addView(tv);
                }
                if (s.getChildrenCount() == 0) {
                    TextView tv = new TextView(VoiceRoomActivity.this);
                    tv.setText("No one online");
                    tv.setTextColor(Color.parseColor("#AAAAAA"));
                    root.addView(tv);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        dialog.setContentView(root);
        dialog.show();
    }

    private void showExitDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Exit Room?");
        b.setMessage("Exit room or Minimize to Home?");
        b.setPositiveButton("Exit Room", (d, w) -> {
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
        if (currentUserId != null && !currentUserId.isEmpty()) {
            databaseReference.child("online_users").child(currentUserId).removeValue();
            for (int i = 0; i < 10; i++) {
                final int idx = i;
                databaseReference.child("seats").child("seat_" + idx).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s) {
                        String uid = s.child("userId").getValue(String.class);
                        if (uid != null && uid.equals(currentUserId)) {
                            databaseReference.child("seats").child("seat_" + idx).removeValue();
                        }
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
        startActivity(intent);
        finish();
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

    private void showIconToolsDialog(boolean isOwner) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 25, 35, 25);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);
        TextView title = new TextView(this);
        title.setText("⚙️ Room Tools & Settings");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);
        Button btnEditRoom = createToolButton("✏️ Edit Room Info & Theme");
        btnEditRoom.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Room Edit Dialog Active", Toast.LENGTH_SHORT).show();
        });
        root.addView(btnEditRoom);
        Button btnClearChat = createToolButton("🧹 Clear Chat History");
        btnClearChat.setOnClickListener(v -> {
            if (messagesRef != null) {
                messagesRef.removeValue();
                Toast.makeText(this, "Chat Cleared!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        root.addView(btnClearChat);
        Button btnLockRoom = createToolButton("🔒 Room Lock Settings");
        btnLockRoom.setOnClickListener(v -> {
            Toast.makeText(this, "Room Lock Active", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        root.addView(btnLockRoom);
        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.75));
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
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
        LinearLayout podiumLayout = new LinearLayout(this);
        podiumLayout.setOrientation(LinearLayout.HORIZONTAL);
        podiumLayout.setGravity(Gravity.CENTER);
        podiumLayout.setPadding(0, 10, 0, 25);
        podiumLayout.setWeightSum(3);
        podiumLayout.addView(createPodiumItem("2", "User Two", "50,000 Coins", Color.parseColor("#B0BEC5")));
        podiumLayout.addView(createPodiumItem("1", "User One", "120,000 Coins", Color.parseColor("#FFD700")));
        podiumLayout.addView(createPodiumItem("3", "User Three", "30,000 Coins", Color.parseColor("#CD7F32")));
        root.addView(podiumLayout);
        ScrollView sv = new ScrollView(this);
        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);
        for (int i = 4; i <= 15; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(20, 15, 20, 15);
            row.setBackgroundColor(Color.parseColor("#140B26"));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 6, 0, 6);
            row.setLayoutParams(rowParams);
            TextView tvRank = new TextView(this);
            tvRank.setText(String.valueOf(i));
            tvRank.setTextColor(Color.parseColor("#B0BEC5"));
            tvRank.setTextSize(14);
            tvRank.setTypeface(null, android.graphics.Typeface.BOLD);
            tvRank.setLayoutParams(new LinearLayout.LayoutParams(60, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(tvRank);
            TextView tvName = new TextView(this);
            tvName.setText("Contributor User " + i);
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(14);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tvName);
            TextView tvCoins = new TextView(this);
            tvCoins.setText((10000 - (i * 500)) + " Coins");
            tvCoins.setTextColor(Color.parseColor("#00E676"));
            tvCoins.setTextSize(13);
            row.addView(tvCoins);
            listLayout.addView(row);
        }
        sv.addView(listLayout);
        root.addView(sv);
        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.85));
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private View createPodiumItem(String rank, String name, String coins, int borderColor) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        item.setLayoutParams(params);
        TextView tvRankBadge = new TextView(this);
        tvRankBadge.setText("Top " + rank);
        tvRankBadge.setTextColor(borderColor);
        tvRankBadge.setTextSize(12);
        tvRankBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        item.addView(tvRankBadge);
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(13);
        tvName.setPadding(0, 4, 0, 2);
        item.addView(tvName);
        TextView tvCoins = new TextView(this);
        tvCoins.setText(coins);
        tvCoins.setTextColor(Color.parseColor("#FFD700"));
        tvCoins.setTextSize(11);
        item.addView(tvCoins);
        return item;
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
        info.setText("Full Gift Store Coming in Step 3 🎁");
        info.setTextColor(Color.parseColor("#AAAAAA"));
        info.setTextSize(13);
        info.setGravity(Gravity.CENTER);
        root.addView(info);
        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.5));
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private void showChatDialog() {
        if (inputChatMessage != null) {
            inputChatMessage.requestFocus();
            Toast.makeText(this, "Type below 👇", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Chat Box", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenToRoomAdmins() {
        adminsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                roomAdminList.clear();
                for (DataSnapshot ds : s.getChildren()) {
                    if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) roomAdminList.add(ds.getKey());
                }
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
                    if (dbName != null && !dbName.isEmpty()) {
                        myDisplayName = dbName;
                        prefs.edit().putString("USER_NAME", dbName).apply();
                    }
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
                            if (uid.equals(currentUserId)) {
                                showSeatActionSheet(idx, true);
                            } else {
                                showMiniProfileCard(uid);
                            }
                        } else {
                            showSeatActionSheet(idx, false);
                        }
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
        View handle = new View(this);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(120, 10);
        hp.gravity = Gravity.CENTER_HORIZONTAL;
        hp.setMargins(0, 0, 0, 25);
        handle.setLayoutParams(hp);
        GradientDrawable hbg = new GradientDrawable();
        hbg.setColor(Color.parseColor("#E0E0E0"));
        hbg.setCornerRadius(5);
        handle.setBackground(hbg);
        root.addView(handle);
        TextView txtSeatNo = new TextView(this);
        txtSeatNo.setText("Seat " + (seatIndex + 1));
        txtSeatNo.setTextColor(Color.parseColor("#888888"));
        txtSeatNo.setTextSize(13);
        txtSeatNo.setGravity(Gravity.CENTER);
        txtSeatNo.setPadding(0, 0, 0, 15);
        root.addView(txtSeatNo);
        Button btnAction = new Button(this);
        btnAction.setText(isMySeat ? "Leave Mic" : "Take Mic");
        btnAction.setTextColor(Color.WHITE);
        btnAction.setTextSize(16);
        btnAction.setAllCaps(false);
        btnAction.setTypeface(null, android.graphics.Typeface.BOLD);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor(isMySeat ? "#FF5252" : "#FF9800"));
        btnBg.setCornerRadius(60);
        btnAction.setBackground(btnBg);
        btnAction.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 130));
        btnAction.setOnClickListener(v -> {
            dialog.dismiss();
            if (isMySeat) leaveSeat(seatIndex);
            else takeSeat(seatIndex);
        });
        root.addView(btnAction);
        Button btnCancel = new Button(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextColor(Color.parseColor("#888888"));
        btnCancel.setTextSize(14);
        btnCancel.setAllCaps(false);
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
                    if (ouid != null && ouid.equals(currentUserId)) {
                        databaseReference.child("seats").child("seat_" + sj).removeValue();
                    }
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
                    if (isOccupied) {
                        imgBg.setColor(Color.parseColor("#FFFFFF"));
                        imgBg.setStroke(4, Color.parseColor(isMine ? "#FF9800" : "#FFFFFF"));
                    } else {
                        imgBg.setColor(Color.parseColor("#40000000"));
                        imgBg.setStroke(3, Color.parseColor("#FFFFFF"));
                    }
                    iv.setBackground(imgBg);
                    iv.setPadding(4, 4, 4, 4);
                    if (isOccupied) {
                        boolean set = false;
                        if (photoBase64 != null && !photoBase64.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(photoBase64, Base64.DEFAULT);
                                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if (b != null) { iv.setImageBitmap(b); set = true; }
                            } catch (Exception ignored) {}
                        }
                        if (!set) iv.setImageResource(android.R.drawable.ic_menu_gallery);
                    } else {
                        int sofaId = getResources().getIdentifier("seat_sofa", "drawable", getPackageName());
                        if (sofaId != 0) iv.setImageResource(sofaId);
                        else iv.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    Object tag = tv.getTag();
                    if (tag != null && "seat_number".equals(String.valueOf(tag))) {
                        tv.setText(String.valueOf(seatIndex + 1));
                        tv.setVisibility(View.VISIBLE);
                        tv.setTextColor(Color.WHITE);
                    } else {
                        if (isOccupied && uname != null && !uname.isEmpty()) {
                            tv.setText(uname);
                            tv.setVisibility(View.VISIBLE);
                        } else {
                            tv.setText("");
                            tv.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    private void showMiniProfileCard(String target9DigitId) {
        userDatabaseRef.orderByChild("my9DigitId").equalTo(target9DigitId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    String name = "User", country = myCountry, gender = "Male", photoBase64 = "";
                    String targetId = target9DigitId;
                    String level = "1", giftCount = "0";
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
                    UserModel um = new UserModel();
                    um.name = name; um.country = country; um.gender = gender;
                    BottomSheetDialog dialog = new BottomSheetDialog(VoiceRoomActivity.this);
                    LinearLayout root = new LinearLayout(VoiceRoomActivity.this);
                    root.setOrientation(LinearLayout.VERTICAL);
                    root.setPadding(40, 30, 40, 40);
                    GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                            new int[]{Color.parseColor("#FFF8E1"), Color.parseColor("#FFFFFF")});
                    bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0});
                    root.setBackground(bg);
                    LinearLayout topRow = new LinearLayout(VoiceRoomActivity.this);
                    topRow.setOrientation(LinearLayout.HORIZONTAL);
                    topRow.setGravity(Gravity.CENTER_VERTICAL);
                    root.addView(topRow);
                    ImageView avatar = new ImageView(VoiceRoomActivity.this);
                    avatar.setLayoutParams(new LinearLayout.LayoutParams(160, 160));
                    avatar.setImageResource(android.R.drawable.ic_menu_gallery);
                    if (!photoBase64.isEmpty()) {
                        try {
                            byte[] bytes = Base64.decode(photoBase64, Base64.DEFAULT);
                            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            if (b != null) avatar.setImageBitmap(b);
                        } catch (Exception ignored) {}
                    }
                    topRow.addView(avatar);
                    LinearLayout nameBox = new LinearLayout(VoiceRoomActivity.this);
                    nameBox.setOrientation(LinearLayout.VERTICAL);
                    nameBox.setPadding(30, 0, 0, 0);
                    nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                    topRow.addView(nameBox);
                    boolean isMale = gender.equalsIgnoreCase("Male");
                    String flag = um.getFlag();
                    String genderIcon = um.getGenderIcon();
                    int genderColor = isMale ? Color.parseColor("#2196F3") : Color.parseColor("#E91E63");
                    TextView txtName = new TextView(VoiceRoomActivity.this);
                    txtName.setText(name + "  " + flag + "  " + genderIcon);
                    txtName.setTextColor(Color.parseColor("#222222"));
                    txtName.setTextSize(18);
                    txtName.setTypeface(null, android.graphics.Typeface.BOLD);
                    nameBox.addView(txtName);
                    TextView txtId = new TextView(VoiceRoomActivity.this);
                    txtId.setText("ID: " + targetId + "   Level: " + level);
                    txtId.setTextColor(Color.parseColor("#777777"));
                    txtId.setTextSize(13);
                    nameBox.addView(txtId);
                    TextView txtCountry = new TextView(VoiceRoomActivity.this);
                    txtCountry.setText(country + "  •  Gift: " + giftCount);
                    txtCountry.setTextColor(genderColor);
                    txtCountry.setTextSize(13);
                    nameBox.addView(txtCountry);
                    LinearLayout btnRow = new LinearLayout(VoiceRoomActivity.this);
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);
                    btnRow.setPadding(0, 30, 0, 0);
                    root.addView(btnRow);
                    Button btnFollow = new Button(VoiceRoomActivity.this);
                    btnFollow.setText(target9DigitId.equals(currentUserId) ? "My Profile" : "+ Follow");
                    btnFollow.setTextColor(Color.WHITE);
                    btnFollow.setAllCaps(false);
                    btnFollow.setBackgroundColor(Color.parseColor("#FF9800"));
                    btnFollow.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                    String finalName = name, finalId = targetId;
                    btnFollow.setOnClickListener(v -> {
                        dialog.dismiss();
                        Intent intent = new Intent(VoiceRoomActivity.this, PublicProfileActivity.class);
                        intent.putExtra("TARGET_9DIGIT_ID", finalId);
                        intent.putExtra("TARGET_NAME", finalName);
                        try { startActivity(intent); } catch (Exception e) {
                            Toast.makeText(VoiceRoomActivity.this, "Full Profile Coming in Step 2", Toast.LENGTH_SHORT).show();
                        }
                    });
                    btnRow.addView(btnFollow);
                    Button btnGift = new Button(VoiceRoomActivity.this);
                    btnGift.setText("🎁 Gift");
                    btnGift.setTextColor(Color.WHITE);
                    btnGift.setAllCaps(false);
                    btnGift.setBackgroundColor(Color.parseColor("#E91E63"));
                    btnGift.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                    btnGift.setOnClickListener(v -> {
                        dialog.dismiss();
                        showNewGiftPanel();
                    });
                    btnRow.addView(btnGift);
                    dialog.setContentView(root);
                    dialog.show();
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }

    private String getFlagEmoji(String country) {
        UserModel um = new UserModel();
        um.country = country;
        return um.getFlag();
    }
}
