package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.sr.voicechat.adapters.GiftAdapter;
import com.sr.voicechat.models.GiftModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageButton btnPower;
    private Button btnMicToggle, btnChatOption, btnGiftBox, btnGameBox;
    private TextView roomTitleText, roomInfoText, textOnlineCount, roomNoticeBarText;
    private ImageView btnOnlineList, roomHeaderProfileImg, btnRoomTools;
    private LinearLayout btnInRoomRanking;
    private boolean isMuted = false;

    private ListView chatListView;
    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private SharedPreferences prefs;

    private DatabaseReference databaseReference;
    private DatabaseReference roomMetaRef;
    private DatabaseReference userDatabaseRef;
    private DatabaseReference messagesRef;
    private DatabaseReference adminsRef;
    private FirebaseAuth mAuth;
    private String roomOwnerId = "";
    private String currentUserId = "";

    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText dialogInputName, dialogInputBio;
    private long roomJoinTimestamp = 0;

    private List<String> roomAdminList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        currentUserId = prefs.getString("MY_9_DIGIT_ID", "");
        if (currentUserId.isEmpty()) {
            currentUserId = String.valueOf((int)(Math.random() * 900000000) + 100000000);
            prefs.edit().putString("MY_9_DIGIT_ID", currentUserId).apply();
        }
        
        roomOwnerId = getIntent().getStringExtra("ROOM_OWNER_ID");
        if (roomOwnerId == null || roomOwnerId.isEmpty()) {
            roomOwnerId = currentUserId;
        }

        roomJoinTimestamp = System.currentTimeMillis();

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + roomOwnerId);
        roomMetaRef = databaseReference.child("meta");
        messagesRef = databaseReference.child("messages");
        adminsRef = databaseReference.child("admins");
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        syncLatestUserProfile();
        listenToRoomDetails();
        listenToRoomAdmins();

        btnPower = findViewById(R.id.btnPower);
        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        textOnlineCount = findViewById(R.id.textOnlineCount);
        btnOnlineList = findViewById(R.id.btnOnlineList);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
        btnInRoomRanking = findViewById(R.id.btnInRoomRanking);
        roomNoticeBarText = findViewById(R.id.roomNoticeBarText);
        btnRoomTools = findViewById(R.id.btnRoomTools);

        if (btnPower != null) {
            btnPower.setOnClickListener(v -> {
                CharSequence[] options = {"Minimize", "Exit Room"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("Room Options");
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        moveTaskToBack(true);
                    } else {
                        leaveRoomAndFinish();
                    }
                });
                builder.show();
            });
        }

        if (btnRoomTools != null) {
            btnRoomTools.setOnClickListener(v -> {
                boolean isOwner = currentUserId.equals(roomOwnerId);
                boolean isAdmin = roomAdminList.contains(currentUserId);

                if (!isOwner && !isAdmin) {
                    Toast.makeText(this, "Only Room Owner or Admins can access Room Tools!", Toast.LENGTH_SHORT).show();
                    return;
                }

                BottomSheetDialog toolsSheet = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                View sheetView = getLayoutInflater().inflate(R.layout.dialog_room_tools, null);
                toolsSheet.setContentView(sheetView);

                LinearLayout toolRoomLock = sheetView.findViewById(R.id.toolRoomLock);
                if (toolRoomLock != null) {
                    toolRoomLock.setOnClickListener(view -> {
                        toolsSheet.dismiss();
                        showRoomLockPasswordDialog();
                    });
                }

                LinearLayout toolCleanChat = sheetView.findViewById(R.id.toolCleanChat);
                if (toolCleanChat != null) {
                    toolCleanChat.setOnClickListener(view -> {
                        messagesRef.removeValue().addOnCompleteListener(task -> {
                            chatMessages.clear();
                            chatAdapter.notifyDataSetChanged();
                            Toast.makeText(VoiceRoomActivity.this, "Chat history cleaned globally!", Toast.LENGTH_SHORT).show();
                        });
                        toolsSheet.dismiss();
                    });
                }

                LinearLayout toolRoomSettings = sheetView.findViewById(R.id.toolRoomSettings);
                if (toolRoomSettings != null) {
                    toolRoomSettings.setOnClickListener(view -> {
                        toolsSheet.dismiss();
                        if (isOwner) {
                            showEditRoomDialog();
                        } else {
                            Toast.makeText(this, "Only Room Owner can edit room settings!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                toolsSheet.show();
            });
        }

        // 🏆 র‍্যাঙ্কিং ক্লিক
        if (btnInRoomRanking != null) {
            btnInRoomRanking.setOnClickListener(v -> showPremiumRankingBottomSheet());
        }

        // 📋 রুম টাইটেল ও হেডার ক্লিক
        if (roomTitleText != null) {
            roomTitleText.setOnClickListener(v -> showRoomInfoAndMembersBottomSheet());
        }
        if (roomHeaderProfileImg != null) {
            roomHeaderProfileImg.setOnClickListener(v -> showRoomInfoAndMembersBottomSheet());
        }
        if (roomInfoText != null) {
            roomInfoText.setOnClickListener(v -> showRoomInfoAndMembersBottomSheet());
        }

        // 👥 অনলাইন মেম্বার লিস্ট ক্লিক
        if (btnOnlineList != null) {
            btnOnlineList.setOnClickListener(v -> showScrollableOnlineMembersDialog());
        }

        chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatMessages.clear();

        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        if (chatListView != null) {
            chatListView.setAdapter(chatAdapter);
        }

        setupSeatInteractions();
        listenToFirebaseSeats();
        listenToOnlineMembers();
        listenToRealtimeChatMessages();

        btnMicToggle = findViewById(R.id.btnMicToggle);
        if (btnMicToggle != null) {
            btnMicToggle.setOnClickListener(v -> {
                isMuted = !isMuted;
                if (isMuted) {
                    btnMicToggle.setText("🔇 Muted");
                    Toast.makeText(this, "Mic Muted", Toast.LENGTH_SHORT).show();
                } else {
                    btnMicToggle.setText("🎙️ Mic");
                    Toast.makeText(this, "Mic Unmuted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnChatOption = findViewById(R.id.btnChatOption);
        if (btnChatOption != null) {
            btnChatOption.setOnClickListener(v -> {
                final Dialog chatDialog = new Dialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                chatDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                chatDialog.setContentView(R.layout.dialog_bottom_chat);

                Window window = chatDialog.getWindow();
                if (window != null) {
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setGravity(Gravity.BOTTOM);
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }

                EditText inputMsg = chatDialog.findViewById(R.id.editRoomMessage);
                Button btnSendMsg = chatDialog.findViewById(R.id.btnSendRoomMessage);

                if (btnSendMsg != null && inputMsg != null) {
                    btnSendMsg.setOnClickListener(view -> {
                        String message = inputMsg.getText().toString().trim();
                        String userName = prefs.getString("USER_NAME", "User");
                        long msgTimestamp = System.currentTimeMillis();
                        if (!message.isEmpty()) {
                            String msgId = messagesRef.push().getKey();
                            if (msgId != null) {
                                String roleBadge = getCorrectBadgeIcon(currentUserId);
                                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                                msgMap.put("senderName", roleBadge + userName);
                                msgMap.put("senderId", currentUserId);
                                msgMap.put("message", message);
                                msgMap.put("timestamp", msgTimestamp);
                                messagesRef.child(msgId).setValue(msgMap);
                            }
                            inputMsg.setText("");
                            chatDialog.dismiss();
                        }
                    });
                }
                chatDialog.show();
            });
        }

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) {
            btnGiftBox.setOnClickListener(v -> showGiftPanel());
        }

        btnGameBox = findViewById(R.id.btnGameBox);
        if (btnGameBox != null) {
            btnGameBox.setOnClickListener(v -> {
                CharSequence[] games = {"🎮 Ludo Game", "🎯 Lucky Wheel", "🎲 Dice Battle"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎮 Voice Room Games");
                builder.setItems(games, (dialog, which) -> {
                    Toast.makeText(this, "Starting " + games[which] + "...", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }
    }

    private void listenToRoomAdmins() {
        adminsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomAdminList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) {
                        roomAdminList.add(ds.getKey());
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getCorrectBadgeIcon(String targetUid) {
        if (targetUid == null || targetUid.isEmpty()) return "";
        if (targetUid.equals(roomOwnerId) || roomAdminList.contains(targetUid)) {
            return "🛡️ ";
        }
        return "";
    }

    @Override
    public void onBackPressed() {
        leaveRoomAndFinish();
    }

    private void listenToRealtimeChatMessages() {
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String senderName = snapshot.child("senderName").getValue(String.class);
                String message = snapshot.child("message").getValue(String.class);
                Long msgTime = snapshot.child("timestamp").getValue(Long.class);

                if (msgTime != null && msgTime < roomJoinTimestamp) return;

                if (senderName != null && message != null) {
                    String fullMsg = senderName + ": " + message;
                    if (!chatMessages.contains(fullMsg)) {
                        chatMessages.add(fullMsg);
                        chatAdapter.notifyDataSetChanged();
                        if (chatListView != null) {
                            chatListView.setSelection(chatMessages.size() - 1);
                        }
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override 
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                chatAdapter.notifyDataSetChanged();
            }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showRoomLockPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
        builder.setTitle("🔒 Room Lock Protection");

        LinearLayout layout = new LinearLayout(VoiceRoomActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        final EditText inputPassword = new EditText(VoiceRoomActivity.this);
        inputPassword.setHint("Enter Room Password...");
        inputPassword.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        builder.setView(layout);

        builder.setPositiveButton("Lock Room", (dialog, which) -> {
            String password = inputPassword.getText().toString().trim();
            if (!password.isEmpty()) {
                roomMetaRef.child("roomPassword").setValue(password);
                roomMetaRef.child("isLocked").setValue(true);
                Toast.makeText(this, "Room locked with password successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Unlock Room", (dialog, which) -> {
            roomMetaRef.child("roomPassword").removeValue();
            roomMetaRef.child("isLocked").setValue(false);
            Toast.makeText(this, "Room is now Unlocked!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showScrollableOnlineMembersDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("👥 Active Online Members");
        title.setTextColor(Color.parseColor("#00E676"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1200));
        scrollView.setFillViewport(true);

        LinearLayout memberListContainer = new LinearLayout(this);
        memberListContainer.setOrientation(LinearLayout.VERTICAL);

        databaseReference.child("online_users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberListContainer.removeAllViews();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String memberName = ds.getValue(String.class);
                    String memberUid = ds.getKey();
                    if (memberName != null) {
                        LinearLayout row = new LinearLayout(VoiceRoomActivity.this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setPadding(20, 20, 20, 20);
                        row.setGravity(Gravity.CENTER_VERTICAL);

                        GradientDrawable rBg = new GradientDrawable();
                        rBg.setColor(Color.parseColor("#2A1B4E"));
                        rBg.setCornerRadius(16);
                        row.setBackground(rBg);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 10);
                        row.setLayoutParams(lp);

                        TextView nameTxt = new TextView(VoiceRoomActivity.this);
                        nameTxt.setText("🟢  " + memberName + " (ID: " + memberUid + ")");
                        nameTxt.setTextColor(Color.WHITE);
                        nameTxt.setTextSize(15);
                        nameTxt.setTypeface(null, android.graphics.Typeface.BOLD);
                        row.addView(nameTxt);

                        memberListContainer.addView(row);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        scrollView.addView(memberListContainer);
        layout.addView(scrollView);
        dialog.setContentView(layout);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1400);
            window.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private void showPremiumRankingBottomSheet() {
        BottomSheetDialog rankSheet = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        
        LinearLayout rankLayout = new LinearLayout(this);
        rankLayout.setOrientation(LinearLayout.VERTICAL);
        rankLayout.setPadding(40, 40, 40, 40);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#2A1B4E"), Color.parseColor("#120A2A")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        rankLayout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("🏆 Room Contribution Ranking");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        rankLayout.addView(title);

        LinearLayout tabLayout = new LinearLayout(this);
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabLayout.setGravity(Gravity.CENTER);
        tabLayout.setPadding(0, 0, 0, 20);

        Button btnDaily = new Button(this);
        btnDaily.setText("Daily");
        btnDaily.setBackgroundColor(Color.parseColor("#FFD700"));
        btnDaily.setTextColor(Color.BLACK);
        btnDaily.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        Button btnWeekly = new Button(this);
        btnWeekly.setText("Weekly");
        btnWeekly.setBackgroundColor(Color.parseColor("#441B1035"));
        btnWeekly.setTextColor(Color.WHITE);
        btnWeekly.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        Button btnMonthly = new Button(this);
        btnMonthly.setText("Monthly");
        btnMonthly.setBackgroundColor(Color.parseColor("#441B1035"));
        btnMonthly.setTextColor(Color.WHITE);
        btnMonthly.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        tabLayout.addView(btnDaily);
        tabLayout.addView(btnWeekly);
        tabLayout.addView(btnMonthly);
        rankLayout.addView(tabLayout);

        ScrollView rankScrollView = new ScrollView(this);
        rankScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        rankScrollView.setFillViewport(true);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);

        addRankUserRow(listContainer, "1", "SR Apon (Owner)", "1,500,000 Coins", "#FFD700");
        addRankUserRow(listContainer, "2", "Top Supporter", "950,000 Coins", "#C0C0C0");
        addRankUserRow(listContainer, "3", "Active Member", "500,000 Coins", "#CD7F32");

        rankScrollView.addView(listContainer);
        rankLayout.addView(rankScrollView);

        rankSheet.setContentView(rankLayout);
        Window w = rankSheet.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500);
            w.setGravity(Gravity.BOTTOM);
        }
        rankSheet.show();
    }

    private void addRankUserRow(LinearLayout container, String rank, String name, String score, String badgeColorHex) {
        LinearLayout row = new LinearLayout(container.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(20, 20, 20, 20);
        row.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable rBg = new GradientDrawable();
        rBg.setColor(Color.parseColor("#1B1035"));
        rBg.setCornerRadius(16);
        row.setBackground(rBg);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, 12);
        row.setLayoutParams(p);

        TextView rankTxt = new TextView(container.getContext());
        rankTxt.setText("Top " + rank);
        rankTxt.setTextColor(Color.parseColor(badgeColorHex));
        rankTxt.setTextSize(14);
        rankTxt.setTypeface(null, android.graphics.Typeface.BOLD);
        rankTxt.setMinWidth(80);
        row.addView(rankTxt);

        LinearLayout textStack = new LinearLayout(container.getContext());
        textStack.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        textStack.setLayoutParams(tp);

        TextView nameTxt = new TextView(container.getContext());
        nameTxt.setText(name);
        nameTxt.setTextColor(Color.WHITE);
        nameTxt.setTextSize(15);
        textStack.addView(nameTxt);

        TextView scoreTxt = new TextView(container.getContext());
        scoreTxt.setText(score);
        scoreTxt.setTextColor(Color.parseColor("#00E676"));
        scoreTxt.setTextSize(12);
        textStack.addView(scoreTxt);

        row.addView(textStack);
        container.addView(row);
    }

    private void showGiftPanel() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_gift_panel, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView rvGifts = sheetView.findViewById(R.id.rvGifts);
        Button btnSendGift = sheetView.findViewById(R.id.btnSendGift);

        List<GiftModel> allGifts = new ArrayList<>();
        allGifts.add(new GiftModel("", "5,000 Coins", R.drawable.ic_gift_flower, "gift"));
        allGifts.add(new GiftModel("", "8,000 Coins", R.drawable.ic_gift_ring, "gift"));
        allGifts.add(new GiftModel("", "10,000 Coins", R.drawable.ic_gift_diamond, "gift"));
        allGifts.add(new GiftModel("", "12,000 Coins", R.drawable.ic_gift_car, "gift"));
        allGifts.add(new GiftModel("", "20,000 Coins", R.drawable.ic_gift_crown, "gift"));
        allGifts.add(new GiftModel("", "30,000 Coins", R.drawable.ic_gift_castle, "lady"));

        final GiftModel[] selectedGiftHolder = {allGifts.get(0)};
        GiftAdapter adapter = new GiftAdapter(allGifts, gift -> selectedGiftHolder[0] = gift);
        rvGifts.setAdapter(adapter);

        btnSendGift.setOnClickListener(v -> {
            String userName = prefs.getString("USER_NAME", "User");
            String priceStr = selectedGiftHolder[0].getGiftPrice().replaceAll("[^0-9]", "");
            long giftCoins = priceStr.isEmpty() ? 5000L : Long.parseLong(priceStr);

            databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (int i = 0; i < 10; i++) {
                        String seatKey = "seat_" + i;
                        if (snapshot.hasChild(seatKey)) {
                            Long currentGiftTotal = snapshot.child(seatKey).child("giftTotal").getValue(Long.class);
                            long newTotal = (currentGiftTotal != null ? currentGiftTotal : 0L) + giftCoins;
                            databaseReference.child("seats").child(seatKey).child("giftTotal").setValue(newTotal);
                            break;
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            long msgTimestamp = System.currentTimeMillis();
            String msgId = messagesRef.push().getKey();
            if (msgId != null) {
                String roleBadge = getCorrectBadgeIcon(currentUserId);
                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("senderName", roleBadge + "🎁 " + userName);
                msgMap.put("message", "sent a luxury gift (" + selectedGiftHolder[0].getGiftPrice() + ")");
                msgMap.put("timestamp", msgTimestamp);
                messagesRef.child(msgId).setValue(msgMap);
            }
            Toast.makeText(this, "Luxury Gift Sent Successfully!", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1300);
            window.setGravity(Gravity.BOTTOM);
        }
    }

    private void syncLatestUserProfile() {
        if (!currentUserId.isEmpty()) {
            userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String dbName = snapshot.child("name").getValue(String.class);
                        String dbImage = snapshot.child("profileImage").getValue(String.class);

                        SharedPreferences.Editor editor = prefs.edit();
                        if (dbName != null && !dbName.isEmpty()) editor.putString("USER_NAME", dbName);
                        if (dbImage != null) {
                            editor.putString("USER_IMAGE", dbImage);
                            editor.putString("USER_PROFILE_IMAGE", dbImage);
                        }
                        editor.apply();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void listenToRoomDetails() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String roomName = snapshot.child("roomName").getValue(String.class);
                String roomBio = snapshot.child("roomBio").getValue(String.class);
                String ownerImage = snapshot.child("roomOwnerImage").getValue(String.class);

                if (roomName != null && !roomName.isEmpty()) {
                    if (roomTitleText != null) roomTitleText.setText(roomName);
                } else {
                    if (roomTitleText != null) roomTitleText.setText("SR Voice Room");
                }

                if (roomInfoText != null) roomInfoText.setText("ID: " + roomOwnerId);

                if (roomNoticeBarText != null) {
                    String bioText = (roomBio != null && !roomBio.isEmpty()) ? roomBio : "Welcome to my voice room!";
                    roomNoticeBarText.setText("📢 Notice: " + bioText);
                }

                if (roomHeaderProfileImg != null && ownerImage != null && !ownerImage.isEmpty()) {
                    try {
                        byte[] decodedString = android.util.Base64.decode(ownerImage, android.util.Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (decodedByte != null) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), decodedByte);
                            circularBitmapDrawable.setCircular(true);
                            roomHeaderProfileImg.setImageDrawable(circularBitmapDrawable);
                        }
                    } catch (Exception e) {
                        roomHeaderProfileImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showEditRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
        builder.setTitle("⚙️ Room Settings & Profile");

        LinearLayout layout = new LinearLayout(VoiceRoomActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        Button btnChangePic = new Button(VoiceRoomActivity.this);
        btnChangePic.setText("🖼️ Change Room Profile Picture");
        btnChangePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        layout.addView(btnChangePic);

        dialogInputName = new EditText(VoiceRoomActivity.this);
        dialogInputName.setHint("Enter Room Name...");
        layout.addView(dialogInputName);

        dialogInputBio = new EditText(VoiceRoomActivity.this);
        dialogInputBio.setHint("Enter Room Bio / Notice...");
        layout.addView(dialogInputBio);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = dialogInputName.getText().toString().trim();
            String newBio = dialogInputBio.getText().toString().trim();

            if (!newName.isEmpty()) roomMetaRef.child("roomName").setValue(newName);
            if (!newBio.isEmpty()) roomMetaRef.child("roomBio").setValue(newBio);
            Toast.makeText(this, "Room settings updated successfully!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String encodedImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);

                    roomMetaRef.child("roomOwnerImage").setValue(encodedImage);
                    Toast.makeText(this, "Room Profile Picture Updated!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void listenToOnlineMembers() {
        String currentUserName = prefs.getString("USER_NAME", "User");
        databaseReference.child("online_users").child(currentUserId).setValue(currentUserName);
        databaseReference.child("online_users").child(currentUserId).onDisconnect().removeValue();

        databaseReference.child("online_users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                if (textOnlineCount != null) textOnlineCount.setText(String.valueOf(count));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showRoomInfoAndMembersBottomSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setBackgroundColor(Color.parseColor("#120A2A"));

        TextView title = new TextView(this);
        title.setText("SR Official Room Info");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 20);
        mainLayout.addView(title);

        TextView info = new TextView(this);
        info.setText("Welcome to SR Voice Chat Room. Enjoy high quality voice streaming and live interactions!");
        info.setTextColor(Color.WHITE);
        info.setTextSize(14);
        mainLayout.addView(info);

        sheetDialog.setContentView(mainLayout);
        sheetDialog.show();
    }

    private void listenToFirebaseSeats() {
        int[] seatImgIds = {
            R.id.seatImg1, R.id.seatImg2, R.id.seatImg3, R.id.seatImg4, R.id.seatImg5,
            R.id.seatImg6, R.id.seatImg7, R.id.seatImg8, R.id.seatImg9, R.id.seatImg10
        };

        int[] seatTxtIds = {
            R.id.seatTxt1, R.id.seatTxt2, R.id.seatTxt3, R.id.seatTxt4, R.id.seatTxt5,
            R.id.seatTxt6, R.id.seatTxt7, R.id.seatTxt8, R.id.seatTxt9, R.id.seatTxt10
        };

        int[] seatGiftTxtIds = {
            R.id.seatGiftTxt1, R.id.seatGiftTxt2, R.id.seatGiftTxt3, R.id.seatGiftTxt4, R.id.seatGiftTxt5,
            R.id.seatGiftTxt6, R.id.seatGiftTxt7, R.id.seatGiftTxt8, R.id.seatGiftTxt9, R.id.seatGiftTxt10
        };

        databaseReference.child("seats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < 10; i++) {
                    ImageView seatImg = findViewById(seatImgIds[i]);
                    TextView seatTxt = findViewById(seatTxtIds[i]);
                    TextView seatGiftTxt = findViewById(seatGiftTxtIds[i]);
                    String seatKey = "seat_" + i;

                    if (snapshot.hasChild(seatKey)) {
                        String name = snapshot.child(seatKey).child("userName").getValue(String.class);
                        String imageBase64 = snapshot.child(seatKey).child("userImage").getValue(String.class);
                        String sUid = snapshot.child(seatKey).child("userId").getValue(String.class);
                        Long giftTotal = snapshot.child(seatKey).child("giftTotal").getValue(Long.class);

                        if (seatTxt != null) {
                            String roleIcon = (sUid != null && (sUid.equals(roomOwnerId) || roomAdminList.contains(sUid))) ? "🛡️ " : "";
                            String displayName = (name != null && !name.isEmpty()) ? name : (i == 0 ? "Host" : String.valueOf(i + 1));
                            seatTxt.setText(roleIcon + displayName);
                        }

                        if (seatGiftTxt != null) {
                            if (giftTotal != null && giftTotal > 0) {
                                seatGiftTxt.setText(giftTotal + " Coins");
                            } else {
                                seatGiftTxt.setText("");
                            }
                        }

                        if (seatImg != null && imageBase64 != null && !imageBase64.isEmpty()) {
                            try {
                                byte[] decodedString = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                if (decodedByte != null) {
                                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), decodedByte);
                                    circularBitmapDrawable.setCircular(true);
                                    seatImg.setImageDrawable(circularBitmapDrawable);
                                    seatImg.setPadding(0, 0, 0, 0);
                                }
                            } catch (Exception e) {
                                seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                            }
                        }
                    } else {
                        if (seatTxt != null) seatTxt.setText(i == 0 ? "Host" : String.valueOf(i + 1));
                        if (seatGiftTxt != null) seatGiftTxt.setText(""); 
                        if (seatImg != null) {
                            seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                            seatImg.setPadding(12, 12, 12, 12);
                        }
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSeatInteractions() {
        int[] seatCardIds = {
            R.id.seatCard1, R.id.seatCard2, R.id.seatCard3, R.id.seatCard4, R.id.seatCard5,
            R.id.seatCard6, R.id.seatCard7, R.id.seatCard8, R.id.seatCard9, R.id.seatCard10
        };

        for (int i = 0; i < seatCardIds.length; i++) {
            final int seatIndex = i;
            final int seatNumber = i + 1;
            View seatContainer = findViewById(seatCardIds[i]);

            if (seatContainer != null) {
                seatContainer.setOnClickListener(v -> {
                    String seatKey = "seat_" + seatIndex;
                    databaseReference.child("seats").child(seatKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot seatSnap) {
                            String occupantName = seatSnap.child("userName").getValue(String.class);
                            String occupantUid = seatSnap.child("userId").getValue(String.class);
                            boolean isOccupied = occupantName != null && !occupantName.isEmpty();

                            List<String> optionsList = new ArrayList<>();
                            if (isOccupied) {
                                optionsList.add("👤 View Profile (ID: " + occupantUid + ")");
                                optionsList.add("🚪 Leave Mic");
                            } else {
                                optionsList.add("🎙️ Take Mic");
                            }
                            optionsList.add("❌ Cancel");

                            CharSequence[] seatOptions = optionsList.toArray(new CharSequence[0]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                            builder.setTitle("🎙️ Seat " + seatNumber);
                            builder.setItems(seatOptions, (dialog, which) -> {
                                String selectedOption = seatOptions[which].toString();
                                if (selectedOption.contains("Take Mic")) {
                                    takeSeat(seatIndex, seatKey);
                                } else if (selectedOption.contains("View Profile")) {
                                    String imgUri = seatSnap.child("userImage").getValue(String.class);
                                    showUserProfileDialog(occupantName, occupantUid, imgUri);
                                } else if (selectedOption.contains("Leave Mic")) {
                                    databaseReference.child("seats").child(seatKey).removeValue();
                                    Toast.makeText(VoiceRoomActivity.this, "Left seat & counter reset", Toast.LENGTH_SHORT).show();
                                }
                            });
                            builder.show();
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                });
            }
        }
    }

    // 🌟 প্রিমিয়াম ইউজার প্রোফাইল ভিউ কার্ড (Follow, Gift, Add Friend বাটনসহ)
    private void showUserProfileDialog(String userName, String userId, String userImage) {
        BottomSheetDialog profileDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 45, 40, 55);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("✨ User Profile Card");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(17);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 25);
        layout.addView(title);

        LinearLayout infoCard = new LinearLayout(this);
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setPadding(30, 30, 30, 30);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#2A1B4E"));
        cardBg.setCornerRadius(24);
        infoCard.setBackground(cardBg);

        TextView nameText = new TextView(this);
        nameText.setText(userName != null ? userName : "SR User");
        nameText.setTextColor(Color.WHITE);
        nameText.setTextSize(18);
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);
        infoCard.addView(nameText);

        TextView idText = new TextView(this);
        idText.setText("ID: " + (userId != null ? userId : "493859069"));
        idText.setTextColor(Color.parseColor("#00E676"));
        idText.setTextSize(14);
        idText.setPadding(0, 5, 0, 15);
        infoCard.addView(idText);

        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setPadding(0, 10, 0, 15);

        TextView followingTxt = new TextView(this);
        followingTxt.setText("2 Following    ");
        followingTxt.setTextColor(Color.parseColor("#CCCCCC"));
        followingTxt.setTextSize(13);
        statsRow.addView(followingTxt);

        TextView fansTxt = new TextView(this);
        fansTxt.setText("15 Fans    ");
        fansTxt.setTextColor(Color.parseColor("#CCCCCC"));
        fansTxt.setTextSize(13);
        statsRow.addView(fansTxt);

        TextView visitorTxt = new TextView(this);
        visitorTxt.setText("1.2K Visitor");
        visitorTxt.setTextColor(Color.parseColor("#CCCCCC"));
        visitorTxt.setTextSize(13);
        statsRow.addView(visitorTxt);

        infoCard.addView(statsRow);
        layout.addView(infoCard);

        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setPadding(10, 20, 10, 20);

        TextView levelTxt = new TextView(this);
        levelTxt.setText("⭐ Level: Lv. 7 (VIP)");
        levelTxt.setTextColor(Color.parseColor("#FFD700"));
        levelTxt.setTextSize(14);
        levelTxt.setPadding(0, 8, 0, 8);
        detailsLayout.addView(levelTxt);

        TextView countryTxt = new TextView(this);
        countryTxt.setText("🌍 Country: Bangladesh");
        countryTxt.setTextColor(Color.WHITE);
        countryTxt.setTextSize(14);
        countryTxt.setPadding(0, 8, 0, 8);
        detailsLayout.addView(countryTxt);

        layout.addView(detailsLayout);

        // 🌟 ৩টি প্রিমিয়াম অ্যাকশন বাটন: Follow, Gift, Add Friend
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        actionRow.setPadding(0, 25, 0, 10);

        Button btnFollow = new Button(this);
        btnFollow.setText("✨ Follow");
        btnFollow.setBackgroundColor(Color.parseColor("#FFD700"));
        btnFollow.setTextColor(Color.BLACK);
        btnFollow.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        p1.setMargins(0, 0, 6, 0);
        btnFollow.setLayoutParams(p1);
        btnFollow.setOnClickListener(v -> {
            Toast.makeText(this, "Successfully Followed " + userName, Toast.LENGTH_SHORT).show();
        });
        actionRow.addView(btnFollow);

        Button btnPopupGift = new Button(this);
        btnPopupGift.setText("🎁 Gift");
        btnPopupGift.setBackgroundColor(Color.parseColor("#FF4081"));
        btnPopupGift.setTextColor(Color.WHITE);
        btnPopupGift.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        p2.setMargins(6, 0, 6, 0);
        btnPopupGift.setLayoutParams(p2);
        btnPopupGift.setOnClickListener(v -> {
            profileDialog.dismiss();
            showGiftPanel();
        });
        actionRow.addView(btnPopupGift);

        Button btnAddFriend = new Button(this);
        btnAddFriend.setText("🤝 Add Friend");
        btnAddFriend.setBackgroundColor(Color.parseColor("#00E676"));
        btnAddFriend.setTextColor(Color.BLACK);
        btnAddFriend.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        p3.setMargins(6, 0, 0, 0);
        btnAddFriend.setLayoutParams(p3);
        btnAddFriend.setOnClickListener(v -> {
            Toast.makeText(this, "Friend Request Sent to " + userName, Toast.LENGTH_SHORT).show();
        });
        actionRow.addView(btnAddFriend);

        layout.addView(actionRow);

        profileDialog.setContentView(layout);
        Window window = profileDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }
        profileDialog.show();
    }

    private void takeSeat(int seatIndex, String seatKey) {
        final String userName = prefs.getString("USER_NAME", "User");
        final String userImageUriStr = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

        databaseReference.child("seats").child(seatKey).child("userId").setValue(currentUserId);
        databaseReference.child("seats").child(seatKey).child("userName").setValue(userName);
        databaseReference.child("seats").child(seatKey).child("userImage").setValue(userImageUriStr);
        databaseReference.child("seats").child(seatKey).child("giftTotal").setValue(0L);
        databaseReference.child("seats").child(seatKey).onDisconnect().removeValue();
        Toast.makeText(VoiceRoomActivity.this, "Joined seat successfully!", Toast.LENGTH_SHORT).show();
    }

    private void leaveRoomAndFinish() {
        databaseReference.child("online_users").child(currentUserId).removeValue();
        
        for (int index = 0; index < 10; index++) {
            final int i = index;
            databaseReference.child("seats").child("seat_" + i).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String sUserId = snapshot.child("userId").getValue(String.class);
                    if (sUserId != null && sUserId.equals(currentUserId)) {
                        databaseReference.child("seats").child("seat_" + i).removeValue();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        finish();
    }
}
