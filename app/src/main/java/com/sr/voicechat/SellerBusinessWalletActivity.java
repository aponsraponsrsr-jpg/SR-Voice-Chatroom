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
    private String my9DigitId = "", firebaseUid = "";
    private TextView txtBalance;
    private EditText edtTargetId, edtAmount;
    private long businessCoins = 0;
    private boolean isSeller = false;
    private String sellerKey = "";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        my9DigitId = getIntent().getStringExtra("SELLER_9DIGIT_ID");
        if (my9DigitId == null || my9DigitId.isEmpty()) my9DigitId = prefs.getString("MY_9_DIGIT_ID", "");
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null) firebaseUid = u.getUid();
        if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");
        sellerKey = !my9DigitId.isEmpty() ? my9DigitId : firebaseUid;

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
        TextView myId = new TextView(this); myId.setText("My Seller ID: " + my9DigitId); myId.setTextColor(Color.WHITE); myId.setTextSize(13); myId.setPadding(0, 8, 0, 0); balBox.addView(myId);

        TextView t2 = new TextView(this); t2.setText("\nSell Coins To User"); t2.setTextColor(Color.parseColor("#FFD700")); t2.setTextSize(17); t2.setTypeface(null, Typeface.BOLD); root.addView(t2);

        edtTargetId = new EditText(this);
        edtTargetId.setHint("Buyer User ID - e.g. 123456789");
        edtTargetId.setInputType(InputType.TYPE_CLASS_NUMBER);
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
        listenSeller();
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

    private void listenSeller() {
        if (sellerKey.isEmpty()) { Toast.makeText(this, "Seller ID not found", Toast.LENGTH_SHORT).show(); finish(); return; }
        FirebaseDatabase.getInstance().getReference("sellers").child(sellerKey)
            .addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!snap.exists()) { isSeller = false; Toast.makeText(SellerBusinessWalletActivity.this, "You are not a Seller", Toast.LENGTH_SHORT).show(); finish(); return; }
                    Object is = snap.child("isSeller").getValue();
                    boolean flag = is instanceof Boolean ? (Boolean) is : "true".equalsIgnoreCase(String.valueOf(is));
                    String st = snap.child("status").getValue() == null ? "" : String.valueOf(snap.child("status").getValue()).toLowerCase();
                    isSeller = flag && !"blocked".equals(st) && !"rejected".equals(st) && !"pending".equals(st);
                    if (!isSeller) { Toast.makeText(SellerBusinessWalletActivity.this, "Seller not active", Toast.LENGTH_SHORT).show(); finish(); return; }
                    long b = 0;
                    try { Object v = snap.child("businessCoins").getValue(); if (v != null) b = Long.parseLong(String.valueOf(v)); } catch (Exception ignored) {}
                    businessCoins = b;
                    if (txtBalance != null) txtBalance.setText("Coins: " + b);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }

    private void confirmSell() {
        if (!isSeller) { Toast.makeText(this, "You are not a Seller", Toast.LENGTH_SHORT).show(); return; }
        String target = edtTargetId.getText().toString().trim();
        String amtStr = edtAmount.getText().toString().trim();
        if (target.isEmpty() || target.length() < 5) { Toast.makeText(this, "Enter correct Buyer ID", Toast.LENGTH_SHORT).show(); return; }
        if (target.equals(my9DigitId) || target.equals(firebaseUid)) { Toast.makeText(this, "You cannot sell to yourself", Toast.LENGTH_SHORT).show(); return; }
        long amt = 0;
        try { amt = Long.parseLong(amtStr); } catch (Exception e) { Toast.makeText(this, "Enter correct Amount", Toast.LENGTH_SHORT).show(); return; }
        if (amt <= 0) { Toast.makeText(this, "Amount must be > 0", Toast.LENGTH_SHORT).show(); return; }
        if (amt > businessCoins) { Toast.makeText(this, "Not enough Business Coins. Balance: " + businessCoins, Toast.LENGTH_LONG).show(); return; }
        long finalAmt = amt;
        new AlertDialog.Builder(this)
            .setTitle("Confirm Sell?")
            .setMessage("Sell " + finalAmt + " Coins\nTo User ID: " + target + "\n\nFrom your Business Wallet?")
            .setPositiveButton("✅ YES, SELL", (d, w) -> doSell(target, finalAmt))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void doSell(String targetId, long amount) {
        // STEP 1: Deduct from Seller Business Wallet - Transaction = No Double Spend, No Negative
        DatabaseReference sellerRef = FirebaseDatabase.getInstance().getReference("sellers").child(sellerKey).child("businessCoins");
        sellerRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData current) {
                Long cur = current.getValue(Long.class);
                if (cur == null) cur = 0L;
                if (cur < amount) return Transaction.abort(); // Not enough - stop
                current.setValue(cur - amount);
                return Transaction.success(current);
            }
            @Override public void onComplete(@Nullable DatabaseError err, boolean committed, @Nullable DataSnapshot snap) {
                if (!committed) { Toast.makeText(SellerBusinessWalletActivity.this, "Failed - Not enough balance", Toast.LENGTH_SHORT).show(); return; }
                // STEP 2: Add to Buyer users/{target}/coins - Transaction
                DatabaseReference buyerCoins = FirebaseDatabase.getInstance().getReference("users").child(targetId).child("coins");
                buyerCoins.runTransaction(new Transaction.Handler() {
                    @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData cur) {
                        Long v = cur.getValue(Long.class);
                        if (v == null) {
                            // Try String stored coins
                            Object o = cur.getValue();
                            try { v = o == null ? 0L : Long.parseLong(String.valueOf(o)); } catch (Exception e) { v = 0L; }
                        }
                        cur.setValue(v + amount);
                        return Transaction.success(cur);
                    }
                    @Override public void onComplete(@Nullable DatabaseError e2, boolean c2, @Nullable DataSnapshot s2) {
                        if (!c2) {
                            // Rollback seller - return coins (rare case)
                            sellerRef.runTransaction(new Transaction.Handler() {
                                @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData cur) {
                                    Long v = cur.getValue(Long.class); if (v == null) v = 0L;
                                    cur.setValue(v + amount); return Transaction.success(cur);
                                }
                                @Override public void onComplete(@Nullable DatabaseError e3, boolean c3, @Nullable DataSnapshot s3) {}
                            });
                            Toast.makeText(SellerBusinessWalletActivity.this, "Buyer not found - Refunded", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // STEP 3: Log for Owner to follow - seller_sales
                        String pushId = FirebaseDatabase.getInstance().getReference("seller_sales").push().getKey();
                        if (pushId != null) {
                            Map<String, Object> log = new HashMap<>();
                            log.put("sellerId", my9DigitId);
                            log.put("sellerUid", firebaseUid);
                            log.put("buyerId", targetId);
                            log.put("amount", amount);
                            log.put("type", "SELLER_SALE");
                            log.put("createdAt", System.currentTimeMillis());
                            FirebaseDatabase.getInstance().getReference("seller_sales").child(pushId).setValue(log);
                            FirebaseDatabase.getInstance().getReference("sellers").child(sellerKey).child("totalSold").setValue(null); // placeholder - keep simple
                        }
                        Toast.makeText(SellerBusinessWalletActivity.this, "✅ Sold " + amount + " Coins to " + targetId, Toast.LENGTH_LONG).show();
                        edtTargetId.setText(""); edtAmount.setText("");
                    }
                });
            }
        });
    }
}
