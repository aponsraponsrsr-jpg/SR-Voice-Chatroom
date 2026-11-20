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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class MeActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView txtName, txtId, txtGenderCountry, txtFollow, txtFans, txtVisitor, txtWalletCoins;
    private SharedPreferences prefs;
    private String firebaseUid = "";
    private String my9DigitId = "";
    private DatabaseReference userRef;
    private String followCount = "0", fansCount = "0", visitorCount = "0";
    private LinearLayout agencyToolView = null;
    private LinearLayout hostCenterRowView = null;
    private boolean isAgencyActive = false;
    private boolean isHostActive = false;
    private boolean inviteDialogShown = false;
    private boolean rtdbListenerStarted = false;
    private boolean vipAssignmentListenerStarted = false;
    private TextView txtVipBadge;
    private TextView txtSvipStatus;
    private TextView txtVipStatus;
    private int myVipLevel = 0;

    // ===== SELLER BUSINESS WALLET - NEW - PERMANENT =====
    private LinearLayout sellerWalletBox = null;
    private TextView txtSellerBalance = null;
    private boolean isSellerActive = false;
    private long sellerBusinessCoins = 0;
    private boolean sellerListenerStarted = false;

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
        if (!my9DigitId.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(my9DigitId);
        } else if (!firebaseUid.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
        }
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);
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
        txtVipBadge = new TextView(this);
        txtVipBadge.setText("");
        txtVipBadge.setTextColor(Color.parseColor("#FFD700"));
        txtVipBadge.setTextSize(13);
        txtVipBadge.setTypeface(null, Typeface.BOLD);
        txtVipBadge.setVisibility(View.GONE);
        nameBox.addView(txtVipBadge);
        TextView btnProfileArrow = new TextView(this);
        btnProfileArrow.setText(">");
        btnProfileArrow.setTextSize(26);
        btnProfileArrow.setTypeface(null, Typeface.BOLD);
        btnProfileArrow.setTextColor(Color.parseColor("#FFD700"));
        btnProfileArrow.setPadding(20, 20, 20, 20);
        btnProfileArrow.setOnClickListener(v -> goProfile());
        topRow.addView(btnProfileArrow);
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
        LinearLayout svipBox = new LinearLayout(this);
        svipBox.setOrientation(LinearLayout.VERTICAL);
        svipBox.setPadding(30, 28, 30, 28);
        GradientDrawable svipBg = new GradientDrawable();
        svipBg.setColors(new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF9800")});
        svipBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        svipBg.setCornerRadius(24);
        svipBox.setBackground(svipBg);
        LinearLayout.LayoutParams svipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        svipLp.setMargins(0, 0, 0, 14);
        svipBox.setLayoutParams(svipLp);
        svipBox.setOnClickListener(v -> showVipListDialog());
        menuContainer.addView(svipBox);
        TextView svipT1 = new TextView(this);
        svipT1.setText("SVIP CENTER");
        svipT1.setTextColor(Color.parseColor("#0A0518"));
        svipT1.setTextSize(19);
        svipT1.setTypeface(null, Typeface.BOLD);
        svipBox.addView(svipT1);
        txtSvipStatus = new TextView(this);
        txtSvipStatus.setText("SVIP 1-7  •  Emperor Dragon  •  Tap to View  >");
        txtSvipStatus.setTextColor(Color.parseColor("#3A2A00"));
        txtSvipStatus.setTextSize(13);
        svipBox.addView(txtSvipStatus);
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
        walletBox.setOnClickListener(v -> openWalletDialog());
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

        // ===== SELLER BUSINESS WALLET BOX - ONLY SELLER SEES =====
        sellerWalletBox = new LinearLayout(this);
        sellerWalletBox.setOrientation(LinearLayout.HORIZONTAL);
        sellerWalletBox.setGravity(Gravity.CENTER_VERTICAL);
        sellerWalletBox.setPadding(28, 24, 28, 24);
        GradientDrawable sellerBg = new GradientDrawable();
        sellerBg.setColors(new int[]{Color.parseColor("#00C853"), Color.parseColor("#00796B")});
        sellerBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        sellerBg.setCornerRadius(24);
        sellerWalletBox.setBackground(sellerBg);
        LinearLayout.LayoutParams sellerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sellerLp.setMargins(0, 0, 0, 14);
        sellerWalletBox.setLayoutParams(sellerLp);
        sellerWalletBox.setVisibility(View.GONE); // Default GONE - Normal user never sees
        sellerWalletBox.setOnClickListener(v -> {
            if (!isSellerActive) return;
            Intent i = new Intent(MeActivity.this, SellerBusinessWalletActivity.class);
            i.putExtra("SELLER_9DIGIT_ID", my9DigitId);
            startActivity(i);
        });
        menuContainer.addView(sellerWalletBox);
        LinearLayout sellerLeft = new LinearLayout(this);
        sellerLeft.setOrientation(LinearLayout.VERTICAL);
        sellerLeft.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        sellerWalletBox.addView(sellerLeft);
        TextView sellerTitle = new TextView(this);
        sellerTitle.setText("🏪 Seller Business Wallet");
        sellerTitle.setTextColor(Color.WHITE);
        sellerTitle.setTextSize(16);
        sellerTitle.setTypeface(null, Typeface.BOLD);
        sellerLeft.addView(sellerTitle);
        txtSellerBalance = new TextView(this);
        txtSellerBalance.setText("Business Coins: 0");
        txtSellerBalance.setTextColor(Color.WHITE);
        txtSellerBalance.setTextSize(14);
        txtSellerBalance.setTypeface(null, Typeface.BOLD);
        sellerLeft.addView(txtSellerBalance);
        TextView sellerArrow = new TextView(this);
        sellerArrow.setText("Sell Coins  >");
        sellerArrow.setTextColor(Color.WHITE);
        sellerArrow.setTextSize(13);
        sellerArrow.setTypeface(null, Typeface.BOLD);
        sellerWalletBox.addView(sellerArrow);

        LinearLayout vipRow = new LinearLayout(this);
        vipRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams vipRowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vipRowLp.setMargins(0, 0, 0, 14);
        vipRow.setLayoutParams(vipRowLp);
        menuContainer.addView(vipRow);
        LinearLayout vipBox = new LinearLayout(this);
        vipBox.setOrientation(LinearLayout.VERTICAL);
        vipBox.setGravity(Gravity.CENTER);
        vipBox.setPadding(25, 20, 25, 20);
        GradientDrawable vipBg = new GradientDrawable();
        vipBg.setColors(new int[]{Color.parseColor("#3A0A4A"), Color.parseColor("#1A0B2E")});
        vipBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        vipBg.setCornerRadius(24);
        vipBox.setBackground(vipBg);
        vipBox.setOnClickListener(v -> showVipListDialog());
        LinearLayout.LayoutParams vipLp = new LinearLayout.LayoutParams(0, 180, 1);
        vipLp.setMargins(0, 0, 10, 0);
        vipBox.setLayoutParams(vipLp);
        vipRow.addView(vipBox);
        TextView vipT1 = new TextView(this);
        vipT1.setText("🐅 VIP CENTER");
        vipT1.setTextColor(Color.parseColor("#FFD700"));
        vipT1.setTextSize(17);
        vipT1.setTypeface(null, Typeface.BOLD);
        vipT1.setGravity(Gravity.CENTER);
        vipBox.addView(vipT1);
        txtVipStatus = new TextView(this);
        txtVipStatus.setText("Black Tiger • Eagle Fire • Dragon\nTap to View >");
        txtVipStatus.setTextColor(Color.WHITE);
        txtVipStatus.setTextSize(12);
        txtVipStatus.setGravity(Gravity.CENTER);
        vipBox.addView(txtVipStatus);
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
        addToolItem(toolsRow, "🛍️", "Store", v -> openStore());
        addToolItem(toolsRow, "👗", "Dress");
        addToolItem(toolsRow, "🎂", "Birthday");
        addToolItem(toolsRow, "📊", "Level");
        agencyToolView = addToolItem(toolsRow, "🏛️", "Agency", v -> openAgency());
        if (agencyToolView != null) agencyToolView.setVisibility(View.GONE);
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
        hostCenterRowView = addMenuRow(listBox, "🎤 Host Center", v -> openHostCenter());
        if (hostCenterRowView != null) hostCenterRowView.setVisibility(View.GONE);
        addMenuRow(listBox, "📅 Daily Tasks");
        addMenuRow(listBox, "⚙️ Setting");
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
        applyRoleVisibility();
        applySellerVisibility();
        loadMeData();
        listenVipAssignments();
        listenFirestoreRole();
        listenSellerStatus(); // NEW
    }

    // ===== SELLER LISTENER - checks sellers/{9DigitId} AND sellers/{firebaseUid} =====
    private void listenSellerStatus() {
        if (sellerListenerStarted) return;
        sellerListenerStarted = true;
        if (!my9DigitId.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("sellers").child(my9DigitId)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) { applySellerSnap(snap); return; }
                        listenSellerByFirebaseUid();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { listenSellerByFirebaseUid(); }
                });
        } else {
            listenSellerByFirebaseUid();
        }
    }
    private void listenSellerByFirebaseUid() {
        if (firebaseUid.isEmpty() || firebaseUid.equals(my9DigitId)) return;
        FirebaseDatabase.getInstance().getReference("sellers").child(firebaseUid)
            .addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (snap.exists()) applySellerSnap(snap);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }
    private void applySellerSnap(DataSnapshot snap) {
        try {
            boolean isSeller = getBool(snap, "isSeller", false);
            String status = getStr(snap, "status", "").toLowerCase();
            // status: active = show, blocked / pending / rejected = hide
            boolean active = isSeller && !"blocked".equals(status) && !"rejected".equals(status) && !"pending".equals(status);
            long bal = 0;
            try {
                Object b = snap.child("businessCoins").getValue();
                if (b != null) bal = Long.parseLong(String.valueOf(b));
            } catch (Exception ignored) {}
            // Also support businessBalance field name
            if (bal == 0) {
                try {
                    Object b2 = snap.child("businessBalance").getValue();
                    if (b2 != null) bal = Long.parseLong(String.valueOf(b2));
                } catch (Exception ignored) {}
            }
            isSellerActive = active;
            sellerBusinessCoins = bal;
            prefs.edit().putBoolean("IS_SELLER", active).putLong("SELLER_BUSINESS_COINS", bal).apply();
            applySellerVisibility();
        } catch (Exception ignored) {}
    }
    private void applySellerVisibility() {
        boolean show = prefs.getBoolean("IS_SELLER", false);
        // Double check: if prefs says seller but live flag false, hide
        if (!isSellerActive && sellerListenerStarted) show = false;
        isSellerActive = show;
        if (sellerWalletBox != null) sellerWalletBox.setVisibility(show ? View.VISIBLE : View.GONE);
        if (txtSellerBalance != null) txtSellerBalance.setText("Business Coins: " + sellerBusinessCoins);
    }

    private void showVipListDialog() {
        try {
            List<VipModel> list = VipModel.createPremiumList();
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.setContentView(R.layout.dialog_vip_list);
            RecyclerView rv = dialog.findViewById(R.id.recyclerVipList);
            if (rv != null) {
                rv.setLayoutManager(new LinearLayoutManager(this));
                VipAdapter adapter = new VipAdapter(this, list);
                rv.setAdapter(adapter);
            }
            Button btnClose = dialog.findViewById(R.id.btnCloseVipDialog);
            if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "VIP - Coming Soon", Toast.LENGTH_SHORT).show();
        }
    }
    private void listenVipAssignments() {
        if (vipAssignmentListenerStarted) return;
        vipAssignmentListenerStarted = true;
        if (!my9DigitId.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("vip_assignments").child(my9DigitId)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) { applyVipSnap(snap); return; }
                        listenVipByFirebaseUid();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { listenVipByFirebaseUid(); }
                });
        } else {
            listenVipByFirebaseUid();
        }
    }
    private void listenVipByFirebaseUid() {
        if (firebaseUid.isEmpty() || firebaseUid.equals(my9DigitId)) return;
        FirebaseDatabase.getInstance().getReference("vip_assignments").child(firebaseUid)
            .addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (snap.exists()) applyVipSnap(snap);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }
    private void applyVipSnap(DataSnapshot snap) {
        try {
            String lvlStr = snap.child("vipLevel").getValue() == null ? "0" : String.valueOf(snap.child("vipLevel").getValue());
            int v = 0;
            try { v = Integer.parseInt(lvlStr); } catch (Exception ignored) {}
            long expire = 0;
            try {
                Object ex = snap.child("expireAt").getValue();
                if (ex != null) expire = Long.parseLong(String.valueOf(ex));
            } catch (Exception ignored) {}
            if (expire == 0) {
                try {
                    Object ex2 = snap.child("expireTs").getValue();
                    if (ex2 != null) expire = Long.parseLong(String.valueOf(ex2));
                } catch (Exception ignored) {}
            }
            if (expire > 0 && System.currentTimeMillis() > expire) v = 0;
            if (v != myVipLevel) {
                myVipLevel = v;
                prefs.edit().putInt("VIP_LEVEL", v).apply();
                updateVipUI();
            }
        } catch (Exception ignored) {}
    }
    private void updateVipUI() {
        if (txtVipBadge == null) return;
        if (myVipLevel >= 1 && myVipLevel <= 8) {
            String[] names = {"", "Black Tiger", "White Tiger", "Eagle Fire", "Phoenix Fire Bird", "Dragon", "Golden Dragon King", "Emperor Dragon", "Purple Dragon"};
            String[] emojis = {"", "🐅", "🐯", "🦅", "🔥", "🐉", "👑", "🐲", "💜"};
            txtVipBadge.setText(emojis[myVipLevel] + " VIP " + myVipLevel + " • " + names[myVipLevel]);
            txtVipBadge.setVisibility(View.VISIBLE);
            if (txtVipStatus != null) txtVipStatus.setText("✅ You Have VIP " + myVipLevel + "\n" + names[myVipLevel] + " Active");
            if (txtSvipStatus != null) txtSvipStatus.setText("✅ You Have VIP " + myVipLevel + "  •  Tap to View  >");
        } else {
            txtVipBadge.setVisibility(View.GONE);
            if (txtVipStatus != null) txtVipStatus.setText("Black Tiger • Eagle Fire • Dragon\nTap to View >");
            if (txtSvipStatus != null) txtSvipStatus.setText("SVIP 1-7  •  Emperor Dragon  •  Tap to View  >");
        }
    }
    private void copyId() {
        if (my9DigitId.isEmpty()) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("ID", my9DigitId));
        Toast.makeText(this, "ID Copied: " + my9DigitId, Toast.LENGTH_SHORT).show();
    }
    private void openStore() {
        try {
            Intent i = new Intent(MeActivity.this, StoreActivity.class);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Store khulte parchi na", Toast.LENGTH_SHORT).show();
        }
    }
    private void openAgency() {
        if (!isAgencyActive) { Toast.makeText(this, "You are not an Agency yet", Toast.LENGTH_SHORT).show(); return; }
        String uid = firebaseUid;
        if (uid.isEmpty()) { FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser(); if (fu != null) uid = fu.getUid(); }
        if (uid.isEmpty()) uid = prefs.getString("FIREBASE_UID", "");
        Intent i = new Intent(this, AgencyDashboardActivity.class);
        i.putExtra("AGENCY_OWNER_ID", uid);
        i.putExtra("AGENCY_NAME", prefs.getString("AGENCY_NAME", "SR Agency"));
        startActivity(i);
    }
    private void openHostCenter() {
        if (!isHostActive) { Toast.makeText(this, "You are not a Host yet", Toast.LENGTH_SHORT).show(); return; }
        try {
            Intent i = new Intent(this, HostCenterActivity.class);
            i.putExtra("HOST_UID", firebaseUid);
            i.putExtra("HOST_AGENCY_ID", prefs.getString("HOST_AGENCY_ID", ""));
            startActivity(i);
        } catch (Exception e) {
            try { Intent i2 = new Intent(this, HostPolicyActivity.class); startActivity(i2); }
            catch (Exception e2) { Toast.makeText(this, "Host Center - Coming Soon", Toast.LENGTH_SHORT).show(); }
        }
    }
    private void applyRoleVisibility() {
        boolean showAgency = prefs.getBoolean("IS_AGENCY", false) && prefs.getBoolean("AGENCY_ACTIVE", false);
        boolean showHost = prefs.getBoolean("IS_HOST", false) && prefs.getBoolean("HOST_ACTIVE", false);
        isAgencyActive = showAgency; isHostActive = showHost;
        if (agencyToolView != null) agencyToolView.setVisibility(showAgency ? View.VISIBLE : View.GONE);
        if (hostCenterRowView != null) hostCenterRowView.setVisibility(showHost ? View.VISIBLE : View.GONE);
        if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
    }
    private void listenFirestoreRole() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (!my9DigitId.isEmpty()) {
                db.collection("agencies").document(my9DigitId).addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    if (snap != null && snap.exists()) {
                        Object st = snap.get("status"); String status = st == null ? "" : String.valueOf(st).toLowerCase();
                        boolean active = !"blocked".equals(status) && !"rejected".equals(status) && !"pending".equals(status);
                        if (status.isEmpty()) active = true;
                        prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("AGENCY_ACTIVE", active).apply();
                        Object nm = snap.get("name"); if (nm != null) prefs.edit().putString("AGENCY_NAME", String.valueOf(nm)).apply();
                    } else {
                        boolean rtdbAgency = prefs.getBoolean("RTDB_IS_AGENCY", false);
                        if (!rtdbAgency) prefs.edit().putBoolean("IS_AGENCY", false).putBoolean("AGENCY_ACTIVE", false).apply();
                    }
                    applyRoleVisibility();
                });
                if (!firebaseUid.isEmpty()) {
                    db.collection("agencies").document(firebaseUid).addSnapshotListener((snap, e) -> {
                        if (e != null) return;
                        if (snap != null && snap.exists()) {
                            Object st = snap.get("status"); String status = st == null ? "" : String.valueOf(st).toLowerCase();
                            boolean active = !"blocked".equals(status) && !"rejected".equals(status);
                            prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("AGENCY_ACTIVE", active).apply();
                            applyRoleVisibility();
                        }
                    });
                    db.collection("agencies").whereEqualTo("ownerUid", firebaseUid).addSnapshotListener((snaps, e) -> {
                        if (e != null || snaps == null) return;
                        if (!snaps.isEmpty()) { prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("AGENCY_ACTIVE", true).apply(); applyRoleVisibility(); }
                    });
                }
            }
            if (!firebaseUid.isEmpty()) {
                db.collection("hosts").document(firebaseUid).addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    if (snap != null && snap.exists()) prefs.edit().putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply();
                    else { boolean rtdbHost = prefs.getBoolean("RTDB_IS_HOST", false); if (!rtdbHost) prefs.edit().putBoolean("IS_HOST", false).putBoolean("HOST_ACTIVE", false).apply(); }
                    applyRoleVisibility();
                });
                db.collection("hosts").whereEqualTo("ownerUid", firebaseUid).addSnapshotListener((snaps, e) -> {
                    if (e != null || snaps == null) return;
                    if (!snaps.isEmpty()) { prefs.edit().putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply(); applyRoleVisibility(); }
                });
                if (!my9DigitId.isEmpty()) {
                    db.collection("hosts").document(my9DigitId).addSnapshotListener((snap, e) -> {
                        if (e != null) return;
                        if (snap != null && snap.exists()) { prefs.edit().putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply(); applyRoleVisibility(); }
                    });
                }
            }
        } catch (Exception ignored) {}
    }
    private void openWalletDialog() {
        try { WalletRechargeDialog dialog = new WalletRechargeDialog(); dialog.show(getSupportFragmentManager(), "WalletRechargeDialog"); }
        catch (Exception e) { Toast.makeText(this, "Wallet - Coming Soon", Toast.LENGTH_SHORT).show(); }
    }
    private void showRechargeLocked() {
        try { WalletRechargeDialog dialog = new WalletRechargeDialog(); dialog.show(getSupportFragmentManager(), "WalletRechargeDialog"); }
        catch (Exception e) { Toast.makeText(this, "Recharge - Coming Soon", Toast.LENGTH_SHORT).show(); }
    }
    @Override protected void onDestroy() { super.onDestroy(); try { unregisterReceiver(profileReceiver); } catch (Exception ignored) {} }
    private TextView addCountItem(LinearLayout parent, String num, String label, Runnable onClick) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(v -> onClick.run()); parent.addView(box);
        TextView tvNum = new TextView(this); tvNum.setText(num); tvNum.setTextColor(Color.WHITE); tvNum.setTextSize(18); tvNum.setTypeface(null, Typeface.BOLD); box.addView(tvNum);
        TextView tvLabel = new TextView(this); tvLabel.setText(label); tvLabel.setTextColor(Color.parseColor("#B0BEC5")); tvLabel.setTextSize(12); box.addView(tvLabel);
        return tvNum;
    }
    private LinearLayout makeBigCard(String title, String sub, String bgColor, int titleColor) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(25, 20, 25, 20);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.parseColor(bgColor)); bg.setCornerRadius(24); box.setBackground(bg);
        TextView t1 = new TextView(this); t1.setText(title); t1.setTextColor(titleColor); t1.setTextSize(17); t1.setTypeface(null, Typeface.BOLD); t1.setGravity(Gravity.CENTER); box.addView(t1);
        TextView t2 = new TextView(this); t2.setText(sub); t2.setTextColor(Color.parseColor("#FFFFFF")); t2.setTextSize(12); t2.setGravity(Gravity.CENTER); box.addView(t2);
        return box;
    }
    private LinearLayout addToolItem(LinearLayout parent, String icon, String label) { return addToolItem(parent, icon, label, v -> Toast.makeText(this, label + " - Coming Soon", Toast.LENGTH_SHORT).show()); }
    private LinearLayout addToolItem(LinearLayout parent, String icon, String label, View.OnClickListener click) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(click); parent.addView(box);
        TextView iv = new TextView(this); iv.setText(icon); iv.setTextSize(30); iv.setGravity(Gravity.CENTER); box.addView(iv);
        TextView tv = new TextView(this); tv.setText(label); tv.setTextColor(Color.WHITE); tv.setTextSize(12); tv.setGravity(Gravity.CENTER); box.addView(tv);
        return box;
    }
    private LinearLayout addMenuRow(LinearLayout parent, String title) { return addMenuRow(parent, title, v -> Toast.makeText(this, title + " - Coming Soon", Toast.LENGTH_SHORT).show()); }
    private LinearLayout addMenuRow(LinearLayout parent, String title, View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(30, 26, 30, 26); row.setOnClickListener(click); parent.addView(row);
        TextView t = new TextView(this); t.setText(title); t.setTextColor(Color.WHITE); t.setTextSize(15); t.setTypeface(null, Typeface.BOLD);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1)); row.addView(t);
        TextView arrow = new TextView(this); arrow.setText(">"); arrow.setTextColor(Color.parseColor("#B0BEC5")); arrow.setTextSize(16); arrow.setTypeface(null, Typeface.BOLD); row.addView(arrow);
        return row;
    }
    private void goProfile() {
        try { Intent i = new Intent(MeActivity.this, PublicProfileActivity.class); i.putExtra("TARGET_9DIGIT_ID", my9DigitId); i.putExtra("TARGET_NAME", prefs.getString("USER_NAME", "User")); startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
    }
    private void showCountDialog(String type, String count) { new AlertDialog.Builder(this).setTitle(type + " - " + count).setMessage("Your " + type + " list will appear here.\n\nOnly you can see this.").setPositiveButton("OK", null).show(); }
    private String getGenderCountryText() {
        String g = prefs.getString("USER_GENDER", ""); String c = prefs.getString("USER_COUNTRY", ""); String base = "";
        if (!g.isEmpty() && !c.isEmpty()) base = g + " | " + c; else if (!c.isEmpty()) base = c; else if (!g.isEmpty()) base = g; else base = "Complete Your Profile";
        if (prefs.getBoolean("IS_AGENCY", false) && prefs.getBoolean("AGENCY_ACTIVE", false)) base = base + "  |  🏷️ AGENCY";
        if (prefs.getBoolean("IS_SELLER", false)) base = base + "  |  🏪 SELLER";
        return base;
    }
    @Override protected void onResume() { super.onResume(); showFromPrefs(); applyRoleVisibility(); applySellerVisibility(); loadMeData(); }
    private void showFromPrefs() {
        String name = prefs.getString("USER_NAME", "User"); String photo = prefs.getString("USER_PHOTO", "");
        if (photo.isEmpty()) photo = prefs.getString("PROFILE_PHOTO", "");
        my9DigitId = prefs.getString("MY_9_DIGIT_ID", my9DigitId); myVipLevel = prefs.getInt("VIP_LEVEL", 0);
        if (txtName != null) txtName.setText(name);
        if (txtId != null) txtId.setText("ID: " + my9DigitId);
        if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
        updateVipUI(); showPhoto(photo, imgAvatar);
    }
    private void loadMeData() {
        if (userRef == null) return; if (rtdbListenerStarted) return; rtdbListenerStarted = true;
        userRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                try {
                    if (!snap.exists()) return;
                    String name = getStr(snap, "name", prefs.getString("USER_NAME", "User"));
                    String id = getStr(snap, "my9DigitId", my9DigitId);
                    if (id.isEmpty()) id = getStr(snap, "idNumber", my9DigitId);
                    if (id.isEmpty()) id = getStr(snap, "nineDigitId", my9DigitId);
                    String country = getStr(snap, "country", prefs.getString("USER_COUNTRY", ""));
                    String gender = getStr(snap, "gender", prefs.getString("USER_GENDER", ""));
                    String photo = getStr(snap, "photoUrl", "");
                    if (photo.isEmpty()) photo = getStr(snap, "photo", "");
                    if (photo.isEmpty()) photo = getStr(snap, "photoBase64", "");
                    followCount = getStr(snap, "followCount", "0"); fansCount = getStr(snap, "fansCount", "0"); visitorCount = getStr(snap, "visitorCount", "0");
                    String coins = getStr(snap, "coins", "0");
                    if (coins.equals("0")) coins = getStr(snap, "walletCoins", "0");
                    if (coins.equals("0")) coins = getStr(snap, "sendingCoins", "0");
                    int vip = 0; try { String vipStr = getStr(snap, "vipLevel", "0"); vip = Integer.parseInt(vipStr); } catch (Exception ignored) {}
                    if (myVipLevel > vip) vip = myVipLevel;
                    myVipLevel = vip;
                    String role = getStr(snap, "role", "user").toLowerCase();
                    boolean isAgency = getBool(snap, "isAgency", false);
                    String agencyStatus = getStr(snap, "agencyStatus", "").toLowerCase();
                    String agencyName = getStr(snap, "agencyName", "SR Agency");
                    String agencyPercent = getStr(snap, "agencyPercent", "0");
                    if (agencyPercent.equals("0")) agencyPercent = getStr(snap, "agencyCommission", "0");
                    boolean isHost = getBool(snap, "isHost", false);
                    String hostStatus = getStr(snap, "hostStatus", "").toLowerCase();
                    String agencyIdForHost = getStr(snap, "hostAgencyId", "");
                    if (agencyIdForHost.isEmpty()) agencyIdForHost = getStr(snap, "agencyId", "");
                    String hostAgencyName = getStr(snap, "hostAgencyName", "");
                    boolean agencyActive = isAgency && !"blocked".equals(agencyStatus) && !"rejected".equals(agencyStatus);
                    boolean hostActive = isHost && !"blocked".equals(hostStatus) && !"rejected".equals(hostStatus);
                    if (!id.isEmpty()) { my9DigitId = id; prefs.edit().putString("MY_9_DIGIT_ID", id).apply(); }
                    prefs.edit().putString("USER_NAME", name).putString("USER_COUNTRY", country).putString("USER_GENDER", gender)
                        .putString("USER_PHOTO", photo).putString("PROFILE_PHOTO", photo)
                        .putBoolean("RTDB_IS_AGENCY", isAgency).putBoolean("RTDB_IS_HOST", isHost)
                        .putBoolean("IS_AGENCY", isAgency).putBoolean("AGENCY_ACTIVE", agencyActive)
                        .putString("AGENCY_NAME", agencyName).putString("AGENCY_PERCENT", agencyPercent)
                        .putBoolean("IS_HOST", isHost).putBoolean("HOST_ACTIVE", hostActive)
                        .putString("HOST_AGENCY_ID", agencyIdForHost).putString("HOST_AGENCY_NAME", hostAgencyName)
                        .putString("USER_ROLE", role).putInt("VIP_LEVEL", vip).apply();
                    if (txtName != null) txtName.setText(name);
                    if (txtId != null) txtId.setText("ID: " + my9DigitId);
                    if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
                    if (txtFollow != null) txtFollow.setText(followCount);
                    if (txtFans != null) txtFans.setText(fansCount);
                    if (txtVisitor != null) txtVisitor.setText(visitorCount);
                    if (txtWalletCoins != null) txtWalletCoins.setText("Coins: " + coins);
                    showPhoto(photo, imgAvatar); applyRoleVisibility(); updateVipUI();
                    boolean pending = getBool(snap, "pendingHostInvite", false);
                    String pendingAgency = getStr(snap, "pendingAgencyName", "");
                    String pendingAgencyId = getStr(snap, "pendingAgencyId", "");
                    if (pending && !inviteDialogShown && !isHost) { inviteDialogShown = true; showHostInviteDialog(pendingAgency, pendingAgencyId); }
                } catch (Exception ignored) {}
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void showHostInviteDialog(String agencyName, String agencyId) {
        new AlertDialog.Builder(this).setTitle("🎤 Host Invite").setMessage((agencyName.isEmpty() ? "Agency" : agencyName) + " tomake Host hisabe invite koreche.\n\nAccept korle tomar Me page e Host Center chole asbe.")
            .setCancelable(false).setPositiveButton("✅ Accept", (d, w) -> acceptHostInvite(agencyId, agencyName)).setNegativeButton("❌ Reject", (d, w) -> rejectHostInvite()).show();
    }
    private void acceptHostInvite(String agencyId, String agencyName) {
        if (firebaseUid.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(my9DigitId.isEmpty() ? firebaseUid : my9DigitId);
        ref.child("isHost").setValue(true); ref.child("hostStatus").setValue("active"); ref.child("role").setValue("host");
        ref.child("hostAgencyId").setValue(agencyId); ref.child("hostAgencyName").setValue(agencyName);
        ref.child("pendingHostInvite").setValue(false); ref.child("pendingAgencyId").setValue(null); ref.child("pendingAgencyName").setValue(null);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("ownerUid").setValue(firebaseUid);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("agencyId").setValue(agencyId);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("agencyName").setValue(agencyName);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("status").setValue("active");
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("createdAt").setValue(System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("hostInvites").orderByChild("hostUid").equalTo(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { for (DataSnapshot c : s.getChildren()) { if ("pending".equals(String.valueOf(c.child("status").getValue()))) c.getRef().child("status").setValue("accepted"); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        Toast.makeText(this, "✅ Host Confirm hoye geche!", Toast.LENGTH_SHORT).show(); loadMeData();
    }
    private void rejectHostInvite() {
        if (firebaseUid.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(my9DigitId.isEmpty() ? firebaseUid : my9DigitId);
        ref.child("pendingHostInvite").setValue(false); ref.child("pendingAgencyId").setValue(null); ref.child("pendingAgencyName").setValue(null);
        Toast.makeText(this, "Invite Reject kora hoyeche", Toast.LENGTH_SHORT).show();
    }
    private void showPhoto(String photo, ImageView iv) {
        if (iv == null || photo == null || photo.isEmpty()) return;
        try { if (photo.startsWith("http")) Glide.with(this).load(photo).circleCrop().into(iv); else showBase64Image(photo, iv); } catch (Exception ignored) {}
    }
    private void showBase64Image(String base64, ImageView iv) {
        if (iv == null || base64 == null || base64.isEmpty()) return;
        try { byte[] bytes = Base64.decode(base64, Base64.DEFAULT); BitmapFactory.Options opt = new BitmapFactory.Options(); opt.inSampleSize = 2; Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt); if (b != null) Glide.with(this).load(b).circleCrop().into(iv); } catch (Exception ignored) {}
    }
    private String getStr(DataSnapshot s, String k, String def) { if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue()); return def; }
    private boolean getBool(DataSnapshot s, String k, boolean def) { if (s.hasChild(k) && s.child(k).getValue() != null) { Object o = s.child(k).getValue(); if (o instanceof Boolean) return (Boolean) o; return "true".equalsIgnoreCase(String.valueOf(o)); } return def; }
}
