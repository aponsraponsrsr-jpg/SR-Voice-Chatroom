package com.sr.voicechat;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class PublicProfileActivity extends AppCompatActivity {
    private String targetId = "", myId = "";
    private TextView txtName, txtId, txtFollow, txtFans, txtVisitor, txtLevelBadge;
    private ImageView imgAvatar, imgCover, imgHeadwear;
    private LinearLayout tabProfileContent, tabAlbumContent, tabMomentsContent, tabIntimacyContent;
    private TextView tabProfile, tabAlbum, tabMoments, tabIntimacy;
    private UserModel user = new UserModel();
    private SharedPreferences prefs;
    private String myName = "";
    private boolean isFriend = false, isFollowing = false, isMutualFollow = false;
    private ValueEventListener userListener = null;
    private DatabaseReference userQueryRef = null;
    private boolean profileTabBuilt = false;
    // ===== LEVEL 1-150 - LevelUtils ONLY - Me + VoiceRoom SAME - NEW GORGEOUS COLOR =====
    private int myWealthLevel = 1;
    private int myCharmLevel = 1;
    private long myTotalSending = 0;
    private long myTotalReceiving = 0;

    private BroadcastReceiver profileReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            showCachedInstantly();
            loadUser();
        }
    };

    private long getLongVal(DataSnapshot s, String k){ try{ Object v=s.child(k).getValue(); if(v==null) return 0; if(v instanceof Number) return ((Number)v).longValue(); return Long.parseLong(String.valueOf(v).replace(",","").trim()); }catch(Exception e){ return 0; } }

    // ===== LEVEL BADGE - 150 + NEW COLOR - SOB JAYGAY EKI =====
    private void updateLevelBadge(){
        if(txtLevelBadge==null) return;
        txtLevelBadge.setText("  ⭐ Wealth Lv."+myWealthLevel+" • 💎 Charm Lv."+myCharmLevel+" • "+LevelUtils.getLevelTitle(myWealthLevel)+"  ");
        txtLevelBadge.setTextColor(Color.WHITE);
        GradientDrawable bg=new GradientDrawable();
        bg.setColors(LevelUtils.getLevelColors(myWealthLevel));
        bg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        bg.setCornerRadius(30);
        txtLevelBadge.setBackground(bg);
        txtLevelBadge.setOnClickListener(v->showLevelDialog());
    }
    private void showLevelDialog(){
        try{
            androidx.appcompat.app.AlertDialog.Builder b=new androidx.appcompat.app.AlertDialog.Builder(this);
            ScrollView sv=new ScrollView(this); sv.setPadding(24,24,24,24); sv.setBackgroundColor(Color.parseColor("#0A0518"));
            LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); sv.addView(root);
            TextView title=new TextView(this); title.setText("⭐ Level - 1 to 150"); title.setTextColor(Color.WHITE); title.setTextSize(22); title.setTypeface(null,Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,16); root.addView(title);
            root.addView(makeLevelCard("💰 Wealth Level", myWealthLevel, myTotalSending, "Sending"));
            root.addView(makeLevelCard("💎 Charm Level", myCharmLevel, myTotalReceiving, "Receiving"));
            b.setView(sv); b.setPositiveButton("Close",null); b.show();
        }catch(Exception e){ Toast.makeText(this,"Level - Coming Soon",Toast.LENGTH_SHORT).show(); }
    }
    private LinearLayout makeLevelCard(String label,int lvl,long exp,String type){
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(28,24,28,24);
        GradientDrawable bg=new GradientDrawable(); bg.setColors(LevelUtils.getLevelColors(lvl)); bg.setOrientation(GradientDrawable.Orientation.TL_BR); bg.setCornerRadius(28); card.setBackground(bg);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,16); card.setLayoutParams(lp);
        TextView t1=new TextView(this); t1.setText(label); t1.setTextColor(Color.WHITE); t1.setTextSize(18); t1.setTypeface(null,Typeface.BOLD); card.addView(t1);
        TextView t2=new TextView(this); t2.setText("Lv."+lvl+" • "+LevelUtils.getLevelTitle(lvl)); t2.setTextColor(Color.WHITE); t2.setTextSize(15); t2.setTypeface(null,Typeface.BOLD); card.addView(t2);
        TextView t3=new TextView(this); t3.setText(type+": "+LevelUtils.formatCoins(exp)+" • Target: "+LevelUtils.getLevelTargetCoinsFormatted(lvl)); t3.setTextColor(Color.WHITE); t3.setTextSize(13); card.addView(t3);
        int pct = LevelUtils.getLevelProgressPercent(lvl, exp);
        android.widget.ProgressBar pb=new android.widget.ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); pb.setMax(100); pb.setProgress(pct); pb.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,28)); card.addView(pb);
        TextView t4=new TextView(this);
        if(lvl>=150) t4.setText("MAX LEVEL 👑 - Legend 150!");
        else t4.setText("Next Lv."+(lvl+1)+" - Need "+LevelUtils.formatCoins(LevelUtils.getCoinsToNextLevel(lvl,exp))+" • "+pct+"%");
        t4.setTextColor(Color.WHITE); t4.setTextSize(12); card.addView(t4);
        return card;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = prefs.getString("MY_9_DIGIT_ID", "");
        myName = prefs.getString("USER_NAME", "");
        if (myName.isEmpty()) myName = prefs.getString("MY_NAME", "User");
        if (myName.isEmpty()) myName = prefs.getString("name", "User");
        targetId = getIntent().getStringExtra("TARGET_9DIGIT_ID");
        if (targetId == null || targetId.isEmpty()) targetId = myId;
        boolean isMine = targetId.equals(myId);
        // Cached Level - 1-150 - 0 ar asbe na
        try{ myWealthLevel = Integer.parseInt(prefs.getString("CACHED_LEVEL_" + targetId, "1")); }catch(Exception e){ myWealthLevel=1; }
        if(myWealthLevel<1) myWealthLevel=1; if(myWealthLevel>150) myWealthLevel=150;
        try{ myCharmLevel = Integer.parseInt(prefs.getString("CACHED_CHARM_" + targetId, "1")); }catch(Exception e){ myCharmLevel=1; }
        if(myCharmLevel<1) myCharmLevel=1; if(myCharmLevel>150) myCharmLevel=150;
        myTotalSending = prefs.getLong("CACHED_SENDING_" + targetId, 0);
        myTotalReceiving = prefs.getLong("CACHED_RECEIVING_" + targetId, 0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A1F16"));
        scroll.addView(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(40, 20, 40, 30);
        GradientDrawable headerBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#1E7A4C"), Color.parseColor("#0F4D2E")});
        headerBg.setCornerRadius(0);
        header.setBackground(headerBg);
        root.addView(header);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(topBar);

        TextView btnBack = new TextView(this);
        btnBack.setText("← Back");
        btnBack.setTextSize(14);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setTextColor(Color.WHITE);
        btnBack.setPadding(10, 10, 10, 10);
        btnBack.setOnClickListener(v -> finish());
        topBar.addView(btnBack);

        TextView spacer = new TextView(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(spacer);

        if (isMine) {
            TextView btnPencil = new TextView(this);
            btnPencil.setText("✏️ Edit");
            btnPencil.setTextSize(14);
            btnPencil.setTypeface(null, Typeface.BOLD);
            btnPencil.setTextColor(Color.WHITE);
            btnPencil.setGravity(Gravity.CENTER);
            btnPencil.setPadding(30, 16, 30, 16);
            GradientDrawable pbg = new GradientDrawable();
            pbg.setColor(Color.parseColor("#FF9800"));
            pbg.setCornerRadius(40);
            btnPencil.setBackground(pbg);
            btnPencil.setOnClickListener(v -> {
                Intent i = new Intent(PublicProfileActivity.this, EditProfileActivity.class);
                i.putExtra("EDIT_MODE", true);
                startActivity(i);
            });
            topBar.addView(btnPencil);
        }

        imgCover = new ImageView(this);
        imgCover.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 280));
        imgCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgCover.setBackgroundColor(Color.parseColor("#14532D"));
        imgCover.setPadding(0, 20, 0, 10);
        imgCover.setOnClickListener(v -> {
            if (isMine) Toast.makeText(this, "Cover Photo - Next Step e change korbo", Toast.LENGTH_SHORT).show();
        });
        header.addView(imgCover);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setPadding(0, 10, 0, 0);
        header.addView(topRow);

        LinearLayout avatarWrap = new LinearLayout(this);
        avatarWrap.setPadding(6, 6, 6, 6);
        GradientDrawable avBg = new GradientDrawable();
        avBg.setColor(Color.WHITE);
        avBg.setShape(GradientDrawable.OVAL);
        avatarWrap.setBackground(avBg);
        topRow.addView(avatarWrap);

        imgAvatar = new ImageView(this);
        imgAvatar.setLayoutParams(new LinearLayout.LayoutParams(170, 170));
        imgAvatar.setBackgroundColor(Color.TRANSPARENT);
        imgAvatar.setPadding(0, 0, 0, 0);
        imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgAvatar.setClipToOutline(true);
        imgAvatar.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        avatarWrap.addView(imgAvatar);

        LinearLayout nameBox = new LinearLayout(this);
        nameBox.setOrientation(LinearLayout.VERTICAL);
        nameBox.setPadding(30, 0, 0, 0);
        nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(nameBox);

        txtName = new TextView(this);
        txtName.setText(getCachedName());
        txtName.setTextSize(20);
        txtName.setTypeface(null, Typeface.BOLD);
        txtName.setTextColor(Color.WHITE);
        nameBox.addView(txtName);

        txtId = new TextView(this);
        txtId.setText("ID: " + targetId + "  📋 Tap to Copy");
        txtId.setTextColor(Color.parseColor("#D1FAE5"));
        txtId.setTextSize(13);
        txtId.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("ID", targetId));
            Toast.makeText(this, "ID Copied", Toast.LENGTH_SHORT).show();
        });
        nameBox.addView(txtId);

        txtLevelBadge = new TextView(this);
        txtLevelBadge.setText("  ⭐ Wealth Lv."+myWealthLevel+" • 💎 Charm Lv."+myCharmLevel+"  ");
        txtLevelBadge.setTextColor(Color.WHITE);
        txtLevelBadge.setTextSize(12);
        txtLevelBadge.setTypeface(null, Typeface.BOLD);
        txtLevelBadge.setPadding(18, 8, 18, 8);
        GradientDrawable lbg = new GradientDrawable();
        lbg.setColors(LevelUtils.getLevelColors(myWealthLevel));
        lbg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        lbg.setCornerRadius(30);
        txtLevelBadge.setBackground(lbg);
        LinearLayout.LayoutParams lbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lbLp.setMargins(0, 10, 0, 0);
        txtLevelBadge.setLayoutParams(lbLp);
        nameBox.addView(txtLevelBadge);

        imgHeadwear = new ImageView(this);
        imgHeadwear.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
        imgHeadwear.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgHeadwear.setBackgroundColor(Color.TRANSPARENT);
        imgHeadwear.setVisibility(View.GONE);
        topRow.addView(imgHeadwear);

        LinearLayout countCard = new LinearLayout(this);
        countCard.setOrientation(LinearLayout.HORIZONTAL);
        countCard.setPadding(20, 20, 20, 20);
        GradientDrawable countBg = new GradientDrawable();
        countBg.setColor(Color.parseColor("#FFFFFF"));
        countBg.setCornerRadius(25);
        countCard.setBackground(countBg);
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        countLp.setMargins(0, 25, 0, 0);
        countCard.setLayoutParams(countLp);
        header.addView(countCard);
        txtFollow = addCount(countCard, prefs.getString("CACHED_FOLLOW_" + targetId, "0"), "Follow", "#111111", "#888888");
        txtFans = addCount(countCard, prefs.getString("CACHED_FANS_" + targetId, "0"), "Fans", "#111111", "#888888");
        txtVisitor = addCount(countCard, prefs.getString("CACHED_VISITOR_" + targetId, "0"), "Visitor", "#111111", "#888888");

        if (!isMine) {
            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setPadding(0, 25, 0, 0);
            header.addView(btnRow);
            TextView btnAddFriend = makeBtn("👥 Add Friend", "#00B0FF");
            btnAddFriend.setOnClickListener(v -> sendFriendRequest());
            btnRow.addView(btnAddFriend);
            TextView btnFollow = makeBtn("+ Follow", "#FF9800");
            btnFollow.setOnClickListener(v -> sendFollowRequest());
            btnRow.addView(btnFollow);
            TextView btnGift = makeBtn("🎁 Gift", "#E91E63");
            btnGift.setOnClickListener(v -> Toast.makeText(this, "Gift - Room theke pathan", Toast.LENGTH_SHORT).show());
            btnRow.addView(btnGift);
            LinearLayout btnRow2 = new LinearLayout(this);
            btnRow2.setOrientation(LinearLayout.HORIZONTAL);
            btnRow2.setPadding(0, 15, 0, 0);
            header.addView(btnRow2);
            TextView btnMessage = makeBtn("💬 Message - Say something...", "#00C853");
            btnMessage.setOnClickListener(v -> openChatIfAllowed());
            btnRow2.addView(btnMessage);
            checkFriendFollowStatus(btnAddFriend, btnFollow, btnMessage);
        }

        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabRow.setBackgroundColor(Color.parseColor("#0A1F16"));
        tabRow.setPadding(10, 15, 10, 10);
        root.addView(tabRow);
        tabProfile = makeTab(tabRow, "Profile", true);
        tabAlbum = makeTab(tabRow, "Photo album", false);
        tabMoments = makeTab(tabRow, "Moments", false);
        tabIntimacy = makeTab(tabRow, "Intimacy", false);

        tabProfileContent = new LinearLayout(this);
        tabProfileContent.setOrientation(LinearLayout.VERTICAL);
        tabProfileContent.setPadding(30, 10, 30, 20);
        root.addView(tabProfileContent);
        tabAlbumContent = new LinearLayout(this);
        tabAlbumContent.setOrientation(LinearLayout.VERTICAL);
        tabAlbumContent.setPadding(30, 10, 30, 20);
        tabAlbumContent.setVisibility(View.GONE);
        root.addView(tabAlbumContent);
        tabMomentsContent = new LinearLayout(this);
        tabMomentsContent.setOrientation(LinearLayout.VERTICAL);
        tabMomentsContent.setPadding(30, 10, 30, 20);
        tabMomentsContent.setVisibility(View.GONE);
        root.addView(tabMomentsContent);
        tabIntimacyContent = new LinearLayout(this);
        tabIntimacyContent.setOrientation(LinearLayout.VERTICAL);
        tabIntimacyContent.setPadding(30, 10, 30, 20);
        tabIntimacyContent.setVisibility(View.GONE);
        root.addView(tabIntimacyContent);

        buildProfileTab();
        buildAlbumTab();
        buildMomentsTab();
        buildIntimacyTab();
        profileTabBuilt = true;

        setContentView(scroll);
        try {
            IntentFilter filter = new IntentFilter("com.sr.voicechat.PROFILE_UPDATED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(profileReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(profileReceiver, filter);
            }
        } catch (Exception ignored) {}
        showCachedInstantly();
        loadUser();
    }

    private String getCachedName() {
        if (targetId.equals(myId)) {
            String n = prefs.getString("USER_NAME", "");
            if (!n.isEmpty()) return n;
            n = prefs.getString("MY_NAME", "");
            if (!n.isEmpty()) return n;
            return "User";
        } else {
            String n = prefs.getString("CACHED_NAME_" + targetId, "");
            if (!n.isEmpty()) return n;
            String intentName = getIntent().getStringExtra("TARGET_NAME");
            if (intentName != null && !intentName.isEmpty()) return intentName;
            return "User";
        }
    }

    private void showCachedInstantly() {
        if (txtName != null) {
            String cached = getCachedName();
            if (user != null && user.name != null && !user.name.isEmpty() && !user.name.equals("User")) {
                cached = user.name;
            }
            txtName.setText(cached);
            user.name = cached;
        }
        String cachedPhoto = "";
        if (targetId.equals(myId)) cachedPhoto = prefs.getString("USER_PHOTO", "");
        else cachedPhoto = prefs.getString("CACHED_PHOTO_" + targetId, "");
        if (!cachedPhoto.isEmpty() && imgAvatar != null) showPhoto(cachedPhoto, imgAvatar);
        updateLevelBadge();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(profileReceiver); } catch (Exception ignored) {}
        try { if (userQueryRef != null && userListener != null) userQueryRef.removeEventListener(userListener); } catch (Exception ignored) {}
    }

    @Override protected void onResume() {
        super.onResume();
        showCachedInstantly();
        loadUser();
    }

    private void checkFriendFollowStatus(TextView btnAddFriend, TextView btnFollow, TextView btnMessage) {
        String myUid = prefs.getString("FIREBASE_UID", "");
        if (myUid.isEmpty()) return;
        DatabaseReference meRef = FirebaseDatabase.getInstance().getReference("users").child(myUid);
        meRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                isFriend = snap.child("friends").hasChild(targetId);
                isFollowing = snap.child("following").hasChild(targetId);
                boolean heFollowsMe = snap.child("followers").hasChild(targetId);
                isMutualFollow = isFollowing && heFollowsMe;
                if (isFriend) {
                    btnAddFriend.setText("✅ Friends");
                    btnMessage.setText("💬 Message - Say something...");
                } else {
                    btnAddFriend.setText("👥 Add Friend");
                    if (!isMutualFollow) btnMessage.setText("🔒 Accept Friend / Mutual Follow to Chat");
                }
                if (isFollowing) btnFollow.setText("✅ Following");
                else btnFollow.setText("+ Follow");
                if (isMutualFollow) btnMessage.setText("💬 Message - Say something...");
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void sendFriendRequest() {
        if (isFriend) { Toast.makeText(this, "Already Friends ❤️", Toast.LENGTH_SHORT).show(); openChatIfAllowed(); return; }
        String key = FirebaseDatabase.getInstance().getReference("users").child(targetId).child("inbox").push().getKey();
        if (key == null) return;
        Map<String, Object> req = new HashMap<>();
        req.put("type", "FRIEND_REQUEST");
        req.put("fromId", myId);
        req.put("fromName", myName);
        req.put("targetId", targetId);
        req.put("title", "👥 Friend Request");
        req.put("message", myName + " (ID: " + myId + ") wants to be your friend");
        req.put("status", "Pending");
        req.put("timestamp", System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("users").child(targetId).child("inbox").child(key).setValue(req)
            .addOnCompleteListener(t -> {
                if (t.isSuccessful()) Toast.makeText(this, "Friend Request Sent! ✅", Toast.LENGTH_LONG).show();
                else Toast.makeText(this, "Failed! Try Again", Toast.LENGTH_SHORT).show();
            });
    }

    private void sendFollowRequest() {
        if (isFollowing) { Toast.makeText(this, "Already Following ✅", Toast.LENGTH_SHORT).show(); return; }
        String key = FirebaseDatabase.getInstance().getReference("users").child(targetId).child("inbox").push().getKey();
        if (key == null) return;
        Map<String, Object> req = new HashMap<>();
        req.put("type", "FOLLOW_REQUEST");
        req.put("fromId", myId);
        req.put("fromName", myName);
        req.put("targetId", targetId);
        req.put("title", "➕ Follow Request");
        req.put("message", myName + " (ID: " + myId + ") started following you - Follow Back to Chat");
        req.put("status", "Pending");
        req.put("timestamp", System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("users").child(targetId).child("inbox").child(key).setValue(req)
            .addOnCompleteListener(t -> {
                if (t.isSuccessful()) {
                    String myUid = prefs.getString("FIREBASE_UID", "");
                    if (!myUid.isEmpty()) FirebaseDatabase.getInstance().getReference("users").child(myUid).child("following").child(targetId).setValue("Pending");
                    Toast.makeText(this, "Follow Request Sent! ✅", Toast.LENGTH_LONG).show();
                }
            });
    }

    private void openChatIfAllowed() {
        if (!isFriend && !isMutualFollow) {
            Toast.makeText(this, "🔒 First Add Friend / Mutual Follow - tarpor SMS korte parba", Toast.LENGTH_LONG).show();
            return;
        }
        String chatId = myId.compareTo(targetId) < 0 ? myId + "_" + targetId : targetId + "_" + myId;
        Intent i = new Intent(PublicProfileActivity.this, PrivateChatActivity.class);
        i.putExtra("MY_ID", myId);
        i.putExtra("MY_NAME", myName);
        i.putExtra("OTHER_ID", targetId);
        i.putExtra("OTHER_NAME", user.name);
        i.putExtra("CHAT_ID", chatId);
        startActivity(i);
    }

    private void buildProfileTab() {
        String headwearText = "View >";
        try {
            if (user.headwearName != null && !user.headwearName.isEmpty()) headwearText = user.headwearName;
            else if (prefs.getString("HEADWEAR_NAME", "").length() > 0) headwearText = prefs.getString("HEADWEAR_NAME", "");
            else if (prefs.getString("headwearName", "").length() > 0) headwearText = prefs.getString("headwearName", "");
        } catch (Exception ignored) {}
        TextView title = new TextView(this);
        title.setText("My Collection");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(5, 15, 5, 15);
        tabProfileContent.addView(title);
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        tabProfileContent.addView(row1);
        row1.addView(makeBadgeCard("🏷️", "Nameplate", "Lv."+myWealthLevel+" • "+LevelUtils.getLevelTitle(myWealthLevel), 1));
        row1.addView(makeBadgeCard("💎", "Supporter", user.supporterName.isEmpty() ? "No supporter yet" : user.supporterName, 1));
        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        tabProfileContent.addView(row2);
        row2.addView(makeBadgeCard("🏅", "Medal (" + user.medalCount + ")", "View Medals >", 1));
        row2.addView(makeBadgeCard("👨‍👩‍👧", "Family", user.getFamilyDisplay(), 1));
        LinearLayout row3 = new LinearLayout(this);
        row3.setOrientation(LinearLayout.HORIZONTAL);
        tabProfileContent.addView(row3);
        row3.addView(makeBadgeCard("🎩", "Headwear (" + user.headwearCount + ")", headwearText, 1));
        row3.addView(makeBadgeCard("🚗", "Car (" + user.carCount + ")", "View >", 1));
        addProfileRow(tabProfileContent, "🎁 Gift (" + user.giftCount + ")", "All gifts - Dollar is Private");
        addProfileRow(tabProfileContent, "⭐ Wealth Level", "Lv."+myWealthLevel+" • "+LevelUtils.getLevelTitle(myWealthLevel)+" • Sending: "+LevelUtils.formatCoins(myTotalSending));
        addProfileRow(tabProfileContent, "💎 Charm Level", "Lv."+myCharmLevel+" • "+LevelUtils.getLevelTitle(myCharmLevel)+" • Receiving: "+LevelUtils.formatCoins(myTotalReceiving));
    }

    private LinearLayout makeBadgeCard(String icon, String title, String value, int weight) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(25, 22, 25, 22);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(25);
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(8, 8, 8, 8);
        card.setLayoutParams(lp);
        card.setOnClickListener(v -> Toast.makeText(this, title + " - Coming Soon", Toast.LENGTH_SHORT).show());
        TextView tIcon = new TextView(this);
        tIcon.setText(icon);
        tIcon.setTextSize(22);
        card.addView(tIcon);
        TextView tTitle = new TextView(this);
        tTitle.setText(title);
        tTitle.setTextColor(Color.parseColor("#111111"));
        tTitle.setTextSize(13);
        tTitle.setTypeface(null, Typeface.BOLD);
        tTitle.setPadding(0, 8, 0, 0);
        card.addView(tTitle);
        TextView tVal = new TextView(this);
        tVal.setText(value);
        tVal.setTextColor(Color.parseColor("#777777"));
        tVal.setTextSize(12);
        tVal.setPadding(0, 4, 0, 0);
        card.addView(tVal);
        return card;
    }

    private void buildAlbumTab() {
        TextView head = new TextView(this);
        head.setText("Photo Album");
        head.setTextColor(Color.WHITE);
        head.setTextSize(16);
        head.setTypeface(null, Typeface.BOLD);
        head.setPadding(5, 10, 5, 15);
        tabAlbumContent.addView(head);
        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            tabAlbumContent.addView(row);
            for (int c = 0; c < 3; c++) {
                LinearLayout box = new LinearLayout(this);
                box.setGravity(Gravity.CENTER);
                box.setPadding(10, 30, 10, 30);
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor("#1E3A2E"));
                bg.setCornerRadius(20);
                box.setBackground(bg);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                lp.setMargins(8, 8, 8, 8);
                box.setLayoutParams(lp);
                TextView t = new TextView(this);
                t.setText(r == 0 && c == 0 ? "+ Upload" : "Photo");
                t.setTextColor(Color.WHITE);
                t.setTextSize(13);
                t.setGravity(Gravity.CENTER);
                box.addView(t);
                box.setOnClickListener(v -> Toast.makeText(this, "Photo Upload - Next Step", Toast.LENGTH_SHORT).show());
                row.addView(box);
            }
        }
        TextView t = new TextView(this);
        t.setText("No photos yet - Upload korle ekhane sobai dekhbe");
        t.setTextColor(Color.parseColor("#888888"));
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 20, 0, 0);
        tabAlbumContent.addView(t);
    }

    private void buildMomentsTab() {
        TextView t = new TextView(this);
        t.setText("No moments yet\n\nShare your moments here");
        t.setTextColor(Color.parseColor("#888888"));
        t.setTextSize(14);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 30, 0, 30);
        tabMomentsContent.addView(t);
    }

    private void buildIntimacyTab() {
        addProfileRow(tabIntimacyContent, "💑 CP Center", "Fall in love 0 days");
        addProfileRow(tabIntimacyContent, "🤝 Friendship Center", "Become friends 0 days");
        TextView note = new TextView(this);
        note.setText("CP / Friendship - Ekhane apnar closest friend dekhte parben");
        note.setTextColor(Color.parseColor("#888888"));
        note.setTextSize(12);
        note.setPadding(20, 20, 20, 20);
        tabIntimacyContent.addView(note);
    }

    private void addProfileRow(LinearLayout parent, String title, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(30, 22, 30, 22);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(25);
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        row.setLayoutParams(lp);
        parent.addView(row);
        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextColor(Color.parseColor("#333333"));
        t1.setTextSize(14);
        t1.setTypeface(null, Typeface.BOLD);
        t1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(t1);
        TextView t2 = new TextView(this);
        t2.setText(value);
        t2.setTextColor(Color.parseColor("#777777"));
        t2.setTextSize(13);
        row.addView(t2);
    }

    private TextView makeTab(LinearLayout parent, String title, boolean active) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(13);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(15, 15, 15, 15);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        tv.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        tv.setTextColor(active ? Color.WHITE : Color.parseColor("#8AB4A0"));
        tv.setOnClickListener(v -> selectTab(title));
        parent.addView(tv);
        return tv;
    }

    private void selectTab(String title) {
        tabProfileContent.setVisibility(title.equals("Profile") ? View.VISIBLE : View.GONE);
        tabAlbumContent.setVisibility(title.equals("Photo album") ? View.VISIBLE : View.GONE);
        tabMomentsContent.setVisibility(title.equals("Moments") ? View.VISIBLE : View.GONE);
        tabIntimacyContent.setVisibility(title.equals("Intimacy") ? View.VISIBLE : View.GONE);
        tabProfile.setTextColor(title.equals("Profile") ? Color.WHITE : Color.parseColor("#8AB4A0"));
        tabAlbum.setTextColor(title.equals("Photo album") ? Color.WHITE : Color.parseColor("#8AB4A0"));
        tabMoments.setTextColor(title.equals("Moments") ? Color.WHITE : Color.parseColor("#8AB4A0"));
        tabIntimacy.setTextColor(title.equals("Intimacy") ? Color.WHITE : Color.parseColor("#8AB4A0"));
    }

    private TextView addCount(LinearLayout parent, String num, String label, String numColor, String labelColor) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        parent.addView(box);
        TextView tvNum = new TextView(this);
        tvNum.setText(num);
        tvNum.setTextSize(16);
        tvNum.setTypeface(null, Typeface.BOLD);
        tvNum.setTextColor(Color.parseColor(numColor));
        tvNum.setGravity(Gravity.CENTER);
        box.addView(tvNum);
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(12);
        tvLabel.setTextColor(Color.parseColor(labelColor));
        tvLabel.setGravity(Gravity.CENTER);
        box.addView(tvLabel);
        return tvNum;
    }

    private TextView makeBtn(String text, String color) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(14);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(20, 22, 20, 22);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(color));
        bg.setCornerRadius(40);
        btn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(10, 0, 10, 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    private void showPhoto(String photo, ImageView iv) {
        if (iv == null || photo == null || photo.isEmpty()) return;
        try {
            if (photo.startsWith("http")) {
                Glide.with(this).load(photo).circleCrop().into(iv);
            } else {
                byte[] b = Base64.decode(photo, Base64.DEFAULT);
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inSampleSize = 2;
                Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length, o);
                if (bmp != null) Glide.with(this).load(bmp).circleCrop().into(iv);
            }
        } catch (Exception ignored) {}
    }

    private void loadUser() {
        showCachedInstantly();
        if (userListener != null && userQueryRef != null) {
            try { userQueryRef.removeEventListener(userListener); } catch (Exception ignored) {}
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        userQueryRef = ref.orderByChild("my9DigitId").equalTo(targetId).getRef();
        userListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                String newPhotoUrl = "";
                String headwearImage = "";
                String headwearName = "";
                String coverUrl = "";
                for (DataSnapshot ds : snap.getChildren()) {
                    if (ds.child("name").getValue() != null) user.name = String.valueOf(ds.child("name").getValue());
                    if (ds.child("gender").getValue() != null) user.gender = String.valueOf(ds.child("gender").getValue());
                    if (ds.child("country").getValue() != null) user.country = String.valueOf(ds.child("country").getValue());
                    if (ds.child("photoBase64").getValue() != null) user.photoBase64 = String.valueOf(ds.child("photoBase64").getValue());
                    if (ds.child("photoUrl").getValue() != null) newPhotoUrl = String.valueOf(ds.child("photoUrl").getValue());
                    if (newPhotoUrl.isEmpty() && ds.child("photo").getValue() != null) newPhotoUrl = String.valueOf(ds.child("photo").getValue());
                    if (!newPhotoUrl.isEmpty()) { user.photoUrl = newPhotoUrl; user.photo = newPhotoUrl; }
                    if (ds.child("coverBase64").getValue() != null) {
                        String cover = String.valueOf(ds.child("coverBase64").getValue());
                        if (!cover.isEmpty()) {
                            try {
                                byte[] cb = Base64.decode(cover, Base64.DEFAULT);
                                BitmapFactory.Options co = new BitmapFactory.Options();
                                co.inSampleSize = 2;
                                Bitmap cbmp = BitmapFactory.decodeByteArray(cb, 0, cb.length, co);
                                if (cbmp != null) imgCover.setImageBitmap(cbmp);
                            } catch (Exception ignored) {}
                        }
                    }
                    if (ds.child("coverUrl").getValue() != null) coverUrl = String.valueOf(ds.child("coverUrl").getValue());
                    if (coverUrl.isEmpty() && ds.child("coverPhoto").getValue() != null) coverUrl = String.valueOf(ds.child("coverPhoto").getValue());
                    if (coverUrl.isEmpty() && ds.child("coverImage").getValue() != null) coverUrl = String.valueOf(ds.child("coverImage").getValue());
                    // ===== LEVEL 1-150 - LevelUtils - Sending / Receiving theke - Purano level field keo recalc diye override =====
                    myTotalSending = Math.max(getLongVal(ds,"sending"), getLongVal(ds,"totalSending"));
                    myTotalReceiving = Math.max(getLongVal(ds,"receiving"), getLongVal(ds,"totalReceiving"));
                    myTotalReceiving = Math.max(myTotalReceiving, Math.max(getLongVal(ds,"targetCoins"), getLongVal(ds,"totalTargetCoins")));
                    myWealthLevel = LevelUtils.calculateWealthLevel(myTotalSending);
                    myCharmLevel = LevelUtils.calculateCharmLevel(myTotalReceiving);
                    // Firebase e purano wealthLevel thakle - boro ta nibo
                    if(getLongVal(ds,"wealthLevel") > myWealthLevel) myWealthLevel = (int) getLongVal(ds,"wealthLevel");
                    if(getLongVal(ds,"charmLevel") > myCharmLevel) myCharmLevel = (int) getLongVal(ds,"charmLevel");
                    if(myWealthLevel<1) myWealthLevel=1; if(myWealthLevel>150) myWealthLevel=150;
                    if(myCharmLevel<1) myCharmLevel=1; if(myCharmLevel>150) myCharmLevel=150;
                    user.level = String.valueOf(myWealthLevel);
                    if (ds.child("giftCount").getValue() != null) user.giftCount = String.valueOf(ds.child("giftCount").getValue());
                    if (ds.child("followCount").getValue() != null) user.followCount = String.valueOf(ds.child("followCount").getValue());
                    if (ds.child("fansCount").getValue() != null) user.fansCount = String.valueOf(ds.child("fansCount").getValue());
                    if (ds.child("visitorCount").getValue() != null) user.visitorCount = String.valueOf(ds.child("visitorCount").getValue());
                    if (ds.child("familyName").getValue() != null) user.familyName = String.valueOf(ds.child("familyName").getValue());
                    if (ds.child("headwearCount").getValue() != null) user.headwearCount = String.valueOf(ds.child("headwearCount").getValue());
                    if (ds.child("headwearName").getValue() != null) headwearName = String.valueOf(ds.child("headwearName").getValue());
                    if (headwearName.isEmpty() && ds.child("headwear").getValue() != null) headwearName = String.valueOf(ds.child("headwear").getValue());
                    if (headwearName.isEmpty() && ds.child("HEADWEAR_NAME").getValue() != null) headwearName = String.valueOf(ds.child("HEADWEAR_NAME").getValue());
                    if (ds.child("headwearImage").getValue() != null) headwearImage = String.valueOf(ds.child("headwearImage").getValue());
                    if (headwearImage.isEmpty() && ds.child("headwearUrl").getValue() != null) headwearImage = String.valueOf(ds.child("headwearUrl").getValue());
                    if (headwearImage.isEmpty() && ds.child("headwearBase64").getValue() != null) headwearImage = String.valueOf(ds.child("headwearBase64").getValue());
                    if (headwearImage.isEmpty() && ds.child("HEADWEAR_IMAGE").getValue() != null) headwearImage = String.valueOf(ds.child("HEADWEAR_IMAGE").getValue());
                    if (ds.child("medalCount").getValue() != null) user.medalCount = String.valueOf(ds.child("medalCount").getValue());
                    if (ds.child("carCount").getValue() != null) user.carCount = String.valueOf(ds.child("carCount").getValue());
                    if (ds.child("supporterName").getValue() != null) user.supporterName = String.valueOf(ds.child("supporterName").getValue());
                    break;
                }
                if (user.name == null || user.name.isEmpty()) user.name = getCachedName();
                txtName.setText(user.name + "  " + user.getFlag() + "  " + user.getGenderIcon());
                txtId.setText("ID: " + targetId + "  •  Wealth Lv." + myWealthLevel + " • Charm Lv." + myCharmLevel + "  •  " + user.country + "  📋");
                updateLevelBadge();
                txtFollow.setText(user.followCount);
                txtFans.setText(user.fansCount);
                txtVisitor.setText(user.visitorCount);
                String finalPhoto = user.getPhotoUrl();
                if (finalPhoto.isEmpty()) finalPhoto = user.photoBase64;
                if (!finalPhoto.isEmpty()) {
                    showPhoto(finalPhoto, imgAvatar);
                    if (targetId.equals(myId)) prefs.edit().putString("USER_PHOTO", finalPhoto).putString("PROFILE_PHOTO", finalPhoto).apply();
                    else prefs.edit().putString("CACHED_PHOTO_" + targetId, finalPhoto).apply();
                }
                SharedPreferences.Editor ed = prefs.edit();
                if (targetId.equals(myId)) ed.putString("USER_NAME", user.name);
                else ed.putString("CACHED_NAME_" + targetId, user.name);
                ed.putString("CACHED_LEVEL_" + targetId, String.valueOf(myWealthLevel));
                ed.putString("CACHED_CHARM_" + targetId, String.valueOf(myCharmLevel));
                ed.putLong("CACHED_SENDING_" + targetId, myTotalSending);
                ed.putLong("CACHED_RECEIVING_" + targetId, myTotalReceiving);
                ed.putString("CACHED_FOLLOW_" + targetId, user.followCount);
                ed.putString("CACHED_FANS_" + targetId, user.fansCount);
                ed.putString("CACHED_VISITOR_" + targetId, user.visitorCount);
                ed.apply();
                if (!coverUrl.isEmpty()) showPhoto(coverUrl, imgCover);
                if (!headwearImage.isEmpty()) {
                    try {
                        if (user.headwearCount == null || user.headwearCount.equals("0") || user.headwearCount.isEmpty()) user.headwearCount = "1";
                        if (user.headwearName == null || user.headwearName.isEmpty()) user.headwearName = headwearName;
                        imgHeadwear.setVisibility(View.VISIBLE);
                        showPhoto(headwearImage, imgHeadwear);
                        if (targetId.equals(myId)) prefs.edit().putString("HEADWEAR_IMAGE", headwearImage).putString("HEADWEAR_NAME", headwearName).apply();
                    } catch (Exception ignored) {}
                }
                if (tabProfileContent != null) {
                    tabProfileContent.removeAllViews();
                    buildProfileTab();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        ref.orderByChild("my9DigitId").equalTo(targetId).addValueEventListener(userListener);
    }
}
