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

public class PublicProfileActivity extends AppCompatActivity {
    private String targetId = "", myId = "";
    private TextView txtName, txtId, txtFollow, txtFans, txtVisitor, txtLevelBadge;
    private ImageView imgAvatar, imgCover, imgHeadwear;
    private LinearLayout tabProfileContent, tabAlbumContent, tabMomentsContent, tabIntimacyContent;
    private TextView tabProfile, tabAlbum, tabMoments, tabIntimacy;
    private UserModel user = new UserModel();
    private SharedPreferences prefs;

    private BroadcastReceiver profileReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            loadUser();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = prefs.getString("MY_9_DIGIT_ID", "");
        targetId = getIntent().getStringExtra("TARGET_9DIGIT_ID");
        if (targetId == null || targetId.isEmpty()) targetId = myId;
        boolean isMine = targetId.equals(myId);

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

        // Avatar - round with white border
        LinearLayout avatarWrap = new LinearLayout(this);
        avatarWrap.setPadding(6, 6, 6, 6);
        GradientDrawable avBg = new GradientDrawable();
        avBg.setColor(Color.WHITE);
        avBg.setShape(GradientDrawable.OVAL);
        avatarWrap.setBackground(avBg);
        topRow.addView(avatarWrap);

        imgAvatar = new ImageView(this);
        imgAvatar.setLayoutParams(new LinearLayout.LayoutParams(170, 170));
        // FIX - Eitai asol dosh chilo - #DDDDDD Charkona Background ekhon Transparent
        imgAvatar.setBackgroundColor(Color.TRANSPARENT);
        imgAvatar.setPadding(0, 0, 0, 0);
        imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // FIX - ImageView nijei Gol hoye jabe, pichone ar charkona thakbe na
        imgAvatar.setClipToOutline(true);
        imgAvatar.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
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
        txtName.setText("Loading...");
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
        txtLevelBadge.setText("  Level 0  ");
        txtLevelBadge.setTextColor(Color.parseColor("#FFD700"));
        txtLevelBadge.setTextSize(12);
        txtLevelBadge.setTypeface(null, Typeface.BOLD);
        txtLevelBadge.setPadding(18, 8, 18, 8);
        GradientDrawable lbg = new GradientDrawable();
        lbg.setColor(Color.parseColor("#1A1A1A"));
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
        txtFollow = addCount(countCard, "0", "Follow", "#111111", "#888888");
        txtFans = addCount(countCard, "0", "Fans", "#111111", "#888888");
        txtVisitor = addCount(countCard, "0", "Visitor", "#111111", "#888888");

        if (!isMine) {
            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setPadding(0, 25, 0, 0);
            header.addView(btnRow);
            TextView btnFollow = makeBtn("+ Follow", "#FF9800");
            btnFollow.setOnClickListener(v -> Toast.makeText(this, "Followed! ❤️", Toast.LENGTH_SHORT).show());
            btnRow.addView(btnFollow);
            TextView btnGift = makeBtn("🎁 Gift", "#E91E63");
            btnGift.setOnClickListener(v -> Toast.makeText(this, "Gift - Room theke pathan", Toast.LENGTH_SHORT).show());
            btnRow.addView(btnGift);
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

        setContentView(scroll);
        try {
            IntentFilter filter = new IntentFilter("com.sr.voicechat.PROFILE_UPDATED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(profileReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(profileReceiver, filter);
            }
        } catch (Exception ignored) {}
        loadUser();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(profileReceiver); } catch (Exception ignored) {}
    }

    @Override protected void onResume() {
        super.onResume();
        loadUser();
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
        row1.addView(makeBadgeCard("🏷️", "Nameplate", "Level " + user.level, 1));
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
                // FIX - Age setImageBitmap diye charkona dekhato, ekhon Glide diye Gol
                if (bmp != null) Glide.with(this).load(bmp).circleCrop().into(iv);
            }
        } catch (Exception ignored) {}
    }

    private void loadUser() {
        String cachedPhoto = prefs.getString("USER_PHOTO", "");
        if (targetId.equals(myId) && !cachedPhoto.isEmpty()) {
            showPhoto(cachedPhoto, imgAvatar);
        }
        String cachedHeadwear = prefs.getString("HEADWEAR_IMAGE", "");
        if (cachedHeadwear.isEmpty()) cachedHeadwear = prefs.getString("headwearImage", "");
        if (cachedHeadwear.isEmpty()) cachedHeadwear = prefs.getString("HEADWEAR_URL", "");
        if (!cachedHeadwear.isEmpty() && imgHeadwear != null) {
            imgHeadwear.setVisibility(View.VISIBLE);
            showPhoto(cachedHeadwear, imgHeadwear);
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.orderByChild("my9DigitId").equalTo(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    if (ds.child("level").getValue() != null) user.level = String.valueOf(ds.child("level").getValue());
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
                txtName.setText(user.name + "  " + user.getFlag() + "  " + user.getGenderIcon());
                txtId.setText("ID: " + targetId + "  •  Level " + user.level + "  •  " + user.country + "  📋");
                if (txtLevelBadge != null) txtLevelBadge.setText("  Level " + user.level + "  ");
                txtFollow.setText(user.followCount);
                txtFans.setText(user.fansCount);
                txtVisitor.setText(user.visitorCount);
                String finalPhoto = user.getPhotoUrl();
                if (finalPhoto.isEmpty()) finalPhoto = user.photoBase64;
                if (!finalPhoto.isEmpty()) {
                    showPhoto(finalPhoto, imgAvatar);
                    if (targetId.equals(myId)) prefs.edit().putString("USER_PHOTO", finalPhoto).putString("PROFILE_PHOTO", finalPhoto).apply();
                }
                if (!coverUrl.isEmpty()) {
                    showPhoto(coverUrl, imgCover);
                }
                if (!headwearImage.isEmpty()) {
                    try {
                        if (user.headwearCount == null || user.headwearCount.equals("0") || user.headwearCount.isEmpty()) user.headwearCount = "1";
                        if (user.headwearName == null || user.headwearName.isEmpty()) user.headwearName = headwearName;
                        imgHeadwear.setVisibility(View.VISIBLE);
                        showPhoto(headwearImage, imgHeadwear);
                        if (targetId.equals(myId)) prefs.edit().putString("HEADWEAR_IMAGE", headwearImage).putString("HEADWEAR_NAME", headwearName).apply();
                    } catch (Exception ignored) {}
                }
                tabProfileContent.removeAllViews();
                buildProfileTab();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
