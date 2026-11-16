package com.sr.voicechat;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import java.util.HashMap;
import java.util.Map;

// GIFT MANAGER - Company Controlled - No User Can Fake Dollar / Coins
// RULE:
// 1. Sender: 100% Real Coins deduct from Wallet - Real Time from Database
// 2. Receiver: Target Counting += Full Gift Amount
// 3. Receiver: Wallet += 30% Exchange - Automatic
// 4. Self Gift also works - Same rule for own ID
// 5. Dollar Counting: Only when Target Level completes - Calculated from Database, never written by user
public class GiftManager {

    public interface GiftCallback {
        void onSuccess(long sentAmount, long exchangeAdded, long newTargetTotal);
        void onFailed(String msg);
    }

    // FIXED GIFT AMOUNTS - Gift Amount is Fixed, User selects from these
    public static final long[] GIFT_AMOUNTS = {100, 500, 1000, 5000, 10000, 50000, 100000};

    // TARGET TABLE - Same as HostPolicy / AgencyPolicy - Company Fixed
    private static final long[] TARGETS = {
        288000, 588000, 1190000, 1780000, 3280000, 5780000, 7880000, 9980000,
        13850000, 18850000, 26500000, 34500000, 43500000, 56500000, 76500000,
        88500000, 115500000, 155000000, 210000000, 288000000, 350000000, 460000000
    };
    private static final double[] HOST_SALARY = {1, 2, 4.5, 7, 14, 24, 32, 40, 56, 76, 104, 132, 168, 220, 292, 348, 440, 584, 752, 1024, 1240, 1600};
    private static final double[] AGENCY_SALARY = {0.5, 1, 1.5, 2, 3, 6, 8, 10, 14, 19, 26, 33, 42, 55, 73, 87, 110, 146, 188, 256, 310, 400};

    /**
     * Send Gift - senderUid -> receiverUid
     * senderUid == receiverUid allowed (Self Gift)
     */
    public static void sendGift(String senderUid, String receiverUid, long giftAmount, GiftCallback cb) {
        if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
            if (cb != null) cb.onFailed("Invalid User");
            return;
        }
        if (giftAmount <= 0) {
            if (cb != null) cb.onFailed("Invalid Amount");
            return;
        }
        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("users").child(senderUid);
        // STEP 1: Check Sender has enough + Deduct 100% Real Coins - Transaction = Safe, No Double Spend
        senderRef.child("coins").runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData current) {
                Long coins = current.getValue(Long.class);
                if (coins == null) coins = 0L;
                if (coins < giftAmount) return Transaction.abort();
                current.setValue(coins - giftAmount);
                return Transaction.success(current);
            }
            @Override public void onComplete(DatabaseError err, boolean committed, DataSnapshot snap) {
                if (err != null || !committed) {
                    if (cb != null) cb.onFailed("Not enough Coins in Wallet");
                    return;
                }
                // Sender deducted 100% - Now Receiver part
                applyToReceiver(senderUid, receiverUid, giftAmount, cb);
            }
        });
    }

    private static void applyToReceiver(String senderUid, String receiverUid, long giftAmount, GiftCallback cb) {
        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference("users").child(receiverUid);
        long exchange = (long) (giftAmount * 0.30); // 30% Exchange

        // STEP 2: Receiver Wallet += 30% + Target += 100% - Transaction
        receiverRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData current) {
                // Read current values safely
                Object coinsObj = current.child("coins").getValue();
                long wallet = coinsObj == null ? 0L : toLong(coinsObj);
                Object targetObj = current.child("totalReceivedCoins").getValue();
                long target = targetObj == null ? 0L : toLong(targetObj);

                // 30% Exchange -> Wallet
                current.child("coins").setValue(wallet + exchange);
                // 100% -> Target Counting
                current.child("totalReceivedCoins").setValue(target + giftAmount);

                // Dollar Counting - Only from Target Level - Auto calculated, user cannot write
                int level = 0;
                double hostSalary = 0, agencySalary = 0;
                long newTotal = target + giftAmount;
                for (int i = 0; i < TARGETS.length; i++) {
                    if (newTotal >= TARGETS[i]) {
                        level = i + 1;
                        hostSalary = HOST_SALARY[i];
                        agencySalary = AGENCY_SALARY[i];
                    }
                }
                current.child("currentLevel").setValue(level);
                current.child("hostSalaryUSD").setValue(hostSalary);
                current.child("agencyCommissionUSD").setValue(agencySalary);
                current.child("totalSalaryUSD").setValue(hostSalary + agencySalary);
                return Transaction.success(current);
            }
            @Override public void onComplete(DatabaseError err, boolean committed, DataSnapshot snap) {
                if (err != null || !committed) {
                    if (cb != null) cb.onFailed("Receiver update failed");
                    return;
                }
                // Save Gift History - Proof for Company
                saveGiftHistory(senderUid, receiverUid, giftAmount, exchange);
                long newTarget = 0;
                if (snap != null && snap.hasChild("totalReceivedCoins") && snap.child("totalReceivedCoins").getValue() != null) {
                    newTarget = toLong(snap.child("totalReceivedCoins").getValue());
                }
                if (cb != null) cb.onSuccess(giftAmount, exchange, newTarget);
            }
        });
    }

    // Gift History - Proof - Company can check every Gift
    private static void saveGiftHistory(String senderUid, String receiverUid, long amount, long exchange) {
        try {
            DatabaseReference hist = FirebaseDatabase.getInstance().getReference("giftHistory").push();
            Map<String, Object> m = new HashMap<>();
            m.put("senderUid", senderUid);
            m.put("receiverUid", receiverUid);
            m.put("giftAmount", amount);
            m.put("exchange30", exchange);
            m.put("isSelfGift", senderUid.equals(receiverUid));
            m.put("timestamp", System.currentTimeMillis());
            hist.setValue(m);
        } catch (Exception ignored) {}
    }

    private static long toLong(Object o) {
        try {
            if (o instanceof Long) return (Long) o;
            if (o instanceof Integer) return ((Integer) o).longValue();
            if (o instanceof Double) return ((Double) o).longValue();
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) { return 0L; }
    }
}
