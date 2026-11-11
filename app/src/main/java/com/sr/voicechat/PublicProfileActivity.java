package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PublicProfileActivity extends AppCompatActivity {
    private String targetId = "", myId = "";
    private TextView txtName, txtId, txtFollow, txtFans;
    private ImageView imgAvatar;
    private LinearLayout tabProfileContent, tabAlbumContent, tabMomentsContent, tabIntimacyContent;
    private TextView tabProfile, tabAlbum, tabMoments, tabIntimacy;
    private UserModel user = new UserModel();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = prefs.getString("MY_9_DIGIT_ID", "");
        targetId = getIntent().getStringExtra("TARGET_9DIGIT_ID");
        if (targetId == null || targetId.isEmpty()) targetId = myId;

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#F5F0D8"));
        scroll.addView(root);

        // ===== HEADER - Green + Ghiya - Demo er moto =====
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(40, 40, 40, 30);
        header.setBackgroundColor(Color.parseColor("#C9E8A8"));
        root.addView(header);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(topRow);

        imgAvatar = new ImageView(this);
        imgAvatar.setLayoutParams(new LinearLayout.LayoutParams(170, 170));
        imgAvatar.setImageResource(android.R.drawable.ic_menu_camera);
        topRow.addView(imgAvatar);

        LinearLayout nameBox = new LinearLayout(this);
        nameBox.setOrientation(LinearLayout.VERTICAL);
        nameBox.setPadding(30, 0, 0, 0);
        nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(nameBox);

        txtName = new TextView(this);
        txtName.setText("Loading...");
        txtName.setTextSize(20);
        txtName.setTypeface(null, Typeface.BOLD);
        txtName.setTextColor(Color.parseColor("#222222"));
        nameBox.addView(txtName);

        txtId = new TextView(this);
        txtId.setText("ID: " + targetId + "  📋 Tap to Copy");
        txtId.setTextColor(Color.parseColor("#555555"));
        txtId.setTextSize(13);
        txtId.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("ID", targetId));
            Toast.makeText(this, "ID Copied", Toast.LENGTH_SHORT).show();
        });
        nameBox.addView(txtId);

        LinearLayout countRow = new LinearLayout(this);
        countRow.setOrientation(LinearLayout.HORIZONTAL);
        countRow.setPadding(0, 25, 0, 0);
        header.addView(countRow);
        txtFollow = addCount(countRow, "0", "Follow");
        txtFans = addCount(countRow, "0", "Fans");

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 25, 0, 0);
        header.addView(btnRow);

        TextView btnFollow = makeBtn(targetId.equals(myId) ? "✏️ Edit Profile" : "+ Follow", "#FF9800");
        btnFollow.setOnClickListener(v -> {
            if (targetId.equals(myId)) {
                startActivity(new Intent(this, EditProfileActivity.class));
            } else {
                Toast.makeText(this, "Followed! ❤️", Toast.LENGTH_SHORT).show();
            }
        });
        btnRow.addView(btnFollow);
        TextView btnGift = makeBtn("🎁 Gift", "#E91E63");
        btnGift.setOnClickListener(v -> Toast.makeText(this, "Gift - Room theke pathan", Toast.LENGTH_SHORT).show());
        btnRow.addView(btnGift);

        // ===== TABS =====
        LinearLayout tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabRow.setBackgroundColor(Color.WHITE);
        tabRow.setPadding(10, 10, 10, 10);
        root.addView(tabRow);
        tabProfile = makeTab(tabRow, "Profile", true);
        tabAlbum = makeTab(tabRow, "Photo album", false);
        tabMoments = makeTab(tabRow, "Moments", false);
        tabIntimacy = makeTab(tabRow, "Intimacy", false);

        tabProfileContent = new LinearLayout(this);
        tabProfileContent.setOrientation(LinearLayout.VERTICAL);
        tabProfileContent.setPadding(30, 20, 30, 20);
        root.addView(tabProfileContent);

        tabAlbumContent = new LinearLayout(this);
        tabAlbumContent.setOrientation(LinearLayout.VERTICAL);
        tabAlbumContent.setPadding(30, 20, 30, 20);
        tabAlbumContent.setVisibility(View.GONE);
        root.addView(tabAlbumContent);

        tabMomentsContent = new LinearLayout(this);
        tabMomentsContent.setOrientation(LinearLayout.VERTICAL);
        tabMomentsContent.setPadding(30, 20, 30, 20);
        tabMomentsContent.setVisibility(View.GONE);
        root.addView(tabMomentsContent);

        tabIntimacyContent = new LinearLayout(this);
        tabIntimacyContent.setOrientation(LinearLayout.VERTICAL);
        tabIntimacyContent.setPadding(30, 20, 30, 20);
        tabIntimacyContent.setVisibility(View.GONE);
        root.addView(tabIntimacyContent);

        buildProfileTab();
        buildAlbumTab();
        buildMomentsTab();
        buildIntimacyTab();

        setContentView(scroll);
        loadUser();
    }

    private void buildProfileTab() {
        addProfileRow(tabProfileContent, "🏷️ Nameplate", "Level " + user.level);
        addProfileRow(tabProfileContent, "💎 Supporter", user.supporterName.isEmpty() ? "No supporter yet" : user.supporterName);
        addProfileRow(tabProfileContent, "🏅 Medal (" + user.medalCount + ")", "View Medals >");
        addProfileRow(tabProfileContent, "👨‍👩‍👧 Family", user.getFamilyDisplay());
        addProfileRow(tabProfileContent, "🎩 Headwear (" + user.headwearCount + ")", "View >");
        addProfileRow(tabProfileContent, "🚗 Car (" + user.carCount + ")", "View >");
        addProfileRow(tabProfileContent, "🎁 Gift (" + user.giftCount + ")", "All gifts - Dollar is Private");
    }

    private void buildAlbumTab() {
        TextView t = new TextView(this);
        t.setText("+ Upload Photo\n\nNo photos yet");
        t.setTextColor(Color.parseColor("#888888"));
        t.setTextSize(14);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 30, 0, 30);
        t.setOnClickListener(v -> Toast.makeText(this, "Photo Upload - Next Step", Toast.LENGTH_SHORT).show());
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
        tv.setTextColor(active ? Color.parseColor("#4CAF50") : Color.parseColor("#888888"));
        tv.setOnClickListener(v -> selectTab(title));
        parent.addView(tv);
        return tv;
    }

    private void selectTab(String title) {
        tabProfileContent.setVisibility(title.equals("Profile") ? View.VISIBLE : View.GONE);
        tabAlbumContent.setVisibility(title.equals("Photo album") ? View.VISIBLE : View.GONE);
        tabMomentsContent.setVisibility(title.equals("Moments") ? View.VISIBLE : View.GONE);
        tabIntimacyContent.setVisibility(title.equals("Intimacy") ? View.VISIBLE : View.GONE);
        tabProfile.setTextColor(title.equals("Profile") ? Color.parseColor("#4CAF50") : Color.parseColor("#888888"));
        tabAlbum.setTextColor(title.equals("Photo album") ? Color.parseColor("#4CAF50") : Color.parseColor("#888888"));
        tabMoments.setTextColor(title.equals("Moments") ? Color.parseColor("#4CAF50") : Color.parseColor("#888888"));
        tabIntimacy.setTextColor(title.equals("Intimacy") ? Color.parseColor("#4CAF50") : Color.parseColor("#888888"));
    }

    private TextView addCount(LinearLayout parent, String num, String label) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        parent.addView(box);
        TextView tvNum = new TextView(this);
        tvNum.setText(num);
        tvNum.setTextSize(16);
        tvNum.setTypeface(null, Typeface.BOLD);
        tvNum.setGravity(Gravity.CENTER);
        box.addView(tvNum);
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(12);
        tvLabel.setTextColor(Color.parseColor("#666666"));
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

    private void loadUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.orderByChild("my9DigitId").equalTo(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                for (DataSnapshot ds : snap.getChildren()) {
                    if (ds.child("name").getValue() != null) user.name = String.valueOf(ds.child("name").getValue());
                    if (ds.child("gender").getValue() != null) user.gender = String.valueOf(ds.child("gender").getValue());
                    if (ds.child("country").getValue() != null) user.country = String.valueOf(ds.child("country").getValue());
                    if (ds.child("photoBase64").getValue() != null) user.photoBase64 = String.valueOf(ds.child("photoBase64").getValue());
                    if (ds.child("level").getValue() != null) user.level = String.valueOf(ds.child("level").getValue());
                    if (ds.child("giftCount").getValue() != null) user.giftCount = String.valueOf(ds.child("giftCount").getValue());
                    if (ds.child("followCount").getValue() != null) user.followCount = String.valueOf(ds.child("followCount").getValue());
                    if (ds.child("fansCount").getValue() != null) user.fansCount = String.valueOf(ds.child("fansCount").getValue());
                    if (ds.child("familyName").getValue() != null) user.familyName = String.valueOf(ds.child("familyName").getValue());
                    break;
                }
                txtName.setText(user.name + "  " + user.getFlag() + "  " + user.getGenderIcon());
                txtId.setText("ID: " + targetId + "  •  Level " + user.level + "  •  " + user.country + "  📋");
                txtFollow.setText(user.followCount);
                txtFans.setText(user.fansCount);
                if (!user.photoBase64.isEmpty()) {
                    try {
                        byte[] bytes = Base64.decode(user.photoBase64, Base64.DEFAULT);
                        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (b != null) imgAvatar.setImageBitmap(b);
                    } catch (Exception ignored) {}
                }
                tabProfileContent.removeAllViews();
                buildProfileTab();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
