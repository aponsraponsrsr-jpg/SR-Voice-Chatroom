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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import androidx.recyclerview.widget.GridLayoutManager;
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
                LinearLayout toolLayout = new LinearLayout(VoiceRoomActivity.this);
                toolLayout.setOrientation(LinearLayout.VERTICAL);
                toolLayout.setPadding(40, 45, 40, 55);

                GradientDrawable bg = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
                );
                bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
                toolLayout.setBackground(bg);

                TextView title = new TextView(VoiceRoomActivity.this);
                title.setText("🎛️ Room Management Tools");
                title.setTextColor(Color.parseColor("#FFD700"));
                title.setTextSize(18);
                title.setTypeface(null, android.graphics.Typeface.BOLD);
                title.setGravity(Gravity.CENTER);
                title.setPadding(0, 0, 0, 25);
                toolLayout.addView(title);

                ScrollView toolScroll = new ScrollView(VoiceRoomActivity.this);
                toolScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1100));
                toolScroll.setFillViewport(true);

                LinearLayout container = new LinearLayout(VoiceRoomActivity.this);
                container.setOrientation(LinearLayout.VERTICAL);

                container.addView(createToolButton("⚙️ Room Settings & Profile", "Change room name, bio & cover image", view -> {
                    toolsSheet.dismiss();
                    if (isOwner) {
                        showEditRoomDialog();
                    } else {
                        Toast.makeText(this, "Only Room Owner can edit room settings!", Toast.LENGTH_SHORT).show();
                    }
                }));

                container.addView(createToolButton("🔒 Room Lock Protection", "Set or remove room security password", view -> {
                    toolsSheet.dismiss();
                    showRoomLockPasswordDialog();
                }));

                container.addView(createToolButton("🏆 Level Center (1 to 150)", "View Wallet & Charm Level Badges", view -> {
                    toolsSheet.dismiss();
                    showFullLevelCenterDialog();
                }));

                container.addView(createToolButton("🧹 Clean Global Chat", "Clear all live messages from room", view -> {
                    messagesRef.removeValue().addOnCompleteListener(task -> {
                        chatMessages.clear();
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(VoiceRoomActivity.this, "Chat history cleaned globally!", Toast.LENGTH_SHORT).show();
                    });
                    toolsSheet.dismiss();
                }));

                toolScroll.addView(container);
                toolLayout.addView(toolScroll);
                toolsSheet.setContentView(toolLayout);

                Window window = toolsSheet.getWindow();
                if (window != null) {
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1300);
                    window.setGravity(Gravity.BOTTOM);
                }
                toolsSheet.show();
            });
        }

        if (btnInRoomRanking != null) {
            btnInRoomRanking.setOnClickListener(v -> showPremiumRankingBottomSheet());
        }

        View.OnClickListener headerClickListener = v -> showPremiumRoomInfoAndMembersDialog();
        if (roomTitleText != null) roomTitleText.setOnClickListener(headerClickListener);
        if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(headerClickListener);
        if (roomInfoText != null) roomInfoText.setOnClickListener(headerClickListener);

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
            btnGiftBox.setOnClickListener(v -> showPremiumGiftPanel());
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

    // 👑 প্রিমিয়াম হাই-ভ্যালু গিফট প্যানেল (৫,০০০ কয়েন থেকে শুরু করে ৩০ লাখ কয়েন পর্যন্ত পশু-পাখি, কার ও ক্রাউন কালেকশন)
    private void showPremiumGiftPanel() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(35, 35, 35, 35);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        rootLayout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("🎁 SR VIP Luxury Gift Center");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 20);
        rootLayout.addView(title);

        // সিট সিলেকশন অপশন (Single বা All সিট)
        final EditText inputTargetSeat = new EditText(this);
        inputTargetSeat.setHint("Target Seat (0 to 9 or type 'All')...");
        inputTargetSeat.setHintTextColor(Color.parseColor("#AAAAAA"));
        inputTargetSeat.setTextColor(Color.WHITE);
        inputTargetSeat.setText("All"); // ডিফল্টভাবে All সিট সিলেক্ট থাকবে
        rootLayout.addView(inputTargetSeat);

        RecyclerView rvGifts = new RecyclerView(this);
        rvGifts.setLayoutManager(new GridLayoutManager(this, 3));
        rvGifts.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900));
        rootLayout.addView(rvGifts);

        List<GiftModel> luxuryGifts = new ArrayList<>();
        // প্রিমিয়াম পশু-পাখি ও কার কালেকশন (৫ হাজার থেকে ৩০ লাখ কয়েন পর্যন্ত হাই-ভ্যালু)
        luxuryGifts.add(new GiftModel("g1", "White Tiger", "50,000", R.drawable.ic_gift_flower, "Animal"));
        luxuryGifts.add(new GiftModel("g2", "Golden Lion", "100,000", R.drawable.ic_gift_ring, "Animal"));
        luxuryGifts.add(new GiftModel("g3", "Royal Dragon", "250,000", R.drawable.ic_gift_diamond, "Animal"));
        luxuryGifts.add(new GiftModel("g4", "Super Sports Car", "500,000", R.drawable.ic_gift_car, "Vehicle"));
        luxuryGifts.add(new GiftModel("g5", "VIP Helicopter", "750,000", R.drawable.ic_gift_car, "Vehicle"));
        luxuryGifts.add(new GiftModel("g6", "Royal Crown", "1,000,000", R.drawable.ic_gift_crown, "Crown"));
        luxuryGifts.add(new GiftModel("g7", "Diamond Castle", "1,250,000", R.drawable.ic_gift_castle, "Castle"));
        luxuryGifts.add(new GiftModel("g8", "Luxury Jet Plane", "1,500,000", R.drawable.ic_gift_car, "Vehicle"));
        luxuryGifts.add(new GiftModel("g9", "Golden Empire", "1,800,000", R.drawable.ic_gift_castle, "Castle"));
        luxuryGifts.add(new GiftModel("g10", "Mega Titan Dragon", "2,200,000", R.drawable.ic_gift_diamond, "Animal"));
        luxuryGifts.add(new GiftModel("g11", "Ultimate Supreme", "2,500,000", R.drawable.ic_gift_crown, "Crown"));
        luxuryGifts.add(new GiftModel("g12", "SR Universe King", "3,000,000", R.drawable.ic_gift_castle, "Crown"));

        final GiftModel[] selectedGiftHolder = {luxuryGifts.get(0)};
        GiftAdapter adapter = new GiftAdapter(luxuryGifts, gift -> selectedGiftHolder[0] = gift);
        rvGifts.setAdapter(adapter);

        Button btnSendGiftAction = new Button(this);
        btnSendGiftAction.setText("🚀 Send Luxury Gift");
        btnSendGiftAction.setBackgroundColor(Color.parseColor("#FF4081"));
        btnSendGiftAction.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
        btnParams.setMargins(0, 20, 0, 10);
        btnSendGiftAction.setLayoutParams(btnParams);
        rootLayout.addView(btnSendGiftAction);

        bottomSheetDialog.setContentView(rootLayout);

        btnSendGiftAction.setOnClickListener(v -> {
            String targetVal = inputTargetSeat.getText().toString().trim();
            String userName = prefs.getString("USER_NAME", "User");
            String priceStr = selectedGiftHolder[0].getGiftPrice().replaceAll("[^0-9]", "");
            long giftCoins = priceStr.isEmpty() ? 50000L : Long.parseLong(priceStr);

            // ফ্লাইং অ্যানিমেশন ট্রিগার করা
            triggerFlyingGiftAnimation(selectedGiftHolder[0].getGiftName());

            if (targetVal.equalsIgnoreCase("All")) {
                // সব সিটে একসাথে কয়েন আপডেট করা
                databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (int i = 0; i < 10; i++) {
                            String seatKey = "seat_" + i;
                            if (snapshot.hasChild(seatKey)) {
                                Long currentTotal = snapshot.child(seatKey).child("giftTotal").getValue(Long.class);
                                long newTotal = (currentTotal != null ? currentTotal : 0L) + giftCoins;
                                databaseReference.child("seats").child(seatKey).child("giftTotal").setValue(newTotal);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            } else {
                try {
                    int seatIdx = Integer.parseInt(targetVal);
                    if (seatIdx >= 0 && seatIdx <= 9) {
                        String seatKey = "seat_" + seatIdx;
                        databaseReference.child("seats").child(seatKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Long currentTotal = snapshot.child("giftTotal").getValue(Long.class);
                                long newTotal = (currentTotal != null ? currentTotal : 0L) + giftCoins;
                                databaseReference.child("seats").child(seatKey).child("giftTotal").setValue(newTotal);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Invalid seat number! Defaulting to All seats.", Toast.LENGTH_SHORT).show();
                }
            }

            // সেন্ডারের সেন্ডিং লেভেল (Sending Level) এবং কয়েন আপডেট করা
            if (!currentUserId.isEmpty()) {
                userDatabaseRef.child(currentUserId).child("sendingCoins").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long currentSend = snapshot.getValue(Long.class);
                        long updatedSend = (currentSend != null ? currentSend : 0L) + giftCoins;
                        userDatabaseRef.child(currentUserId).child("sendingCoins").setValue(updatedSend);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            // গ্লোবাল চ্যাটে গিফট নোটিফিকেশন পাঠানো
            long msgTimestamp = System.currentTimeMillis();
            String msgId = messagesRef.push().getKey();
            if (msgId != null) {
                String roleBadge = getCorrectBadgeIcon(currentUserId);
                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("senderName", roleBadge + "🎁 " + userName);
                msgMap.put("message", "sent luxury gift [" + selectedGiftHolder[0].getGiftName() + " - " + selectedGiftHolder[0].getGiftPrice() + " Coins]");
                msgMap.put("timestamp", msgTimestamp);
                messagesRef.child(msgId).setValue(msgMap);
            }

            Toast.makeText(this, "Luxury Gift Sent Successfully!", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
        Window window = bottomSheetDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500);
            window.setGravity(Gravity.BOTTOM);
        }
    }

    // 🚀 প্রিমিয়াম ফ্লাইং গিফট অ্যানিমেশন (নিচ থেকে উড়ে ইউজারের দিকে যাওয়া)
    private void triggerFlyingGiftAnimation(String giftName) {
        try {
            ViewGroup rootView = findViewById(android.R.id.content);
            if (rootView == null) return;

            final TextView flyingView = new TextView(this);
            flyingView.setText("🎁 " + giftName + " 🚀");
            flyingView.setTextColor(Color.parseColor("#FFD700"));
            flyingView.setTextSize(20);
            flyingView.setTypeface(null, android.graphics.Typeface.BOLD);
            flyingView.setGravity(Gravity.CENTER);
            flyingView.setBackgroundColor(Color.parseColor("#88000000"));
            flyingView.setPadding(30, 20, 30, 20);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.setMargins(0, 0, 0, 300);
            rootView.addView(flyingView, params);

            // নিচ থেকে সোজা ওপরের দিকে উড়ে যাওয়ার অ্যানিমেশন
            TranslateAnimation animation = new TranslateAnimation(
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, -1000f
            );
            animation.setDuration(2000); // ২ সেকেন্ড স্থায়ী হবে
            animation.setFillAfter(true);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    rootView.removeView(flyingView);
                }
                @Override public void onAnimationRepeat(Animation animation) {}
            });

            flyingView.startAnimation(animation);
        } catch (Exception ignored) {}
    }

    private View createToolButton(String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(25, 20, 25, 20);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOnClickListener(listener);

        GradientDrawable rBg = new GradientDrawable();
        rBg.setColor(Color.parseColor("#2A1B4E"));
        rBg.setCornerRadius(18);
        row.setBackground(rBg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 14);
        row.setLayoutParams(lp);

        TextView titleTxt = new TextView(this);
        titleTxt.setText(title);
        titleTxt.setTextColor(Color.WHITE);
        titleTxt.setTextSize(16);
        titleTxt.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(titleTxt);

        TextView subTxt = new TextView(this);
        subTxt.setText(subtitle);
        subTxt.setTextColor(Color.parseColor("#00E676"));
        subTxt.setTextSize(12);
        row.addView(subTxt);

        return row;
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

        ScrollView rankScrollView = new ScrollView(this);
        rankScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        rankScrollView.setFillViewport(true);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);

        addRankUserRow(listContainer, "1", "Top Contributor #1", "0 Coins", "#FFD700");
        addRankUserRow(listContainer, "2", "Top Contributor #2", "0 Coins", "#C0C0C0");
        addRankUserRow(listContainer, "3", "Top Contributor #3", "0 Coins", "#CD7F32");

        rankScrollView.addView(listContainer);
        rankLayout.addView(rankScrollView);

        rankSheet.setContentView(rankLayout);
        Window w = rankSheet.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1900);
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

    private void showPremiumRoomInfoAndMembersDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);

        LinearLayout tabHeader = new LinearLayout(this);
        tabHeader.setOrientation(LinearLayout.HORIZONTAL);
        tabHeader.setGravity(Gravity.CENTER);
        tabHeader.setPadding(0, 0, 0, 20);

        Button tabInfoBtn = new Button(this);
        tabInfoBtn.setText("ℹ️ Room Info");
        tabInfoBtn.setBackgroundColor(Color.parseColor("#FFD700"));
        tabInfoBtn.setTextColor(Color.BLACK);
        tabInfoBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        Button tabMembersBtn = new Button(this);
        tabMembersBtn.setText("👥 Members");
        tabMembersBtn.setBackgroundColor(Color.parseColor("#2A1B4E"));
        tabMembersBtn.setTextColor(Color.WHITE);
        tabMembersBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        tabHeader.addView(tabInfoBtn);
        tabHeader.addView(tabMembersBtn);
        root.addView(tabHeader);

        ScrollView contentScrollView = new ScrollView(this);
        contentScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1200));
        contentScrollView.setFillViewport(true);

        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout ownerCard = new LinearLayout(this);
        ownerCard.setOrientation(LinearLayout.VERTICAL);
        ownerCard.setPadding(25, 25, 25, 25);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#2A1B4E"));
        cardBg.setCornerRadius(20);
        ownerCard.setBackground(cardBg);

        TextView ownerTitle = new TextView(this);
        ownerTitle.setText("👑 Room Owner: SR Official");
        ownerTitle.setTextColor(Color.parseColor("#FFD700"));
        ownerTitle.setTextSize(16);
        ownerTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        ownerCard.addView(ownerTitle);

        TextView ownerIdTxt = new TextView(this);
        ownerIdTxt.setText("ID: " + roomOwnerId);
        ownerIdTxt.setTextColor(Color.parseColor("#00E676"));
        ownerIdTxt.setTextSize(13);
        ownerIdTxt.setPadding(0, 4, 0, 10);
        ownerCard.addView(ownerIdTxt);

        TextView noticeTxt = new TextView(this);
        noticeTxt.setText("📢 Welcome to SR Voice Chat Room! Enjoy live stream & luxury gifts.");
        noticeTxt.setTextColor(Color.WHITE);
        noticeTxt.setTextSize(13);
        ownerCard.addView(noticeTxt);

        infoLayout.addView(ownerCard);
        mainContainer.addView(infoLayout);

        LinearLayout membersLayout = new LinearLayout(this);
        membersLayout.setOrientation(LinearLayout.VERTICAL);
        membersLayout.setVisibility(View.GONE);

        databaseReference.child("online_users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                membersLayout.removeAllViews();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uName = ds.getValue(String.class);
                    String uId = ds.getKey();
                    if (uName != null) {
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

                        String roleBadge = (uId != null && uId.equals(roomOwnerId)) ? "👑 Owner " : (roomAdminList.contains(uId) ? "🛡️ Admin " : "🟢 Member ");

                        TextView nameTxt = new TextView(VoiceRoomActivity.this);
                        nameTxt.setText(roleBadge + uName + " (ID: " + uId + ")");
                        nameTxt.setTextColor(Color.WHITE);
                        nameTxt.setTextSize(14);
                        nameTxt.setTypeface(null, android.graphics.Typeface.BOLD);
                        row.addView(nameTxt);

                        membersLayout.addView(row);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        mainContainer.addView(membersLayout);

        tabInfoBtn.setOnClickListener(v -> {
            tabInfoBtn.setBackgroundColor(Color.parseColor("#FFD700"));
            tabInfoBtn.setTextColor(Color.BLACK);
            tabMembersBtn.setBackgroundColor(Color.parseColor("#2A1B4E"));
            tabMembersBtn.setTextColor(Color.WHITE);
            infoLayout.setVisibility(View.VISIBLE);
            membersLayout.setVisibility(View.GONE);
        });

        tabMembersBtn.setOnClickListener(v -> {
            tabMembersBtn.setBackgroundColor(Color.parseColor("#FFD700"));
            tabMembersBtn.setTextColor(Color.BLACK);
            tabInfoBtn.setBackgroundColor(Color.parseColor("#2A1B4E"));
            tabInfoBtn.setTextColor(Color.WHITE);
            infoLayout.setVisibility(View.GONE);
            membersLayout.setVisibility(View.VISIBLE);
        });

        contentScrollView.addView(mainContainer);
        root.addView(contentScrollView);

        dialog.setContentView(root);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500);
            window.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
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
                                    fetchUserDataAndShowProfile(occupantName, occupantUid, imgUri);
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

    private void fetchUserDataAndShowProfile(String userName, String userId, String userImage) {
        if (userId == null || userId.isEmpty()) {
            showUserProfileDialog(userName, userId, userImage, 0L, 0L, "♂", "🇧🇩");
            return;
        }

        userDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long sendingCoins = 0L;
                long receivingCoins = 0L;
                String gender = "♂"; 
                String country = "🇧🇩";

                if (snapshot.exists()) {
                    Long send = snapshot.child("sendingCoins").getValue(Long.class);
                    Long recv = snapshot.child("receivingCoins").getValue(Long.class);
                    String dbGender = snapshot.child("gender").getValue(String.class);
                    String dbCountry = snapshot.child("country").getValue(String.class);

                    if (send != null) sendingCoins = send;
                    if (recv != null) receivingCoins = recv;
                    
                    if (dbGender != null) {
                        String lowerGender = dbGender.toLowerCase();
                        if (lowerGender.contains("female") || lowerGender.contains("girl") || lowerGender.contains("মেয়ে") || lowerGender.contains("♀")) {
                            gender = "♀";
                        } else {
                            gender = "♂";
                        }
                    }
                    if (dbCountry != null && !dbCountry.isEmpty()) country = dbCountry;
                }
                showUserProfileDialog(userName, userId, userImage, sendingCoins, receivingCoins, gender, country);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                showUserProfileDialog(userName, userId, userImage, 0L, 0L, "♂", "🇧🇩");
            }
        });
    }

    private void showUserProfileDialog(String userName, String userId, String userImage, long sendingCoins, long receivingCoins, String gender, String country) {
        BottomSheetDialog profileDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 50, 40, 60);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{60, 60, 60, 60, 0, 0, 0, 0});
        layout.setBackground(bg);

        ImageView imgProfileBig = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(240, 240);
        imgParams.gravity = Gravity.CENTER_HORIZONTAL;
        imgParams.setMargins(0, 0, 0, 20);
        imgProfileBig.setLayoutParams(imgParams);
        imgProfileBig.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (userImage != null && !userImage.isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(userImage, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), decodedByte);
                    circularBitmapDrawable.setCircular(true);
                    imgProfileBig.setImageDrawable(circularBitmapDrawable);
                } else {
                    imgProfileBig.setImageResource(android.R.drawable.ic_menu_my_calendar);
                }
            } catch (Exception e) {
                imgProfileBig.setImageResource(android.R.drawable.ic_menu_my_calendar);
            }
        } else {
            imgProfileBig.setImageResource(android.R.drawable.ic_menu_my_calendar);
        }
        layout.addView(imgProfileBig);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER);
        nameRow.setPadding(0, 0, 0, 8);

        TextView nameText = new TextView(this);
        nameText.setText(userName != null ? userName : "SR User");
        nameText.setTextColor(Color.WHITE);
        nameText.setTextSize(20);
        nameText.setTypeface(null, android.graphics.Typeface.BOLD);
        nameRow.addView(nameText);

        TextView genderFlagText = new TextView(this);
        genderFlagText.setText(" " + gender + " " + country);
        genderFlagText.setTextSize(18);
        if (gender.equals("♀")) {
            genderFlagText.setTextColor(Color.parseColor("#FF4081"));
        } else {
            genderFlagText.setTextColor(Color.parseColor("#00B0FF"));
        }
        nameRow.addView(genderFlagText);

        layout.addView(nameRow);

        TextView idText = new TextView(this);
        String displayId = (userId != null && !userId.isEmpty()) ? userId : "493859069";
        idText.setText("ID: " + displayId + " 📋");
        idText.setTextColor(Color.parseColor("#00E676"));
        idText.setTextSize(14);
        idText.setGravity(Gravity.CENTER);
        idText.setPadding(0, 0, 0, 20);
        
        idText.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("User ID", displayId);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "ID Copied: " + displayId, Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(idText);

        int sendLevel = LevelUtils.calculateLevel(sendingCoins);
        int recvLevel = LevelUtils.calculateLevel(receivingCoins);

        LinearLayout levelRow = new LinearLayout(this);
        levelRow.setOrientation(LinearLayout.HORIZONTAL);
        levelRow.setGravity(Gravity.CENTER);
        levelRow.setPadding(0, 0, 0, 25);

        View sendBadgeView = createSoftBadgeView("⭐ Send Lv." + sendLevel, LevelUtils.getLevelColor(sendLevel));
        levelRow.addView(sendBadgeView);

        View recvBadgeView = createSoftBadgeView("👑 Recv Lv." + recvLevel, LevelUtils.getLevelColor(recvLevel));
        levelRow.addView(recvBadgeView);

        layout.addView(levelRow);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        actionRow.setPadding(0, 10, 0, 10);

        Button btnFollow = new Button(this);
        btnFollow.setText("✨ Follow");
        btnFollow.setBackgroundColor(Color.parseColor("#FFD700"));
        btnFollow.setTextColor(Color.BLACK);
        btnFollow.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, 120, 1.0f);
        p1.setMargins(0, 0, 8, 0);
        btnFollow.setLayoutParams(p1);
        btnFollow.setOnClickListener(v -> Toast.makeText(this, "Successfully Followed " + userName, Toast.LENGTH_SHORT).show());
        actionRow.addView(btnFollow);

        Button btnPopupGift = new Button(this);
        btnPopupGift.setText("🎁 Gift");
        btnPopupGift.setBackgroundColor(Color.parseColor("#FF4081"));
        btnPopupGift.setTextColor(Color.WHITE);
        btnPopupGift.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, 120, 1.0f);
        p2.setMargins(8, 0, 8, 0);
        btnPopupGift.setLayoutParams(p2);
        btnPopupGift.setOnClickListener(v -> {
            profileDialog.dismiss();
            showPremiumGiftPanel();
        });
        actionRow.addView(btnPopupGift);

        layout.addView(actionRow);

        profileDialog.setContentView(layout);
        Window window = profileDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1450);
            window.setGravity(Gravity.BOTTOM);
        }
        profileDialog.show();
    }

    private View createSoftBadgeView(String text, int colorInt) {
        TextView badge = new TextView(this);
        badge.setText(text);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(12);
        badge.setTypeface(null, android.graphics.Typeface.BOLD);
        badge.setPadding(22, 10, 22, 10);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(colorInt);
        bg.setCornerRadius(30);
        badge.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 16, 0);
        badge.setLayoutParams(lp);

        return badge;
    }

    private void showFullLevelCenterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 35, 35, 35);

        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(0, 0, 0, 20);

        TextView title = new TextView(this);
        title.setText("🏆 SR Level Center (Lv.1 - Lv.150)");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        topBar.addView(title);

        Button btnClose = new Button(this);
        btnClose.setText("✕");
        btnClose.setBackgroundColor(Color.TRANSPARENT);
        btnClose.setTextColor(Color.WHITE);
        btnClose.setTextSize(16);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        topBar.addView(btnClose);

        root.addView(topBar);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1400));
        scrollView.setFillViewport(true);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);

        for (int lvl = 1; lvl <= 150; lvl++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(25, 20, 25, 20);
            row.setGravity(Gravity.CENTER_VERTICAL);

            GradientDrawable rBg = new GradientDrawable();
            rBg.setColor(Color.parseColor("#2A1B4E"));
            rBg.setCornerRadius(20);
            row.setBackground(rBg);

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p.setMargins(0, 0, 0, 14);
            row.setLayoutParams(p);

            int levelColor = LevelUtils.getLevelColor(lvl);
            String badgeText = LevelUtils.getLevelBadgeText(lvl);
            String targetCoins = LevelUtils.getLevelTargetCoinsFormatted(lvl);

            TextView badge = new TextView(this);
            badge.setText(badgeText);
            badge.setTextColor(Color.WHITE);
            badge.setTextSize(14);
            badge.setTypeface(null, android.graphics.Typeface.BOLD);
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(20, 12, 20, 12);

            GradientDrawable bBg = new GradientDrawable();
            bBg.setColor(levelColor);
            bBg.setCornerRadius(25);
            badge.setBackground(bBg);
            badge.setMinWidth(110);
            row.addView(badge);

            LinearLayout textStack = new LinearLayout(this);
            textStack.setOrientation(LinearLayout.VERTICAL);
            textStack.setPadding(20, 0, 0, 0);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            textStack.setLayoutParams(tp);

            TextView titleTxt = new TextView(this);
            titleTxt.setText("Level Badge Tier " + lvl);
            titleTxt.setTextColor(Color.WHITE);
            titleTxt.setTextSize(15);
            titleTxt.setTypeface(null, android.graphics.Typeface.BOLD);
            textStack.addView(titleTxt);

            TextView targetTxt = new TextView(this);
            targetTxt.setText("Target: " + targetCoins);
            targetTxt.setTextColor(Color.parseColor("#00E676"));
            targetTxt.setTextSize(12);
            textStack.addView(targetTxt);

            row.addView(textStack);
            listContainer.addView(row);
        }

        scrollView.addView(listContainer);
        root.addView(scrollView);

        dialog.setContentView(root);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1600);
            window.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private void takeSeat(int seatIndex, String seatKey) {
        final String userName = prefs.getString("USER_NAME", "User");
        final String userImageUriStr = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

        for (int i = 0; i < 10; i++) {
            final int oldIndex = i;
            databaseReference.child("seats").child("seat_" + i).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String sUserId = snapshot.child("userId").getValue(String.class);
                    if (sUserId != null && sUserId.equals(currentUserId)) {
                        databaseReference.child("seats").child("seat_" + oldIndex).removeValue();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

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
