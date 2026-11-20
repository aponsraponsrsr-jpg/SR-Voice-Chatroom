package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class SellerBusinessWalletActivity extends AppCompatActivity {
    // PROFILE ID ONLY SYSTEM - UID TUMI LIKHBA NA - APP VITOR THEKE AUTO MILABE
    // My Profile ID = 477984629, Buyer Profile ID = 845083699 - SUDHU EITA LIKHBA
    private String myProfileId = "";
    private String myUid = ""; // Auto Match hobe - Profile ID -> UID - Tumi dekhba na
    private TextView txtBalance;
    private EditText edtTargetId, edtAmount;
    private long businessCoins = 0;
    private boolean isSeller = false;
    private boolean uidResolved = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        // PROFILE ID - Intent > Prefs - Sob purono key + notun key try korbe
        myProfileId = getIntent().getStringExtra("SELLER_PROFILE_ID");
        if (myProfileId == null || myProfileId.isEmpty()) myProfileId = getIntent().getStringExtra("SELLER_ANY_ID");
        if (myProfileId == null || myProfileId.isEmpty()) myProfileId = getIntent().getStringExtra("SELLER_9DIGIT_ID");
        if (myProfileId == null || myProfileId.isEmpty()) myProfileId = getIntent().getStringExtra("PROFILE_ID");
        if (myProfileId == null || myProfileId.isEmpty()) myProfileId = prefs.getString("MY_PROFILE_ID", "");
        if (myProfileId == null || myProfileId.isEmpty()) myProfileId = prefs.getString("PROFILE_ID", "");
        if (myProfileId == null || myProfileId.isEmpty()) myProfileId = prefs.getString("MY_ANY_ID", "");
        if (myProfileId == null || myProfileId.isEmpty()) myProfileId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myProfileId == null) myProfileId = "";
        myProfileId = myProfileId.trim();
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null) myUid = u.getUid();
        if (myUid.isEmpty()) myUid = prefs.getString("FIREBASE_UID", "");
        if (myUid == null) myUid = "";

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 40, 30, 30);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🏪 Seller Business Wallet");
        title.setTextColor(Color.WHITE); title.setTextSize(22); title.setTypeface(null, Typeface.BOLD);
        root.addView(title);
        TextView sub = new TextView(this);
        sub.setText("Only YOU can see this. Normal users cannot see.\nBusiness Coins are separate from My Wallet.");
        sub.setTextColor(Color.parseColor("#B0BEC5")); sub.setTextSize(13);
        sub.setPadding(0, 10, 0, 25);
        root.addView(sub);

        LinearLayout balBox = new LinearLayout(this);
        balBox.setOrientation(LinearLayout.VERTICAL);
        balBox.setPadding(30, 28, 30, 28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColors(new int[]{Color.parseColor("#00C853"), Color.parseColor("#00796B")});
        bg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        bg.setCornerRadius(24);
        balBox.setBackground(bg);
        root.addView(balBox);
        TextView l1 = new TextView(this); l1.setText("Business Balance"); l1.setTextColor(Color.WHITE); l1.setTextSize(14); balBox.addView(l1);
        txtBalance = new TextView(this); txtBalance.setText("Coins: 0"); txtBalance.setTextColor(Color.WHITE); txtBalance.setTextSize(26); txtBalance.setTypeface(null, Typeface.BOLD); balBox.addView(txtBalance);
        TextView myId = new TextView(this); myId.setText("My Profile ID: " + myProfileId); myId.setTextColor(Color.WHITE); myId.setTextSize(13); myId.setPadding(0, 8, 0, 0); balBox.addView(myId);

        TextView t2 = new TextView(this); t2.setText("\nSell Coins To User"); t2.setTextColor(Color.parseColor("#FFD700")); t2.setTextSize(17); t2.setTypeface(null, Typeface.BOLD); root.addView(t2);

        edtTargetId = new EditText(this);
        edtTargetId.setHint("Buyer Profile ID - ex: 845083699");
        edtTargetId.setInputType(InputType.TYPE_CLASS_TEXT);
        edtTargetId.setTextColor(Color.WHITE); edtTargetId.setHintTextColor(Color.parseColor("#888888"));
        edtTargetId.setPadding(25, 25, 25, 25);
        edtTargetId.setBackground(makeInputBg());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 15); edtTargetId.setLayoutParams(lp);
        root.addView(edtTargetId);

        edtAmount = new EditText(this);
        edtAmount.setHint("Amount - e.g. 1000");
        edtAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtAmount.setTextColor(Color.WHITE); edtAmount.setHintTextColor(Color.parseColor("#888888"));
        edtAmount.setPadding(25, 25, 25, 25);
        edtAmount.setBackground(makeInputBg());
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(0, 0, 0, 20); edtAmount.setLayoutParams(lp2);
        root.addView(edtAmount);

        Button btnSell = new Button(this);
        btnSell.setText("✅ CONFIRM & SELL");
        btnSell.setTextColor(Color.WHITE); btnSell.setTextSize(16); btnSell.setTypeface(null, Typeface.BOLD);
        btnSell.setBackground(makeBtnBg());
        btnSell.setPadding(20, 25, 20, 25);
        btnSell.setOnClickListener(v -> confirmSell());
        root.addView(btnSell);

        TextView warn = new TextView(this);
        warn.setText("\n⚠️ Coins will be deducted from YOUR Business Wallet only.\nYou CANNOT create coins. Only Company can load coins to you.");
        warn.setTextColor(Color.parseColor("#FFAB91")); warn.setTextSize(12);
        warn.setGravity(Gravity.CENTER);
        root.addView(warn);

        setContentView(scroll);
        // AGE Profile ID -> UID AUTO MATCH, TARPOR BALANCE SUNBO
        resolveMyUidThenListen();
    }

    private GradientDrawable makeInputBg() {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor("#1B1035")); d.setCornerRadius(20);
        d.setStroke(2, Color.parseColor("#FFD700"));
        return d;
    }
    private GradientDrawable makeBtnBg() {
        GradientDrawable d = new GradientDrawable();
        d.setColors(new int[]{Color.parseColor("#00C853"), Color.parseColor("#00796B")});
        d.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        d.setCornerRadius(24);
        return d;
    }

    private boolean getBool(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        if (v instanceof Boolean) return (Boolean) v;
        return "true".equalsIgnoreCase(String.valueOf(v));
    }
    private long getLong(DataSnapshot snap, String key) {
        try { Object v = snap.child(key).getValue(); if (v == null) return 0; return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0; }
    }
    private long getMaxBalance(DataSnapshot snap) {
        return Math.max(Math.max(getLong(snap, "businessCoins"), getLong(snap, "coinBalance")), Math.max(getLong(snap, "businessBalance"), getLong(snap, "sellerCoinBalance")));
    }
    private String safeStr(DataSnapshot snap, String key) {
        Object v = snap.child(key).getValue();
        return v == null ? "" : String.valueOf(v).trim();
    }
    // Profile ID milano - Sob field check korbo - profileId / anyId / myId / idNumber / displayId / userId / srId / my9DigitId
    private boolean profileIdMatches(DataSnapshot u, String target) {
        if (target == null || target.isEmpty()) return false;
        String t = target.trim().toLowerCase();
        String[] fields = new String[]{ safeStr(u, "profileId"), safeStr(u, "anyId"), safeStr(u, "myId"), safeStr(u, "idNumber"), safeStr(u, "displayId"), safeStr(u, "userId"), safeStr(u, "srId"), safeStr(u, "my9DigitId"), safeStr(u, "my9DigitID"), safeStr(u, "profileID") };
        for (String f : fields) { if (!f.isEmpty() && f.toLowerCase().equals(t)) return true; }
        return false;
    }

    // ===== 1. PROFILE ID -> UID AUTO MATCH =====
    // Tumi sudhu Profile ID diba - Firebase Service theke UID auto khuje nibo
    private void resolveMyUidThenListen() {
        if (myProfileId.isEmpty()) {
            if (!myUid.isEmpty()) { listenSellerAllKeys(); return; }
            Toast.makeText(this, "Profile ID not found", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        // Jodi Profile ID tai asole UID hoy - tahole scan lagbe na
        FirebaseDatabase.getInstance().getReference("users").child(myProfileId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot direct) {
                    if (direct.exists() && (profileIdMatches(direct, myProfileId) || !myUid.isEmpty())) {
                        // users/{Profile ID} tei user ache - UID = Profile ID / Auth UID
                        if (myUid.isEmpty()) myUid = direct.getKey();
                        uidResolved = true;
                        listenSellerAllKeys();
                        return;
                    }
                    // Na paile - Sob user scan kore Profile ID milao - UID ber koro
                    FirebaseDatabase.getInstance().getReference("users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot all) {
                                for (DataSnapshot u : all.getChildren()) {
                                    if (profileIdMatches(u, myProfileId)) { myUid = u.getKey(); break; }
                                }
                                uidResolved = true;
                                listenSellerAllKeys();
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) { listenSellerAllKeys(); }
                        });
                }
                @Override public void onCancelled(@NonNull DatabaseError e) { listenSellerAllKeys(); }
            });
    }

    // ===== 2. BALANCE - 4 JAYGA THEKE MAX =====
    // sellers/{Profile ID} + sellers/{UID} + users/{UID} + users/{Profile ID}
    // Jekhane beshi - oitai real balance - 0 diye 100000 muche felbo na
    private void listenSellerAllKeys() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        // 1. sellers/{Profile ID} - ex: sellers/477984629
        if (!myProfileId.isEmpty()) {
            db.getReference("sellers").child(myProfileId).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) { if (snap.exists()) syncFromSnap(snap); }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
        // 2. sellers/{UID} - Admin ekhane Load korechilo - eita miss korlei 0 dekhato
        if (!myUid.isEmpty() && !myUid.equals(myProfileId)) {
            db.getReference("sellers").child(myUid).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) { if (snap.exists()) syncFromSnap(snap); }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
        // 3. users/{UID} - Me ekhan theke 100000 dekhacche
        if (!myUid.isEmpty()) {
            db.getReference("users").child(myUid).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!snap.exists()) return;
                    boolean flag = getBool(snap, "isSeller") || getBool(snap, "showSellerWallet");
                    if (!flag) return;
                    long b = Math.max(getLong(snap, "businessCoins"), getLong(snap, "sellerCoinBalance"));
                    if (b > 0) syncBalance(b, true);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
        // 4. users/{Profile ID}
        if (!myProfileId.isEmpty() && !myProfileId.equals(myUid)) {
            db.getReference("users").child(myProfileId).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!snap.exists()) return;
                    boolean flag = getBool(snap, "isSeller") || getBool(snap, "showSellerWallet");
                    if (!flag) return;
                    long b = Math.max(getLong(snap, "businessCoins"), getLong(snap, "sellerCoinBalance"));
                    if (b > 0) syncBalance(b, true);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
        // Jodi kono jaygay seller flag na thake - 2 sec por check korbo - tokhono na paile Seller na bolbo
        txtBalance.postDelayed(() -> {
            if (!isSeller && businessCoins == 0) {
                // Chup chap thakbo - User ke ber kore dibo na - karon Admin pore Load korle Balance asbe
            }
        }, 2000);
    }

    private void syncFromSnap(DataSnapshot snap) {
        String st = snap.child("status").getValue() == null ? "" : String.valueOf(snap.child("status").getValue()).toLowerCase();
        if ("blocked".equals(st) || "rejected".equals(st) || "pending".equals(st)) return;
        // sellers node e thaka manei seller
        long b = getMaxBalance(snap);
        // 0 asle ager 100000 muche felbo na - sudhu beshi hoile update korbo
        if (b > 0) syncBalance(b, true);
        else if (businessCoins == 0) syncBalance(0, true);
    }

    private void syncBalance(long newBal, boolean sellerFound) {
        if (newBal > businessCoins) businessCoins = newBal;
        if (sellerFound) isSeller = true;
        if (txtBalance != null) txtBalance.setText("Coins: " + businessCoins);
    }

    private void confirmSell() {
        if (!isSeller) { Toast.makeText(this, "You are not a Seller", Toast.LENGTH_SHORT).show(); return; }
        String target = edtTargetId.getText().toString().trim();
        String amtStr = edtAmount.getText().toString().trim();
        if (target.isEmpty()) { Toast.makeText(this, "Enter Buyer Profile ID", Toast.LENGTH_SHORT).show(); return; }
        if (target.equals(myProfileId) || (!myUid.isEmpty() && target.equals(myUid))) { Toast.makeText(this, "You cannot sell to yourself", Toast.LENGTH_SHORT).show(); return; }
        long amt = 0;
        try { amt = Long.parseLong(amtStr); } catch (Exception e) { Toast.makeText(this, "Enter correct Amount", Toast.LENGTH_SHORT).show(); return; }
        if (amt <= 0) { Toast.makeText(this, "Amount must be > 0", Toast.LENGTH_SHORT).show(); return; }
        if (amt > businessCoins) { Toast.makeText(this, "Not enough Business Coins. Balance: " + businessCoins, Toast.LENGTH_LONG).show(); return; }
        long finalAmt = amt;
        new AlertDialog.Builder(this)
            .setTitle("Confirm Sell?")
            .setMessage("Sell " + finalAmt + " Coins\nTo Buyer Profile ID: " + target + "\n\nFrom your Business Wallet?")
            .setPositiveButton("✅ YES, SELL", (d, w) -> resolveBuyerAndSell(target, finalAmt))
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ===== 3. BUYER PROFILE ID -> BUYER UID AUTO MATCH =====
    // Tumi Buyer er Profile ID diba - 845083699 - Vitor theke UID milabe
    private void resolveBuyerAndSell(String targetProfileId, long amount) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(targetProfileId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) { doSell(targetProfileId, snap.getKey(), amount); return; }
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot all) {
                        String foundUid = null;
                        for (DataSnapshot u : all.getChildren()) {
                            if (profileIdMatches(u, targetProfileId)) { foundUid = u.getKey(); break; }
                        }
                        if (foundUid == null) { Toast.makeText(SellerBusinessWalletActivity.this, "Buyer not found - Profile ID: " + targetProfileId, Toast.LENGTH_SHORT).show(); return; }
                        doSell(targetProfileId, foundUid, amount);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { Toast.makeText(SellerBusinessWalletActivity.this, "Buyer search failed", Toast.LENGTH_SHORT).show(); }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { Toast.makeText(SellerBusinessWalletActivity.this, "Buyer search failed", Toast.LENGTH_SHORT).show(); }
        });
    }

    // ===== 4. SELL - 4 JAYGAY BALANCE KOMABO - 1 JAYGAY NA =====
    // AGE TRANSACTION CHILO SUDHU sellers/{Profile ID} - 0 THEKE - TAI ABORT HOITO
    // EKHON MAX BALANCE THEKE HISAB KORE - 4 JAYGAY SETVALUE KORBO
    private void doSell(String targetProfileId, String buyerUid, long amount) {
        long newBal = businessCoins - amount;
        if (newBal < 0) { Toast.makeText(this, "Not enough Business Coins. Balance: " + businessCoins, Toast.LENGTH_LONG).show(); return; }
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        // Seller - 4 jaygay ek sathe komabo - Me + Wallet + Admin - 3 jaygay ek dekhabe
        if (!myProfileId.isEmpty()) {
            db.getReference("sellers").child(myProfileId).child("businessCoins").setValue(newBal);
            db.getReference("sellers").child(myProfileId).child("coinBalance").setValue(newBal);
            db.getReference("sellers").child(myProfileId).child("businessBalance").setValue(newBal);
            db.getReference("users").child(myProfileId).child("businessCoins").setValue(newBal);
            db.getReference("users").child(myProfileId).child("sellerCoinBalance").setValue(newBal);
        }
        if (!myUid.isEmpty() && !myUid.equals(myProfileId)) {
            db.getReference("sellers").child(myUid).child("businessCoins").setValue(newBal);
            db.getReference("sellers").child(myUid).child("coinBalance").setValue(newBal);
            db.getReference("sellers").child(myUid).child("businessBalance").setValue(newBal);
            db.getReference("users").child(myUid).child("businessCoins").setValue(newBal);
            db.getReference("users").child(myUid).child("sellerCoinBalance").setValue(newBal);
            db.getReference("users").child(myUid).child("sellerBalance").setValue(newBal);
        }
        businessCoins = newBal;
        if (txtBalance != null) txtBalance.setText("Coins: " + businessCoins);

        // Buyer ke Coins dao - Transaction
        DatabaseReference buyerCoins = db.getReference("users").child(buyerUid).child("coins");
        buyerCoins.runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData cur) {
                Long v = cur.getValue(Long.class);
                if (v == null) { try { Object o = cur.getValue(); v = o == null ? 0L : Long.parseLong(String.valueOf(o)); } catch (Exception e) { v = 0L; } }
                cur.setValue(v + amount);
                return Transaction.success(cur);
            }
            @Override public void onComplete(@Nullable DatabaseError e2, boolean c2, @Nullable DataSnapshot s2) {
                if (!c2) { Toast.makeText(SellerBusinessWalletActivity.this, "Buyer not found", Toast.LENGTH_SHORT).show(); return; }
                long buyerNew = 0;
                try { Object bv = s2.getValue(); buyerNew = bv == null ? amount : Long.parseLong(String.valueOf(bv)); } catch (Exception e) { buyerNew = amount; }
                db.getReference("users").child(buyerUid).child("coinBalance").setValue(buyerNew);
                db.getReference("users").child(buyerUid).child("walletCoins").setValue(buyerNew);
                // totalSold update - Profile ID + UID - 2 jaygay
                if (!myProfileId.isEmpty()) {
                    db.getReference("sellers").child(myProfileId).child("totalSold").runTransaction(new Transaction.Handler() {
                        @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData cur) {
                            Long v = cur.getValue(Long.class); if (v == null) { try { Object o = cur.getValue(); v = o == null ? 0L : Long.parseLong(String.valueOf(o)); } catch (Exception e) { v = 0L; } }
                            cur.setValue(v + amount); return Transaction.success(cur);
                        }
                        @Override public void onComplete(@Nullable DatabaseError e, boolean c, @Nullable DataSnapshot s) {}
                    });
                }
                String pushId = db.getReference("seller_sales").push().getKey();
                if (pushId != null) {
                    Map<String, Object> log = new HashMap<>();
                    log.put("sellerId", myProfileId);
                    log.put("sellerUid", myUid);
                    log.put("buyerId", targetProfileId);
                    log.put("buyerUid", buyerUid);
                    log.put("amount", amount);
                    log.put("type", "SELLER_SALE");
                    log.put("createdAt", System.currentTimeMillis());
                    db.getReference("seller_sales").child(pushId).setValue(log);
                    Map<String, Object> tx = new HashMap<>();
                    tx.put("type", "SELLER_TO_USER");
                    tx.put("typeLabel", "Seller → User Sale");
                    tx.put("amount", amount);
                    tx.put("fromUid", myUid);
                    tx.put("fromLabel", "Seller " + myProfileId);
                    tx.put("toUid", buyerUid);
                    tx.put("toLabel", "User (Profile ID: " + targetProfileId + ")");
                    tx.put("sellerUid", myUid);
                    tx.put("status", "Done");
                    tx.put("createdAt", System.currentTimeMillis());
                    db.getReference("coin_transactions").push().setValue(tx);
                    Map<String, Object> inbox = new HashMap<>();
                    inbox.put("type", "COIN_SYSTEM");
                    inbox.put("title", "🪙 " + amount + " Coins Peyecho!");
                    inbox.put("message", "Seller (Profile ID: " + myProfileId + ") tomake " + amount + " Coins diyeche. Tomar Total Coins ekhon: " + buyerNew + ".");
                    inbox.put("status", "unread");
                    inbox.put("timestamp", System.currentTimeMillis());
                    inbox.put("createdAt", System.currentTimeMillis());
                    inbox.put("fromId", "SR_SELLER");
                    inbox.put("fromName", "SR Seller");
                    inbox.put("amount", amount);
                    db.getReference("users").child(buyerUid).child("inbox").push().setValue(inbox);
                }
                Toast.makeText(SellerBusinessWalletActivity.this, "✅ Sold " + amount + " Coins to " + targetProfileId, Toast.LENGTH_LONG).show();
                edtTargetId.setText(""); edtAmount.setText("");
            }
        });
    }
}
