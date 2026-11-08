package com.sr.voicechat;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
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
    private DatabaseReference followsRef;
    private FirebaseAuth mAuth;
    private String roomOwnerId = "";
    private String currentUserId = "";

    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText dialogInputName, dialogInputBio;
    private DatabaseReference activeRoomsRef;
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
        followsRef = FirebaseDatabase.getInstance().getReference("follows");
        
        activeRoomsRef = FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId);
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

        if (btnInRoomRanking != null) {
            btnInRoomRanking.setOnClickListener(v -> showInRoomRankingBottomSheet());
        }

        if (roomTitleText != null) {
            roomTitleText.setOnClickListener(v -> showRoomInfoAndMembersBottomSheet());
        }

        // 🌟 হেডার প্রোফাইল পিকচার ক্লিক লজিক: শুধু সিইও (Room Owner) ডাবল ট্যাপ বা লং প্রেস বা নিজের আইডিতে সুপার প্যানেল পাবে, নরমাল ক্লিক করলে মেম্বার লিস্ট দেখাবে
        if (roomHeaderProfileImg != null) {
            roomHeaderProfileImg.setOnClickListener(v -> {
                showRoomInfoAndMembersBottomSheet();
            });
            // সিক্রেট সুপার প্যানেল ওপেন করার জন্য লং প্রেস (Long Click) ব্যবহার করা হলো, যাতে নরমাল ক্লিক ডিস্টার্ব না করে
            roomHeaderProfileImg.setOnLongClickListener(v -> {
                if (currentUserId.equals(roomOwnerId)) {
                    showSRSuperPanel();
                    return true;
                }
                return false;
            });
        }

        if (roomInfoText != null) {
            roomInfoText.setOnClickListener(v -> showRoomInfoAndMembersBottomSheet());
        }

        if (btnOnlineList != null) {
            btnOnlineList.setOnClickListener(v -> showRoomInfoAndMembersBottomSheet());
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

    // 👑 "এসআর সুপার প্যানেল" (সিইও বা ওনারের জন্য সিক্রেট ড্যাশবোর্ড)
    private void showSRSuperPanel() {
        BottomSheetDialog superPanelDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 60);
        
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{40, 40, 40, 40, 0, 0, 0, 0});
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("👑 এসআর সুপার প্যানেল (SR Super Panel)");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        final EditText inputTargetId = new EditText(this);
        inputTargetId.setHint("Enter Target User 9-Digit ID...");
        inputTargetId.setHintTextColor(Color.parseColor("#888888"));
        inputTargetId.setTextColor(Color.WHITE);
        inputTargetId.setBackgroundColor(Color.parseColor("#2A1B4E"));
        inputTargetId.setPadding(30, 30, 30, 30);
        layout.addView(inputTargetId);

        final EditText inputCoinAmount = new EditText(this);
        inputCoinAmount.setHint("Enter Coin Amount (e.g., 500000)...");
        inputCoinAmount.setHintTextColor(Color.parseColor("#888888"));
        inputCoinAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputCoinAmount.setTextColor(Color.WHITE);
        inputCoinAmount.setBackgroundColor(Color.parseColor("#2A1B4E"));
        inputCoinAmount.setPadding(30, 30, 30, 30);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 30);
        inputCoinAmount.setLayoutParams(params);
        layout.addView(inputCoinAmount);

        Button btnAddCoins = new Button(this);
        btnAddCoins.setText("💎 Add Free Coins to User ID");
        btnAddCoins.setBackgroundColor(Color.parseColor("#00E676"));
        btnAddCoins.setTextColor(Color.BLACK);
        btnAddCoins.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAddCoins.setOnClickListener(v -> {
            String targetId = inputTargetId.getText().toString().trim();
            String coinStr = inputCoinAmount.getText().toString().trim();

            if (targetId.isEmpty() || coinStr.isEmpty()) {
                Toast.makeText(this, "Please enter both User ID and Coin Amount!", Toast.LENGTH_SHORT).show();
                return;
            }

            long coinsToAdd = Long.parseLong(coinStr);
            DatabaseReference targetUserRef = FirebaseDatabase.getInstance().getReference("users").child(targetId);
            
            targetUserRef.child("totalSpentCoins").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long currentCoins = 0;
                    if (snapshot.exists() && snapshot.getValue(Long.class) != null) {
                        currentCoins = snapshot.getValue(Long.class);
                    }
                    long updatedTotal = currentCoins + coinsToAdd;
                    
                    targetUserRef.child("totalSpentCoins").setValue(updatedTotal);
                    Toast.makeText(VoiceRoomActivity.this, "Successfully added " + coinsToAdd + " coins to ID: " + targetId, Toast.LENGTH_LONG).show();
                    superPanelDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(VoiceRoomActivity.this, "Failed to update database!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        layout.addView(btnAddCoins);

        superPanelDialog.setContentView(layout);
        Window window = superPanelDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }
        superPanelDialog.show();
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
        if (targetUid == null || targetUid.isEmpty()) {
            return ""; 
        }
        if (targetUid.equals(roomOwnerId)) {
            return "🛡️ ";
        } else if (roomAdminList.contains(targetUid)) {
            return "🛡️ ";
        } else {
            return "";
        }
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

                if (msgTime != null && msgTime < roomJoinTimestamp) {
                    return; 
                }

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

    private void showInRoomRankingBottomSheet() {
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

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setPadding(0, 0, 0, 20);

        TextView title = new TextView(this);
        title.setText("🏆 Room Contribution Ranking");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        title.setLayoutParams(tParams);
        topRow.addView(title);

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextColor(Color.WHITE);
        close.setTextSize(20);
        close.setOnClickListener(v -> rankSheet.dismiss());
        topRow.addView(close);
        rankLayout.addView(topRow);

        ScrollView rankScrollView = new ScrollView(this);
        rankScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        rankScrollView.setFillViewport(true);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(10, 10, 10, 10);

        addRankUserRow(listContainer, "1", "SR Apon (Owner)", "1,500,000 Coins", "#FFD700");
        addRankUserRow(listContainer, "2", "Admin User", "950,000 Coins", "#C0C0C0");
        addRankUserRow(listContainer, "3", "Active Supporter", "500,000 Coins", "#CD7F32");

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
        nameTxt.setTypeface(null, android.graphics.Typeface.BOLD);
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

        List<GiftModel> displayedGifts = new ArrayList<>(allGifts);
        final GiftModel[] selectedGiftHolder = {displayedGifts.get(0)};

        GiftAdapter adapter = new GiftAdapter(displayedGifts, gift -> {
            selectedGiftHolder[0] = gift;
            Toast.makeText(this, "Selected: " + gift.getGiftPrice(), Toast.LENGTH_SHORT).show();
        });
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
                        if (dbName != null && !dbName.isEmpty()) {
                            editor.putString("USER_NAME", dbName);
                        }
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
        
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#FFFFFF"), Color.parseColor("#F8F9FA")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        mainLayout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("SR Official Voice Room Members");
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(title);

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
                            boolean isOccupied = occupantName != null && !occupantName.isEmpty();

                            List<String> optionsList = new ArrayList<>();
                            if (isOccupied) {
                                optionsList.add("👤 View Profile");
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
