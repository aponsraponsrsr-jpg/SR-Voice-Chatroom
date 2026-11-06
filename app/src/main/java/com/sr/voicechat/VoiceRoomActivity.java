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
    private TextView roomTitleText, roomInfoText, textOnlineCount;
    private ImageView btnOnlineList, roomHeaderProfileImg;
    private boolean isMuted = false;

    private ListView chatListView;
    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private SharedPreferences prefs;

    private DatabaseReference databaseReference;
    private DatabaseReference roomMetaRef;
    private DatabaseReference userDatabaseRef;
    private DatabaseReference messagesRef;
    private FirebaseAuth mAuth;
    private String roomOwnerId = "";
    private String currentUserId = "";

    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText dialogInputName, dialogInputBio;
    private DatabaseReference activeRoomsRef;

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

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + roomOwnerId);
        roomMetaRef = databaseReference.child("meta");
        messagesRef = databaseReference.child("messages");
        
        activeRoomsRef = FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId);
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        syncLatestUserProfile();
        listenToRoomDetails();

        btnPower = findViewById(R.id.btnPower);
        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        textOnlineCount = findViewById(R.id.textOnlineCount);
        btnOnlineList = findViewById(R.id.btnOnlineList);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);

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

        // Header Click -> Opens Large Premium Room Info & Member List Dialog
        if (roomTitleText != null) {
            roomTitleText.setOnClickListener(v -> showRoomInfoAndMembersBottomSheet());
        }

        if (roomHeaderProfileImg != null) {
            roomHeaderProfileImg.setOnClickListener(v -> {
                if (currentUserId.equals(roomOwnerId)) {
                    showEditRoomDialog();
                } else {
                    showRoomInfoAndMembersBottomSheet();
                }
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
                        if (!message.isEmpty()) {
                            String msgId = messagesRef.push().getKey();
                            if (msgId != null) {
                                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                                msgMap.put("senderName", userName);
                                msgMap.put("message", message);
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

    @Override
    public void onBackPressed() {
        leaveRoomAndFinish();
    }

    private void showGiftPanel() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_gift_panel, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tabGift = sheetView.findViewById(R.id.tabGift);
        TextView tabCpGift = sheetView.findViewById(R.id.tabCpGift);
        TextView tabLuckyGift = sheetView.findViewById(R.id.tabLuckyGift);
        RecyclerView rvGifts = sheetView.findViewById(R.id.rvGifts);
        Button btnSendGift = sheetView.findViewById(R.id.btnSendGift);

        List<GiftModel> allGifts = new ArrayList<>();
        allGifts.add(new GiftModel("Sports Car", "100K", R.drawable.ic_gift_placeholder, "gift"));
        allGifts.add(new GiftModel("Dragon", "500K", R.drawable.ic_gift_placeholder, "gift"));
        allGifts.add(new GiftModel("Tiger", "250K", R.drawable.ic_gift_placeholder, "gift"));
        allGifts.add(new GiftModel("Spiderman", "300K", R.drawable.ic_gift_placeholder, "gift"));
        
        allGifts.add(new GiftModel("Love Ring", "99K", R.drawable.ic_gift_placeholder, "cp"));
        allGifts.add(new GiftModel("Rose Bouquet", "50K", R.drawable.ic_gift_placeholder, "cp"));
        
        allGifts.add(new GiftModel("Mystery Box", "10K", R.drawable.ic_coin, "lucky"));
        allGifts.add(new GiftModel("Lucky Star", "20K", R.drawable.ic_coin, "lucky"));

        List<GiftModel> displayedGifts = new ArrayList<>();
        for (GiftModel g : allGifts) {
            if (g.getCategory().equals("gift")) {
                displayedGifts.add(g);
            }
        }

        GiftAdapter adapter = new GiftAdapter(displayedGifts, gift -> {
            Toast.makeText(this, "Selected: " + gift.getGiftName(), Toast.LENGTH_SHORT).show();
        });
        rvGifts.setAdapter(adapter);

        tabGift.setOnClickListener(v -> {
            displayedGifts.clear();
            for (GiftModel g : allGifts) { if (g.getCategory().equals("gift")) displayedGifts.add(g); }
            adapter.notifyDataSetChanged();
        });

        tabCpGift.setOnClickListener(v -> {
            displayedGifts.clear();
            for (GiftModel g : allGifts) { if (g.getCategory().equals("cp")) displayedGifts.add(g); }
            adapter.notifyDataSetChanged();
        });

        tabLuckyGift.setOnClickListener(v -> {
            displayedGifts.clear();
            for (GiftModel g : allGifts) { if (g.getCategory().equals("lucky")) displayedGifts.add(g); }
            adapter.notifyDataSetChanged();
        });

        btnSendGift.setOnClickListener(v -> {
            String userName = prefs.getString("USER_NAME", "User");
            String msgId = messagesRef.push().getKey();
            if (msgId != null) {
                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                msgMap.put("senderName", "🎁 " + userName);
                msgMap.put("message", "sent a premium gift!");
                messagesRef.child(msgId).setValue(msgMap);
            }
            Toast.makeText(this, "Gift Sent Successfully!", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
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

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void listenToRealtimeChatMessages() {
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String senderName = snapshot.child("senderName").getValue(String.class);
                String message = snapshot.child("message").getValue(String.class);
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
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenToRoomDetails() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String roomName = snapshot.child("roomName").getValue(String.class);
                String roomBio = snapshot.child("roomBio").getValue(String.class);
                String ownerImage = snapshot.child("roomOwnerImage").getValue(String.class);

                if (roomName != null && !roomName.isEmpty()) {
                    if (roomTitleText != null) {
                        roomTitleText.setText("🌟 " + roomName);
                    }
                } else {
                    if (roomTitleText != null) {
                        roomTitleText.setText("🌟 SR Voice Room");
                    }
                }

                if (roomInfoText != null) {
                    String bioText = (roomBio != null && !roomBio.isEmpty()) ? roomBio : "Welcome to my voice room!";
                    roomInfoText.setText("ID: " + roomOwnerId + " | 📝 " + bioText);
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

                if (currentUserId.equals(roomOwnerId)) {
                    activeRoomsRef.child("roomOwnerId").setValue(roomOwnerId);
                    activeRoomsRef.child("roomName").setValue(roomName != null ? roomName : "🌟 SR Voice Room");
                    activeRoomsRef.child("roomBio").setValue(roomBio != null ? roomBio : "Welcome to room!");
                    activeRoomsRef.child("roomOwnerImage").setValue(ownerImage != null ? ownerImage : "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
        dialogInputBio.setHint("Enter Room Bio...");
        layout.addView(dialogInputBio);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = dialogInputName.getText().toString().trim();
            String newBio = dialogInputBio.getText().toString().trim();

            if (!newName.isEmpty()) {
                roomMetaRef.child("roomName").setValue(newName);
            }
            if (!newBio.isEmpty()) {
                roomMetaRef.child("roomBio").setValue(newBio);
            }
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
                    activeRoomsRef.child("roomOwnerImage").setValue(encodedImage);
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
                if (textOnlineCount != null) {
                    textOnlineCount.setText(String.valueOf(count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 🌟 Large, Clean & Premium Room Info & Member List Dialog (Matching Your Reference Image Style)
    private void showRoomInfoAndMembersBottomSheet() {
        BottomSheetDialog sheetDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 60);
        
        // Clean White/Light Premium Background matching real chat apps
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#FFFFFF"), Color.parseColor("#F8F9FA")}
        );
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        mainLayout.setBackground(bg);

        // Top Header Row with Title and Close Button
        LinearLayout topHeaderLayout = new LinearLayout(this);
        topHeaderLayout.setOrientation(LinearLayout.HORIZONTAL);
        topHeaderLayout.setGravity(Gravity.CENTER_VERTICAL);
        topHeaderLayout.setPadding(0, 0, 0, 20);

        TextView title = new TextView(this);
        title.setText("Bangladesh Official Room");
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        title.setLayoutParams(titleParams);
        topHeaderLayout.addView(title);

        TextView closeBtn = new TextView(this);
        closeBtn.setText("✕");
        closeBtn.setTextColor(Color.parseColor("#666666"));
        closeBtn.setTextSize(20);
        closeBtn.setPadding(20, 10, 10, 10);
        closeBtn.setOnClickListener(v -> sheetDialog.dismiss());
        topHeaderLayout.addView(closeBtn);

        mainLayout.addView(topHeaderLayout);

        // Tab Navigation Layout (Room information & Member List)
        LinearLayout tabLayout = new LinearLayout(this);
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabLayout.setGravity(Gravity.CENTER);
        tabLayout.setPadding(0, 10, 0, 25);

        Button btnTabInfo = new Button(this);
        btnTabInfo.setText("Room information");
        btnTabInfo.setAllCaps(false);
        btnTabInfo.setBackgroundColor(Color.parseColor("#7B1FA2"));
        btnTabInfo.setTextColor(Color.WHITE);
        btnTabInfo.setTextSize(14);

        Button btnTabMembers = new Button(this);
        btnTabMembers.setText("Member List");
        btnTabMembers.setAllCaps(false);
        btnTabMembers.setBackgroundColor(Color.parseColor("#E0E0E0"));
        btnTabMembers.setTextColor(Color.parseColor("#555555"));
        btnTabMembers.setTextSize(14);

        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tabParams.setMargins(10, 0, 10, 0);
        btnTabInfo.setLayoutParams(tabParams);
        btnTabMembers.setLayoutParams(tabParams);

        tabLayout.addView(btnTabInfo);
        tabLayout.addView(btnTabMembers);
        mainLayout.addView(tabLayout);

        // Dynamic Content Scroll Container
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(10, 10, 10, 10);
        mainLayout.addView(contentLayout);

        // Load Room Info View by default
        loadRoomInfoLargeView(contentLayout);

        btnTabInfo.setOnClickListener(v -> {
            btnTabInfo.setBackgroundColor(Color.parseColor("#7B1FA2"));
            btnTabInfo.setTextColor(Color.WHITE);
            btnTabMembers.setBackgroundColor(Color.parseColor("#E0E0E0"));
            btnTabMembers.setTextColor(Color.parseColor("#555555"));
            loadRoomInfoLargeView(contentLayout);
        });

        btnTabMembers.setOnClickListener(v -> {
            btnTabMembers.setBackgroundColor(Color.parseColor("#7B1FA2"));
            btnTabMembers.setTextColor(Color.WHITE);
            btnTabInfo.setBackgroundColor(Color.parseColor("#E0E0E0"));
            btnTabInfo.setTextColor(Color.parseColor("#555555"));
            loadMemberListLargeView(contentLayout);
        });

        sheetDialog.setContentView(mainLayout);
        
        // Force larger height / full display window look like the reference photo
        Window window = sheetDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500); // Large height for clear viewing
            window.setGravity(Gravity.BOTTOM);
        }

        sheetDialog.show();
    }

    private void loadRoomInfoLargeView(LinearLayout container) {
        container.removeAllViews();

        roomMetaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String rName = snapshot.child("roomName").getValue(String.class);
                String rBio = snapshot.child("roomBio").getValue(String.class);

                // Premium Banner Card Box matching reference photo
                LinearLayout bannerCard = new LinearLayout(VoiceRoomActivity.this);
                bannerCard.setOrientation(LinearLayout.HORIZONTAL);
                bannerCard.setPadding(25, 25, 25, 25);
                bannerCard.setGravity(Gravity.CENTER_VERTICAL);
                
                GradientDrawable bannerBg = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{Color.parseColor("#FFE082"), Color.parseColor("#FFB300")}
                );
                bannerBg.setCornerRadius(20);
                bannerCard.setBackground(bannerBg);

                LinearLayout textStack = new LinearLayout(VoiceRoomActivity.this);
                textStack.setOrientation(LinearLayout.VERTICAL);

                TextView nameTxt = new TextView(VoiceRoomActivity.this);
                nameTxt.setText(rName != null ? rName : "Bangladesh Official Room");
                nameTxt.setTextColor(Color.parseColor("#111111"));
                nameTxt.setTextSize(16);
                nameTxt.setTypeface(null, android.graphics.Typeface.BOLD);
                textStack.addView(nameTxt);

                TextView idTxt = new TextView(VoiceRoomActivity.this);
                idTxt.setText("ID: " + roomOwnerId);
                idTxt.setTextColor(Color.parseColor("#333333"));
                idTxt.setTextSize(13);
                textStack.addView(idTxt);

                bannerCard.addView(textStack);
                container.addView(bannerCard);

                // Members count detail row
                addDetailRow(container, "Number of members", "1/500");
                addDetailRow(container, "Room Owner", prefs.getString("USER_NAME", "MR ADIL"));
                addDetailRow(container, "Country/Region", "🇧🇩 Bangladesh");
                addDetailRow(container, "Tag", "Chat");
                addDetailRow(container, "Notice of Room", (rBio != null && !rBio.isEmpty() ? rBio : "Girls Respect\nPersonal Chat Not Allowed\nBad Comments Not Allowed"));

                if (currentUserId.equals(roomOwnerId)) {
                    Button btnEdit = new Button(VoiceRoomActivity.this);
                    btnEdit.setText("⚙️ EDIT ROOM DETAILS");
                    btnEdit.setBackgroundColor(Color.parseColor("#00E676"));
                    btnEdit.setTextColor(Color.BLACK);
                    btnEdit.setTypeface(null, android.graphics.Typeface.BOLD);
                    btnEdit.setOnClickListener(v -> showEditRoomDialog());
                    container.addView(btnEdit);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addDetailRow(LinearLayout container, String label, String value) {
        LinearLayout row = new LinearLayout(container.getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 15, 0, 15);

        TextView lbl = new TextView(container.getContext());
        lbl.setText(label);
        lbl.setTextColor(Color.parseColor("#777777"));
        lbl.setTextSize(12);
        row.addView(lbl);

        TextView val = new TextView(container.getContext());
        val.setText(value);
        val.setTextColor(Color.parseColor("#111111"));
        val.setTextSize(15);
        val.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(val);

        // Divider line
        View divider = new View(container.getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        
        container.addView(row);
        container.addView(divider);
    }

    private void loadMemberListLargeView(LinearLayout container) {
        container.removeAllViews();

        databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView countHeader = new TextView(VoiceRoomActivity.this);
                countHeader.setText("Number of members: " + snapshot.getChildrenCount() + "/500   |   🛡️ Admins 2/3");
                countHeader.setTextColor(Color.parseColor("#555555"));
                countHeader.setTextSize(13);
                countHeader.setPadding(0, 0, 0, 20);
                container.addView(countHeader);

                // Owner Row matching your reference screenshot 38733.jpg
                LinearLayout ownerRow = createMemberRowItem("MR ADIL", "👑 Owner", "#FFD700", roomOwnerId.equals(currentUserId));
                container.addView(ownerRow);

                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.getValue(String.class);
                    String uId = child.getKey();
                    if (name != null && !uId.equals(roomOwnerId)) {
                        LinearLayout memberRow = createMemberRowItem(name, "🛡️ Admin", "#00E676", false);
                        container.addView(memberRow);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private LinearLayout createMemberRowItem(String name, String role, String badgeColorHex, boolean isSelf) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(20, 20, 20, 20);
        row.setGravity(Gravity.CENTER_VERTICAL);
        
        GradientDrawable rowBg = new GradientDrawable();
        rowBg.setColor(Color.parseColor("#FFFFFF"));
        rowBg.setCornerRadius(16);
        row.setBackground(rowBg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 15);
        row.setLayoutParams(params);

        // Avatar placeholder
        ImageView avatar = new ImageView(this);
        avatar.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
        avatar.setImageResource(android.R.drawable.ic_menu_my_calendar);
        avatar.setBackgroundColor(Color.parseColor("#DDDDDD"));
        row.addView(avatar);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setPadding(25, 0, 0, 0);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        textLayout.setLayoutParams(textParams);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(Color.parseColor("#111111"));
        nameView.setTextSize(16);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        textLayout.addView(nameView);

        TextView roleView = new TextView(this);
        roleView.setText(role);
        roleView.setTextColor(Color.parseColor(badgeColorHex));
        roleView.setTextSize(12);
        roleView.setTypeface(null, android.graphics.Typeface.BOLD);
        textLayout.addView(roleView);

        row.addView(textLayout);
        return row;
    }

    private void showOnlineMembersDialog() {
        showRoomInfoAndMembersBottomSheet();
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

        databaseReference.child("seats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < 10; i++) {
                    ImageView seatImg = findViewById(seatImgIds[i]);
                    TextView seatTxt = findViewById(seatTxtIds[i]);
                    String seatKey = "seat_" + i;

                    if (snapshot.hasChild(seatKey)) {
                        String name = snapshot.child(seatKey).child("userName").getValue(String.class);
                        String imageBase64 = snapshot.child(seatKey).child("userImage").getValue(String.class);

                        if (seatTxt != null) {
                            if (i == 0) {
                                seatTxt.setText(name != null && !name.isEmpty() ? name : "Host");
                            } else {
                                seatTxt.setText(name != null && !name.isEmpty() ? name : String.valueOf(i + 1));
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
                                    seatImg.setImageTintList(null);
                                }
                            } catch (Exception e) {
                                seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                            }
                        } else {
                            if (seatImg != null) {
                                seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                                seatImg.setPadding(12, 12, 12, 12);
                                seatImg.setImageTintList(null);
                            }
                        }
                    } else {
                        if (seatTxt != null) {
                            seatTxt.setText(i == 0 ? "Host" : String.valueOf(i + 1));
                        }
                        if (seatImg != null) {
                            seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                            seatImg.setPadding(12, 12, 12, 12);
                            seatImg.setImageTintList(null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
                            String occupantUserId = seatSnap.child("userId").getValue(String.class);
                            boolean isOccupied = occupantName != null && !occupantName.isEmpty();

                            List<String> optionsList = new ArrayList<>();
                            if (isOccupied) {
                                optionsList.add("👤 View Profile"); 
                                
                                if (currentUserId.equals(roomOwnerId)) {
                                    if (seatIndex > 0 && (occupantUserId == null || !occupantUserId.equals(roomOwnerId))) {
                                        optionsList.add("🚫 Remove from Mic (Kick)");
                                    }
                                    if (seatIndex > 0) {
                                        optionsList.add("🔇 Mute Seat");
                                    }
                                } else {
                                    if (occupantUserId != null && occupantUserId.equals(roomOwnerId)) {
                                        // Owner is fully protected!
                                    } else if (currentUserId.equals(occupantUserId)) {
                                        optionsList.add("🚪 Leave Mic");
                                    }
                                }
                            } else {
                                optionsList.add("🎙️ Take Mic");
                            }
                            optionsList.add("❌ Cancel");

                            CharSequence[] seatOptions = optionsList.toArray(new CharSequence[0]);

                            AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                            builder.setTitle("🎙️ Seat " + seatNumber + (isOccupied ? " (" + occupantName + ")" : " (Empty)"));
                            builder.setItems(seatOptions, (dialog, which) -> {
                                String selectedOption = seatOptions[which].toString();
                                if (selectedOption.contains("View Profile")) {
                                    showUserProfileCard(occupantName, seatIndex, seatSnap);
                                } else if (selectedOption.contains("Take Mic")) {
                                    takeSeat(seatIndex, seatKey);
                                } else if (selectedOption.contains("Remove from Mic")) {
                                    if (occupantUserId != null && occupantUserId.equals(roomOwnerId)) {
                                        Toast.makeText(VoiceRoomActivity.this, "Security Alert: Cannot kick Room Owner!", Toast.LENGTH_LONG).show();
                                    } else {
                                        databaseReference.child("seats").child(seatKey).removeValue();
                                        Toast.makeText(VoiceRoomActivity.this, occupantName + " removed from seat!", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (selectedOption.contains("Leave Mic")) {
                                    databaseReference.child("seats").child(seatKey).removeValue();
                                    Toast.makeText(VoiceRoomActivity.this, "Left seat", Toast.LENGTH_SHORT).show();
                                } else if (selectedOption.contains("Mute Seat")) {
                                    Toast.makeText(VoiceRoomActivity.this, "Seat muted", Toast.LENGTH_SHORT).show();
                                }
                            });
                            builder.show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                });
            }
        }
    }

    private void showUserProfileCard(String userName, int seatIndex, DataSnapshot seatSnap) {
        BottomSheetDialog profileDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 60, 50, 60);
        
        GradientDrawable gradientDrawable = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#2A1B4E"), Color.parseColor("#120A2A")}
        );
        gradientDrawable.setCornerRadii(new float[]{40, 40, 40, 40, 0, 0, 0, 0});
        layout.setBackground(gradientDrawable);
        
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layout.setLayoutParams(params);

        String occupantId = seatSnap.child("userId").getValue(String.class);
        if (occupantId == null || occupantId.isEmpty()) {
            occupantId = currentUserId;
        }

        boolean isOwner = occupantId.equals(roomOwnerId);

        TextView txtBadge = new TextView(this);
        txtBadge.setText(isOwner ? "👑 SR ROOM OWNER & VIP" : "👑 SR VIP VERIFIED PROFILE");
        txtBadge.setTextColor(Color.parseColor("#FFD700"));
        txtBadge.setTextSize(13);
        txtBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        txtBadge.setGravity(Gravity.CENTER);
        layout.addView(txtBadge);

        TextView txtName = new TextView(this);
        txtName.setText(userName != null ? userName : "User");
        txtName.setTextSize(24);
        txtName.setTypeface(null, android.graphics.Typeface.BOLD);
        txtName.setTextColor(Color.parseColor("#FFFFFF"));
        txtName.setGravity(Gravity.CENTER);
        txtName.setPadding(0, 15, 0, 8);
        layout.addView(txtName);

        TextView txtIdInfo = new TextView(this);
        txtIdInfo.setText("ID: " + occupantId + "  |  Seat: #" + (seatIndex == 0 ? "Host" : (seatIndex + 1)));
        txtIdInfo.setTextColor(Color.parseColor("#00E676"));
        txtIdInfo.setTextSize(14);
        txtIdInfo.setTypeface(null, android.graphics.Typeface.BOLD);
        txtIdInfo.setGravity(Gravity.CENTER);
        layout.addView(txtIdInfo);

        LinearLayout statsLayout = new LinearLayout(this);
        statsLayout.setOrientation(LinearLayout.HORIZONTAL);
        statsLayout.setGravity(Gravity.CENTER);
        statsLayout.setPadding(0, 25, 0, 35);

        TextView txtFans = new TextView(this);
        txtFans.setText("👥 Fans: 1.5K");
        txtFans.setPadding(25, 12, 25, 12);
        txtFans.setTextColor(Color.parseColor("#FFFFFF"));
        statsLayout.addView(txtFans);

        TextView txtFollowers = new TextView(this);
        txtFollowers.setText("❤️ Following: 420");
        txtFollowers.setPadding(25, 12, 25, 12);
        txtFollowers.setTextColor(Color.parseColor("#FFFFFF"));
        statsLayout.addView(txtFollowers);

        layout.addView(statsLayout);

        Button btnSendDirectGift = new Button(this);
        btnSendDirectGift.setText("🎁 Send Gift to " + (userName != null ? userName : "User"));
        btnSendDirectGift.setBackgroundColor(Color.parseColor("#FF4081"));
        btnSendDirectGift.setTextColor(Color.parseColor("#FFFFFF"));
        btnSendDirectGift.setTypeface(null, android.graphics.Typeface.BOLD);
        btnSendDirectGift.setOnClickListener(v -> {
            profileDialog.dismiss();
            showGiftPanel();
        });
        layout.addView(btnSendDirectGift);

        profileDialog.setContentView(layout);
        profileDialog.show();
    }

    private void takeSeat(int seatIndex, String seatKey) {
        final String userName = prefs.getString("USER_NAME", "User");
        final String userImageUriStr = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

        for (int j = 0; j < 10; j++) {
            final int oldIndex = j;
            databaseReference.child("seats").child("seat_" + oldIndex).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String sUserId = snapshot.child("userId").getValue(String.class);
                    String existingName = snapshot.child("userName").getValue(String.class);
                    if ((sUserId != null && sUserId.equals(currentUserId)) || (existingName != null && existingName.equals(userName))) {
                        databaseReference.child("seats").child("seat_" + oldIndex).removeValue();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        final String[] imageBase64 = {userImageUriStr};
        
        if (userImageUriStr != null && !userImageUriStr.isEmpty() && (userImageUriStr.startsWith("content://") || userImageUriStr.startsWith("file://"))) {
            try {
                Uri uri = Uri.parse(userImageUriStr);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    byte[] imageBytes = baos.toByteArray();
                    imageBase64[0] = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                }
            } catch (Exception e) {
                // keep base64 from prefs
            }
        }

        databaseReference.child("seats").child(seatKey).child("userId").setValue(currentUserId);
        databaseReference.child("seats").child(seatKey).child("userName").setValue(userName);
        databaseReference.child("seats").child(seatKey).child("userImage").setValue(imageBase64[0]);
        
        databaseReference.child("seats").child(seatKey).onDisconnect().removeValue();
        
        Toast.makeText(VoiceRoomActivity.this, "Joined seat successfully!", Toast.LENGTH_SHORT).show();
    }

    private void leaveRoomAndFinish() {
        databaseReference.child("online_users").child(currentUserId).removeValue();
        
        for (int i = 0; i < 10; i++) {
            final int index = i;
            databaseReference.child("seats").child("seat_" + index).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String sUserId = snapshot.child("userId").getValue(String.class);
                    String name = snapshot.child("userName").getValue(String.class);
                    String uName = prefs.getString("USER_NAME", "User");
                    if ((sUserId != null && sUserId.equals(currentUserId)) || (name != null && name.equals(uName))) {
                        databaseReference.child("seats").child("seat_" + index).removeValue();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        finish();
    }
}
