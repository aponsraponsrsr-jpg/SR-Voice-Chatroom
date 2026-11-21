package com.sr.voicechat;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

// GIFT MANAGER - Company Controlled - No User Can Fake Dollar / Coins
// RULE:
// 1. Sender: 100% Real Coins deduct from Wallet - Real Time from Database
// 2. Receiver: MONTHLY Target Counting += Full Gift Amount - Running Month Only
//    users/{receiverId}/monthly/{2026-11}/receivedCoins += giftAmount
// 3. Receiver: Wallet += 30% Exchange - Automatic
// 4. Self Gift also works - Same rule for own ID - Sender = Receiver হলেও Count হবে
// 5. Dollar Counting: Only from Monthly Total - Calculated from Database, never written by user
// 6. Month Change হইলেই New Month ZERO - Before Month আলাদা History - কখনো মিলবে না
public class GiftManager {

    public interface GiftCallback {
        void onSuccess(long sentAmount, long exchangeAdded, long newTargetTotal);
        void onFailed(String msg);
    }

    // NEW PREMIUM GIFT AMOUNTS ONLY - 4 GIFTS
    // Rose 1000, Angel 5000, Rocket 10000, Porsche Car 20000
    public static final long[] GIFT_AMOUNTS = {1000, 5000, 10000, 20000};

    // MONTHLY KEY - Running Month: 2026-11 -> 2026-12 হইলেই Automatic ZERO
    public static String getMonthKey() {
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + (m < 10 ? "0" + m : "" + m);
    }

