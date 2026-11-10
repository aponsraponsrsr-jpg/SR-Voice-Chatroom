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
import java.util.Collections;
import java.util.List;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageButton btnPower;
    private Button btnMicToggle, btnChatOption, btnGiftBox, btnGameBox;
    private TextView roomTitleText, roomInfoText, textOnlineCount, roomNoticeBarText, roomThemeSceneryText;
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
    private String currentThemeName = "🌙 Night Purple - Premium";

    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText dialogInputName, dialogInputBio;
    private long roomJoinTimestamp = 0;
    private List<String> roomAdminList = new ArrayList<>();

    private String rankTab = "Wealth";
    private String rankPeriod = "Day";
    private List<RankItem> rankCache = new ArrayList<>();

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

        btnPower = findViewById(R.id.btnPower);
        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        textOnlineCount = findViewById(R.id.textOnlineCount);
        btnOnlineList = findViewById(R.id.btnOnlineList);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
        btnInRoomRanking = findViewById(R.id.btnInRoomRanking);
        roomNoticeBarText = findViewById(R.id.roomNoticeBarText);
        btnRoomTools = findViewById(R.id.btnRoomTools);
        roomThemeSceneryText = findViewById(R.id.roomThemeSceneryText);

        if (btnPower != null) {
            btnPower.setOnClickListener(v -> {
                CharSequence[] options = {"Minimize", "Exit Room"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("Room Options");
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) moveTaskToBack(true); else leaveRoomAndFinish();
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
                showPremiumGlobalTools(isOwner);
            });
        }

        if (btnInRoomRanking != null) btnInRoomRanking.setOnClickListener(v -> showPremiumRankingDialog());

        View.OnClickListener headerClickListener = v -> showPremiumRoomInfoAndMembersDialog();
        if (roomTitleText != null) roomTitleText.setOnClickListener(headerClickListener);
        if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(headerClickListener);
        if (roomInfoText != null) roomInfoText.setOnClickListener(headerClickListener);
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
        if (btnMicToggle != null) {
            btnMicToggle.setOnClickListener(v -> {
                isMuted = !isMuted;
                btnMicToggle.setText(isMuted ? "🔇 Muted" : "🎙️ Mic");
                Toast.makeText(this, isMuted ? "Mic Muted" : "Mic Unmuted", Toast.LENGTH_SHORT).show();
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
                                java.util.Map<String, Object> msgMap = new java.util.HashMap<>();
                                msgMap.put("senderName", getCorrectBadgeIcon(currentUserId) + userName);
                                msgMap.put("senderId", currentUserId);
                                msgMap.put("message", message);
                                msgMap.put("timestamp", msgTimestamp);
                                messagesRef.child(msgId).setValue(msgMap);
                            }
                            inputMsg.setText(""); chatDialog.dismiss();
                        }
                    });
                }
                chatDialog.show();
            });
        }
        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) btnGiftBox.setOnClickListener(v -> showPremiumGiftPanel());
        btnGameBox = findViewById(R.id.btnGameBox);
        if (btnGameBox != null) {
            btnGameBox.setOnClickListener(v -> {
                CharSequence[] games = {"🎮 Ludo Game", "🎯 Lucky Wheel", "🎲 Dice Battle"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎮 Voice Room Games");
                builder.setItems(games, (dialog, which) -> Toast.makeText(this, "Starting " + games[which] + "...", Toast.LENGTH_SHORT).show());
                builder.show();
            });
        }
    }

    // ================= PREMIUM GLOBAL TOOLS =================
    private void showPremiumGlobalTools(boolean isOwner) {
        BottomSheetDialog toolsSheet = new BottomSheetDialog(VoiceRoomActivity.this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout toolLayout = new LinearLayout(VoiceRoomActivity.this);
        toolLayout.setOrientation(LinearLayout.VERTICAL);
        toolLayout.setPadding(40, 45, 40, 55);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0});
        toolLayout.setBackground(bg);
        TextView title = new TextView(VoiceRoomActivity.this);
        title.setText("🎛️ Room Management Tools");
        title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,8);
        toolLayout.addView(title);
        TextView sub = new TextView(VoiceRoomActivity.this);
        sub.setText("Global • সবার রুমে একই Tools আসবে • Premium");
        sub.setTextColor(Color.parseColor("#00E676")); sub.setTextSize(12); sub.setGravity(Gravity.CENTER); sub.setPadding(0,0,0,25);
        toolLayout.addView(sub);
        ScrollView toolScroll = new ScrollView(VoiceRoomActivity.this);
        toolScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1100));
        toolScroll.setFillViewport(true);
        LinearLayout container = new LinearLayout(VoiceRoomActivity.this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(createToolButton("⚙️ Room Settings & Profile", "Change room name, bio & cover image", view -> {
            toolsSheet.dismiss();
            if (isOwner) showEditRoomDialog(); else Toast.makeText(this, "Only Room Owner can edit room settings!", Toast.LENGTH_SHORT).show();
        }));
        container.addView(createToolButton("🔒 Room Lock Protection", "Set or remove room security password", view -> { toolsSheet.dismiss(); showRoomLockPasswordDialog(); }));
        container.addView(createToolButton("🏆 Level Center (1 to 150)", "View Wallet & Charm Level Badges", view -> { toolsSheet.dismiss(); showFullLevelCenterDialog(); }));
        container.addView(createToolButton("🏆 Premium Ranking", "Wealth / Charm • Day / Week / Month • Live", view -> { toolsSheet.dismiss(); showPremiumRankingDialog(); }));
        container.addView(createToolButton("👥 Members & Admins", "Manage members, make Admin / Kick", view -> { toolsSheet.dismiss(); showPremiumRoomInfoAndMembersDialog(); }));
        container.addView(createToolButton("🎨 Room Theme - Beautiful Scenery", "Couple • Sea • Mountain • Sunset • 10 Premium Scenery", view -> { toolsSheet.dismiss(); showRoomThemeDialog(); }));
        container.addView(createToolButton("🧹 Clean Global Chat", "Clear all live messages from room", view -> {
            messagesRef.removeValue().addOnCompleteListener(task -> { chatMessages.clear(); chatAdapter.notifyDataSetChanged(); Toast.makeText(VoiceRoomActivity.this, "Chat history cleaned globally!", Toast.LENGTH_SHORT).show(); });
            toolsSheet.dismiss();
        }));
        toolScroll.addView(container); toolLayout.addView(toolScroll); toolsSheet.setContentView(toolLayout);
        Window window = toolsSheet.getWindow();
        if (window != null) { window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1300); window.setGravity(Gravity.BOTTOM); }
        toolsSheet.show();
    }

    // ================= 🎨 ROOM THEME - BEAUTIFUL SCENERY - FULL PREMIUM =================
    private void showRoomThemeDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 50);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0});
        root.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("🎨 Room Theme - Beautiful Scenery");
        title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(19); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,8);
        root.addView(title);
        TextView sub = new TextView(this);
        sub.setText("Couple Scene • Sea Scene • যত Beautiful Scenery আছে সব এখানে");
        sub.setTextColor(Color.parseColor("#00E676")); sub.setTextSize(12); sub.setGravity(Gravity.CENTER); sub.setPadding(0,0,0,25);
        root.addView(sub);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1200));
        scroll.setFillViewport(true);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        // Name, Emoji Scenery, Color1, Color2, Description
        String[][] themes = {
            {"💑 Couple Sunset Beach", "🌅 Couple Scene - সূর্য ডুবা সমুদ্রের পাড়ে কাপেল বসে আছে", "#FF6B35", "#2A1B4E"},
            {"💏 Couple Style Night", "🌙 Couple Style - রাতের আলোয় কাপেল, Romantic Night", "#4A0E2E", "#FF4081"},
            {"💖 Couple Love Garden", "🌸 Couple Garden - ফুলের বাগানে কাপেল হাঁটছে", "#FF4081", "#7B1FA2"},
            {"🌊 Beautiful Sea Scene", "🌊 সৌন্দর্য সমুদ্র সিন - নীল সমুদ্র, ঢেউ, বালি", "#001F3F", "#0074D9"},
            {"🏖️ Sea Beach Paradise", "🏖️ সমুদ্র সৈকত - সাদা বালি, নারকেল গাছ, নীল পানি", "#00BCD4", "#00695C"},
            {"🏔️ Mountain Beauty", "🏔️ পাহাড়ের সিন - বরফের পাহাড়, মেঘ, সুন্দর ভিউ", "#37474F", "#78909C"},
            {"🌅 Golden Sunset", "🌅 সূর্যাস্ত - আকাশ লাল সোনালী, সুন্দর সিনারি", "#FF5722", "#FFD700"},
            {"🌌 Starry Sky Night", "🌌 তারার আকাশ - রাতের আকাশে হাজার তারা", "#0A0518", "#3F51B5"},
            {"🌸 Cherry Blossom", "🌸 ফুলের সিন - জাপানি সাকুরা ফুল ঝরছে", "#F48FB1", "#AD1457"},
            {"🏝️ Tropical Island", "🏝️ দ্বীপ - Tropical Island, পাম গাছ, নীল সমুদ্র", "#00695C", "#FFCA28"},
            {"🌿 Natural Green", "🌿 প্রকৃতি - সবুজ বন, পাখির ডাক", "#0F3D2E", "#1B7A4D"},
            {"🦁 Animal Kingdom", "🦁 পশু পাখি - সিংহ, হরিণ, পাখি, জঙ্গল", "#3E1B00", "#8B4513"},
            {"🌙 Night Purple Premium", "🌙 আগের Premium Purple", "#1B1035", "#0A0518"},
            {"🔥 Royal Gold", "👑 Royal Gold - রাজকীয় সোনালী", "#3B2A00", "#FFD700"},
        };

        for (String[] t : themes) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(28, 22, 28, 22);
            GradientDrawable rBg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Color.parseColor(t[2]), Color.parseColor(t[3])});
            rBg.setCornerRadius(22);
            row.setBackground(rBg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0,0,0,14);
            row.setLayoutParams(lp);

            TextView name = new TextView(this);
            name.setText(t[0]);
            name.setTextColor(Color.WHITE); name.setTextSize(16); name.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(name);

            TextView desc = new TextView(this);
            desc.setText(t[1]);
            desc.setTextColor(Color.parseColor("#E0E0E0")); desc.setTextSize(12); desc.setPadding(0,4,0,10);
            row.addView(desc);

            Button btnApply = new Button(this);
            btnApply.setText("✨ Apply This Scenery");
            btnApply.setBackgroundColor(Color.parseColor("#FFD700")); btnApply.setTextColor(Color.BLACK); btnApply.setTypeface(null, android.graphics.Typeface.BOLD);
            btnApply.setOnClickListener(v -> {
                currentThemeName = t[0];
                roomMetaRef.child("roomTheme").setValue(t[0]);
                roomMetaRef.child("roomThemeDesc").setValue(t[1]);
                roomMetaRef.child("roomThemeColor1").setValue(t[2]);
                roomMetaRef.child("roomThemeColor2").setValue(t[3]);
                prefs.edit().putString("ROOM_THEME_NAME", t[0]).putString("ROOM_THEME_DESC", t[1]).apply();
                applyRoomTheme(t[2], t[3], t[0] + " - " + t[1]);
                Toast.makeText(this, "Theme Applied: " + t[0], Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            row.addView(btnApply);
            list.addView(row);
        }

        scroll.addView(list);
        root.addView(scroll);
        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) { w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500); w.setGravity(Gravity.BOTTOM); }
        dialog.show();
    }

    private void listenToRoomTheme() {
        roomMetaRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                String n = s.child("roomTheme").getValue(String.class);
                String d = s.child("roomThemeDesc").getValue(String.class);
                String c1 = s.child("roomThemeColor1").getValue(String.class);
                String c2 = s.child("roomThemeColor2").getValue(String.class);
                if (n != null && !n.isEmpty()) {
                    currentThemeName = n;
                    if (c1 != null && c2 != null) applyRoomTheme(c1, c2, n + (d != null ? " - " + d : ""));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void applyRoomTheme(String c1, String c2, String fullText) {
        try {
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor(c1), Color.parseColor(c2)});
                rootView.setBackground(bg);
            }
            if (roomThemeSceneryText != null) {
                roomThemeSceneryText.setText("🎨 " + fullText);
                roomThemeSceneryText.setVisibility(View.VISIBLE);
            } else {
                // যদি layout এ TextView না থাকে, Toast দিয়ে দেখাবে
                // কিন্তু লেআউটে id roomThemeSceneryText অ্যাড করলে উপরে দেখাবে
            }
        } catch (Exception ignored) {}
    }

    // ================= PREMIUM RANKING - LIVE / SACHOL =================
    private void showPremiumRankingDialog() {
        final Dialog rankDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        rankDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        rankDialog.setContentView(R.layout.dialog_room_ranking);
        TextView btnClose = rankDialog.findViewById(R.id.btnCloseRanking);
        if (btnClose != null) btnClose.setOnClickListener(v -> rankDialog.dismiss());
        Button btnTabDaily = rankDialog.findViewById(R.id.btnTabDaily);
        Button btnTabWeekly = rankDialog.findViewById(R.id.btnTabWeekly);
        Button btnTabMonthly = rankDialog.findViewById(R.id.btnTabMonthly);
        LinearLayout listContainer = rankDialog.findViewById(R.id.rankingListContainer);
        LinearLayout premiumTop = new LinearLayout(this);
        premiumTop.setOrientation(LinearLayout.HORIZONTAL); premiumTop.setGravity(Gravity.CENTER); premiumTop.setPadding(0,20,0,10);
        Button btnWealth = new Button(this); btnWealth.setText("💰 Wealth");
        Button btnCharm = new Button(this); btnCharm.setText("❤️ Charm");
        premiumTop.addView(btnWealth); premiumTop.addView(btnCharm);
        Runnable loadRank = () -> {
            btnWealth.setBackgroundColor(rankTab.equals("Wealth") ? Color.parseColor("#FFD700") : Color.parseColor("#2A1B4E"));
            btnWealth.setTextColor(rankTab.equals("Wealth") ? Color.BLACK : Color.WHITE);
            btnCharm.setBackgroundColor(rankTab.equals("Charm") ? Color.parseColor("#FF4081") : Color.parseColor("#2A1B4E"));
            btnCharm.setTextColor(Color.WHITE);
            if (btnTabDaily != null) { btnTabDaily.setBackgroundColor(rankPeriod.equals("Day") ? Color.parseColor("#FFD700") : Color.parseColor("#2A1B4E")); btnTabDaily.setTextColor(rankPeriod.equals("Day") ? Color.BLACK : Color.WHITE); }
            if (btnTabWeekly != null) { btnTabWeekly.setBackgroundColor(rankPeriod.equals("Week") ? Color.parseColor("#FFD700") : Color.parseColor("#2A1B4E")); btnTabWeekly.setTextColor(rankPeriod.equals("Week") ? Color.BLACK : Color.WHITE); }
            if (btnTabMonthly != null) { btnTabMonthly.setBackgroundColor(rankPeriod.equals("Month") ? Color.parseColor("#FFD700") : Color.parseColor("#2A1B4E")); btnTabMonthly.setTextColor(rankPeriod.equals("Month") ? Color.BLACK : Color.WHITE); }
            // LIVE - addValueEventListener, Single না
            userDatabaseRef.orderByChild(rankTab.equals("Wealth") ? "sendingCoins" : "receivingCoins").limitToLast(20).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    rankCache.clear();
                    for (DataSnapshot ds : snap.getChildren()) {
                        String n = ds.child("name").getValue(String.class);
                        Long c = ds.child(rankTab.equals("Wealth") ? "sendingCoins" : "receivingCoins").getValue(Long.class);
                        String img = ds.child("profileImage").getValue(String.class);
                        if (n == null) n = "User";
                        rankCache.add(new RankItem(n, c != null ? c : 0L, img, ds.getKey()));
                    }
                    Collections.sort(rankCache, (a,b) -> Long.compare(b.coins, a.coins));
                    if (listContainer != null) {
                        listContainer.removeAllViews();
                        if (rankCache.size() >= 3) {
                            LinearLayout podium = new LinearLayout(VoiceRoomActivity.this);
                            podium.setOrientation(LinearLayout.HORIZONTAL); podium.setGravity(Gravity.BOTTOM | Gravity.CENTER); podium.setPadding(0,10,0,15);
                            podium.addView(makePodiumItem(rankCache.get(1), "🥈 Top 2", false));
                            podium.addView(makePodiumItem(rankCache.get(0), "👑 Top 1", true));
                            podium.addView(makePodiumItem(rankCache.get(2), "🥉 Top 3", false));
                            listContainer.addView(podium);
                        }
                        for (int i = 0; i < rankCache.size(); i++) {
                            String badge = i==0 ? "#FFD700" : i==1 ? "#C0C0C0" : i==2 ? "#CD7F32" : "#FFFFFF";
                            String label = i<3 ? "🏆 Top " + (i+1) + " • " + rankCache.get(i).name : (i+1) + ". " + rankCache.get(i).name;
                            addRankUserRow(listContainer, String.valueOf(i+1), label, rankCache.get(i).coins + " Coins", badge);
                        }
                        if (rankCache.isEmpty()) addRankUserRow(listContainer, "1", "No Ranking Yet - Send Gift to Rank Up", "0 Coins", "#FFD700");
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        };
        btnWealth.setOnClickListener(v -> { rankTab="Wealth"; loadRank.run(); });
        btnCharm.setOnClickListener(v -> { rankTab="Charm"; loadRank.run(); });
        if (btnTabDaily != null) btnTabDaily.setOnClickListener(v -> { rankPeriod="Day"; loadRank.run(); Toast.makeText(this, "Showing Daily Ranking", Toast.LENGTH_SHORT).show(); });
        if (btnTabWeekly != null) btnTabWeekly.setOnClickListener(v -> { rankPeriod="Week"; loadRank.run(); Toast.makeText(this, "Showing Weekly Ranking", Toast.LENGTH_SHORT).show(); });
        if (btnTabMonthly != null) btnTabMonthly.setOnClickListener(v -> { rankPeriod="Month"; loadRank.run(); Toast.makeText(this, "Showing Monthly Ranking", Toast.LENGTH_SHORT).show(); });
        try { ViewGroup parent = (ViewGroup) listContainer.getParent(); parent.addView(premiumTop, 0); } catch (Exception ignored) {}
        loadRank.run();
        rankDialog.show();
    }

    private LinearLayout makePodiumItem(RankItem item, String topText, boolean isTop1) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView crown = new TextView(this); crown.setText(isTop1 ? "👑" : ""); crown.setTextSize(22); crown.setGravity(Gravity.CENTER); box.addView(crown);
        ImageView avatar = new ImageView(this);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(isTop1 ? 170 : 130, isTop1 ? 170 : 130); ap.gravity = Gravity.CENTER; avatar.setLayoutParams(ap);
        if (item.image != null && !item.image.isEmpty()) {
            try { byte[] b = android.util.Base64.decode(item.image, android.util.Base64.DEFAULT); Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length); if (bmp!=null){ RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getResources(), bmp); d.setCircular(true); avatar.setImageDrawable(d); } } catch (Exception e){ avatar.setImageResource(android.R.drawable.ic_menu_my_calendar); }
        } else avatar.setImageResource(android.R.drawable.ic_menu_my_calendar);
        box.addView(avatar);
        TextView top = new TextView(this); top.setText(topText); top.setTextColor(isTop1 ? Color.parseColor("#FFD700") : Color.WHITE); top.setTypeface(null, android.graphics.Typeface.BOLD); top.setTextSize(isTop1 ? 15 : 12); top.setGravity(Gravity.CENTER); if(isTop1) top.setBackgroundColor(Color.parseColor("#B71C1C")); box.addView(top);
        TextView n = new TextView(this); n.setText(item.name); n.setTextColor(Color.WHITE); n.setTextSize(12); n.setGravity(Gravity.CENTER); box.addView(n);
        TextView c = new TextView(this); c.setText("🪙 " + item.coins); c.setTextColor(Color.parseColor("#FFD700")); c.setTextSize(11); c.setGravity(Gravity.CENTER); box.addView(c);
        return box;
    }

    private void addRankUserRow(LinearLayout container, String rank, String name, String score, String badgeColorHex) {
        Context context = container.getContext();
        LinearLayout row = new LinearLayout(context); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(20,20,20,20); row.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable rBg = new GradientDrawable(); rBg.setColor(Color.parseColor("#1B1035")); rBg.setCornerRadius(16); row.setBackground(rBg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(0,0,0,12); row.setLayoutParams(p);
        TextView rankTxt = new TextView(context); rankTxt.setText("Top " + rank); rankTxt.setTextColor(Color.parseColor(badgeColorHex)); rankTxt.setTextSize(14); rankTxt.setTypeface(null, android.graphics.Typeface.BOLD); rankTxt.setMinWidth(90); row.addView(rankTxt);
        LinearLayout textStack = new LinearLayout(context); textStack.setOrientation(LinearLayout.VERTICAL); textStack.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        TextView nameTxt = new TextView(context); nameTxt.setText(name); nameTxt.setTextColor(Color.WHITE); nameTxt.setTextSize(15); textStack.addView(nameTxt);
        TextView scoreTxt = new TextView(context); scoreTxt.setText(score); scoreTxt.setTextColor(Color.parseColor("#00E676")); scoreTxt.setTextSize(12); textStack.addView(scoreTxt);
        row.addView(textStack); container.addView(row);
    }

    static class RankItem { String name; long coins; String image; String uid; RankItem(String n, long c, String i, String u){ name=n; coins=c; image=i; uid=u; } }

    // ================= GIFT =================
    private void showPremiumGiftPanel() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout rootLayout = new LinearLayout(this); rootLayout.setOrientation(LinearLayout.VERTICAL); rootLayout.setPadding(35,35,35,35);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0}); rootLayout.setBackground(bg);
        TextView title = new TextView(this); title.setText("🎁 SR VIP Luxury Gift Center"); title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(18); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setPadding(0,0,0,20); rootLayout.addView(title);
        final EditText inputTargetSeat = new EditText(this); inputTargetSeat.setHint("Target Seat (0 to 9 or type 'All')..."); inputTargetSeat.setHintTextColor(Color.parseColor("#AAAAAA")); inputTargetSeat.setTextColor(Color.WHITE); inputTargetSeat.setText("All"); rootLayout.addView(inputTargetSeat);
        RecyclerView rvGifts = new RecyclerView(this); rvGifts.setLayoutManager(new GridLayoutManager(this, 3)); rvGifts.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900)); rootLayout.addView(rvGifts);
        List<GiftModel> luxuryGifts = new ArrayList<>();
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
        GiftAdapter adapter = new GiftAdapter(luxuryGifts, gift -> selectedGiftHolder[0] = gift); rvGifts.setAdapter(adapter);
        Button btnSendGiftAction = new Button(this); btnSendGiftAction.setText("🚀 Send Luxury Gift"); btnSendGiftAction.setBackgroundColor(Color.parseColor("#FF4081")); btnSendGiftAction.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120); btnParams.setMargins(0,20,0,10); btnSendGiftAction.setLayoutParams(btnParams); rootLayout.addView(btnSendGiftAction);
        bottomSheetDialog.setContentView(rootLayout);
        btnSendGiftAction.setOnClickListener(v -> {
            String targetVal = inputTargetSeat.getText().toString().trim();
            String userName = prefs.getString("USER_NAME", "User");
            String priceStr = selectedGiftHolder[0].getGiftPrice().replaceAll("[^0-9]", "");
            long giftCoins = priceStr.isEmpty() ? 50000L : Long.parseLong(priceStr);
            triggerFlyingGiftAnimation(selectedGiftHolder[0].getGiftName());
            if (targetVal.equalsIgnoreCase("All")) {
                databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (int i=0;i<10;i++){ String seatKey="seat_"+i; if(snapshot.hasChild(seatKey)){ Long cur=snapshot.child(seatKey).child("giftTotal").getValue(Long.class); databaseReference.child("seats").child(seatKey).child("giftTotal").setValue((cur!=null?cur:0L)+giftCoins); String sid=snapshot.child(seatKey).child("userId").getValue(String.class); if(sid!=null && !sid.isEmpty()) updateReceivingCoinsForUser(sid, giftCoins); } }
                    } @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            } else {
                try { int seatIdx=Integer.parseInt(targetVal); if(seatIdx>=0 && seatIdx<=9){ String seatKey="seat_"+seatIdx; databaseReference.child("seats").child(seatKey).addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot snap){ Long cur=snap.child("giftTotal").getValue(Long.class); databaseReference.child("seats").child(seatKey).child("giftTotal").setValue((cur!=null?cur:0L)+giftCoins); String sid=snap.child("userId").getValue(String.class); if(sid!=null && !sid.isEmpty()) updateReceivingCoinsForUser(sid, giftCoins); } @Override public void onCancelled(@NonNull DatabaseError e){} }); } } catch(Exception e){ Toast.makeText(this, "Invalid seat! Updating all seats.", Toast.LENGTH_SHORT).show(); }
            }
            if (!currentUserId.isEmpty()) userDatabaseRef.child(currentUserId).child("sendingCoins").addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ Long cur=s.getValue(Long.class); userDatabaseRef.child(currentUserId).child("sendingCoins").setValue((cur!=null?cur:0L)+giftCoins); } @Override public void onCancelled(@NonNull DatabaseError e){} });
            String msgId = messagesRef.push().getKey();
            if (msgId != null) { java.util.Map<String,Object> m=new java.util.HashMap<>(); m.put("senderName", getCorrectBadgeIcon(currentUserId)+"🎁 "+userName); m.put("message", "sent luxury gift ["+selectedGiftHolder[0].getGiftName()+" - "+selectedGiftHolder[0].getGiftPrice()+" Coins]"); m.put("timestamp", System.currentTimeMillis()); messagesRef.child(msgId).setValue(m); }
            Toast.makeText(this, "Luxury Gift Sent Successfully!", Toast.LENGTH_SHORT).show(); bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
        Window window = bottomSheetDialog.getWindow(); if(window!=null){ window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500); window.setGravity(Gravity.BOTTOM); }
    }
    private void updateReceivingCoinsForUser(String targetUserId, long giftCoins) {
        userDatabaseRef.child(targetUserId).child("receivingCoins").addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ Long cur=s.getValue(Long.class); userDatabaseRef.child(targetUserId).child("receivingCoins").setValue((cur!=null?cur:0L)+giftCoins); } @Override public void onCancelled(@NonNull DatabaseError e){} });
    }
    private void triggerFlyingGiftAnimation(String giftName) {
        try { ViewGroup rootView=findViewById(android.R.id.content); if(rootView==null) return; final TextView flyingView=new TextView(this); flyingView.setText("🎁 "+giftName+" 🚀"); flyingView.setTextColor(Color.parseColor("#FFD700")); flyingView.setTextSize(20); flyingView.setTypeface(null, android.graphics.Typeface.BOLD); flyingView.setGravity(Gravity.CENTER); flyingView.setBackgroundColor(Color.parseColor("#88000000")); flyingView.setPadding(30,20,30,20); FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); params.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL; params.setMargins(0,0,0,300); rootView.addView(flyingView, params); TranslateAnimation animation=new TranslateAnimation(Animation.ABSOLUTE,0f,Animation.ABSOLUTE,0f,Animation.ABSOLUTE,0f,Animation.ABSOLUTE,-1000f); animation.setDuration(2000); animation.setFillAfter(true); animation.setAnimationListener(new Animation.AnimationListener(){ @Override public void onAnimationStart(Animation a){} @Override public void onAnimationEnd(Animation a){ rootView.removeView(flyingView);} @Override public void onAnimationRepeat(Animation a){} }); flyingView.startAnimation(animation); } catch(Exception ignored){}
    }
    private View createToolButton(String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(25,20,25,20); row.setGravity(Gravity.CENTER_VERTICAL); row.setOnClickListener(listener);
        GradientDrawable rBg=new GradientDrawable(); rBg.setColor(Color.parseColor("#2A1B4E")); rBg.setCornerRadius(18); row.setBackground(rBg);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,14); row.setLayoutParams(lp);
        TextView titleTxt=new TextView(this); titleTxt.setText(title); titleTxt.setTextColor(Color.WHITE); titleTxt.setTextSize(16); titleTxt.setTypeface(null, android.graphics.Typeface.BOLD); row.addView(titleTxt);
        TextView subTxt=new TextView(this); subTxt.setText(subtitle); subTxt.setTextColor(Color.parseColor("#00E676")); subTxt.setTextSize(12); row.addView(subTxt); return row;
    }
    private void listenToRoomAdmins(){ adminsRef.addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ roomAdminList.clear(); for(DataSnapshot ds:s.getChildren()) if(Boolean.TRUE.equals(ds.getValue(Boolean.class))) roomAdminList.add(ds.getKey()); } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
    private String getCorrectBadgeIcon(String targetUid){ if(targetUid==null||targetUid.isEmpty()) return ""; if(targetUid.equals(roomOwnerId)||roomAdminList.contains(targetUid)) return "🛡️ "; return ""; }
    @Override public void onBackPressed(){ leaveRoomAndFinish(); }
    private void listenToRealtimeChatMessages(){
        messagesRef.addChildEventListener(new ChildEventListener(){
            @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String prev){ String senderName=snapshot.child("senderName").getValue(String.class); String message=snapshot.child("message").getValue(String.class); Long msgTime=snapshot.child("timestamp").getValue(Long.class); if(msgTime!=null && msgTime<roomJoinTimestamp) return; if(senderName!=null && message!=null){ String fullMsg=senderName+": "+message; if(!chatMessages.contains(fullMsg)){ chatMessages.add(fullMsg); chatAdapter.notifyDataSetChanged(); if(chatListView!=null) chatListView.setSelection(chatMessages.size()-1); } } }
            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p){} @Override public void onChildRemoved(@NonNull DataSnapshot s){ chatMessages.clear(); chatAdapter.notifyDataSetChanged(); } @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p){} @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void showRoomLockPasswordDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(VoiceRoomActivity.this); builder.setTitle("🔒 Room Lock Protection");
        LinearLayout layout=new LinearLayout(VoiceRoomActivity.this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(50,30,50,30);
        final EditText inputPassword=new EditText(VoiceRoomActivity.this); inputPassword.setHint("Enter Room Password..."); inputPassword.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD); layout.addView(inputPassword); builder.setView(layout);
        builder.setPositiveButton("Lock Room", (dialog,which)->{ String password=inputPassword.getText().toString().trim(); if(!password.isEmpty()){ roomMetaRef.child("roomPassword").setValue(password); roomMetaRef.child("isLocked").setValue(true); Toast.makeText(this,"Room locked with password successfully!",Toast.LENGTH_SHORT).show(); } else Toast.makeText(this,"Password cannot be empty!",Toast.LENGTH_SHORT).show(); });
        builder.setNeutralButton("Unlock Room", (dialog,which)->{ roomMetaRef.child("roomPassword").removeValue(); roomMetaRef.child("isLocked").setValue(false); Toast.makeText(this,"Room is now Unlocked!",Toast.LENGTH_SHORT).show(); });
        builder.setNegativeButton("Cancel", (dialog,which)->dialog.cancel()); builder.show();
    }
    private void showScrollableOnlineMembersDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout=new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(40,40,40,40);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}); bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0}); layout.setBackground(bg);
        TextView title=new TextView(this); title.setText("👥 Active Online Members"); title.setTextColor(Color.parseColor("#00E676")); title.setTextSize(18); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setPadding(0,0,0,20); layout.addView(title);
        ScrollView scrollView=new ScrollView(this); scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1200)); scrollView.setFillViewport(true);
        LinearLayout memberListContainer=new LinearLayout(this); memberListContainer.setOrientation(LinearLayout.VERTICAL);
        databaseReference.child("online_users").addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot snapshot){ memberListContainer.removeAllViews(); int serial=1; for(DataSnapshot ds:snapshot.getChildren()){ String memberName=ds.getValue(String.class); String memberUid=ds.getKey(); if(memberName!=null){ LinearLayout row=new LinearLayout(VoiceRoomActivity.this); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(20,20,20,20); row.setGravity(Gravity.CENTER_VERTICAL); GradientDrawable rBg=new GradientDrawable(); rBg.setColor(Color.parseColor("#2A1B4E")); rBg.setCornerRadius(16); row.setBackground(rBg); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,10); row.setLayoutParams(lp); String roleBadge=(memberUid!=null && memberUid.equals(roomOwnerId))?"👑 Owner ":(roomAdminList.contains(memberUid)?"🛡️ Admin ":"🟢 Member "); TextView nameTxt=new TextView(VoiceRoomActivity.this); nameTxt.setText(serial+". "+roleBadge+memberName+" (ID: "+memberUid+")"); nameTxt.setTextColor(Color.WHITE); nameTxt.setTextSize(15); nameTxt.setTypeface(null, android.graphics.Typeface.BOLD); row.addView(nameTxt); final String fName=memberName; final String fUid=memberUid; row.setOnClickListener(v->{ dialog.dismiss(); fetchUserDataAndShowProfile(fName, fUid, null); }); memberListContainer.addView(row); serial++; } } } @Override public void onCancelled(@NonNull DatabaseError e){} });
        scrollView.addView(memberListContainer); layout.addView(scrollView); dialog.setContentView(layout);
        Window window=dialog.getWindow(); if(window!=null){ window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1400); window.setGravity(Gravity.BOTTOM); } dialog.show();
    }
    private void syncLatestUserProfile(){ if(!currentUserId.isEmpty()) userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot snapshot){ if(snapshot.exists()){ String dbName=snapshot.child("name").getValue(String.class); String dbImage=snapshot.child("profileImage").getValue(String.class); SharedPreferences.Editor editor=prefs.edit(); if(dbName!=null && !dbName.isEmpty()) editor.putString("USER_NAME", dbName); if(dbImage!=null){ editor.putString("USER_IMAGE", dbImage); editor.putString("USER_PROFILE_IMAGE", dbImage); } editor.apply(); } } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
    private void listenToRoomDetails(){
        roomMetaRef.addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot snapshot){ String roomName=snapshot.child("roomName").getValue(String.class); String roomBio=snapshot.child("roomBio").getValue(String.class); String ownerImage=snapshot.child("roomOwnerImage").getValue(String.class); if(roomName!=null && !roomName.isEmpty()){ if(roomTitleText!=null) roomTitleText.setText(roomName); } else if(roomTitleText!=null) roomTitleText.setText("SR Voice Room"); if(roomInfoText!=null) roomInfoText.setText("ID: "+roomOwnerId); if(roomNoticeBarText!=null){ String bioText=(roomBio!=null && !roomBio.isEmpty())?roomBio:"Welcome to my voice room!"; roomNoticeBarText.setText("📢 Notice: "+bioText); } if(roomHeaderProfileImg!=null && ownerImage!=null && !ownerImage.isEmpty()){ try{ byte[] decodedString=android.util.Base64.decode(ownerImage, android.util.Base64.DEFAULT); Bitmap decodedByte=BitmapFactory.decodeByteArray(decodedString,0,decodedString.length); if(decodedByte!=null){ RoundedBitmapDrawable d=RoundedBitmapDrawableFactory.create(getResources(), decodedByte); d.setCircular(true); roomHeaderProfileImg.setImageDrawable(d); } } catch(Exception e){ roomHeaderProfileImg.setImageResource(android.R.drawable.ic_menu_my_calendar); } } } @Override public void onCancelled(@NonNull DatabaseError e){} });
    }
    private void showEditRoomDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(VoiceRoomActivity.this); builder.setTitle("⚙️ Room Settings & Profile");
        LinearLayout layout=new LinearLayout(VoiceRoomActivity.this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(50,20,50,20);
        Button btnChangePic=new Button(VoiceRoomActivity.this); btnChangePic.setText("🖼️ Change Room Profile Picture"); btnChangePic.setOnClickListener(v->{ Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); startActivityForResult(intent, PICK_IMAGE_REQUEST); }); layout.addView(btnChangePic);
        dialogInputName=new EditText(VoiceRoomActivity.this); dialogInputName.setHint("Enter Room Name..."); layout.addView(dialogInputName);
        dialogInputBio=new EditText(VoiceRoomActivity.this); dialogInputBio.setHint("Enter Room Bio / Notice..."); layout.addView(dialogInputBio); builder.setView(layout);
        builder.setPositiveButton("Save", (dialog,which)->{ String newName=dialogInputName.getText().toString().trim(); String newBio=dialogInputBio.getText().toString().trim(); if(!newName.isEmpty()) roomMetaRef.child("roomName").setValue(newName); if(!newBio.isEmpty()) roomMetaRef.child("roomBio").setValue(newBio); Toast.makeText(this,"Room settings updated successfully!",Toast.LENGTH_SHORT).show(); });
        builder.setNegativeButton("Cancel", (dialog,which)->dialog.cancel()); builder.show();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null){ Uri imageUri=data.getData(); try{ InputStream inputStream=getContentResolver().openInputStream(imageUri); Bitmap bitmap=BitmapFactory.decodeStream(inputStream); if(bitmap!=null){ Bitmap resizedBitmap=Bitmap.createScaledBitmap(bitmap,150,150,true); ByteArrayOutputStream baos=new ByteArrayOutputStream(); resizedBitmap.compress(Bitmap.CompressFormat.JPEG,70,baos); String encodedImage=android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT); roomMetaRef.child("roomOwnerImage").setValue(encodedImage); Toast.makeText(this,"Room Profile Picture Updated!",Toast.LENGTH_SHORT).show(); } } catch(Exception e){ Toast.makeText(this,"Failed to load image",Toast.LENGTH_SHORT).show(); } }
    }
    private void listenToOnlineMembers(){
        String currentUserName=prefs.getString("USER_NAME","User");
        databaseReference.child("online_users").child(currentUserId).setValue(currentUserName);
        databaseReference.child("online_users").child(currentUserId).onDisconnect().removeValue();
        databaseReference.child("online_users").addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ long count=s.getChildrenCount(); if(textOnlineCount!=null) textOnlineCount.setText(String.valueOf(count)); } @Override public void onCancelled(@NonNull DatabaseError e){} });
    }
    private void showPremiumRoomInfoAndMembersDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(40,40,40,40);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}); bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0}); root.setBackground(bg);
        LinearLayout tabHeader=new LinearLayout(this); tabHeader.setOrientation(LinearLayout.HORIZONTAL); tabHeader.setGravity(Gravity.CENTER); tabHeader.setPadding(0,0,0,20);
        Button tabInfoBtn=new Button(this); tabInfoBtn.setText("ℹ️ Room Info"); tabInfoBtn.setBackgroundColor(Color.parseColor("#FFD700")); tabInfoBtn.setTextColor(Color.BLACK); tabInfoBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        Button tabMembersBtn=new Button(this); tabMembersBtn.setText("👥 Members"); tabMembersBtn.setBackgroundColor(Color.parseColor("#2A1B4E")); tabMembersBtn.setTextColor(Color.WHITE); tabMembersBtn.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        tabHeader.addView(tabInfoBtn); tabHeader.addView(tabMembersBtn); root.addView(tabHeader);
        ScrollView contentScrollView=new ScrollView(this); contentScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1200)); contentScrollView.setFillViewport(true);
        LinearLayout mainContainer=new LinearLayout(this); mainContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout infoLayout=new LinearLayout(this); infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout ownerCard=new LinearLayout(this); ownerCard.setOrientation(LinearLayout.VERTICAL); ownerCard.setPadding(25,25,25,25); GradientDrawable cardBg=new GradientDrawable(); cardBg.setColor(Color.parseColor("#2A1B4E")); cardBg.setCornerRadius(20); ownerCard.setBackground(cardBg);
        TextView ownerTitle=new TextView(this); ownerTitle.setText("👑 Room Owner: SR Official"); ownerTitle.setTextColor(Color.parseColor("#FFD700")); ownerTitle.setTextSize(16); ownerTitle.setTypeface(null, android.graphics.Typeface.BOLD); ownerCard.addView(ownerTitle);
        TextView ownerIdTxt=new TextView(this); ownerIdTxt.setText("ID: "+roomOwnerId); ownerIdTxt.setTextColor(Color.parseColor("#00E676")); ownerIdTxt.setTextSize(13); ownerIdTxt.setPadding(0,4,0,10); ownerCard.addView(ownerIdTxt);
        TextView noticeTxt=new TextView(this); noticeTxt.setText("📢 Welcome to SR Voice Chat Room! Enjoy live stream & luxury gifts."); noticeTxt.setTextColor(Color.WHITE); noticeTxt.setTextSize(13); ownerCard.addView(noticeTxt);
        TextView themeTxt=new TextView(this); themeTxt.setText("🎨 Current Theme: "+currentThemeName); themeTxt.setTextColor(Color.parseColor("#FFD700")); themeTxt.setTextSize(13); themeTxt.setPadding(0,10,0,0); ownerCard.addView(themeTxt);
        infoLayout.addView(ownerCard); mainContainer.addView(infoLayout);
        LinearLayout membersLayout=new LinearLayout(this); membersLayout.setOrientation(LinearLayout.VERTICAL); membersLayout.setVisibility(View.GONE);
        databaseReference.child("online_users").addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot snapshot){ membersLayout.removeAllViews(); int serial=1; for(DataSnapshot ds:snapshot.getChildren()){ String uName=ds.getValue(String.class); String uId=ds.getKey(); if(uName!=null){ LinearLayout row=new LinearLayout(VoiceRoomActivity.this); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(20,20,20,20); row.setGravity(Gravity.CENTER_VERTICAL); GradientDrawable rBg=new GradientDrawable(); rBg.setColor(Color.parseColor("#2A1B4E")); rBg.setCornerRadius(16); row.setBackground(rBg); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,10); row.setLayoutParams(lp); String roleBadge=(uId!=null && uId.equals(roomOwnerId))?"👑 Owner ":(roomAdminList.contains(uId)?"🛡️ Admin ":"🟢 Member "); TextView nameTxt=new TextView(VoiceRoomActivity.this); nameTxt.setText(serial+". "+roleBadge+uName+" (ID: "+uId+")"); nameTxt.setTextColor(Color.WHITE); nameTxt.setTextSize(14); nameTxt.setTypeface(null, android.graphics.Typeface.BOLD); row.addView(nameTxt); final String fn=uName; final String fu=uId; row.setOnClickListener(v->{ dialog.dismiss(); fetchUserDataAndShowProfile(fn, fu, null); }); membersLayout.addView(row); serial++; } } } @Override public void onCancelled(@NonNull DatabaseError e){} });
        mainContainer.addView(membersLayout);
        tabInfoBtn.setOnClickListener(v->{ tabInfoBtn.setBackgroundColor(Color.parseColor("#FFD700")); tabInfoBtn.setTextColor(Color.BLACK); tabMembersBtn.setBackgroundColor(Color.parseColor("#2A1B4E")); tabMembersBtn.setTextColor(Color.WHITE); infoLayout.setVisibility(View.VISIBLE); membersLayout.setVisibility(View.GONE); });
        tabMembersBtn.setOnClickListener(v->{ tabMembersBtn.setBackgroundColor(Color.parseColor("#FFD700")); tabMembersBtn.setTextColor(Color.BLACK); tabInfoBtn.setBackgroundColor(Color.parseColor("#2A1B4E")); tabInfoBtn.setTextColor(Color.WHITE); infoLayout.setVisibility(View.GONE); membersLayout.setVisibility(View.VISIBLE); });
        contentScrollView.addView(mainContainer); root.addView(contentScrollView); dialog.setContentView(root);
        Window window=dialog.getWindow(); if(window!=null){ window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500); window.setGravity(Gravity.BOTTOM); } dialog.show();
    }
    private void listenToFirebaseSeats(){
        int[] seatImgIds={R.id.seatImg1,R.id.seatImg2,R.id.seatImg3,R.id.seatImg4,R.id.seatImg5,R.id.seatImg6,R.id.seatImg7,R.id.seatImg8,R.id.seatImg9,R.id.seatImg10};
        int[] seatTxtIds={R.id.seatTxt1,R.id.seatTxt2,R.id.seatTxt3,R.id.seatTxt4,R.id.seatTxt5,R.id.seatTxt6,R.id.seatTxt7,R.id.seatTxt8,R.id.seatTxt9,R.id.seatTxt10};
        int[] seatGiftTxtIds={R.id.seatGiftTxt1,R.id.seatGiftTxt2,R.id.seatGiftTxt3,R.id.seatGiftTxt4,R.id.seatGiftTxt5,R.id.seatGiftTxt6,R.id.seatGiftTxt7,R.id.seatGiftTxt8,R.id.seatGiftTxt9,R.id.seatGiftTxt10};
        databaseReference.child("seats").addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot snapshot){ for(int i=0;i<10;i++){ ImageView seatImg=findViewById(seatImgIds[i]); TextView seatTxt=findViewById(seatTxtIds[i]); TextView seatGiftTxt=findViewById(seatGiftTxtIds[i]); String seatKey="seat_"+i; if(snapshot.hasChild(seatKey)){ String name=snapshot.child(seatKey).child("userName").getValue(String.class); String imageBase64=snapshot.child(seatKey).child("userImage").getValue(String.class); String sUid=snapshot.child(seatKey).child("userId").getValue(String.class); Long giftTotal=snapshot.child(seatKey).child("giftTotal").getValue(Long.class); if(seatTxt!=null){ String roleIcon=(sUid!=null && (sUid.equals(roomOwnerId)||roomAdminList.contains(sUid)))?"🛡️ ":""; String displayName=(name!=null && !name.isEmpty())?name:(i==0?"Host":String.valueOf(i+1)); seatTxt.setText(roleIcon+displayName); } if(seatGiftTxt!=null){ seatGiftTxt.setText((giftTotal!=null && giftTotal>0)?giftTotal+" Coins":""); } if(seatImg!=null && imageBase64!=null && !imageBase64.isEmpty()){ try{ byte[] decodedString=android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT); Bitmap decodedByte=BitmapFactory.decodeByteArray(decodedString,0,decodedString.length); if(decodedByte!=null){ RoundedBitmapDrawable d=RoundedBitmapDrawableFactory.create(getResources(), decodedByte); d.setCircular(true); seatImg.setImageDrawable(d); seatImg.setPadding(0,0,0,0); } } catch(Exception e){ seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar); } } } else { if(seatTxt!=null) seatTxt.setText(i==0?"Host":String.valueOf(i+1)); if(seatGiftTxt!=null) seatGiftTxt.setText(""); if(seatImg!=null){ seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar); seatImg.setPadding(12,12,12,12); } } } } @Override public void onCancelled(@NonNull DatabaseError e){} });
    }
    private void setupSeatInteractions(){
        int[] seatCardIds={R.id.seatCard1,R.id.seatCard2,R.id.seatCard3,R.id.seatCard4,R.id.seatCard5,R.id.seatCard6,R.id.seatCard7,R.id.seatCard8,R.id.seatCard9,R.id.seatCard10};
        for(int i=0;i<seatCardIds.length;i++){ final int seatIndex=i; final int seatNumber=i+1; View seatContainer=findViewById(seatCardIds[i]); if(seatContainer!=null){ seatContainer.setOnClickListener(v->{ String seatKey="seat_"+seatIndex; databaseReference.child("seats").child(seatKey).addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot seatSnap){ String occupantName=seatSnap.child("userName").getValue(String.class); String occupantUid=seatSnap.child("userId").getValue(String.class); boolean isOccupied=occupantName!=null && !occupantName.isEmpty(); List<String> optionsList=new ArrayList<>(); if(isOccupied){ optionsList.add("👤 View Profile (ID: "+occupantUid+")"); optionsList.add("🚪 Leave Mic"); } else optionsList.add("🎙️ Take Mic"); optionsList.add("❌ Cancel"); CharSequence[] seatOptions=optionsList.toArray(new CharSequence[0]); AlertDialog.Builder builder=new AlertDialog.Builder(VoiceRoomActivity.this); builder.setTitle("🎙️ Seat "+seatNumber); builder.setItems(seatOptions,(dialog,which)->{ String selectedOption=seatOptions[which].toString(); if(selectedOption.contains("Take Mic")) takeSeat(seatIndex,seatKey); else if(selectedOption.contains("View Profile")){ String imgUri=seatSnap.child("userImage").getValue(String.class); fetchUserDataAndShowProfile(occupantName, occupantUid, imgUri); } else if(selectedOption.contains("Leave Mic")){ databaseReference.child("seats").child(seatKey).removeValue(); Toast.makeText(VoiceRoomActivity.this,"Left seat & counter reset",Toast.LENGTH_SHORT).show(); } }); builder.show(); } @Override public void onCancelled(@NonNull DatabaseError e){} }); }); } }
    }
    private void fetchUserDataAndShowProfile(String userName, String userId, String userImage){
        if(userId==null||userId.isEmpty()){ showUserProfileDialog(userName,userId,userImage,0L,0L,"♂","🇧🇩"); return; }
        userDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot snapshot){ long sendingCoins=0L; long receivingCoins=0L; String gender="♂"; String country="🇧🇩"; if(snapshot.exists()){ Long send=snapshot.child("sendingCoins").getValue(Long.class); Long recv=snapshot.child("receivingCoins").getValue(Long.class); String dbGender=snapshot.child("gender").getValue(String.class); String dbCountry=snapshot.child("country").getValue(String.class); if(send!=null) sendingCoins=send; if(recv!=null) receivingCoins=recv; if(dbGender!=null){ String lowerGender=dbGender.toLowerCase(); if(lowerGender.contains("female")||lowerGender.contains("girl")||lowerGender.contains("মেয়ে")||lowerGender.contains("♀")) gender="♀"; else gender="♂"; } if(dbCountry!=null && !dbCountry.isEmpty()) country=dbCountry; } showUserProfileDialog(userName,userId,userImage,sendingCoins,receivingCoins,gender,country); } @Override public void onCancelled(@NonNull DatabaseError e){ showUserProfileDialog(userName,userId,userImage,0L,0L,"♂","🇧🇩"); } });
    }

    // ================= PREMIUM BIG PROFILE - COINS NAI, SHUDHU LEVEL =================
    private void showUserProfileDialog(String userName, String userId, String userImage, long sendingCoins, long receivingCoins, String gender, String country) {
        BottomSheetDialog profileDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 50, 40, 60);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#2A1B4E"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{60,60,60,60,0,0,0,0}); layout.setBackground(bg);
        ImageView imgProfileBig = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(340, 340);
        imgParams.gravity = Gravity.CENTER_HORIZONTAL; imgParams.setMargins(0,0,0,20);
        imgProfileBig.setLayoutParams(imgParams); imgProfileBig.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (userImage != null && !userImage.isEmpty()) {
            try { byte[] decodedString = android.util.Base64.decode(userImage, android.util.Base64.DEFAULT); Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); if (decodedByte != null) { RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getResources(), decodedByte); d.setCircular(true); imgProfileBig.setImageDrawable(d); } else imgProfileBig.setImageResource(android.R.drawable.ic_menu_my_calendar); } catch (Exception e) { imgProfileBig.setImageResource(android.R.drawable.ic_menu_my_calendar); }
        } else imgProfileBig.setImageResource(android.R.drawable.ic_menu_my_calendar);
        layout.addView(imgProfileBig);
        LinearLayout nameRow = new LinearLayout(this); nameRow.setOrientation(LinearLayout.HORIZONTAL); nameRow.setGravity(Gravity.CENTER); nameRow.setPadding(0,0,0,8);
        TextView nameText = new TextView(this); nameText.setText(userName != null ? userName : "SR User"); nameText.setTextColor(Color.WHITE); nameText.setTextSize(24); nameText.setTypeface(null, android.graphics.Typeface.BOLD); nameRow.addView(nameText);
        TextView genderFlagText = new TextView(this); genderFlagText.setText(" " + gender + " " + country); genderFlagText.setTextSize(20); genderFlagText.setTextColor(gender.equals("♀") ? Color.parseColor("#FF4081") : Color.parseColor("#00B0FF")); nameRow.addView(genderFlagText);
        layout.addView(nameRow);
        String displayId = (userId != null && !userId.isEmpty()) ? userId : "493859069";
        TextView idText = new TextView(this); idText.setText("ID: " + displayId + " 📋"); idText.setTextColor(Color.parseColor("#00E676")); idText.setTextSize(16); idText.setGravity(Gravity.CENTER); idText.setPadding(0,0,0,10);
        idText.setOnClickListener(v -> { ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); ClipData clip = ClipData.newPlainText("User ID", displayId); if (clipboard != null) { clipboard.setPrimaryClip(clip); Toast.makeText(this, "ID Copied: " + displayId, Toast.LENGTH_SHORT).show(); } });
        layout.addView(idText);
        TextView followCountText = new TextView(this); followCountText.setText("Followers: ...  •  Following: ..."); followCountText.setTextColor(Color.parseColor("#FFD700")); followCountText.setTextSize(14); followCountText.setTypeface(null, android.graphics.Typeface.BOLD); followCountText.setGravity(Gravity.CENTER); followCountText.setPadding(0,0,0,15); layout.addView(followCountText);
        if (userId != null && !userId.isEmpty()) {
            userDatabaseRef.child(userId).child("followers").addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ long fc=s.getChildrenCount(); userDatabaseRef.child(userId).child("following").addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s2){ followCountText.setText("👥 Followers: "+fc+"  •  Following: "+s2.getChildrenCount()); } @Override public void onCancelled(@NonNull DatabaseError e){} }); } @Override public void onCancelled(@NonNull DatabaseError e){} });
        }
        int sendLevel = LevelUtils.calculateLevel(sendingCoins);
        int recvLevel = LevelUtils.calculateLevel(receivingCoins);
        LinearLayout levelRow = new LinearLayout(this); levelRow.setOrientation(LinearLayout.HORIZONTAL); levelRow.setGravity(Gravity.CENTER); levelRow.setPadding(0,0,0,25);
        levelRow.addView(createSoftBadgeView("⭐ Send Lv." + sendLevel, LevelUtils.getLevelColor(sendLevel)));
        levelRow.addView(createSoftBadgeView("👑 Recv Lv." + recvLevel, LevelUtils.getLevelColor(recvLevel)));
        layout.addView(levelRow);
        // Coins line deleted - only Level
        LinearLayout actionRow = new LinearLayout(this); actionRow.setOrientation(LinearLayout.HORIZONTAL); actionRow.setGravity(Gravity.CENTER); actionRow.setPadding(0,10,0,10);
        Button btnFollow = new Button(this); btnFollow.setText("✨ Follow"); btnFollow.setBackgroundColor(Color.parseColor("#FFD700")); btnFollow.setTextColor(Color.BLACK); btnFollow.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, 130, 1.0f); p1.setMargins(0,0,8,0); btnFollow.setLayoutParams(p1);
        btnFollow.setOnClickListener(v -> {
            if (userId==null || userId.equals(currentUserId)) { Toast.makeText(this, "You cannot follow yourself!", Toast.LENGTH_SHORT).show(); return; }
            userDatabaseRef.child(userId).child("followers").child(currentUserId).setValue(true);
            userDatabaseRef.child(currentUserId).child("following").child(userId).setValue(true);
            btnFollow.setText("✅ Following"); btnFollow.setBackgroundColor(Color.parseColor("#00E676"));
            Toast.makeText(this, "Successfully Followed " + userName, Toast.LENGTH_SHORT).show();
        });
        actionRow.addView(btnFollow);
        Button btnAdd = new Button(this); btnAdd.setText("➕ Add Friend"); btnAdd.setBackgroundColor(Color.parseColor("#00B0FF")); btnAdd.setTextColor(Color.WHITE); btnAdd.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams pAdd = new LinearLayout.LayoutParams(0, 130, 1.0f); pAdd.setMargins(8,0,8,0); btnAdd.setLayoutParams(pAdd);
        btnAdd.setOnClickListener(v -> {
            if (userId==null || userId.equals(currentUserId)) { Toast.makeText(this, "You cannot add yourself!", Toast.LENGTH_SHORT).show(); return; }
            userDatabaseRef.child(userId).child("friendRequests").child(currentUserId).setValue(prefs.getString("USER_NAME","User"));
            userDatabaseRef.child(currentUserId).child("friends").child(userId).setValue(true);
            userDatabaseRef.child(userId).child("friends").child(currentUserId).setValue(true);
            btnAdd.setText("✅ Added"); Toast.makeText(this, "Friend Request Sent to " + userName, Toast.LENGTH_SHORT).show();
        });
        actionRow.addView(btnAdd);
        Button btnPopupGift = new Button(this); btnPopupGift.setText("🎁 Gift"); btnPopupGift.setBackgroundColor(Color.parseColor("#FF4081")); btnPopupGift.setTextColor(Color.WHITE); btnPopupGift.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, 130, 1.0f); p2.setMargins(8,0,0,0); btnPopupGift.setLayoutParams(p2);
        btnPopupGift.setOnClickListener(v -> { profileDialog.dismiss(); showPremiumGiftPanel(); });
        actionRow.addView(btnPopupGift);
        layout.addView(actionRow);
        LinearLayout actionRow2 = new LinearLayout(this); actionRow2.setOrientation(LinearLayout.HORIZONTAL); actionRow2.setGravity(Gravity.CENTER); actionRow2.setPadding(0,12,0,0);
        Button btnChatUser = new Button(this); btnChatUser.setText("💬 Chat"); btnChatUser.setBackgroundColor(Color.parseColor("#2A1B4E")); btnChatUser.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams pc = new LinearLayout.LayoutParams(0, 120, 1.0f); pc.setMargins(0,0,8,0); btnChatUser.setLayoutParams(pc);
        btnChatUser.setOnClickListener(v -> { profileDialog.dismiss(); Toast.makeText(this, "Chat with " + userName, Toast.LENGTH_SHORT).show(); });
        actionRow2.addView(btnChatUser);
        Button btnViewFull = new Button(this); btnViewFull.setText("👤 Full Profile"); btnViewFull.setBackgroundColor(Color.parseColor("#2A1B4E")); btnViewFull.setTextColor(Color.parseColor("#FFD700"));
        LinearLayout.LayoutParams pv = new LinearLayout.LayoutParams(0, 120, 1.0f); pv.setMargins(8,0,0,0); btnViewFull.setLayoutParams(pv);
        btnViewFull.setOnClickListener(v -> { profileDialog.dismiss(); showFullProfileInside(userName, userId, userImage, sendingCoins, receivingCoins, gender, country); });
        actionRow2.addView(btnViewFull);
        layout.addView(actionRow2);
        profileDialog.setContentView(layout);
        Window window = profileDialog.getWindow(); if (window != null) { window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1750); window.setGravity(Gravity.BOTTOM); try { window.getAttributes().windowAnimations = android.R.style.Animation_Dialog; } catch (Exception ignored) {} }
        profileDialog.show();
    }

    private void showFullProfileInside(String userName, String userId, String userImage, long sendingCoins, long receivingCoins, String gender, String country) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 60);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0});
        layout.setBackground(bg);
        TextView title = new TextView(this);
        title.setText("👤 Full Profile - " + userName);
        title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(19); title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,20);
        layout.addView(title);
        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(400, 400);
        ip.gravity = Gravity.CENTER_HORIZONTAL; ip.setMargins(0,0,0,20);
        img.setLayoutParams(ip); img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (userImage != null && !userImage.isEmpty()) {
            try { byte[] b = android.util.Base64.decode(userImage, android.util.Base64.DEFAULT); Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length); if (bmp!=null){ RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getResources(), bmp); d.setCircular(true); img.setImageDrawable(d); } } catch (Exception e){ img.setImageResource(android.R.drawable.ic_menu_my_calendar); }
        }
        layout.addView(img);
        TextView info = new TextView(this);
        info.setText("ID: " + userId + "\nGender: " + gender + "  Country: " + country + "\nSend Level: " + LevelUtils.calculateLevel(sendingCoins) + "  •  Recv Level: " + LevelUtils.calculateLevel(receivingCoins) + "\nCurrent Theme: " + currentThemeName + "\n\nBio: Welcome to my profile! Enjoy SR Voice Chat. 💑🌊");
        info.setTextColor(Color.WHITE); info.setTextSize(15); info.setGravity(Gravity.CENTER); info.setPadding(0,0,0,20);
        layout.addView(info);
        Button btnClose = new Button(this); btnClose.setText("⬅ Back to Room"); btnClose.setBackgroundColor(Color.parseColor("#FFD700")); btnClose.setTextColor(Color.BLACK);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        layout.addView(btnClose);
        dialog.setContentView(layout);
        Window w = dialog.getWindow();
        if (w != null) { w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1600); w.setGravity(Gravity.BOTTOM); }
        dialog.show();
    }

    private View createSoftBadgeView(String text, int colorInt){ TextView badge=new TextView(this); badge.setText(text); badge.setTextColor(Color.WHITE); badge.setTextSize(12); badge.setTypeface(null, android.graphics.Typeface.BOLD); badge.setPadding(22,10,22,10); GradientDrawable bg=new GradientDrawable(); bg.setColor(colorInt); bg.setCornerRadius(30); badge.setBackground(bg); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,16,0); badge.setLayoutParams(lp); return badge; }
    private void showFullLevelCenterDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(35,35,35,35);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}); bg.setCornerRadii(new float[]{45,45,45,45,0,0,0,0}); root.setBackground(bg);
        LinearLayout topBar=new LinearLayout(this); topBar.setOrientation(LinearLayout.HORIZONTAL); topBar.setGravity(Gravity.CENTER_VERTICAL); topBar.setPadding(0,0,0,20);
        TextView title=new TextView(this); title.setText("🏆 SR Level Center (Lv.1 - Lv.150)"); title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(18); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)); topBar.addView(title);
        Button btnClose=new Button(this); btnClose.setText("✕"); btnClose.setBackgroundColor(Color.TRANSPARENT); btnClose.setTextColor(Color.WHITE); btnClose.setTextSize(16); btnClose.setOnClickListener(v->dialog.dismiss()); topBar.addView(btnClose); root.addView(topBar);
        ScrollView scrollView=new ScrollView(this); scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1400)); scrollView.setFillViewport(true);
        LinearLayout listContainer=new LinearLayout(this); listContainer.setOrientation(LinearLayout.VERTICAL);
        for(int lvl=1;lvl<=150;lvl++){ LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setPadding(25,20,25,20); row.setGravity(Gravity.CENTER_VERTICAL); GradientDrawable rBg=new GradientDrawable(); rBg.setColor(Color.parseColor("#2A1B4E")); rBg.setCornerRadius(20); row.setBackground(rBg); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(0,0,0,14); row.setLayoutParams(p); int levelColor=LevelUtils.getLevelColor(lvl); String badgeText=LevelUtils.getLevelBadgeText(lvl); String targetCoins=LevelUtils.getLevelTargetCoinsFormatted(lvl); TextView badge=new TextView(this); badge.setText(badgeText); badge.setTextColor(Color.WHITE); badge.setTextSize(14); badge.setTypeface(null, android.graphics.Typeface.BOLD); badge.setGravity(Gravity.CENTER); badge.setPadding(20,12,20,12); GradientDrawable bBg=new GradientDrawable(); bBg.setColor(levelColor); bBg.setCornerRadius(25); badge.setBackground(bBg); badge.setMinWidth(110); row.addView(badge); LinearLayout textStack=new LinearLayout(this); textStack.setOrientation(LinearLayout.VERTICAL); textStack.setPadding(20,0,0,0); textStack.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)); TextView titleTxt=new TextView(this); titleTxt.setText("Level Badge Tier "+lvl); titleTxt.setTextColor(Color.WHITE); titleTxt.setTextSize(15); titleTxt.setTypeface(null, android.graphics.Typeface.BOLD); textStack.addView(titleTxt); TextView targetTxt=new TextView(this); targetTxt.setText("Target: "+targetCoins); targetTxt.setTextColor(Color.parseColor("#00E676")); targetTxt.setTextSize(12); textStack.addView(targetTxt); row.addView(textStack); listContainer.addView(row); }
        scrollView.addView(listContainer); root.addView(scrollView); dialog.setContentView(root);
        Window window=dialog.getWindow(); if(window!=null){ window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1600); window.setGravity(Gravity.BOTTOM); } dialog.show();
    }
    private void takeSeat(int seatIndex, String seatKey){
        final String userName=prefs.getString("USER_NAME","User"); final String userImageUriStr=prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE",""));
        for(int i=0;i<10;i++){ final int oldIndex=i; databaseReference.child("seats").child("seat_"+i).addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ String sUserId=s.child("userId").getValue(String.class); if(sUserId!=null && sUserId.equals(currentUserId)) databaseReference.child("seats").child("seat_"+oldIndex).removeValue(); } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
        databaseReference.child("seats").child(seatKey).child("userId").setValue(currentUserId);
        databaseReference.child("seats").child(seatKey).child("userName").setValue(userName);
        databaseReference.child("seats").child(seatKey).child("userImage").setValue(userImageUriStr);
        databaseReference.child("seats").child(seatKey).child("giftTotal").setValue(0L);
        databaseReference.child("seats").child(seatKey).onDisconnect().removeValue();
        Toast.makeText(VoiceRoomActivity.this,"Joined seat successfully!",Toast.LENGTH_SHORT).show();
    }
    private void leaveRoomAndFinish(){
        databaseReference.child("online_users").child(currentUserId).removeValue();
        for(int index=0;index<10;index++){ final int i=index; databaseReference.child("seats").child("seat_"+i).addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ String sUserId=s.child("userId").getValue(String.class); if(sUserId!=null && sUserId.equals(currentUserId)) databaseReference.child("seats").child("seat_"+i).removeValue(); } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
        finish();
    }
}
