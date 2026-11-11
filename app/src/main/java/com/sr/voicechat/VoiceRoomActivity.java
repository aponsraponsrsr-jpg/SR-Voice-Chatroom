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
    private Button btnMicToggle, btnChatOption, btnGiftBox;
    private TextView roomTitleText, roomInfoText, textOnlineCount, roomNoticeBarText;
    private ImageView btnOnlineList, roomHeaderProfileImg, btnRoomTools;
    private LinearLayout btnInRoomRanking;
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

        btnPower = findViewById(R.id.btnPower);
        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        textOnlineCount = findViewById(R.id.textOnlineCount);
        btnOnlineList = findViewById(R.id.btnOnlineList);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
        btnInRoomRanking = findViewById(R.id.btnInRoomRanking);
        roomNoticeBarText = findViewById(R.id.roomNoticeBarText);
        btnRoomTools = findViewById(R.id.btnRoomTools);

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

        setupSeatInteractions();
        listenToFirebaseSeats();
        listenToOnlineMembers();

        btnMicToggle = findViewById(R.id.btnMicToggle);
        if (btnMicToggle != null) btnMicToggle.setOnClickListener(v -> {
            isMuted = !isMuted;
            btnMicToggle.setText(isMuted ? "Muted" : "Mic");
        });

        btnChatOption = findViewById(R.id.btnChatOption);
        if (btnChatOption != null) btnChatOption.setOnClickListener(v -> showChatDialog());

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) btnGiftBox.setOnClickListener(v -> showNewGiftPanel());
    }

    @Override public void onBackPressed() { showExitDialog(); }

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
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToRoomTheme() {
        if (roomMetaRef == null) return;
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String n = s.child("roomTheme").getValue(String.class);
                if (n != null && !n.isEmpty()) {
                    currentThemeName = n;
                }
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
        Toast.makeText(this, "Chat Box", Toast.LENGTH_SHORT).show();
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

    // ===== SEAT - PERMANENT - Click = Profile, Empty = Sit =====
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
                            // Karo seat - Mini Profile Card khulbe - DEMO er moto
                            showMiniProfileCard(uid);
                        } else {
                            Map<String, Object> seatData = new HashMap<>();
                            seatData.put("userId", currentUserId);
                            seatData.put("userName", getMyDisplayName());
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
                            databaseReference.child("seats").child("seat_" + idx).setValue(seatData);
                            Toast.makeText(VoiceRoomActivity.this, "Seated Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            });
        }
    }

    private void listenToFirebaseSeats() {
        databaseReference.child("seats").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                for (int i = 0; i < 10; i++) {
                    int resId = getResources().getIdentifier("seat_" + i, "id", getPackageName());
                    if (resId == 0) continue;
                    View seat = findViewById(resId);
                    if (seat == null) continue;
                    String uname = s.child("seat_" + i).child("userName").getValue(String.class);
                    if (seat instanceof ViewGroup) {
                        ViewGroup vg = (ViewGroup) seat;
                        for (int c = 0; c < vg.getChildCount(); c++) {
                            View child = vg.getChildAt(c);
                            if (child instanceof TextView && uname != null && !uname.isEmpty()) {
                                ((TextView) child).setText(uname);
                            }
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // ===== STEP 1 - MINI PROFILE CARD - DEMO ER MOTO - PERMANENT =====
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
                    avatar.setImageResource(android.R.drawable.ic_menu_camera);
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