    // BEFORE MONTH - History - Running শেষ হইলে Before এ চলে যাবে
    public static String getBeforeMonthKey() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        int m = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + (m < 10 ? "0" + m : "" + m);
    }

    // Gift Amount -> SVGA URL - Animation এর জন্য
    public static String getSvgaUrlForAmount(long amount) {
        if (amount == 1000) return "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/rose.svga";
        if (amount == 5000) return "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/angel-1.svga";
        if (amount == 10000) return "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/Rocket.svga";
        if (amount == 20000) return "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/posche.svga";
        return "";
    }

    // Gift Amount -> Gift Name
    public static String getGiftNameForAmount(long amount) {
        if (amount == 1000) return "Rose";
        if (amount == 5000) return "Angel";
        if (amount == 10000) return "Rocket";
        if (amount == 20000) return "Porsche Car";
        return "Gift";
    }

    // TARGET TABLE - Same as HostPolicy / AgencyPolicy - Company Fixed
    private static final long[] TARGETS = {
        288000, 588000, 1190000, 1780000, 3280000, 5780000, 7880000, 9980000,
        13850000, 18850000, 26500000, 34500000, 43500000, 56500000, 76500000,
        88500000, 115500000, 155000000, 210000000, 288000000, 350000000, 460000000
    };
    private static final double[] HOST_SALARY = {1, 2, 4.5, 7, 14, 24, 32, 40, 56, 76, 104, 132, 168, 220, 292, 348, 440, 584, 752, 1024, 1240, 1600};
    private static final double[] AGENCY_SALARY = {0.5, 1, 1.5, 2, 3, 6, 8, 10, 14, 19, 26, 33, 42, 55, 73, 87, 110, 146, 188, 256, 310, 400};

    public static void sendGift(String senderUid, String receiverUid, long giftAmount, GiftCallback cb) {
        if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
            if (cb != null) cb.onFailed("Invalid User");
            return;
        }
        if (giftAmount <= 0) {
            if (cb != null) cb.onFailed("Invalid Amount");
            return;
        }
        // Only allow 4 Premium amounts - পুরানো দাম দিয়ে কেউ Gift পাঠাতে পারবে না
        boolean allowed = false;
        for (long a : GIFT_AMOUNTS) { if (a == giftAmount) { allowed = true; break; } }
        if (!allowed) {
            if (cb != null) cb.onFailed("This gift is removed");
            return;
        }
        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("users").child(senderUid);
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
                applyToReceiver(senderUid, receiverUid, giftAmount, cb);
            }
        });
    }

    private static void applyToReceiver(String senderUid, String receiverUid, long giftAmount, GiftCallback cb) {
        String monthKey = getMonthKey();
        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference("users").child(receiverUid);
        long exchange = (long) (giftAmount * 0.30);

        // STEP 1: MONTHLY COUNTING - Running Month Only - সরাসরি Firebase থেকে
        // users/{receiverId}/monthly/{2026-11}/receivedCoins += giftAmount
        // মাস Change হইলেই New Key - ZERO থেকে শুরু - Before আলাদা
        receiverRef.child("monthly").child(monthKey).child("receivedCoins").runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData current) {
                Long cur = current.getValue(Long.class);
                if (cur == null) cur = 0L;
                current.setValue(cur + giftAmount);
                return Transaction.success(current);
            }
            @Override public void onComplete(DatabaseError err, boolean committed, DataSnapshot monthlySnap) {
                if (err != null || !committed) {
                    if (cb != null) cb.onFailed("Monthly counting failed");
                    return;
                }
                long monthlyTotal = 0;
                if (monthlySnap != null && monthlySnap.getValue() != null) {
                    monthlyTotal = toLong(monthlySnap.getValue());
                }
                // STEP 2: Wallet + Level + Salary - Monthly Total দিয়ে হিসাব - Lifetime দিয়ে না
                updateWalletAndSalary(receiverUid, giftAmount, exchange, monthlyTotal, monthKey, senderUid, cb);
            }
        });
    }

    private static void updateWalletAndSalary(String receiverUid, long giftAmount, long exchange, long monthlyTotal, String monthKey, String senderUid, GiftCallback cb) {
        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference("users").child(receiverUid);
        receiverRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData current) {
                Object coinsObj = current.child("coins").getValue();
                long wallet = coinsObj == null ? 0L : toLong(coinsObj);
                current.child("coins").setValue(wallet + exchange);

                // Monthly Total - Running Month এর Total - এইটা দিয়েই Level / Salary
                current.child("monthly").child(monthKey).child("receivedCoins").setValue(monthlyTotal);
                // Old totalReceivedCoins - Backwards Compatibility এর জন্য রাখা - কিন্তু Salary Monthly দিয়ে
                Object oldTotalObj = current.child("totalReceivedCoins").getValue();
                long oldTotal = oldTotalObj == null ? 0L : toLong(oldTotalObj);
                current.child("totalReceivedCoins").setValue(oldTotal + giftAmount);

                int level = 0;
                double hostSalary = 0, agencySalary = 0;
                for (int i = 0; i < TARGETS.length; i++) {
                    if (monthlyTotal >= TARGETS[i]) {
                        level = i + 1;
                        hostSalary = HOST_SALARY[i];
                        agencySalary = AGENCY_SALARY[i];
                    }
                }
                current.child("currentLevel").setValue(level);
                current.child("hostSalaryUSD").setValue(hostSalary);
                current.child("agencyCommissionUSD").setValue(agencySalary);
                current.child("totalSalaryUSD").setValue(hostSalary + agencySalary);
                // Monthly Salary আলাদা করে রাখা - HostWorkData / Agency এইটা দেখবে
                current.child("monthly").child(monthKey).child("hostSalaryUSD").setValue(hostSalary);
                current.child("monthly").child(monthKey).child("agencyCommissionUSD").setValue(agencySalary);
                current.child("monthly").child(monthKey).child("level").setValue(level);
                return Transaction.success(current);
            }
            @Override public void onComplete(DatabaseError err, boolean committed, DataSnapshot snap) {
                if (err != null || !committed) {
                    if (cb != null) cb.onFailed("Receiver update failed");
                    return;
                }
                saveGiftHistory(senderUid, receiverUid, giftAmount, exchange, monthKey, monthlyTotal);
                if (cb != null) cb.onSuccess(giftAmount, exchange, monthlyTotal);
            }
        });
    }

    private static void saveGiftHistory(String senderUid, String receiverUid, long amount, long exchange, String monthKey, long monthlyTotal) {
        try {
            DatabaseReference hist = FirebaseDatabase.getInstance().getReference("giftHistory").push();
            Map<String, Object> m = new HashMap<>();
            m.put("senderUid", senderUid);
            m.put("receiverUid", receiverUid);
            m.put("giftName", getGiftNameForAmount(amount));
            m.put("giftAmount", amount);
            m.put("exchange30", exchange);
            m.put("isSelfGift", senderUid.equals(receiverUid));
            m.put("monthKey", monthKey);
            m.put("monthlyTotalAfterGift", monthlyTotal);
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
