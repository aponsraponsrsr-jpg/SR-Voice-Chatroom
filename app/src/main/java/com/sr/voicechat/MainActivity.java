package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private DatabaseReference db;
    private ScrollView pageHome, pageMoment, pageInbox, pageMe;
    private LinearLayout homeRoomList, meMenuList;
    private ImageView meProfileImg;
    private TextView meNameText, meIdText, navHomeText, navMomentText, navInboxText, navMeText;
    private String myUid = "", my9DigitId = "", myName = "User", myPhotoBase64 = "", myCountry = "Bangladesh", myGender = "Male";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("SR_PREFS", MODE_PRIVATE);
        myUid = prefs.getString("USER_UID", "");
        my9DigitId = prefs.getString("USER_9DIGIT_ID", "");
        myName = prefs.getString("USER_NAME", "User");
        myPhotoBase64 = prefs.getString("USER_PHOTO_BASE64", "");
        myCountry = prefs.getString("USER_COUNTRY", "Bangladesh");
        myGender = prefs.getString("USER_GENDER", "Male");

        if (myUid.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db = FirebaseDatabase.getInstance().getReference();
        pageHome = findViewById(R.id.pageHome);
        pageMoment = findViewById(R.id.pageMoment);
        pageInbox = findViewById(R.id.pageInbox);
        pageMe = findViewById(R.id.pageMe);
        homeRoomList = findViewById(R.id.homeRoomList);
        meMenuList = findViewById(R.id.meMenuList);
        meProfileImg = findViewById(R.id.meProfileImg);
        meNameText = findViewById(R.id.meNameText);
        meIdText = findViewById(R.id.meIdText);
        navHomeText = findViewById(R.id.navHomeText);
        navMomentText = findViewById(R.id.navMomentText);
        navInboxText = findViewById(R.id.navInboxText);
        navMeText = findViewById(R.id.navMeText);

        // BOTTOM NAV - FIXED - KOKHONO SORBE NA - BACK BUTTON LAGBE NA
        findViewById(R.id.navHome).setOnClickListener(v -> showPage(0));
        findViewById(R.id.navMoment).setOnClickListener(v -> showPage(1));
        findViewById(R.id.navInbox).setOnClickListener(v -> showPage(2));
        findViewById(R.id.navMe).setOnClickListener(v -> showPage(3));

        // ME PROFILE CLICK - GOL PROFILE
        if (meProfileImg != null) meProfileImg.setOnClickListener(v -> Toast.makeText(this, "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show());

        // LOGOUT - SETTING ER NICHE - SCROLL KORE PABE
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        buildMeMenu();
        loadMyProfile();
        loadRooms();
        showPage(0);
    }

    // PAGE SWITCH - BOTTOM BAR SOB SOMOY THAKBE
    private void showPage(int index) {
        pageHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        pageMoment.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        pageInbox.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        pageMe.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        navHomeText.setTextColor(index == 0 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navMomentText.setTextColor(index == 1 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navInboxText.setTextColor(index == 2 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navMeText.setTextColor(index == 3 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navHomeText.setTypeface(null, index == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        navMomentText.setTypeface(null, index == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        navInboxText.setTypeface(null, index == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        navMeText.setTypeface(null, index == 3 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    // BACK BUTTON - HOME E JABE - APP BONDHO HOBE NA
    @Override
    public void onBackPressed() {
        if (pageMe.getVisibility() == View.VISIBLE || pageMoment.getVisibility() == View.VISIBLE || pageInbox.getVisibility() == View.VISIBLE) {
            showPage(0);
            return;
        }
        super.onBackPressed();
    }

    // ME MENU - DEMO SERIAL - SETTING ER PORE LOGOUT XML E ACHE
    private void buildMeMenu() {
        if (meMenuList == null) return;
        meMenuList.removeAllViews();
        String[][] items = {
            {"🎁", "Newbie mission"},
            {"👥", "Invite Friend"},
            {"🏅", "Medal"},
            {"👨‍👩‍👧", "Family"},
            {"🎙️", "Host Center"},
            {"📅", "Daily Tasks"},
            {"⚙️", "Setting"}
        };
        for (String[] it : items) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(16, 16, 16, 16);
            meMenuList.addView(row);
            TextView icon = new TextView(this);
            icon.setText(it[0]);
            icon.setTextSize(18);
            row.addView(icon);
            TextView label = new TextView(this);
            label.setText(it[1]);
            label.setTextColor(Color.parseColor("#222222"));
            label.setTextSize(14);
            label.setPadding(12, 0, 0, 0);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(label);
            TextView arrow = new TextView(this);
            arrow.setText(">");
            arrow.setTextColor(Color.parseColor("#AAAAAA"));
            arrow.setTextSize(16);
            row.addView(arrow);
            View line = new View(this);
            line.setBackgroundColor(Color.parseColor("#EEEEEE"));
            line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
            meMenuList.addView(line);
            row.setOnClickListener(v -> Toast.makeText(this, it[1] + " - Coming Soon", Toast.LENGTH_SHORT).show());
        }
    }

    // PROFILE LOAD - GOL KORE DEKHABE
    private void loadMyProfile() {
        if (meNameText != null) meNameText.setText(myName);
        if (meIdText != null) meIdText.setText("ID: " + (my9DigitId.isEmpty() ? myUid : my9DigitId) + "  Copy");
        Bitmap bmp = decodeBase64(myPhotoBase64);
        if (bmp != null && meProfileImg != null) {
            meProfileImg.setImageBitmap(bmp);
        }
        // Firebase theke latest profile
        if (!myUid.isEmpty()) {
            db.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot s) {
                    if (!s.exists()) return;
                    String n = s.child("name").getValue(String.class);
                    if (n != null && !n.isEmpty()) { myName = n; meNameText.setText(n); prefs.edit().putString("USER_NAME", n).apply(); }
                    String ph = s.child("photoBase64").getValue(String.class);
                    if (ph != null && !ph.isEmpty()) {
                        myPhotoBase64 = ph;
                        prefs.edit().putString("USER_PHOTO_BASE64", ph).apply();
                        Bitmap b2 = decodeBase64(ph);
                        if (b2 != null) meProfileImg.setImageBitmap(b2);
                    }
                    String id9 = s.child("my9DigitId").getValue(String.class);
                    if (id9 != null && !id9.isEmpty()) { my9DigitId = id9; meIdText.setText("ID: " + id9 + "  Copy"); }
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
    }

    // ROOM LIST - ROOM ER PROFILE O GOL
    private void loadRooms() {
        if (homeRoomList == null) return;
        db.child("rooms").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {
                homeRoomList.removeAllViews();
                if (!s.exists()) {
                    addDemoRoom("SR Apon's Room", "Welcome to my voice chat room!", "499506534");
                    addDemoRoom("SHAFIN's Room", "Welcome to my voice chat room!", "499506535");
                    return;
                }
                for (DataSnapshot room : s.getChildren()) {
                    String ownerId = room.getKey();
                    String rName = room.child("meta").child("roomName").getValue(String.class);
                    if (rName == null) rName = "Room " + ownerId;
                    String rPhoto = room.child("meta").child("roomPhotoBase64").getValue(String.class);
                    addRoomCard(rName, "Welcome to room!", ownerId, rPhoto);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addDemoRoom(String name, String desc, String ownerId) { addRoomCard(name, desc, ownerId, null); }

    private void addRoomCard(String name, String desc, String ownerId, String photoBase64) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#2A1B4A"));
        card.setPadding(12, 12, 12, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        card.setLayoutParams(lp);
        homeRoomList.addView(card);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top);

        // ROOM PROFILE - GOL
        ImageView roomImg = new ImageView(this);
        roomImg.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
        roomImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        roomImg.setBackgroundResource(R.drawable.bg_profile_gradient);
        roomImg.setPadding(2, 2, 2, 2);
        roomImg.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap bmp = decodeBase64(photoBase64);
        if (bmp != null) roomImg.setImageBitmap(bmp);
        top.addView(roomImg);

        LinearLayout txtBox = new LinearLayout(this);
        txtBox.setOrientation(LinearLayout.VERTICAL);
        txtBox.setPadding(12, 0, 0, 0);
        txtBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(txtBox);
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#FFD700"));
        tvName.setTextSize(15);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        txtBox.addView(tvName);
        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextColor(Color.parseColor("#AAAAAA"));
        tvDesc.setTextSize(12);
        txtBox.addView(tvDesc);

        TextView live = new TextView(this);
        live.setText("● LIVE");
        live.setTextColor(Color.RED);
        live.setTextSize(11);
        live.setTypeface(null, android.graphics.Typeface.BOLD);
        top.addView(live);

        card.setOnClickListener(v -> {
            Intent i = new Intent(this, VoiceRoomActivity.class);
            i.putExtra("ROOM_OWNER_ID", ownerId);
            i.putExtra("MY_9DIGIT_ID", my9DigitId);
            i.putExtra("MY_UID", myUid);
            startActivity(i);
        });
    }

    private Bitmap decodeBase64(String b64) {
        if (b64 == null || b64.isEmpty()) return null;
        try {
            byte[] b = Base64.decode(b64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } catch (Exception e) { return null; }
    }
}
