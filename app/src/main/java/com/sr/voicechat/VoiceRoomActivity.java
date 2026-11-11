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
    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText dialogInputName, dialogInputBio;
    private long roomJoinTimestamp = 0;
    private List<String> roomAdminList = new ArrayList<>();
    private String rankTab = "Daily";
    private String rankType = "Wealth";
    private List<RankItem> rankCache = new ArrayList<>();
    private String selectedGiftTargetUid = "";
    private String selectedGiftTargetName = "All";
    private int giftQuantity = 1;
    private String giftCategory = "Gift";
    private long myWalletCoins = 0L;
    private TextView walletTextInGift = null;
    private static final double EXCHANGE_RATE = 0.30;
    private String myDisplayName = "User";
    private String myCountry = "";
    private String myGender = "Male";
    private String myLanguage = "English";

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
        userDatabaseRef.child(currentUserId).child("name").setValue(newName);
        databaseReference.child("online_users").child(currentUserId).setValue(newName);
        final String finalName = newName;
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            databaseReference.child("seats").child("seat_" + idx).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot s) {
                    String uid = s.child("userId").getValue(String.class);
                    if (uid != null && uid.equals(currentUserId)) {
                        databaseReference.child("seats").child("seat_" + idx).child("userName").setValue(finalName);
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
    }

    private void showEditMyProfileDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Edit My Profile");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        EditText inputName = new EditText(this);
        inputName.setHint("Enter your name...");
        inputName.setText(getMyDisplayName());
        inputName.setTextColor(Color.BLACK);
        inputName.setBackgroundColor(Color.WHITE);
        inputName.setPadding(25, 20, 25, 20);
        layout.addView(inputName);

        TextView gLabel = new TextView(this);
        gLabel.setText("Gender:");
        gLabel.setPadding(0, 20, 0, 8);
        layout.addView(gLabel);

        android.widget.Spinner genderSpin = new android.widget.Spinner(this);
        String[] genders = {"Male", "Female"};
        genderSpin.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders));
        if (myGender != null && myGender.toLowerCase().contains("female")) genderSpin.setSelection(1);
        layout.addView(genderSpin);

        TextView cLabel = new TextView(this);
        cLabel.setText("Country:");
        cLabel.setPadding(0, 20, 0, 8);
        layout.addView(cLabel);

        android.widget.Spinner countrySpin = new android.widget.Spinner(this);
        String[] countries = {"Bangladesh","India","Pakistan","Nepal","Sri Lanka","USA","UK","Canada","Australia","Saudi Arabia","UAE","Qatar","Kuwait","Oman","Malaysia","Indonesia","Philippines","Germany","France"};
        countrySpin.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries));
        int sel = 0;
        for (int i = 0; i < countries.length; i++) {
            if (countries[i].equalsIgnoreCase(myCountry)) {
                sel = i;
                break;
            }
        }
        countrySpin.setSelection(sel);
        layout.addView(countrySpin);

        EditText inputBio = new EditText(this);
        inputBio.setHint("Enter Bio...");
        inputBio.setTextColor(Color.BLACK);
        inputBio.setBackgroundColor(Color.WHITE);
        inputBio.setPadding(25, 20, 25, 20);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.topMargin = 20;
        inputBio.setLayoutParams(bp);
        layout.addView(inputBio);

        b.setView(layout);
        b.setPositiveButton("Save", (d, w) -> {
            String nn = inputName.getText().toString().trim();
            String newGender = genders[genderSpin.getSelectedItemPosition()];
            String newCountry = countries[countrySpin.getSelectedItemPosition()];
            String newBio = inputBio.getText().toString().trim();
            if (!nn.isEmpty()) saveMyNameGlobal(nn);
            myGender = newGender;
            myCountry = newCountry;
            prefs.edit().putString("USER_COUNTRY", newCountry).apply();
            prefs.edit().putString("USER_GENDER", newGender).apply();
            userDatabaseRef.child(currentUserId).child("gender").setValue(newGender);
            userDatabaseRef.child(currentUserId).child("country").setValue(newCountry);
            userDatabaseRef.child(currentUserId).child("bio").setValue(newBio);
            Toast.makeText(this, "Saved: " + newCountry + " " + getCountryFlag(newCountry), Toast.LENGTH_SHORT).show();
        });
        b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
        b.show();
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
        setupThemeBackgroundImage();

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
        if (btnInRoomRanking != null) btnInRoomRanking.setOnClickListener(v -> showPremiumRankingDialog());

        View.OnClickListener headerClick = v -> showPremiumRoomInfoAndMembersDialog();
        if (roomTitleText != null) roomTitleText.setOnClickListener(headerClick);
        if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(headerClick);
        if (roomInfoText != null) roomInfoText.setOnClickListener(headerClick);
        if (btnOnlineList != null) btnOnlineList.setOnClickListener(v -> showScrollableOnlineMembersDialog());

        chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        if (chatListView != null) chatListView.setAdapter(chatAdapter);

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
        btnGameBox = findViewById(R.id.btnGameBox);
        if (btnGameBox != null) btnGameBox.setOnClickListener(v -> showRewardEventDialog());
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

    private void leaveRoomAndFinish() {
        databaseReference.child("online_users").child(currentUserId).removeValue();
        leaveRoomKeepHome();
        finishAffinity();
    }

    private void listenToMyWallet() {
        if (currentUserId.isEmpty()) return;
        userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                Long coins = s.child("coins").getValue(Long.class);
                Long avail = s.child("availableBalance").getValue(Long.class);
                Long bal = s.child("balance").getValue(Long.class);
                Long oldW = s.child("walletCoins").getValue(Long.class);
                long w = 0L;
                if (coins != null) w = coins;
                else if (avail != null) w = avail;
                else if (bal != null) w = bal;
                else if (oldW != null) w = oldW;
                myWalletCoins = w;
                if (walletTextInGift != null) walletTextInGift.setText("Coins: " + formatCoins(w));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private String formatCoins(long v) { return String.format("%,d", v); }

    private void showQuantityPopup(Button btnQtyPopup, GiftModel selectedGift) {
        AlertDialog.Builder qb = new AlertDialog.Builder(this);
        qb.setTitle("Select Quantity (1 to 10)");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 20);

        TextView label = new TextView(this);
        label.setText("Select 1, 3, 5, 10 or type below");
        label.setTextSize(13);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 0, 0, 15);
        layout.addView(label);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        int[] presets = {1, 3, 5, 10};
        for (int q : presets) {
            Button b = new Button(this);
            b.setText("x" + q);
            b.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(6, 0, 6, 0);
            b.setLayoutParams(lp);
            if (q == giftQuantity) {
                b.setBackgroundColor(Color.parseColor("#FFD700"));
                b.setTextColor(Color.BLACK);
            } else {
                b.setBackgroundColor(Color.parseColor("#2A1B4E"));
                b.setTextColor(Color.WHITE);
            }
            row.addView(b);
        }
        layout.addView(row);
        qb.setView(layout);
        AlertDialog dialog = qb.create();
        dialog.show();
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof Button) {
                Button bb = (Button) child;
                bb.setOnClickListener(v -> {
                    String t = bb.getText().toString().replace("x", "");
                    try {
                        giftQuantity = Integer.parseInt(t);
                    } catch (Exception ignored) {}
                    btnQtyPopup.setText("x" + giftQuantity);
                    dialog.dismiss();
                });
            }
        }
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

        TextView targetInfo = new TextView(this);
        targetInfo.setText("To: " + selectedGiftTargetName + " - Tap a seat user below");
        targetInfo.setTextColor(Color.WHITE);
        targetInfo.setTextSize(13);
        targetInfo.setGravity(Gravity.CENTER);
        targetInfo.setPadding(0, 0, 0, 10);
        root.addView(targetInfo);

        HorizontalScrollView seatScroll = new HorizontalScrollView(this);
        seatScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180));
        LinearLayout seatRow = new LinearLayout(this);
        seatRow.setOrientation(LinearLayout.HORIZONTAL);
        seatRow.setGravity(Gravity.CENTER_VERTICAL);
        seatRow.addView(makeSeatAvatarView("All", "All", null, targetInfo, true));
        
        databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                for (int i = 0; i < 10; i++) {
                    String k = "seat_" + i;
                    if (!snap.hasChild(k)) continue;
                    String n = snap.child(k).child("userName").getValue(String.class);
                    String uid = snap.child(k).child("userId").getValue(String.class);
                    String img = snap.child(k).child("userImage").getValue(String.class);
                    if (n == null || n.isEmpty() || uid == null) continue;
                    seatRow.addView(makeSeatAvatarView(n, uid, img, targetInfo, false));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        seatScroll.addView(seatRow);
        root.addView(seatScroll);

        RecyclerView rv = new RecyclerView(this);
        rv.setLayoutManager(new GridLayoutManager(this, 4));
        rv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 650));
        final GiftModel[] holder = {getGiftsForCategory("Gift").get(0)};
        List<GiftModel> list = getGiftsForCategory("Gift");
        GiftAdapter ad = new GiftAdapter(list, gift -> { holder[0] = gift; });
        rv.setAdapter(ad);
        root.addView(rv);

        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setPadding(0, 15, 0, 5);

        walletTextInGift = new TextView(this);
        walletTextInGift.setText("Coins: " + formatCoins(myWalletCoins));
        walletTextInGift.setTextColor(Color.parseColor("#00E676"));
        walletTextInGift.setTextSize(13);
        walletTextInGift.setTypeface(null, android.graphics.Typeface.BOLD);
        walletTextInGift.setPadding(14, 12, 14, 12);
        GradientDrawable wBg = new GradientDrawable();
        wBg.setColor(Color.parseColor("#2A1B4E"));
        wBg.setCornerRadius(25);
        walletTextInGift.setBackground(wBg);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        walletTextInGift.setLayoutParams(wLp);
        bottomBar.addView(walletTextInGift);

        Button btnSend = new Button(this);
        btnSend.setText("SEND");
        btnSend.setBackgroundColor(Color.parseColor("#FF4081"));
        btnSend.setTextColor(Color.WHITE);
        btnSend.setTypeface(null, android.graphics.Typeface.BOLD);
        btnSend.setTextSize(13);
        LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(0, 110, 0.9f);
        sendLp.setMargins(8, 0, 8, 0);
        btnSend.setLayoutParams(sendLp);
        bottomBar.addView(btnSend);

        Button btnQtyBottom = new Button(this);
        btnQtyBottom.setText("x" + giftQuantity);
        btnQtyBottom.setBackgroundColor(Color.parseColor("#FFD700"));
        btnQtyBottom.setTextColor(Color.BLACK);
        btnQtyBottom.setTypeface(null, android.graphics.Typeface.BOLD);
        btnQtyBottom.setTextSize(13);
        btnQtyBottom.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(0, 110, 0.5f);
        btnQtyBottom.setLayoutParams(qLp);
        btnQtyBottom.setOnClickListener(v -> showQuantityPopup(btnQtyBottom, holder[0]));
        bottomBar.addView(btnQtyBottom);
        root.addView(bottomBar);

        btnSend.setOnClickListener(v -> {
            if (holder[0] == null) return;
            String priceStr = holder[0].getGiftPrice().replaceAll("[^0-9]", "");
            long onePrice = 20L;
            if (!priceStr.isEmpty()) onePrice = Long.parseLong(priceStr);
            long totalCost = onePrice * giftQuantity;
            long receiverGets = (long) (totalCost * EXCHANGE_RATE);
            if (receiverGets <= 0 && totalCost > 0) receiverGets = 1;
            if (myWalletCoins < totalCost) {
                Toast.makeText(this, "Low balance! Need " + formatCoins(totalCost) + ", Have " + formatCoins(myWalletCoins), Toast.LENGTH_LONG).show();
                return;
            }
            
            userDatabaseRef.child(currentUserId).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot s) {
                    Long cur = s.getValue(Long.class);
                    long now = (cur != null ? cur : 0L) - totalCost;
                    if (now < 0) now = 0;
                    userDatabaseRef.child(currentUserId).child("coins").setValue(now);
                    userDatabaseRef.child(currentUserId).child("walletCoins").setValue(now);
                    myWalletCoins = now;
                    if (walletTextInGift != null) walletTextInGift.setText("Coins: " + formatCoins(now));
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });

            if (selectedGiftTargetUid.equals("") || selectedGiftTargetUid.equals("All")) {
                databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        for (int i = 0; i < 10; i++) {
                            String k = "seat_" + i;
                            if (!snap.hasChild(k)) continue;
                            Long cur = snap.child(k).child("giftTotal").getValue(Long.class);
                            long nv = (cur != null ? cur : 0L) + receiverGets;
                            databaseReference.child("seats").child(k).child("giftTotal").setValue(nv);
                            final String sid = snap.child(k).child("userId").getValue(String.class);
                            if (sid != null && !sid.isEmpty()) {
                                userDatabaseRef.child(sid).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                                        Long cc = s2.getValue(Long.class);
                                        long nnv = (cc != null ? cc : 0L) + receiverGets;
                                        userDatabaseRef.child(sid).child("coins").setValue(nnv);
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                                });
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            } else {
                databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        for (int i = 0; i < 10; i++) {
                            String k = "seat_" + i;
                            if (!snap.hasChild(k)) continue;
                            String sid = snap.child(k).child("userId").getValue(String.class);
                            if (selectedGiftTargetUid.equals(sid)) {
                                Long cur = snap.child(k).child("giftTotal").getValue(Long.class);
                                long nv = (cur != null ? cur : 0L) + receiverGets;
                                databaseReference.child("seats").child(k).child("giftTotal").setValue(nv);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
                userDatabaseRef.child(selectedGiftTargetUid).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        Long cc = s2.getValue(Long.class);
                        long nnv = (cc != null ? cc : 0L) + receiverGets;
                        userDatabaseRef.child(selectedGiftTargetUid).child("coins").setValue(nnv);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            }

            triggerFlyingGiftAnimation(holder[0].getGiftName() + " x" + giftQuantity);
            String msgId = messagesRef.push().getKey();
            if (msgId != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("senderName", getCorrectBadgeIcon(currentUserId) + getMyDisplayName());
                m.put("message", "sent gift [" + holder[0].getGiftName() + " x" + giftQuantity + "] to " + selectedGiftTargetName);
                m.put("timestamp", System.currentTimeMillis());
                messagesRef.child(msgId).setValue(m);
            }
            Toast.makeText(this, "Gift Sent Successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.67));
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private View makeSeatAvatarView(String name, String uid, String imgBase64, TextView targetInfo, boolean isAll) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        col.setPadding(12, 8, 12, 8);
        ImageView av = new ImageView(this);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(110, 110);
        av.setLayoutParams(ap);
        av.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (imgBase64 != null && !imgBase64.isEmpty()) {
            try {
                byte[] b = android.util.Base64.decode(imgBase64, android.util.Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                if (bmp != null) {
                    RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                    d.setCircular(true);
                    av.setImageDrawable(d);
                } else av.setImageResource(android.R.drawable.ic_menu_my_calendar);
            } catch (Exception e) {
                av.setImageResource(android.R.drawable.ic_menu_my_calendar);
            }
        } else av.setImageResource(android.R.drawable.ic_menu_my_calendar);
        col.addView(av);
        TextView nt = new TextView(this);
        String sn = name;
        if (name.length() > 8) sn = name.substring(0, 8);
        nt.setText(sn);
        nt.setTextColor(Color.WHITE);
        nt.setTextSize(11);
        nt.setGravity(Gravity.CENTER);
        col.addView(nt);
        col.setOnClickListener(v -> {
            if (isAll) selectedGiftTargetUid = "";
            else selectedGiftTargetUid = uid;
            selectedGiftTargetName = name;
            if (targetInfo != null) targetInfo.setText("To: " + name + " - Selected");
            Toast.makeText(this, "Selected: " + name, Toast.LENGTH_SHORT).show();
        });
        return col;
    }

    private List<GiftModel> getGiftsForCategory(String cat) {
        List<GiftModel> list = new ArrayList<>();
        list.add(new GiftModel("g1", "Rose", "20", R.drawable.ic_gift_flower, "Gift"));
        list.add(new GiftModel("g2", "Kiss", "66", R.drawable.ic_gift_ring, "Gift"));
        list.add(new GiftModel("g3", "Love", "100", R.drawable.ic_gift_diamond, "Gift"));
        list.add(new GiftModel("g4", "Cake", "199", R.drawable.ic_gift_car, "Gift"));
        list.add(new GiftModel("g5", "Crown", "500", R.drawable.ic_gift_crown, "Gift"));
        list.add(new GiftModel("g6", "Diamond", "9999", R.drawable.ic_gift_castle, "Gift"));
        return list;
    }

    private void setupThemeBackgroundImage() {
        try {
            ViewGroup root = findViewById(android.R.id.content);
            if (root == null) return;
            themeBackgroundImg = new ImageView(this);
            themeBackgroundImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            themeBackgroundImg.setAlpha(0.85f);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            root.addView(themeBackgroundImg, 0);
        } catch (Exception ignored) {}
    }

    private void applyRoomTheme(String c1, String c2, String fullText, String imageName) {
        try {
            currentThemeName = fullText;
            int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            if (themeBackgroundImg != null) {
                if (resId != 0) {
                    themeBackgroundImg.setImageResource(resId);
                    themeBackgroundImg.setVisibility(View.VISIBLE);
                } else {
                    themeBackgroundImg.setVisibility(View.GONE);
                }
            }
        } catch (Exception ignored) {}
    }

    private void listenToRoomTheme() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String n = s.child("roomTheme").getValue(String.class);
                String c1 = s.child("roomThemeColor1").getValue(String.class);
                String c2 = s.child("roomThemeColor2").getValue(String.class);
                String img = s.child("roomThemeImage").getValue(String.class);
                if (n != null && !n.isEmpty() && c1 != null && c2 != null) {
                    applyRoomTheme(c1, c2, n, img != null ? img : "theme_night");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showRoomThemeDialog() { Toast.makeText(this, "Theme - Coming Soon", Toast.LENGTH_SHORT).show(); }

    private void showIconToolsDialog(boolean isOwner) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 35, 35, 45);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("Basic Tools");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 15);
        root.addView(title);

        Button editNameBtn = new Button(this);
        editNameBtn.setText("Edit My Profile");
        editNameBtn.setBackgroundColor(Color.parseColor("#FFD700"));
        editNameBtn.setTextColor(Color.BLACK);
        editNameBtn.setOnClickListener(v -> {
            dialog.dismiss();
            showEditMyProfileDialog();
        });
        root.addView(editNameBtn);

        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1000);
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private void showChatDialog() {
        final Dialog chatDialog = new Dialog(this, R.style.Theme_Design_BottomSheetDialog);
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
                if (!message.isEmpty()) {
                    String msgId = messagesRef.push().getKey();
                    if (msgId != null) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("senderName", getCorrectBadgeIcon(currentUserId) + getMyDisplayName());
                        m.put("senderId", currentUserId);
                        m.put("message", message);
                        m.put("timestamp", System.currentTimeMillis());
                        messagesRef.child(msgId).setValue(m);
                    }
                    inputMsg.setText("");
                    chatDialog.dismiss();
                }
            });
        }
        chatDialog.show();
    }

    private void showPremiumRankingDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 30, 30, 30);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("Room Contribution Ranking");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 15);
        root.addView(title);

        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.75));
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private void showRewardEventDialog() {
        Toast.makeText(this, "Reward Event", Toast.LENGTH_SHORT).show();
    }

    private void triggerFlyingGiftAnimation(String giftName) {
        try {
            ViewGroup rv = findViewById(android.R.id.content);
            if (rv == null) return;
            final TextView fv = new TextView(this);
            fv.setText("Gift " + giftName);
            fv.setTextColor(Color.parseColor("#FFD700"));
            fv.setTextSize(20);
            fv.setTypeface(null, android.graphics.Typeface.BOLD);
            fv.setGravity(Gravity.CENTER);
            fv.setBackgroundColor(Color.parseColor("#88000000"));
            fv.setPadding(30, 20, 30, 20);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.setMargins(0, 0, 0, 300);
            rv.addView(fv, params);
            TranslateAnimation anim = new TranslateAnimation(Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -1000f);
            anim.setDuration(2000);
            anim.setFillAfter(true);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationEnd(Animation a) { rv.removeView(fv); }
                @Override public void onAnimationRepeat(Animation a) {}
            });
            fv.startAnimation(anim);
        } catch (Exception ignored) {}
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
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot snap, @Nullable String prev) {
                String sn = snap.child("senderName").getValue(String.class);
                String msg = snap.child("message").getValue(String.class);
                Long t = snap.child("timestamp").getValue(Long.class);
                if (t != null && t < roomJoinTimestamp) return;
                if (sn != null && msg != null) {
                    String full = sn + ": " + msg;
                    if (!chatMessages.contains(full)) {
                        chatMessages.add(full);
                        chatAdapter.notifyDataSetChanged();
                        if (chatListView != null) chatListView.setSelection(chatMessages.size() - 1);
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {
                chatMessages.clear();
                chatAdapter.notifyDataSetChanged();
            }
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showScrollableOnlineMembersDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 35, 35, 35);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("Online Members");
        title.setTextColor(Color.parseColor("#00E676"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 15);
        layout.addView(title);

        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1100));
        sv.setFillViewport(true);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        sv.addView(container);
        layout.addView(sv);

        databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                container.removeAllViews();
                int serial = 1;
                for (DataSnapshot ds : snap.getChildren()) {
                    String mn = ds.getValue(String.class);
                    String mu = ds.getKey();
                    if (mn == null) continue;
                    final int sNum = serial;
                    final String fName = mn;
                    final String fUid = mu;
                    userDatabaseRef.child(mu).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot us) {
                            String img = us.child("profileImage").getValue(String.class);
                            String realName = us.child("name").getValue(String.class);
                            String showName = (realName != null && !realName.isEmpty()) ? realName : fName;
                            String gender = us.child("gender").getValue(String.class);
                            if (gender == null) gender = "Male";
                            String country = us.child("country").getValue(String.class);
                            if (country == null) country = "";

                            LinearLayout row = new LinearLayout(VoiceRoomActivity.this);
                            row.setOrientation(LinearLayout.HORIZONTAL);
                            row.setPadding(18, 18, 18, 18);
                            row.setGravity(Gravity.CENTER_VERTICAL);
                            GradientDrawable rBg = new GradientDrawable();
                            rBg.setColor(Color.parseColor("#2A1B4E"));
                            rBg.setCornerRadius(16);
                            row.setBackground(rBg);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 0, 10);
                            row.setLayoutParams(lp);

                            ImageView av = new ImageView(VoiceRoomActivity.this);
                            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(110, 110);
                            ap.setMargins(0, 0, 15, 0);
                            av.setLayoutParams(ap);
                            av.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            if (img != null && !img.isEmpty()) {
                                try {
                                    byte[] bb = android.util.Base64.decode(img, android.util.Base64.DEFAULT);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bb, 0, bb.length);
                                    if (bmp != null) {
                                        RoundedBitmapDrawable rd = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                                        rd.setCircular(true);
                                        av.setImageDrawable(rd);
                                    } else av.setImageResource(android.R.drawable.ic_menu_my_calendar);
                                } catch (Exception e) {
                                    av.setImageResource(android.R.drawable.ic_menu_my_calendar);
                                }
                            } else av.setImageResource(android.R.drawable.ic_menu_my_calendar);
                            row.addView(av);

                            LinearLayout stack = new LinearLayout(VoiceRoomActivity.this);
                            stack.setOrientation(LinearLayout.VERTICAL);
                            stack.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                            
                            String badge = "Member ";
                            if (fUid != null && fUid.equals(roomOwnerId)) badge = "Owner ";
                            else if (roomAdminList.contains(fUid)) badge = "Admin ";

                            TextView nt = new TextView(VoiceRoomActivity.this);
                            nt.setText(sNum + ". " + badge + showName + " (ID: " + fUid + ")");
                            nt.setTextColor(Color.WHITE);
                            nt.setTextSize(14);
                            nt.setTypeface(null, android.graphics.Typeface.BOLD);
                            stack.addView(nt);
                            stack.addView(makeGenderFlagRow(gender, country));
                            row.addView(stack);

                            row.setOnClickListener(v -> {
                                dialog.dismiss();
                                fetchUserDataAndShowProfile(showName, fUid, img);
                            });
                            container.addView(row);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                    serial++;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        dialog.setContentView(layout);
        Window w = dialog.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.75));
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private void syncLatestUserProfile() {
        if (currentUserId.isEmpty()) return;
        userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) {
                    String dbName = s.child("name").getValue(String.class);
                    String dbImg = s.child("profileImage").getValue(String.class);
                    String dbCountry = s.child("country").getValue(String.class);
                    String dbGender = s.child("gender").getValue(String.class);
                    if (dbName != null && !dbName.isEmpty()) {
                        myDisplayName = dbName;
                        prefs.edit().putString("USER_NAME", dbName).apply();
                    }
                    if (dbImg != null) {
                        prefs.edit().putString("USER_IMAGE", dbImg).apply();
                        prefs.edit().putString("USER_PROFILE_IMAGE", dbImg).apply();
                    }
                    if (dbCountry != null && !dbCountry.isEmpty()) myCountry = dbCountry;
                    if (dbGender != null && !dbGender.isEmpty()) myGender = dbGender;
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void listenToRoomDetails() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String rn = s.child("roomName").getValue(String.class);
                String rb = s.child("roomBio").getValue(String.class);
                String oi = s.child("roomOwnerImage").getValue(String.class);
                if (rn != null && !rn.isEmpty() && roomTitleText != null) roomTitleText.setText(rn);
                if (roomInfoText != null) roomInfoText.setText("ID: " + roomOwnerId);
                if (roomNoticeBarText != null) {
                    String bio = (rb != null && !rb.isEmpty()) ? rb : "Welcome to my voice room!";
                    roomNoticeBarText.setText("Notice: " + bio);
                }
                if (roomHeaderProfileImg != null && oi != null && !oi.isEmpty()) {
                    try {
                        byte[] d = android.util.Base64.decode(oi, android.util.Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(d, 0, d.length);
                        if (bmp != null) {
                            RoundedBitmapDrawable rd = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                            rd.setCircular(true);
                            roomHeaderProfileImg.setImageDrawable(rd);
                        }
                    } catch (Exception ignored) {}
                }
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

    private void showPremiumRoomInfoAndMembersDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 35, 35, 35);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
        root.setBackground(bg);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(25, 25, 25, 25);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#2A1B4E"));
        cardBg.setCornerRadius(20);
        card.setBackground(cardBg);

        TextView ot = new TextView(this);
        ot.setText("Room: " + (roomTitleText != null ? roomTitleText.getText().toString() : "SR Voice Room"));
        ot.setTextColor(Color.parseColor("#FFD700"));
        ot.setTextSize(17);
        ot.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(ot);

        TextView owner = new TextView(this);
        owner.setText("RoomOwner: " + getMyDisplayName() + " (ID:" + roomOwnerId + ")");
        owner.setTextColor(Color.WHITE);
        owner.setTextSize(14);
        card.addView(owner);
        card.addView(makeGenderFlagRow(myGender, myCountry));
        root.addView(card);

        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800);
            w.setGravity(Gravity.BOTTOM);
        }
        dialog.show();
    }

    private String getCountryFlag(String country) {
        if (country == null) return "";
        String c = country.trim().toLowerCase();
        if (c.contains("bangladesh") || c.equals("bd")) return "\uD83C\uDDE7\uD83C\uDDE9";
        if (c.contains("india") || c.equals("in")) return "\uD83C\uDDEE\uD83C\uDDF3";
        if (c.contains("pakistan") || c.equals("pk")) return "\uD83C\uDDF5\uD83C\uDDF0";
        if (c.contains("nepal") || c.equals("np")) return "\uD83C\uDDF3\uD83C\uDDF5";
        if (c.contains("sri lanka") || c.equals("lk")) return "\uD83C\uDDF1\uD83C\uDDF0";
        if (c.contains("usa") || c.contains("united states") || c.equals("us")) return "\uD83C\uDDFA\uD83C\uDDF8";
        if (c.contains("uk") || c.contains("united kingdom") || c.equals("gb")) return "\uD83C\uDDEC\uD83C\uDDE7";
        if (c.contains("canada") || c.equals("ca")) return "\uD83C\uDDE8\uD83C\uDDE6";
        if (c.contains("australia") || c.equals("au")) return "\uD83C\uDDE6\uD83C\uDDFA";
        if (c.contains("saudi") || c.equals("sa")) return "\uD83C\uDDF8\uD83C\uDDE6";
        if (c.contains("uae") || c.contains("emirates") || c.equals("ae")) return "\uD83C\uDDE6\uD83C\uDDEA";
        if (c.contains("qatar") || c.equals("qa")) return "\uD83C\uDDF6\uD83C\uDDE6";
        if (c.contains("kuwait") || c.equals("kw")) return "\uD83C\uDDF0\uD83C\uDDFC";
        if (c.contains("oman") || c.equals("om")) return "\uD83C\uDDF4\uD83C\uDDF2";
        if (c.contains("malaysia") || c.equals("my")) return "\uD83C\uDDF2\uD83C\uDDFE";
        if (c.contains("indonesia") || c.equals("id")) return "\uD83C\uDDEE\uD83C\uDDE9";
        if (c.contains("philippines") || c.equals("ph")) return "\uD83C\uDDF5\uD83C\uDDED";
        if (c.contains("germany") || c.equals("de")) return "\uD83C\uDDE9\uD83C\uDDEA";
        if (c.contains("france") || c.equals("fr")) return "\uD83C\uDDEB\uD83C\uDDF7";
        return "\uD83C\uDF0D";
    }

    private LinearLayout makeGenderFlagRow(String gender, String country) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView genderTv = new TextView(this);
        boolean isFemale = gender != null && (gender.toLowerCase().contains("female") || gender.toLowerCase().equals("f"));
        if (isFemale) {
            genderTv.setText("\u2640");
            genderTv.setTextColor(Color.parseColor("#FF4081"));
        } else {
            genderTv.setText("\u2642");
            genderTv.setTextColor(Color.parseColor("#2196F3"));
        }
        genderTv.setTextSize(22);
        genderTv.setTypeface(null, android.graphics.Typeface.BOLD);
        genderTv.setPadding(0, 0, 12, 0);
        row.addView(genderTv);
        TextView flagTv = new TextView(this);
        flagTv.setText(getCountryFlag(country));
        flagTv.setTextSize(22);
        row.addView(flagTv);
        return row;
    }

    private void fetchUserDataAndShowProfile(String fallbackName, String uid, String fallbackImg) {
        userDatabaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String name = s.child("name").getValue(String.class);
                if (name == null || name.isEmpty()) name = fallbackName;
                String country = s.child("country").getValue(String.class);
                if (country == null) country = "";
                String gender = s.child("gender").getValue(String.class);
                if (gender == null) gender = "Male";
                String bio = s.child("bio").getValue(String.class);
                if (bio == null) bio = "";
                String img = s.child("profileImage").getValue(String.class);
                if (img == null || img.isEmpty()) img = fallbackImg;

                BottomSheetDialog dialog = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
                LinearLayout root = new LinearLayout(VoiceRoomActivity.this);
                root.setOrientation(LinearLayout.VERTICAL);
                root.setPadding(35, 35, 35, 35);
                root.setGravity(Gravity.CENTER_HORIZONTAL);
                GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
                bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
                root.setBackground(bg);

                ImageView av = new ImageView(VoiceRoomActivity.this);
                LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(220, 220);
                ap.setMargins(0, 10, 0, 15);
                av.setLayoutParams(ap);
                av.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (img != null && !img.isEmpty()) {
                    try {
                        byte[] b = android.util.Base64.decode(img, android.util.Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                        if (bmp != null) {
                            RoundedBitmapDrawable rd = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                            rd.setCircular(true);
                            av.setImageDrawable(rd);
                        } else av.setImageResource(android.R.drawable.ic_menu_my_calendar);
                    } catch (Exception e) {
                        av.setImageResource(android.R.drawable.ic_menu_my_calendar);
                    }
                } else av.setImageResource(android.R.drawable.ic_menu_my_calendar);
                root.addView(av);

                TextView nameTv = new TextView(VoiceRoomActivity.this);
                nameTv.setText(name);
                nameTv.setTextColor(Color.WHITE);
                nameTv.setTextSize(20);
                nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
                nameTv.setGravity(Gravity.CENTER);
                root.addView(nameTv);

                TextView idTv = new TextView(VoiceRoomActivity.this);
                idTv.setText("ID: " + uid);
                idTv.setTextColor(Color.parseColor("#00E676"));
                idTv.setTextSize(13);
                idTv.setGravity(Gravity.CENTER);
                idTv.setPadding(0, 6, 0, 12);
                root.addView(idTv);
                root.addView(makeGenderFlagRow(gender, country));

                if (!bio.isEmpty()) {
                    TextView bioTv = new TextView(VoiceRoomActivity.this);
                    bioTv.setText(bio);
                    bioTv.setTextColor(Color.WHITE);
                    bioTv.setTextSize(14);
                    bioTv.setGravity(Gravity.CENTER);
                    bioTv.setPadding(0, 15, 0, 0);
                    root.addView(bioTv);
                }

                if (uid.equals(currentUserId)) {
                    Button editBtn = new Button(VoiceRoomActivity.this);
                    editBtn.setText("Edit My Profile");
                    editBtn.setBackgroundColor(Color.parseColor("#FFD700"));
                    editBtn.setTextColor(Color.BLACK);
                    editBtn.setOnClickListener(v -> {
                        dialog.dismiss();
                        showEditMyProfileDialog();
                    });
                    root.addView(editBtn);
                }

                dialog.setContentView(root);
                Window w = dialog.getWindow();
                if (w != null) {
                    w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    w.setGravity(Gravity.BOTTOM);
                }
                dialog.show();
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
                        String uname = s.child("userName").getValue(String.class);
                        String uimg = s.child("userImage").getValue(String.class);
                        if (uid != null && !uid.isEmpty()) {
                            if (uid.equals(currentUserId)) {
                                databaseReference.child("seats").child("seat_" + idx).removeValue();
                                Toast.makeText(VoiceRoomActivity.this, "Left Seat", Toast.LENGTH_SHORT).show();
                            } else {
                                fetchUserDataAndShowProfile(uname != null ? uname : "User", uid, uimg);
                            }
                        } else {
                            Map<String, Object> seatData = new HashMap<>();
                            seatData.put("userId", currentUserId);
                            seatData.put("userName", getMyDisplayName());
                            seatData.put("giftTotal", 0L);
                            String myImg = prefs.getString("USER_IMAGE", "");
                            if (myImg != null && !myImg.isEmpty()) seatData.put("userImage", myImg);
                            
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
                            Toast.makeText(VoiceRoomActivity.this, "Seated!", Toast.LENGTH_SHORT).show();
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
                    String uid = s.child("seat_" + i).child("userId").getValue(String.class);
                    String uname = s.child("seat_" + i).child("userName").getValue(String.class);
                    String uimg = s.child("seat_" + i).child("userImage").getValue(String.class);
                    if (seat instanceof ViewGroup) {
                        ViewGroup vg = (ViewGroup) seat;
                        for (int c = 0; c < vg.getChildCount(); c++) {
                            View child = vg.getChildAt(c);
                            if (child instanceof TextView && uname != null && !uname.isEmpty()) {
                                ((TextView) child).setText(uname);
                            }
                            if (child instanceof ImageView && uimg != null && !uimg.isEmpty()) {
                                try {
                                    byte[] bb = android.util.Base64.decode(uimg, android.util.Base64.DEFAULT);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bb, 0, bb.length);
                                    if (bmp != null) {
                                        RoundedBitmapDrawable rd = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                                        rd.setCircular(true);
                                        ((ImageView) child).setImageDrawable(rd);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                        if (uid == null || uid.isEmpty()) {
                            for (int c = 0; c < vg.getChildCount(); c++) {
                                if (vg.getChildAt(c) instanceof TextView) ((TextView) vg.getChildAt(c)).setText("Seat " + (i + 1));
                                if (vg.getChildAt(c) instanceof ImageView) ((ImageView) vg.getChildAt(c)).setImageResource(android.R.drawable.ic_menu_my_calendar);
                            }
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    public static class RankItem {
        public String name;
        public long coins;
        public String image;
        public String uid;

        public RankItem(String name, long coins, String image, String uid) {
            this.name = name;
            this.coins = coins;
            this.image = image;
            this.uid = uid;
        }
    }
}
