package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

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
        activeRoomsRef = FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId);
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        checkAndSyncPermanentUser();
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
                        finish();
                    }
                });
                builder.show();
            });
        }

        if (roomTitleText != null) {
            roomTitleText.setOnClickListener(v -> {
                if (currentUserId.equals(roomOwnerId)) {
                    showEditRoomDialog();
                } else {
                    Toast.makeText(this, "Only Room Owner can edit room details!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (roomHeaderProfileImg != null) {
            roomHeaderProfileImg.setOnClickListener(v -> {
                if (currentUserId.equals(roomOwnerId)) {
                    showEditRoomDialog();
                }
            });
        }

        if (roomInfoText != null) {
            roomInfoText.setOnClickListener(v -> showMembersAndAdminsDialog());
        }

        if (btnOnlineList != null) {
            btnOnlineList.setOnClickListener(v -> showOnlineMembersDialog());
        }

        chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatMessages.add("🌟 System: SR Voice Room-এ স্বাগতম!");

        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        if (chatListView != null) {
            chatListView.setAdapter(chatAdapter);
        }

        setupSeatInteractions();
        listenToFirebaseSeats();
        listenToOnlineMembers();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("💬 Send Message to Room");

                final EditText input = new EditText(VoiceRoomActivity.this);
                input.setHint("Type your message here...");
                builder.setView(input);

                builder.setPositiveButton("Send", (dialog, which) -> {
                    String message = input.getText().toString().trim();
                    String userName = prefs.getString("USER_NAME", "User");
                    if (!message.isEmpty()) {
                        chatMessages.add("👤 " + userName + ": " + message);
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            });
        }

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) {
            btnGiftBox.setOnClickListener(v -> {
                CharSequence[] gifts = {"🌹 Rose (10 Coins)", "👑 Crown (500 Coins)", "💎 Diamond (1000 Coins)"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎁 Send Gift");
                builder.setItems(gifts, (dialog, which) -> {
                    chatMessages.add("🎁 Gift Sent: " + gifts[which]);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Successfully Sent Gift!", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }

        btnGameBox = findViewById(R.id.btnGameBox);
        if (btnGameBox != null) {
            btnGameBox.setOnClickListener(v -> {
                CharSequence[] games = {"🎮 Ludo Game", "🎯 Lucky Wheel", "🎲 Dice Battle"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎮 Voice Room Games");
                builder.setItems(games, (dialog, which) -> {
                    Toast.makeText(this, "Starting " + games[games.length > 0 ? which : 0] + "...", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }
    }

    private void checkAndSyncPermanentUser() {
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            prefs.edit().putString("USER_EMAIL", userEmail).apply();
            String safeEmailKey = userEmail.replace(".", "_").replace("@", "_");

            userDatabaseRef.child(safeEmailKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    SharedPreferences.Editor editor = prefs.edit();
                    if (snapshot.exists()) {
                        String savedId = snapshot.child("userId").getValue(String.class);
                        String savedName = snapshot.child("userName").getValue(String.class);
                        String savedImage = snapshot.child("userImage").getValue(String.class);

                        String existingLocalId = prefs.getString("MY_9_DIGIT_ID", "");
                        if (existingLocalId != null && !existingLocalId.isEmpty()) {
                            currentUserId = existingLocalId;
                        } else if (savedId != null && !savedId.isEmpty()) {
                            editor.putString("USER_ID", savedId);
                            editor.putString("MY_9_DIGIT_ID", savedId);
                            currentUserId = savedId;
                        } else {
                            editor.putString("USER_ID", currentUserId);
                            editor.putString("MY_9_DIGIT_ID", currentUserId);
                            userDatabaseRef.child(safeEmailKey).child("userId").setValue(currentUserId);
                        }

                        if (savedName != null && !savedName.isEmpty()) {
                            editor.putString("USER_NAME", savedName);
                        }
                        if (savedImage != null) {
                            editor.putString("USER_IMAGE", savedImage);
                        }
                        editor.apply();

                        if (currentUserId.equals(roomOwnerId)) {
                            roomMetaRef.child("roomOwnerId").setValue(roomOwnerId);
                            roomMetaRef.child("roomOwnerName").setValue(savedName != null ? savedName : "Owner");
                        }
                    } else {
                        userDatabaseRef.child(safeEmailKey).child("userId").setValue(currentUserId);
                        userDatabaseRef.child(safeEmailKey).child("userName").setValue(prefs.getString("USER_NAME", "User"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
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
                            roomHeaderProfileImg.setImageBitmap(decodedByte);
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

    private void showMembersAndAdminsDialog() {
        databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> memberList = new ArrayList<>();
                memberList.add("👑 Room Owner ID: " + roomOwnerId);
                memberList.add("🛡️ Admins: Managed by Owner");
                memberList.add("--- Active Members ---");

                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.getValue(String.class);
                    if (name != null) {
                        memberList.add("👤 " + name);
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("👥 Members & Admins List");
                builder.setItems(memberList.toArray(new CharSequence[0]), null);
                builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showOnlineMembersDialog() {
        databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> onlineList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.getValue(String.class);
                    if (name != null) {
                        onlineList.add("🟢 " + name);
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🌐 Online Users List");
                if (onlineList.isEmpty()) {
                    builder.setMessage("No active users found.");
                } else {
                    builder.setItems(onlineList.toArray(new CharSequence[0]), null);
                }
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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
                                seatTxt.setText(name != null && !name.isEmpty() ? name + " (Owner)" : "Host (Owner)");
                            } else {
                                seatTxt.setText(name != null && !name.isEmpty() ? name : String.valueOf(i + 1));
                            }
                        }

                        if (seatImg != null && imageBase64 != null && !imageBase64.isEmpty()) {
                            try {
                                byte[] decodedString = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                if (decodedByte != null) {
                                    seatImg.setImageBitmap(decodedByte);
                                    seatImg.setPadding(0, 0, 0, 0);
                                    seatImg.setImageTintList(null);
                                }
                            } catch (Exception e) {
                                seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                                seatImg.setPadding(12, 12, 12, 12);
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
                            seatTxt.setText(i == 0 ? "Host (Owner)" : String.valueOf(i + 1));
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
                            boolean isOccupied = occupantName != null && !occupantName.isEmpty();

                            CharSequence[] seatOptions;
                            if (currentUserId.equals(roomOwnerId)) {
                                if (isOccupied) {
                                    seatOptions = new CharSequence[]{"Remove from Mic (Kick)", "Mute Seat", "Cancel"};
                                } else {
                                    seatOptions = new CharSequence[]{"Take Mic", "Cancel"};
                                }
                            } else {
                                if (isOccupied) {
                                    seatOptions = new CharSequence[]{"View Profile", "Leave Mic"};
                                } else {
                                    seatOptions = new CharSequence[]{"Take Mic", "Leave Mic"};
                                }
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                            builder.setTitle("🎙️ Seat " + seatNumber + (isOccupied ? " (" + occupantName + ")" : " (Empty)"));
                            builder.setItems(seatOptions, (dialog, which) -> {
                                String userName = prefs.getString("USER_NAME", "User");
                                String userImageUriStr = prefs.getString("USER_IMAGE", "");

                                if (currentUserId.equals(roomOwnerId)) {
                                    if (isOccupied) {
                                        if (which == 0) { 
                                            databaseReference.child("seats").child(seatKey).removeValue();
                                            Toast.makeText(VoiceRoomActivity.this, occupantName + " removed from seat!", Toast.LENGTH_SHORT).show();
                                        } else if (which == 1) { 
                                            Toast.makeText(VoiceRoomActivity.this, "Seat muted by owner", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (which == 0) { 
                                            takeSeat(seatIndex, seatKey, userName, userImageUriStr);
                                        }
                                    }
                                } else {
                                    if (which == 0 && !isOccupied) {
                                        takeSeat(seatIndex, seatKey, userName, userImageUriStr);
                                    } else if (which == 1 || (isOccupied && which == 1)) {
                                        databaseReference.child("seats").child(seatKey).removeValue();
                                        Toast.makeText(VoiceRoomActivity.this, "Left seat", Toast.LENGTH_SHORT).show();
                                    }
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

    private void takeSeat(int seatIndex, String seatKey, String userName, String userImageUriStr) {
        for (int j = 0; j < 10; j++) {
            final int oldIndex = j;
            databaseReference.child("seats").child("seat_" + oldIndex).child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String existingName = snapshot.getValue(String.class);
                    if (existingName != null && existingName.equals(userName)) {
                        databaseReference.child("seats").child("seat_" + oldIndex).removeValue();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        final String[] imageBase64 = {"v"};
        if (!userImageUriStr.isEmpty()) {
            try {
                if (userImageUriStr.startsWith("content://") || userImageUriStr.startsWith("file://")) {
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
                } else {
                    imageBase64[0] = userImageUriStr;
                }
            } catch (Exception e) {
                imageBase64[0] = "";
            }
        }

        databaseReference.child("seats").child(seatKey).child("userName").setValue(userName);
        databaseReference.child("seats").child(seatKey).child("userImage").setValue(imageBase64[0]);
        Toast.makeText(VoiceRoomActivity.this, "Joined seat successfully!", Toast.LENGTH_SHORT).show();
    }
}
