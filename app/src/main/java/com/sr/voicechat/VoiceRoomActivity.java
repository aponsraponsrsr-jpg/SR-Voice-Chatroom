package com.sr.voicechat;

import android.app.Dialog;
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
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {
    private ImageButton btnPower;
    private Button btnMicToggle, btnChatOption, btnGiftBox, btnGameBox;
    private TextView roomTitleText, roomInfoText, textOnlineCount, roomNoticeBarText;
    private ImageView btnOnlineList, roomHeaderProfileImg, btnRoomTools, themeBackgroundImg;
    private LinearLayout btnInRoomRanking;
    private boolean isMuted = false;
    private ListView chatListView;
    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private SharedPreferences prefs;
    private DatabaseReference databaseReference, roomMetaRef, userDatabaseRef, messagesRef, adminsRef;
    private FirebaseAuth mAuth;
    private String roomOwnerId = "", currentUserId = "";
    private String currentThemeName = "Night Purple";
    private long roomJoinTimestamp = 0;
    private List<String> roomAdminList = new ArrayList<>();
    private String selectedGiftTargetUid = "";
    private String selectedGiftTargetName = "All";
    private int giftQuantity = 1;
    private long myWalletCoins = 0L;
    private TextView walletTextInGift = null;
    private static final double EXCHANGE_RATE = 0.30;
    private String myDisplayName = "User";
    private String myCountry = "Bangladesh";
    private String myGender = "Male";

    private String getMyDisplayName() {
        String n = prefs.getString("USER_NAME", "");
        if (n != null && !n.isEmpty()) return n;
        if (myDisplayName != null && !myDisplayName.isEmpty()) return myDisplayName;
        return "User";
    }

    private void saveMyNameGlobal(String inputName) {
        if (inputName == null) return;
        String newName = inputName.trim();
        if (newName.isEmpty()) return;
        myDisplayName = newName;
        prefs.edit().putString("USER_NAME", newName).apply();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            userDatabaseRef.child(currentUserId).child("name").setValue(newName);
            databaseReference.child("online_users").child(currentUserId).setValue(newName);
        }
    }

    private void showEditMyProfileDialog() {
        Intent intent = new Intent(VoiceRoomActivity.this, EditProfileActivity.class);
        startActivity(intent);
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
        roomJoinTimestamp = System.currentTimeMillis();

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

        setupSeatInteractions();
        listenToFirebaseSeats();
        listenToOnlineMembers();
        listenToRealtimeChatMessages();

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
            Intent home = new Intent(VoiceRoomActivity.this, HomeActivity.class);
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
        finishAffinity();
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

    private void showIconToolsDialog(boolean isOwner) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Room Tools");
        b.setMessage("Select an action:");
        b.setPositiveButton("Edit Profile", (d, w) -> {
            startActivity(new Intent(VoiceRoomActivity.this, EditProfileActivity.class));
        });
        b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        b.show();
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

    private String getCorrectBadgeIcon(String uid) {
        if (uid == null || uid.isEmpty()) return "";
        if (uid.equals(roomOwnerId)) return "Owner ";
        if (roomAdminList.contains(uid)) return "Admin ";
        return "";
    }

    private void listenToRealtimeChatMessages() {
        // Chat listener
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
                                databaseReference.child("seats").child("seat_" + idx).removeValue();
                                Toast.makeText(VoiceRoomActivity.this, "Left Seat", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(VoiceRoomActivity.this, "Seat occupied!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Map<String, Object> seatData = new HashMap<>();
                            seatData.put("userId", currentUserId);
                            seatData.put("userName", getMyDisplayName());
                            
                            // আগের সিট থেকে রিমুভ করা
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
}
