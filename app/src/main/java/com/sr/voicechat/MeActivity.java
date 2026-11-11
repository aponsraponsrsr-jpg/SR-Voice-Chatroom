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

        // ===== ROOT - Scroll =====
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#F5F0D8")); // Halka Ghiya / Off-White - Video er moto
        scroll.addView(root);

        // ===== TOP HEADER - Halka Deep Green =====
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(40, 50, 40, 30);
        GradientDrawable headerBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#C9E8A8"), Color.parseColor("#E9F5C8")});
        headerBg.setCornerRadius(0);
        header.setBackground(headerBg);
        root.addView(header);

        // Top Row - Avatar + Name + Pencil
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(topRow);

        imgAvatar = new ImageView(this);
        imgAvatar.setImageResource(android.R.drawable.ic_menu_camera);
        imgAvatar.setBackgroundColor(Color.parseColor("#FFFFFF"));
        imgAvatar.setPadding(6, 6, 6, 6);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(170, 170);
        avatarParams.setMargins(0, 0, 30, 0);
        imgAvatar.setLayoutParams(avatarParams);
        imgAvatar.setOnClickListener(v -> goEdit());
        topRow.addView(imgAvatar);

        LinearLayout nameBox = new LinearLayout(this);
        nameBox.setOrientation(LinearLayout.VERTICAL);
        nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(nameBox);

        txtName = new TextView(this);
        txtName.setText(prefs.getString("USER_NAME", "User"));
        txtName.setTextColor(Color.parseColor("#222222"));
        txtName.setTextSize(20);
        txtName.setTypeface(null, Typeface.BOLD);
        nameBox.addView(txtName);

        LinearLayout idRow = new LinearLayout(this);
        idRow.setOrientation(LinearLayout.HORIZONTAL);
        idRow.setGravity(Gravity.CENTER_VERTICAL);
        nameBox.addView(idRow);

        txtId = new TextView(this);
        txtId.setText("ID: " + my9DigitId);
        txtId.setTextColor(Color.parseColor("#555555"));
        txtId.setTextSize(13);
        idRow.addView(txtId);

        TextView txtCopy = new TextView(this);
        txtCopy.setText("  📋 Copy");
        txtCopy.setTextColor(Color.parseColor("#4CAF50"));
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
        txtGenderCountry.setTextColor(Color.parseColor("#666666"));
        txtGenderCountry.setTextSize(13);
        nameBox.addView(txtGenderCountry);

        TextView btnPencil = new TextView(this);
        btnPencil.setText("✏️");
        btnPencil.setTextSize(22);
        btnPencil.setPadding(20, 20, 20, 20);
        btnPencil.setOnClickListener(v -> goEdit());
        topRow.addView(btnPencil);

        // Follow / Fans / Visitor - SACHOL - Sudhu apni dekhben
        LinearLayout countRow = new LinearLayout(this);
        countRow.setOrientation(LinearLayout.HORIZONTAL);
        countRow.setGravity(Gravity.CENTER);
        countRow.setPadding(0, 30, 0, 10);
        header.addView(countRow);

        txtFollow = addCountItem(countRow, "0", "Follow", () -> showCountDialog("Follow", followCount));
        txtFans = addCountItem(countRow, "0", "Fans", () -> showCountDialog("Fans", fansCount));
        txtVisitor = addCountItem(countRow, "0", "Visitor", () -> showCountDialog("Visitor", visitorCount));

        // ===== SVIP CENTER - LOCKED =====
        LinearLayout svip = new LinearLayout(this);
        svip.setOrientation(LinearLayout.HORIZONTAL);
        svip.setGravity(Gravity.CENTER_VERTICAL);
        svip.setPadding(30, 25, 30, 25);
        GradientDrawable svipBg = new GradientDrawable();
        svipBg.setColor(Color.parseColor("#3A2A00"));
        svipBg.setCornerRadius(30);
        svip.setBackground(svipBg);
        LinearLayout.LayoutParams svipParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        svipParams.setMargins(30, 25, 30, 15);
        svip.setLayoutParams(svipParams);
        svip.setOnClickListener(v -> Toast.makeText(this, "🔒 SVIP Locked - Coming Soon", Toast.LENGTH_SHORT).show());
        root.addView(svip);

        TextView svipIcon = new TextView(this);
        svipIcon.setText("👑 SVIP");
        svipIcon.setTextColor(Color.parseColor("#FFD700"));
        svipIcon.setTextSize(16);
        svipIcon.setTypeface(null, Typeface.BOLD);
        svip.addView(svipIcon);

        TextView svipSub = new TextView(this);
        svipSub.setText("  SVIP Center - Get SVIP and enjoy various privileges >");
        svipSub.setTextColor(Color.parseColor("#FFE9A8"));
        svipSub.setTextSize(12);
        svipSub.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        svip.addView(svipSub);

        // ===== VIP / Recharge =====
        LinearLayout vipRow = new LinearLayout(this);
        vipRow.setOrientation(LinearLayout.HORIZONTAL);
        vipRow.setPadding(30, 0, 30, 0);
        root.addView(vipRow);

        LinearLayout vipBox = makeSmallCard("👑 VIP", "check now >", "#FFF0D6");
        vipBox.setOnClickListener(v -> Toast.makeText(this, "VIP - Coming Soon", Toast.LENGTH_SHORT).show());
        LinearLayout.LayoutParams vipLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        vipLp.setMargins(0, 0, 15, 0);
        vipBox.setLayoutParams(vipLp);
        vipRow.addView(vipBox);

        LinearLayout rechargeBox = makeSmallCard("🎁 Recharge", "Top up >", "#FFE4EC");
        rechargeBox.setOnClickListener(v -> Toast.makeText(this, "Recharge - Coming Soon", Toast.LENGTH_SHORT).show());
        LinearLayout.LayoutParams recLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        recLp.setMargins(15, 0, 0, 0);
        rechargeBox.setLayoutParams(recLp);
        vipRow.addView(rechargeBox);

        // ===== General Tools =====
        TextView toolsTitle = new TextView(this);
        toolsTitle.setText("General Tools");
        toolsTitle.setTextColor(Color.parseColor("#333333"));
        toolsTitle.setTextSize(15);
        toolsTitle.setTypeface(null, Typeface.BOLD);
        toolsTitle.setPadding(40, 30, 0, 15);
        root.addView(toolsTitle);

        LinearLayout toolsRow = new LinearLayout(this);
        toolsRow.setOrientation(LinearLayout.HORIZONTAL);
        toolsRow.setGravity(Gravity.CENTER);
        toolsRow.setPadding(20, 0, 20, 10);
        root.addView(toolsRow);

        addToolItem(toolsRow, "🛍️", "Store");
        addToolItem(toolsRow, "👗", "Dress");
        addToolItem(toolsRow, "🎂", "Birthday gift");
        addToolItem(toolsRow, "📊", "Level");

        // ===== List Menu - Arrow > Soho =====
        LinearLayout listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        listLp.setMargins(30, 20, 30, 20);
        listBox.setLayoutParams(listLp);
        GradientDrawable listBg = new GradientDrawable();
        listBg.setColor(Color.WHITE);
        listBg.setCornerRadius(30);
        listBox.setBackground(listBg);
        root.addView(listBox);

        addMenuRow(listBox, "🎁 Newbie mission");
        addMenuRow(listBox, "👥 Invite Friend");
        addMenuRow(listBox, "🏅 Medal");
        addMenuRow(listBox, "👨‍👩‍👧 Family");
        addMenuRow(listBox, "🎤 Host Center");
        addMenuRow(listBox, "📅 Daily Tasks");
        addMenuRow(listBox, "⚙️ Setting");

        // ===== Logout =====
        TextView btnLogout = new TextView(this);
        btnLogout.setText("🚪 Logout");
        btnLogout.setTextColor(Color.parseColor("#E91E63"));
        btnLogout.setTextSize(15);
        btnLogout.setTypeface(null, Typeface.BOLD);
        btnLogout.setGravity(Gravity.CENTER);
        btnLogout.setPadding(0, 30, 0, 40);
        btnLogout.setOnClickListener(v -> {
            prefs.edit().putBoolean("IS_LOGGED_IN", false).apply();
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(MeActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
        root.addView(btnLogout);

        setContentView(scroll);
        showFromPrefs();
        loadMeData();
    }

    private TextView addCountItem(LinearLayout parent, String num, String label, Runnable onClick) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setPadding(10, 10, 10, 10);
        box.setOnClickListener(v -> onClick.run());
        parent.addView(box);
        TextView tvNum = new TextView(this);
        tvNum.setText(num);
        tvNum.setTextColor(Color.parseColor("#222222"));
        tvNum.setTextSize(18);
        tvNum.setTypeface(null, Typeface.BOLD);
        tvNum.setGravity(Gravity.CENTER);
        box.addView(tvNum);
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#666666"));
        tvLabel.setTextSize(12);
        tvLabel.setGravity(Gravity.CENTER);
        box.addView(tvLabel);
        return tvNum;
    }

    private LinearLayout makeSmallCard(String title, String sub, String bgColor) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(30, 25, 30, 25);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(30);
        box.setBackground(bg);
        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextColor(Color.parseColor("#333333"));
        t1.setTextSize(15);
        t1.setTypeface(null, Typeface.BOLD);
        box.addView(t1);
        TextView t2 = new TextView(this);
        t2.setText(sub);
        t2.setTextColor(Color.parseColor("#888888"));
        t2.setTextSize(12);
        box.addView(t2);
        return box;
    }

    private void addToolItem(LinearLayout parent, String icon, String label) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setPadding(10, 10, 10, 10);
        box.setOnClickListener(v -> Toast.makeText(this, label + " - Coming Soon", Toast.LENGTH_SHORT).show());
        parent.addView(box);
        TextView iv = new TextView(this);
        iv.setText(icon);
        iv.setTextSize(24);
        iv.setGravity(Gravity.CENTER);
        box.addView(iv);
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.parseColor("#444444"));
        tv.setTextSize(12);
        tv.setGravity(Gravity.CENTER);
        box.addView(tv);
    }

    private void addMenuRow(LinearLayout parent, String title) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(35, 28, 35, 28);
        row.setOnClickListener(v -> Toast.makeText(this, title + " - Coming Soon", Toast.LENGTH_SHORT).show());
        parent.addView(row);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.parseColor("#333333"));
        t.setTextSize(14);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(t);
        TextView arrow = new TextView(this);
        arrow.setText(">");
        arrow.setTextColor(Color.parseColor("#AAAAAA"));
        arrow.setTextSize(18);
        arrow.setTypeface(null, Typeface.BOLD);
        row.addView(arrow);
        // Divider
        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#EEEEEE"));
        div.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        parent.addView(div);
    }

    private void goEdit() {
        try {
            Intent i = new Intent(MeActivity.this, EditProfileActivity.class);
            i.putExtra("EDIT_MODE", true);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCountDialog(String type, String count) {
        // PRIVACY RULE: Follow/Fans/Visitor sudhu nijer ID diye dekhbe, onno keu na
        new AlertDialog.Builder(this)
            .setTitle(type + " - " + count)
            .setMessage("Apnar " + type + " list ekhane asbe.\n\nEita sudhu apni dekhte parben. Onno keu apnar Follow/Fans dekhte parbe na.\n\nPorer step e eikhane full list banabo InshaAllah.")
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
            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (b != null) iv.setImageBitmap(b);
        } catch (Exception ignored) {}
    }

    private String getStr(DataSnapshot s, String k, String def) {
        if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue());
        return def;
    }
}
