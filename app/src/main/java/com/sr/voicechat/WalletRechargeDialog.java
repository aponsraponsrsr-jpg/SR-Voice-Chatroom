package com.sr.voicechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WalletRechargeDialog extends BottomSheetDialogFragment {

    private TextView tvCoinBalance;
    private DatabaseReference userRef;
    private ValueEventListener coinEventListener;
    private SharedPreferences prefs;

    // 5 Packages - Same as Demo
    private final long[] PACKAGE_COINS = {100000, 500000, 1000000, 5000000, 10000000};
    private final String[] PACKAGE_PRICES = {"USD 0.99", "USD 4.99", "USD 9.99", "USD 49.99", "USD 99.99"};
    private int selectedIndex = 0;
    private LinearLayout[] packageViews = new LinearLayout[5];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        prefs = requireActivity().getSharedPreferences("SR_USER", Context.MODE_PRIVATE);

        String firebaseUid = "";
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu != null) firebaseUid = fu.getUid();
        if (firebaseUid == null || firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");
        String nineDigitId = prefs.getString("MY_9_DIGIT_ID", "");
        if (nineDigitId == null) nineDigitId = "";

        if (firebaseUid != null && !firebaseUid.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUid);
        } else if (!nineDigitId.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(nineDigitId);
        }

        // ===== ROOT - WHITE PREMIUM SHEET =====
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 28, 28, 28);

        // Top Bar - Back + Coins Title
        LinearLayout topBar = new LinearLayout(requireContext());
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(topBar);

        TextView btnBack = new TextView(requireContext());
        btnBack.setText("<");
        btnBack.setTextSize(22);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setTextColor(Color.BLACK);
        btnBack.setPadding(10, 10, 20, 10);
        btnBack.setOnClickListener(v -> dismiss());
        topBar.addView(btnBack);

        TextView title = new TextView(requireContext());
        title.setText("Coins");
        title.setTextSize(19);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        title.setGravity(Gravity.CENTER);
        topBar.addView(title);

        TextView spacer = new TextView(requireContext());
        spacer.setText("   ");
        topBar.addView(spacer);

        // Coins Balance Row - Like Demo
        LinearLayout coinsRow = new LinearLayout(requireContext());
        coinsRow.setOrientation(LinearLayout.HORIZONTAL);
        coinsRow.setGravity(Gravity.CENTER_VERTICAL);
        coinsRow.setPadding(10, 24, 10, 8);
        root.addView(coinsRow);

        TextView coinIcon = new TextView(requireContext());
        coinIcon.setText("🪙");
        coinIcon.setTextSize(28);
        coinIcon.setPadding(0, 0, 12, 0);
        coinsRow.addView(coinIcon);

        tvCoinBalance = new TextView(requireContext());
        tvCoinBalance.setText("0");
        tvCoinBalance.setTextSize(28);
        tvCoinBalance.setTypeface(null, Typeface.BOLD);
        tvCoinBalance.setTextColor(Color.BLACK);
        coinsRow.addView(tvCoinBalance);

        // Detailed Bill
        TextView bill = new TextView(requireContext());
        bill.setText("Detailed Bill  >");
        bill.setTextSize(13);
        bill.setTextColor(Color.parseColor("#9E9E9E"));
        bill.setPadding(10, 4, 10, 20);
        root.addView(bill);

        // Recharge Label
        TextView rechargeLabel = new TextView(requireContext());
        rechargeLabel.setText("Recharge");
        rechargeLabel.setTextSize(17);
        rechargeLabel.setTypeface(null, Typeface.BOLD);
        rechargeLabel.setTextColor(Color.BLACK);
        rechargeLabel.setPadding(10, 4, 10, 14);
        root.addView(rechargeLabel);

        // ===== 5 PACKAGE BOXES - SEPARATE BANNERS =====
        for (int i = 0; i < 5; i++) {
            final int index = i;
            LinearLayout box = new LinearLayout(requireContext());
            box.setOrientation(LinearLayout.HORIZONTAL);
            box.setGravity(Gravity.CENTER_VERTICAL);
            box.setPadding(24, 22, 24, 22);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.WHITE);
            bg.setCornerRadius(18);
            bg.setStroke(2, Color.parseColor("#FFD700"));
            box.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 14);
            box.setLayoutParams(lp);
            box.setOnClickListener(v -> selectPackage(index));
            root.addView(box);
            packageViews[i] = box;

            TextView leftIcon = new TextView(requireContext());
            leftIcon.setText("🪙");
            leftIcon.setTextSize(18);
            leftIcon.setPadding(0, 0, 12, 0);
            box.addView(leftIcon);

            TextView coinsTxt = new TextView(requireContext());
            coinsTxt.setText(String.valueOf(PACKAGE_COINS[i]));
            coinsTxt.setTextSize(16);
            coinsTxt.setTypeface(null, Typeface.BOLD);
            coinsTxt.setTextColor(Color.BLACK);
            coinsTxt.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            box.addView(coinsTxt);

            TextView priceTxt = new TextView(requireContext());
            priceTxt.setText(PACKAGE_PRICES[i]);
            priceTxt.setTextSize(14);
            priceTxt.setTextColor(Color.parseColor("#757575"));
            box.addView(priceTxt);
        }
        updateSelection();

        // ===== BIG CHARGE BUTTON - YELLOW =====
        TextView btnCharge = new TextView(requireContext());
        btnCharge.setText("Charge");
        btnCharge.setTextSize(17);
        btnCharge.setTypeface(null, Typeface.BOLD);
        btnCharge.setTextColor(Color.parseColor("#3E2723"));
        btnCharge.setGravity(Gravity.CENTER);
        btnCharge.setPadding(20, 26, 20, 26);
        GradientDrawable chargeBg = new GradientDrawable();
        chargeBg.setColors(new int[]{Color.parseColor("#FFEB3B"), Color.parseColor("#FFC107")});
        chargeBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        chargeBg.setCornerRadius(30);
        btnCharge.setBackground(chargeBg);
        LinearLayout.LayoutParams chargeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chargeLp.setMargins(0, 16, 0, 10);
        btnCharge.setLayoutParams(chargeLp);
        btnCharge.setOnClickListener(v -> showContactSeller());
        root.addView(btnCharge);

        // Load Coins Live
        if (userRef != null) {
            coinEventListener = new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!isAdded()) return;
                    Long coins = null;
                    if (snap.child("sendingCoins").getValue() != null) {
                        try { coins = snap.child("sendingCoins").getValue(Long.class); } catch (Exception ignored) {}
                    }
                    if (coins == null && snap.child("coins").getValue() != null) {
                        try { coins = snap.child("coins").getValue(Long.class); } catch (Exception ignored) {}
                    }
                    if (coins == null && snap.child("walletCoins").getValue() != null) {
                        try { coins = snap.child("walletCoins").getValue(Long.class); } catch (Exception ignored) {}
                    }
                    if (tvCoinBalance != null) tvCoinBalance.setText(coins != null ? String.format("%,d", coins) : "0");
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            };
            userRef.addValueEventListener(coinEventListener);
        }

        return root;
    }

    private void selectPackage(int index) {
        selectedIndex = index;
        updateSelection();
    }

    private void updateSelection() {
        for (int i = 0; i < 5; i++) {
            if (packageViews[i] == null) continue;
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(18);
            if (i == selectedIndex) {
                bg.setColor(Color.parseColor("#FFFEF0"));
                bg.setStroke(3, Color.parseColor("#FFC107"));
            } else {
                bg.setColor(Color.WHITE);
                bg.setStroke(1, Color.parseColor("#E0E0E0"));
            }
            packageViews[i].setBackground(bg);
        }
    }

    // PERMANENT LOCK - No Google Play, No Card, No Online Payment
    // Click Charge -> Only Contact Seller / Coming Soon in ENGLISH
    private void showContactSeller() {
        if (!isAdded()) return;
        long selectedCoins = PACKAGE_COINS[selectedIndex];
        String selectedPrice = PACKAGE_PRICES[selectedIndex];
        new AlertDialog.Builder(requireContext())
            .setTitle("Coming Soon")
            .setMessage("You selected: " + selectedCoins + " Coins - " + selectedPrice + "\n\nOnline Recharge is currently OFF.\n\nTo buy coins, please Contact Seller.\n\nOfficial Seller will add coins to your Wallet.")
            .setPositiveButton("Contact Seller", (d, w) -> {
                // Future: Open Seller WhatsApp / Seller List
                // For now, only show message - No payment will open
                new AlertDialog.Builder(requireContext())
                    .setTitle("Contact Seller")
                    .setMessage("Please contact Official Seller to buy coins.\n\nYour ID will be used to add coins to your Wallet.")
                    .setPositiveButton("OK", null)
                    .show();
            })
            .setNegativeButton("Close", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRef != null && coinEventListener != null) userRef.removeEventListener(coinEventListener);
    }
}
