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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MeActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView txtName, txtId, txtGenderCountry, txtFollow, txtFans, txtVisitor;
    private SharedPreferences prefs;
    private String firebaseUid = "";
    private String my9DigitId = "";
    private DatabaseReference userRef;
    private String followCount = "0", fansCount = "0", visitorCount = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) firebaseUid = user.getUid();
        if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");
        my9DigitId = prefs.getString("MY_9_DIGIT_ID", "");
        if (!firebaseUid.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
        }

        // ===== ROOT - ScrollView (সম্পূর্ণ পেজ স্মুথলি স্ক্রোল হবে, হেডার ফিক্সড থাকবে না) =====
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);

        // ===== TOP HEADER PROFILE SECTION (ডার্ক প্রিমিয়াম ব্যাকগ্রাউন্ড ও গোল অবতার) =====
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(30, 40, 30, 25);
        header.setBackgroundColor(Color.parseColor("#1B1035"));
        root.addView(header);

        // Top Row - Avatar + Name + Profile Arrow ( > )
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(topRow);

        // 🌟 গোল প্রোফাইল পিকচারের জন্য CardView (CornerRadius 35dp)
        CardView cardAvatar = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(160, 160);
        cardParams.setMargins(0, 0, 25, 0);
        cardAvatar.setLayoutParams(cardParams);
        cardAvatar.setRadius(80); // গোল করার জন্য রেডিয়াস
        cardAvatar.setCardElevation(4);
        cardAvatar.setCardBackgroundColor(Color.TRANSPARENT);
        cardAvatar.setOnClickListener(v -> goProfile());

        imgAvatar = new ImageView(this);
        imgAvatar.setImageResource(android.R.drawable.ic_menu_my_calendar);
        imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        cardAvatar.addView(imgAvatar);
        topRow.addView(cardAvatar);

        LinearLayout nameBox = new LinearLayout(this);
        nameBox.setOrientation(LinearLayout.VERTICAL);
        nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        nameBox.setOnClickListener(v -> goProfile());
        topRow.addView(nameBox);

        txtName = new TextView(this);
        txtName.setText(prefs.getString("USER_NAME", "User"));
        txtName.setTextColor(Color.WHITE);
        txtName.setTextSize(18);
        txtName.setTypeface(null, Typeface.BOLD);
        nameBox.addView(txtName);

        LinearLayout idRow = new LinearLayout(this);
        idRow.setOrientation(LinearLayout.HORIZONTAL);
        idRow.setGravity(Gravity.CENTER_VERTICAL);
        nameBox.addView(idRow);

        txtId = new TextView(this);
        txtId.setText("ID: " + my9DigitId);
        txtId.setTextColor(Color.parseColor("#00E676"));
        txtId.setTextSize(13);
        idRow.addView(txtId);

        TextView txtCopy = new TextView(this);
        txtCopy.setText("  📋 Copy");
        txtCopy.setTextColor(Color.parseColor("#FFD700"));
        txtCopy.setTextSize(13);
        txtCopy.setTypeface(null, Typeface.BOLD);
        txtCopy.setOnClickListener(v -> {
            if (my9DigitId.isEmpty()) return;
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("ID", my9DigitId));
            Toast.makeText(this, "ID Copied: " + my9DigitId, Toast.LENGTH_SHORT).show();
        });
        idRow.addView(txtCopy);

        txtGenderCountry = new TextView(this);
        txtGenderCountry.setText(getGenderCountryText());
        txtGenderCountry.setTextColor(Color.parseColor("#B0BEC5"));
        txtGenderCountry.setTextSize(12);
        nameBox.addView(txtGenderCountry);

        TextView btnProfileArrow = new TextView(this);
        btnProfileArrow.setText(">");
        btnProfileArrow.setTextSize(24);
        btnProfileArrow.setTypeface(null, Typeface.BOLD);
        btnProfileArrow.setTextColor(Color.parseColor("#B0BEC5"));
        btnProfileArrow.setPadding(20, 20, 20, 20);
        btnProfileArrow.setOnClickListener(v -> goProfile());
        topRow.addView(btnProfileArrow);

        // ===== Copy ID Full Row =====
        LinearLayout copyIdBar = new LinearLayout(this);
        copyIdBar.setOrientation(LinearLayout.HORIZONTAL);
        copyIdBar.setBackgroundColor(Color.parseColor("#140B26"));
        copyIdBar.setPadding(30, 15, 30, 15);
        copyIdBar.setOnClickListener(v -> {
            if (my9DigitId.isEmpty()) return;
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("ID", my9DigitId));
            Toast.makeText(this, "ID Copied: " + my9DigitId, Toast.LENGTH_SHORT).show();
        });
        TextView tvCopyTitle = new TextView(this);
        tvCopyTitle.setText("Copy ID");
        tvCopyTitle.setTextColor(Color.parseColor("#FFD700"));
        tvCopyTitle.setTextSize(14);
        tvCopyTitle.setTypeface(null, Typeface.BOLD);
        copyIdBar.addView(tvCopyTitle);
        root.addView(copyIdBar);

        // ===== Stats Bar (Follow / Fans / Visitor) =====
        LinearLayout countRow = new LinearLayout(this);
        countRow.setOrientation(LinearLayout.HORIZONTAL);
        countRow.setGravity(Gravity.CENTER);
        countRow.setBackgroundColor(Color.parseColor("#1B1035"));
        countRow.setPadding(15, 20, 15, 20);
        root.addView(countRow);

        txtFollow = addCountItem(countRow, "0", "Follow", () -> showCountDialog("Follow", followCount));
        txtFans = addCountItem(countRow, "0", "Fans", () -> showCountDialog("Fans", fansCount));
        txtVisitor = addCountItem(countRow, "0", "Visitor", () -> showCountDialog("Visitor", visitorCount));

        // ===== Scrollable Menu Items Container =====
        LinearLayout menuContainer = new LinearLayout(this);
        menuContainer.setOrientation(LinearLayout.VERTICAL);
        menuContainer.setPadding(20, 20, 20, 30);
        root.addView(menuContainer);

        // SVIP Center
        addMenuCard(menuContainer, "👑 SVIP Center", "Get SVIP and enjoy privileges", () -> Toast.makeText(this, "SVIP Locked - Coming Soon", Toast.LENGTH_SHORT).show());
        
        // VIP & Recharge Row
        LinearLayout vipRow = new LinearLayout(this);
        vipRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams vipRowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vipRowLp.setMargins(0, 10, 0, 10);
        vipRow.setLayoutParams(vipRowLp);
        menuContainer.addView(vipRow);

        LinearLayout vipBox = makeSmallCard("👑 VIP", "Check now >", "#2A1B4E");
        vipBox.setOnClickListener(v -> Toast.makeText(this, "VIP - Coming Soon", Toast.LENGTH_SHORT).show());
        LinearLayout.LayoutParams vipLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        vipLp.setMargins(0, 0, 10, 0);
        vipBox.setLayoutParams(vipLp);
        vipRow.addView(vipBox);

        LinearLayout rechargeBox = makeSmallCard("🎁 Recharge", "Top up >", "#2A1B4E");
        rechargeBox.setOnClickListener(v -> Toast.makeText(this, "Recharge - Coming Soon", Toast.LENGTH_SHORT).show());
        LinearLayout.LayoutParams recLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        recLp.setMargins(10, 0, 0, 0);
        rechargeBox.setLayoutParams(recLp);
        vipRow.addView(rechargeBox);

        // General Tools Title
        TextView toolsTitle = new TextView(this);
        toolsTitle.setText("General Tools");
        toolsTitle.setTextColor(Color.parseColor("#B0BEC5"));
        toolsTitle.setTextSize(14);
        toolsTitle.setTypeface(null, Typeface.BOLD);
        toolsTitle.setPadding(10, 15, 10, 10);
        menuContainer.addView(toolsTitle);

        LinearLayout toolsRow = new LinearLayout(this);
        toolsRow.setOrientation(LinearLayout.HORIZONTAL);
        toolsRow.setGravity(Gravity.CENTER);
        toolsRow.setBackgroundColor(Color.parseColor("#1B1035"));
        toolsRow.setPadding(15, 20, 15, 20);
        LinearLayout.LayoutParams toolsLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toolsLp.setMargins(0, 0, 0, 15);
        toolsRow.setLayoutParams(toolsLp);
        menuContainer.addView(toolsRow);

        addToolItem(toolsRow, "🛍️", "Store");
        addToolItem(toolsRow, "👗", "Dress");
        addToolItem(toolsRow, "🎂", "Birthday");
        addToolItem(toolsRow, "📊", "Level");

        // List Menu Items (Arrow > সহ)
        LinearLayout listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setBackgroundColor(Color.parseColor("#1B1035"));
        GradientDrawable listBg = new GradientDrawable();
        listBg.setColor(Color.parseColor("#1B1035"));
        listBg.setCornerRadius(20);
        listBox.setBackground(listBg);
        menuContainer.addView(listBox);

        addMenuRow(listBox, "🎁 Newbie mission");
        addMenuRow(listBox, "👥 Invite Friend");
        addMenuRow(listBox, "🏅 Medal");
        addMenuRow(listBox, "👨‍👩‍👧 Family");
        addMenuRow(listBox, "🎤 Host Center");
        addMenuRow(listBox, "📅 Daily Tasks");
        addMenuRow(listBox, "⚙️ Setting");

        // ===== Logout Button (সেটিংস ও অন্যান্য মেনুর ঠিক নিচে প্রিমিয়াম কার্ড স্টাইলে) =====
        LinearLayout logoutLayout = new LinearLayout(this);
        logoutLayout.setOrientation(LinearLayout.HORIZONTAL);
        logoutLayout.setGravity(Gravity.CENTER);
        logoutLayout.setBackgroundColor(Color.parseColor("#2A1B4E"));
        logoutLayout.setPadding(20, 25, 20, 25);
        GradientDrawable logoutBg = new GradientDrawable();
        logoutBg.setColor(Color.parseColor("#2A1B4E"));
        logoutBg.setCornerRadius(20);
        logoutLayout.setBackground(logoutBg);
        
        LinearLayout.LayoutParams logoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        logoutLp.setMargins(0, 25, 0, 40);
        logoutLayout.setLayoutParams(logoutLp);

        TextView btnLogout = new TextView(this);
        btnLogout.setText("🚪 Log Out");
        btnLogout.setTextColor(Color.parseColor("#FF4081"));
        btnLogout.setTextSize(16);
        btnLogout.setTypeface(null, Typeface.BOLD);
        logoutLayout.addView(btnLogout);

        logoutLayout.setOnClickListener(v -> {
            prefs.edit().putBoolean("IS_LOGGED_IN", false).apply();
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(MeActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
        menuContainer.addView(logoutLayout);

        setContentView(scroll);
        showFromPrefs();
        loadMeData();
    }

    private TextView addCountItem(LinearLayout parent, String num, String label, Runnable onClick) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(v -> onClick.run());
        parent.addView(box);
        TextView tvNum = new TextView(this);
        tvNum.setText(num);
        tvNum.setTextColor(Color.WHITE);
        tvNum.setTextSize(16);
        tvNum.setTypeface(null, Typeface.BOLD);
        box.addView(tvNum);
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#B0BEC5"));
        tvLabel.setTextSize(12);
        box.addView(tvLabel);
        return tvNum;
    }

    private LinearLayout makeSmallCard(String title, String sub, String bgColor) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(25, 20, 25, 20);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(20);
        box.setBackground(bg);
        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextColor(Color.WHITE);
        t1.setTextSize(14);
        t1.setTypeface(null, Typeface.BOLD);
        box.addView(t1);
        TextView t2 = new TextView(this);
        t2.setText(sub);
        t2.setTextColor(Color.parseColor("#B0BEC5"));
        t2.setTextSize(11);
        box.addView(t2);
        return box;
    }

    private void addMenuCard(LinearLayout parent, String title, String sub, Runnable onClick) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(25, 20, 25, 20);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#2A1B4E"));
        bg.setCornerRadius(20);
        box.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        box.setLayoutParams(lp);
        box.setOnClickListener(v -> onClick.run());

        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextColor(Color.parseColor("#FFD700"));
        t1.setTextSize(15);
        t1.setTypeface(null, Typeface.BOLD);
        box.addView(t1);

        TextView t2 = new TextView(this);
        t2.setText(sub);
        t2.setTextColor(Color.parseColor("#B0BEC5"));
        t2.setTextSize(12);
        box.addView(t2);
        parent.addView(box);
    }

    private void addToolItem(LinearLayout parent, String icon, String label) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(v -> Toast.makeText(this, label + " - Coming Soon", Toast.LENGTH_SHORT).show());
        parent.addView(box);
        TextView iv = new TextView(this);
        iv.setText(icon);
        iv.setTextSize(22);
        iv.setGravity(Gravity.CENTER);
        box.addView(iv);
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(12);
        tv.setGravity(Gravity.CENTER);
        box.addView(tv);
    }

    private void addMenuRow(LinearLayout parent, String title) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(30, 25, 30, 25);
        row.setOnClickListener(v -> Toast.makeText(this, title + " - Coming Soon", Toast.LENGTH_SHORT).show());
        parent.addView(row);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.WHITE);
        t.setTextSize(14);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(t);
        TextView arrow = new TextView(this);
        arrow.setText(">");
        arrow.setTextColor(Color.parseColor("#B0BEC5"));
        arrow.setTextSize(16);
        arrow.setTypeface(null, Typeface.BOLD);
        row.addView(arrow);
    }

    private void goProfile() {
        try {
            Intent i = new Intent(MeActivity.this, PublicProfileActivity.class);
            i.putExtra("TARGET_9DIGIT_ID", my9DigitId);
            i.putExtra("TARGET_NAME", prefs.getString("USER_NAME", "User"));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCountDialog(String type, String count) {
        new AlertDialog.Builder(this)
            .setTitle(type + " - " + count)
            .setMessage("Apnar " + type + " list ekhane asbe.\n\nEita sudhu apni dekhte parben.")
            .setPositiveButton("OK", null)
            .show();
    }

    private String getGenderCountryText() {
        String g = prefs.getString("USER_GENDER", "");
        String c = prefs.getString("USER_COUNTRY", "");
        if (!g.isEmpty() && !c.isEmpty()) return g + " | " + c;
        if (!c.isEmpty()) return c;
        if (!g.isEmpty()) return g;
        return "Complete Your Profile";
    }

    @Override
    protected void onResume() {
        super.onResume();
        showFromPrefs();
        loadMeData();
    }

    private void showFromPrefs() {
        String name = prefs.getString("USER_NAME", "User");
        String photoBase64 = prefs.getString("USER_PHOTO", "");
        my9DigitId = prefs.getString("MY_9_DIGIT_ID", my9DigitId);
        if (txtName != null) txtName.setText(name);
        if (txtId != null) txtId.setText("ID: " + my9DigitId);
        if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
        showBase64Image(photoBase64, imgAvatar);
    }

    private void loadMeData() {
        if (userRef == null) return;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                try {
                    if (!snap.exists()) return;
                    String name = getStr(snap, "name", prefs.getString("USER_NAME", "User"));
                    String id = getStr(snap, "my9DigitId", my9DigitId);
                    String country = getStr(snap, "country", prefs.getString("USER_COUNTRY", ""));
                    String gender = getStr(snap, "gender", prefs.getString("USER_GENDER", ""));
                    String photoBase64 = getStr(snap, "photoBase64", "");
                    if (photoBase64.isEmpty()) photoBase64 = getStr(snap, "photoUrl", "");
                    followCount = getStr(snap, "followCount", "0");
                    fansCount = getStr(snap, "fansCount", "0");
                    visitorCount = getStr(snap, "visitorCount", "0");

                    if (!id.isEmpty()) {
                        my9DigitId = id;
                        prefs.edit().putString("MY_9_DIGIT_ID", id).apply();
                    }
                    prefs.edit()
                        .putString("USER_NAME", name)
                        .putString("USER_COUNTRY", country)
                        .putString("USER_GENDER", gender)
                        .putString("USER_PHOTO", photoBase64)
                        .apply();

                    if (txtName != null) txtName.setText(name);
                    if (txtId != null) txtId.setText("ID: " + my9DigitId);
                    if (txtGenderCountry != null) txtGenderCountry.setText(gender + " | " + country);
                    if (txtFollow != null) txtFollow.setText(followCount);
                    if (txtFans != null) txtFans.setText(fansCount);
                    if (txtVisitor != null) txtVisitor.setText(visitorCount);
                    showBase64Image(photoBase64, imgAvatar);
                } catch (Exception ignored) {}
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showBase64Image(String base64, ImageView iv) {
        if (iv == null || base64 == null || base64.isEmpty()) return;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 2;
            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
            if (b != null) iv.setImageBitmap(b);
        } catch (Exception ignored) {}
    }

    private String getStr(DataSnapshot s, String k, String def) {
        if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue());
        return def;
    }
}
