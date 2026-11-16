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
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MeActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView txtName, txtId, txtGenderCountry, txtFollow, txtFans, txtVisitor, txtWalletCoins;
    private SharedPreferences prefs;
    private String firebaseUid = "";
    private String my9DigitId = "";
    private DatabaseReference userRef;
    private String followCount = "0", fansCount = "0", visitorCount = "0";

    private BroadcastReceiver profileReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            showFromPrefs();
            loadMeData();
        }
    };

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

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);

        // ===== HEADER =====
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(30, 40, 30, 25);
        header.setBackgroundColor(Color.parseColor("#1B1035"));
        root.addView(header);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(topRow);

        CardView cardAvatar = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(170, 170);
        cardParams.setMargins(0, 0, 25, 0);
        cardAvatar.setLayoutParams(cardParams);
        cardAvatar.setRadius(85);
        cardAvatar.setCardElevation(10);
        cardAvatar.setCardBackgroundColor(Color.parseColor("#FFD700"));
        cardAvatar.setPreventCornerOverlap(false);
        cardAvatar.setUseCompatPadding(false);
        cardAvatar.setOnClickListener(v -> goProfile());

        // NEW: Super Panel opens via Owner Gmail + Password - NOT via ID
        // ID can change, Owner Login stays same. Only Owner knows this Long Press.
        cardAvatar.setOnLongClickListener(v -> {
            startActivity(new Intent(MeActivity.this, SuperPanelLoginActivity.class));
            return true;
        });

        imgAvatar = new ImageView(this);
        imgAvatar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imgAvatar.setImageResource(android.R.drawable.ic_menu_my_calendar);
        imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgAvatar.setBackgroundColor(Color.TRANSPARENT);
        cardAvatar.addView(imgAvatar);
        topRow.addView(cardAvatar);

        // NEW: Avatar Long Press also opens Owner Login
        imgAvatar.setOnLongClickListener(v -> {
            startActivity(new Intent(MeActivity.this, SuperPanelLoginActivity.class));
            return true;
        });

        LinearLayout nameBox = new LinearLayout(this);
        nameBox.setOrientation(LinearLayout.VERTICAL);
        nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        nameBox.setOnClickListener(v -> goProfile());
        topRow.addView(nameBox);

        txtName = new TextView(this);
        txtName.setText(prefs.getString("USER_NAME", "User"));
        txtName.setTextColor(Color.WHITE);
        txtName.setTextSize(20);
        txtName.setTypeface(null, Typeface.BOLD);
        nameBox.addView(txtName);

        LinearLayout idRow = new LinearLayout(this);
        idRow.setOrientation(LinearLayout.HORIZONTAL);
        idRow.setGravity(Gravity.CENTER_VERTICAL);
        nameBox.addView(idRow);

        txtId = new TextView(this);
        txtId.setText("ID: " + my9DigitId);
        txtId.setTextColor(Color.parseColor("#FFD700"));
        txtId.setTextSize(14);
        txtId.setTypeface(null, Typeface.BOLD);
        idRow.addView(txtId);

        TextView txtCopy = new TextView(this);
        txtCopy.setText("  📋 Copy");
        txtCopy.setTextColor(Color.parseColor("#FFD700"));
        txtCopy.setTextSize(13);
        txtCopy.setTypeface(null, Typeface.BOLD);
        txtCopy.setOnClickListener(v -> copyId());
        idRow.addView(txtCopy);

        txtGenderCountry = new TextView(this);
        txtGenderCountry.setText(getGenderCountryText());
        txtGenderCountry.setTextColor(Color.parseColor("#B0BEC5"));
        txtGenderCountry.setTextSize(12);
        nameBox.addView(txtGenderCountry);

        TextView btnProfileArrow = new TextView(this);
        btnProfileArrow.setText(">");
        btnProfileArrow.setTextSize(26);
        btnProfileArrow.setTypeface(null, Typeface.BOLD);
        btnProfileArrow.setTextColor(Color.parseColor("#FFD700"));
        btnProfileArrow.setPadding(20, 20, 20, 20);
        btnProfileArrow.setOnClickListener(v -> goProfile());
        topRow.addView(btnProfileArrow);

        // Copy ID Bar
        LinearLayout copyIdBar = new LinearLayout(this);
        copyIdBar.setOrientation(LinearLayout.HORIZONTAL);
        copyIdBar.setBackgroundColor(Color.parseColor("#140B26"));
        copyIdBar.setPadding(30, 15, 30, 15);
        copyIdBar.setOnClickListener(v -> copyId());
        TextView tvCopyTitle = new TextView(this);
        tvCopyTitle.setText("Copy ID");
        tvCopyTitle.setTextColor(Color.parseColor("#FFD700"));
        tvCopyTitle.setTextSize(14);
        tvCopyTitle.setTypeface(null, Typeface.BOLD);
        copyIdBar.addView(tvCopyTitle);
        root.addView(copyIdBar);

        // Count Row
        LinearLayout countRow = new LinearLayout(this);
        countRow.setOrientation(LinearLayout.HORIZONTAL);
        countRow.setGravity(Gravity.CENTER);
        countRow.setBackgroundColor(Color.parseColor("#1B1035"));
        countRow.setPadding(15, 20, 15, 20);
        root.addView(countRow);
        txtFollow = addCountItem(countRow, "0", "Follow", () -> showCountDialog("Follow", followCount));
        txtFans = addCountItem(countRow, "0", "Fans", () -> showCountDialog("Fans", fansCount));
        txtVisitor = addCountItem(countRow, "0", "Visitor", () -> showCountDialog("Visitor", visitorCount));

        LinearLayout menuContainer = new LinearLayout(this);
        menuContainer.setOrientation(LinearLayout.VERTICAL);
        menuContainer.setPadding(20, 20, 20, 30);
        root.addView(menuContainer);

        // ===== SVIP - BIG GOLDEN =====
        LinearLayout svipBox = new LinearLayout(this);
        svipBox.setOrientation(LinearLayout.VERTICAL);
        svipBox.setPadding(30, 28, 30, 28);
        GradientDrawable svipBg = new GradientDrawable();
        svipBg.setColor(Color.parseColor("#FFD700"));
        svipBg.setCornerRadius(24);
        svipBox.setBackground(svipBg);
        LinearLayout.LayoutParams svipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        svipLp.setMargins(0, 0, 0, 14);
        svipBox.setLayoutParams(svipLp);
        svipBox.setOnClickListener(v -> Toast.makeText(this, "SVIP Locked - Coming Soon", Toast.LENGTH_SHORT).show());
        menuContainer.addView(svipBox);
        TextView svipT1 = new TextView(this);
        svipT1.setText("👑 SVIP Center");
        svipT1.setTextColor(Color.parseColor("#0A0518"));
        svipT1.setTextSize(19);
        svipT1.setTypeface(null, Typeface.BOLD);
        svipBox.addView(svipT1);
        TextView svipT2 = new TextView(this);
        svipT2.setText("Get SVIP and enjoy privileges  >");
        svipT2.setTextColor(Color.parseColor("#3A2A00"));
        svipT2.setTextSize(13);
        svipBox.addView(svipT2);

        // ===== WALLET ROW - FIREBASE COINS - FIXED =====
        LinearLayout walletBox = new LinearLayout(this);
        walletBox.setOrientation(LinearLayout.HORIZONTAL);
        walletBox.setGravity(Gravity.CENTER_VERTICAL);
        walletBox.setPadding(28, 24, 28, 24);
        GradientDrawable walletBg = new GradientDrawable();
        walletBg.setColor(Color.parseColor("#2A1B4E"));
        walletBg.setCornerRadius(24);
        walletBox.setBackground(walletBg);
        LinearLayout.LayoutParams walletLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        walletLp.setMargins(0, 0, 0, 14);
        walletBox.setLayoutParams(walletLp);
        walletBox.setOnClickListener(v -> {
            openWalletDialog();
        });
        menuContainer.addView(walletBox);
        LinearLayout walletLeft = new LinearLayout(this);
        walletLeft.setOrientation(LinearLayout.VERTICAL);
        walletLeft.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        walletBox.addView(walletLeft);
        TextView walletTitle = new TextView(this);
        walletTitle.setText("💰 My Wallet");
        walletTitle.setTextColor(Color.WHITE);
        walletTitle.setTextSize(16);
        walletTitle.setTypeface(null, Typeface.BOLD);
        walletLeft.addView(walletTitle);
        txtWalletCoins = new TextView(this);
        txtWalletCoins.setText("Coins: 0");
        txtWalletCoins.setTextColor(Color.parseColor("#FFD700"));
        txtWalletCoins.setTextSize(14);
        txtWalletCoins.setTypeface(null, Typeface.BOLD);
        walletLeft.addView(txtWalletCoins);
        TextView walletArrow = new TextView(this);
        walletArrow.setText("View Bill  >");
        walletArrow.setTextColor(Color.parseColor("#FFD700"));
        walletArrow.setTextSize(13);
        walletArrow.setTypeface(null, Typeface.BOLD);
        walletBox.addView(walletArrow);

        // ===== VIP & RECHARGE - BIG =====
        LinearLayout vipRow = new LinearLayout(this);
        vipRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams vipRowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vipRowLp.setMargins(0, 0, 0, 14);
        vipRow.setLayoutParams(vipRowLp);
        menuContainer.addView(vipRow);

        LinearLayout vipBox = makeBigCard("👑 VIP", "Check now >", "#2A1B4E", Color.WHITE);
        vipBox.setOnClickListener(v -> Toast.makeText(this, "VIP - Coming Soon", Toast.LENGTH_SHORT).show());
        LinearLayout.LayoutParams vipLp = new LinearLayout.LayoutParams(0, 180, 1);
        vipLp.setMargins(0, 0, 10, 0);
        vipBox.setLayoutParams(vipLp);
        vipRow.addView(vipBox);

        // RECHARGE - OPENS PREMIUM RECHARGE POPUP - NO SMALL TEXT POPUP
        LinearLayout rechargeBox = makeBigCard("🎁 Recharge", "Top up >", "#FF6D00", Color.WHITE);
        rechargeBox.setOnClickListener(v -> showRechargeLocked());
        LinearLayout.LayoutParams recLp = new LinearLayout.LayoutParams(0, 180, 1);
        recLp.setMargins(10, 0, 0, 0);
        rechargeBox.setLayoutParams(recLp);
        vipRow.addView(rechargeBox);

        TextView toolsTitle = new TextView(this);
        toolsTitle.setText("General Tools");
        toolsTitle.setTextColor(Color.parseColor("#FFD700"));
        toolsTitle.setTextSize(14);
        toolsTitle.setTypeface(null, Typeface.BOLD);
        toolsTitle.setPadding(10, 15, 10, 10);
        menuContainer.addView(toolsTitle);

        LinearLayout toolsRow = new LinearLayout(this);
        toolsRow.setOrientation(LinearLayout.HORIZONTAL);
        toolsRow.setGravity(Gravity.CENTER);
        toolsRow.setBackgroundColor(Color.parseColor("#1B1035"));
        toolsRow.setPadding(15, 22, 15, 22);
        GradientDrawable toolsBg = new GradientDrawable();
        toolsBg.setColor(Color.parseColor("#1B1035"));
        toolsBg.setCornerRadius(24);
        toolsRow.setBackground(toolsBg);
        LinearLayout.LayoutParams toolsLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toolsLp.setMargins(0, 0, 0, 16);
        toolsRow.setLayoutParams(toolsLp);
        menuContainer.addView(toolsRow);
        addToolItem(toolsRow, "🛍️", "Store");
        addToolItem(toolsRow, "👗", "Dress");
        addToolItem(toolsRow, "🎂", "Birthday");
        addToolItem(toolsRow, "📊", "Level");
        // NEW: Agency Tool - Opens YOUR OLD AgencyDashboardActivity
        addToolItem(toolsRow, "🏛️", "Agency", v -> openAgency());

        TextView serviceTitle = new TextView(this);
        serviceTitle.setText("My Services");
        serviceTitle.setTextColor(Color.parseColor("#FFD700"));
        serviceTitle.setTextSize(14);
        serviceTitle.setTypeface(null, Typeface.BOLD);
        serviceTitle.setPadding(10, 5, 10, 10);
        menuContainer.addView(serviceTitle);

        LinearLayout listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable listBg = new GradientDrawable();
        listBg.setColor(Color.parseColor("#1B1035"));
        listBg.setCornerRadius(24);
        listBox.setBackground(listBg);
        menuContainer.addView(listBox);
        addMenuRow(listBox, "🎯 Newbie Mission");
        addMenuRow(listBox, "👥 Invite Friend");
        addMenuRow(listBox, "🏅 Medal");
        addMenuRow(listBox, "👨‍👩‍👧 Family");
        addMenuRow(listBox, "🎤 Host Center");
        addMenuRow(listBox, "📅 Daily Tasks");
        addMenuRow(listBox, "⚙️ Setting");
        // NOTE: Reseller option intentionally NOT shown here. Only Super Admin creates Reseller from Admin Panel.

        LinearLayout logoutLayout = new LinearLayout(this);
        logoutLayout.setOrientation(LinearLayout.HORIZONTAL);
        logoutLayout.setGravity(Gravity.CENTER);
        logoutLayout.setPadding(20, 28, 20, 28);
        GradientDrawable logoutBg = new GradientDrawable();
        logoutBg.setColor(Color.parseColor("#2A1B4E"));
        logoutBg.setCornerRadius(24);
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
        try {
            IntentFilter filter = new IntentFilter("com.sr.voicechat.PROFILE_UPDATED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(profileReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(profileReceiver, filter);
            }
        } catch (Exception ignored) {}
        showFromPrefs();
        loadMeData();
    }

    private void copyId() {
        if (my9DigitId.isEmpty()) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("ID", my9DigitId));
        Toast.makeText(this, "ID Copied: " + my9DigitId, Toast.LENGTH_SHORT).show();
    }

    // NEW: Agency Icon - Opens YOUR OLD Dashboard with Intent Extras
    // Your Dashboard needs AGENCY_OWNER_ID = FirebaseUID, AGENCY_NAME
    private void openAgency() {
        String uid = firebaseUid;
        if (uid.isEmpty()) {
            FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
            if (fu != null) uid = fu.getUid();
        }
        if (uid.isEmpty()) uid = prefs.getString("FIREBASE_UID", "");
        Intent i = new Intent(this, AgencyDashboardActivity.class);
        i.putExtra("AGENCY_OWNER_ID", uid);
        i.putExtra("AGENCY_NAME", prefs.getString("AGENCY_NAME", "SR Agency"));
        startActivity(i);
    }

    // PERMANENT FIX: Wallet opens Premium WalletRechargeDialog. No WalletActivity needed.
    private void openWalletDialog() {
        try {
            WalletRechargeDialog dialog = new WalletRechargeDialog();
            dialog.show(getSupportFragmentManager(), "WalletRechargeDialog");
        } catch (Exception e) {
            Toast.makeText(this, "Wallet - Coming Soon", Toast.LENGTH_SHORT).show();
        }
    }

    // PERMANENT FIX: Recharge opens SAME Premium Popup as Wallet.
    // No small text popup, No Bangla, No Online Payment.
    // Inside popup: 5 packages, Select, Charge -> Contact Seller / Coming Soon
    private void showRechargeLocked() {
        try {
            WalletRechargeDialog dialog = new WalletRechargeDialog();
            dialog.show(getSupportFragmentManager(), "WalletRechargeDialog");
        } catch (Exception e) {
            Toast.makeText(this, "Recharge - Coming Soon", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(profileReceiver); } catch (Exception ignored) {}
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
        tvNum.setTextSize(18);
        tvNum.setTypeface(null, Typeface.BOLD);
        box.addView(tvNum);
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#B0BEC5"));
        tvLabel.setTextSize(12);
        box.addView(tvLabel);
        return tvNum;
    }

    private LinearLayout makeBigCard(String title, String sub, String bgColor, int titleColor) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(25, 20, 25, 20);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(24);
        box.setBackground(bg);
        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextColor(titleColor);
        t1.setTextSize(17);
        t1.setTypeface(null, Typeface.BOLD);
        t1.setGravity(Gravity.CENTER);
        box.addView(t1);
        TextView t2 = new TextView(this);
        t2.setText(sub);
        t2.setTextColor(Color.parseColor("#FFFFFF"));
        t2.setTextSize(12);
        t2.setGravity(Gravity.CENTER);
        box.addView(t2);
        return box;
    }

    private LinearLayout makeSmallCard(String title, String sub, String bgColor) {
        return makeBigCard(title, sub, bgColor, Color.WHITE);
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
        addToolItem(parent, icon, label, v -> Toast.makeText(this, label + " - Coming Soon", Toast.LENGTH_SHORT).show());
    }

    // NEW: Tool with custom click - for Agency
    private void addToolItem(LinearLayout parent, String icon, String label, View.OnClickListener click) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(click);
        parent.addView(box);
        TextView iv = new TextView(this);
        iv.setText(icon);
        iv.setTextSize(30);
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
        row.setPadding(30, 26, 30, 26);
        row.setOnClickListener(v -> Toast.makeText(this, title + " - Coming Soon", Toast.LENGTH_SHORT).show());
        parent.addView(row);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(Color.WHITE);
        t.setTextSize(15);
        t.setTypeface(null, Typeface.BOLD);
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
            .setMessage("Your " + type + " list will appear here.\n\nOnly you can see this.")
            .setPositiveButton("OK", null)
            .show();
    }

    private String getGenderCountryText() {
        String g = prefs.getString("USER_GENDER", "");
        String c = prefs.getString("USER_COUNTRY", "");
        String base = "";
        if (!g.isEmpty() && !c.isEmpty()) base = g + " | " + c;
        else if (!c.isEmpty()) base = c;
        else if (!g.isEmpty()) base = g;
        else base = "Complete Your Profile";
        // NEW: Agency Badge under ID - Auto shows if Agency
        if (prefs.getBoolean("IS_AGENCY", false)) {
            base = base + "  |  🏷️ AGENCY";
        }
        return base;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showFromPrefs();
        loadMeData();
    }

    private void showFromPrefs() {
        String name = prefs.getString("USER_NAME", "User");
        String photo = prefs.getString("USER_PHOTO", "");
        if (photo.isEmpty()) photo = prefs.getString("PROFILE_PHOTO", "");
        my9DigitId = prefs.getString("MY_9_DIGIT_ID", my9DigitId);
        if (txtName != null) txtName.setText(name);
        if (txtId != null) txtId.setText("ID: " + my9DigitId);
        if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
        showPhoto(photo, imgAvatar);
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
                    String photo = getStr(snap, "photoUrl", "");
                    if (photo.isEmpty()) photo = getStr(snap, "photo", "");
                    if (photo.isEmpty()) photo = getStr(snap, "photoBase64", "");
                    followCount = getStr(snap, "followCount", "0");
                    fansCount = getStr(snap, "fansCount", "0");
                    visitorCount = getStr(snap, "visitorCount", "0");
                    String coins = getStr(snap, "coins", "0");
                    if (coins.isEmpty()) coins = getStr(snap, "walletCoins", "0");
                    // NEW: Agency data from Firebase - for Badge + Dashboard
                    boolean isAgency = false;
                    if (snap.hasChild("isAgency") && snap.child("isAgency").getValue() != null) {
                        Object o = snap.child("isAgency").getValue();
                        isAgency = o instanceof Boolean ? (Boolean) o : "true".equalsIgnoreCase(String.valueOf(o));
                    }
                    String agencyName = getStr(snap, "agencyName", "SR Agency");
                    String agencyPercent = getStr(snap, "agencyPercent", "0");

                    if (!id.isEmpty()) {
                        my9DigitId = id;
                        prefs.edit().putString("MY_9_DIGIT_ID", id).apply();
                    }
                    prefs.edit()
                        .putString("USER_NAME", name)
                        .putString("USER_COUNTRY", country)
                        .putString("USER_GENDER", gender)
                        .putString("USER_PHOTO", photo)
                        .putString("PROFILE_PHOTO", photo)
                        .putBoolean("IS_AGENCY", isAgency)
                        .putString("AGENCY_NAME", agencyName)
                        .putString("AGENCY_PERCENT", agencyPercent)
                        .apply();

                    if (txtName != null) txtName.setText(name);
                    if (txtId != null) txtId.setText("ID: " + my9DigitId);
                    if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
                    if (txtFollow != null) txtFollow.setText(followCount);
                    if (txtFans != null) txtFans.setText(fansCount);
                    if (txtVisitor != null) txtVisitor.setText(visitorCount);
                    if (txtWalletCoins != null) txtWalletCoins.setText("Coins: " + coins);
                    showPhoto(photo, imgAvatar);
                } catch (Exception ignored) {}
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showPhoto(String photo, ImageView iv) {
        if (iv == null || photo == null || photo.isEmpty()) return;
        try {
            if (photo.startsWith("http")) {
                Glide.with(this).load(photo).circleCrop().into(iv);
            } else {
                showBase64Image(photo, iv);
            }
        } catch (Exception ignored) {}
    }

    private void showBase64Image(String base64, ImageView iv) {
        if (iv == null || base64 == null || base64.isEmpty()) return;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 2;
            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
            if (b != null) Glide.with(this).load(b).circleCrop().into(iv);
        } catch (Exception ignored) {}
    }

    private String getStr(DataSnapshot s, String k, String def) {
        if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue());
        return def;
    }
}
