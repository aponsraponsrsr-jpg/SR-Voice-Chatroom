package com.sr.voicechat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * PART 6 - VipPrivilegeManager.java
 * VIP LOCK SYSTEM - User nije nite parbe NA
 * 2 ta rasta sudhu:
 * 1. COIN SENDING - Coin pathale Auto VIP
 * 2. COMPANY / ADMIN - Tumi Firebase theke VIP diba - Test er jonno
 *
 * USE:
 * VipPrivilegeManager.checkVip(userId, callback)
 * VipPrivilegeManager.grantVipByAdmin(targetUserId, vipLevel) // Sudhu Admin
 * VipPrivilegeManager.onCoinReceived(userId, coinAmount) // Coin asle call korba
 */
public class VipPrivilegeManager {

    // ===== TUMAR ADMIN UID - EKHANE BOSAO =====
    // Firebase Auth -> Tomar UID ta ekhane bosao
    // Tahole sudhu tumi VIP dite parba
    public static final String ADMIN_UID = "PUT_YOUR_ADMIN_UID_HERE";

    // ===== COIN TO VIP - Tumi chaile change korte parba =====
    // Koto coin pathale koto VIP pabe
    public static int getVipFromCoins(long totalCoinsReceived) {
        if (totalCoinsReceived >= 50000) return 8;
        if (totalCoinsReceived >= 30000) return 7;
        if (totalCoinsReceived >= 20000) return 6;
        if (totalCoinsReceived >= 12000) return 5;
        if (totalCoinsReceived >= 7000) return 4;
        if (totalCoinsReceived >= 3500) return 3;
        if (totalCoinsReceived >= 1500) return 2;
        if (totalCoinsReceived >= 500) return 1;
        return 0; // VIP nai
    }

    public interface VipCallback {
        void onResult(int vipLevel, boolean hasVip, boolean isAdminGranted);
    }

    // ===== 1. VIP CHECK - Sob jaygay ei ta diye check korba =====
    public static void checkVip(String userId, VipCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onResult(0, false, false);
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                int vipLevel = 0;
                boolean isAdminGranted = false;
                if (snap.exists()) {
                    // Admin / Company theke deya VIP - Sobcheye age dekhbe
                    if (snap.hasChild("vipLevel")) {
                        vipLevel = snap.child("vipLevel").getValue(Integer.class) != null ?
                                snap.child("vipLevel").getValue(Integer.class) : 0;
                    }
                    if (snap.hasChild("vipByAdmin")) {
                        Boolean b = snap.child("vipByAdmin").getValue(Boolean.class);
                        isAdminGranted = b != null && b;
                    }
                    // Jodi Admin VIP na thake - Tahole Coin diye VIP check korbe
                    if (!isAdminGranted) {
                        long coins = 0;
                        if (snap.hasChild("totalCoinsReceived")) {
                            Long c = snap.child("totalCoinsReceived").getValue(Long.class);
                            if (c != null) coins = c;
                        }
                        int coinVip = getVipFromCoins(coins);
                        // Coin VIP boro hole - Coin VIP nibe
                        if (coinVip > vipLevel) vipLevel = coinVip;
                    }
                }
                callback.onResult(vipLevel, vipLevel > 0, isAdminGranted);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onResult(0, false, false);
            }
        });
    }

    // ===== 2. COIN ASLE - Ei function ta Coin Sending er pore call korba =====
    // Example: Gift / Coin pathanor code er pore - VipPrivilegeManager.onCoinReceived(toUserId, coinAmount)
    public static void onCoinReceived(String toUserId, long coinAmount) {
        if (toUserId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(toUserId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                long oldTotal = 0;
                if (snap.hasChild("totalCoinsReceived")) {
                    Long c = snap.child("totalCoinsReceived").getValue(Long.class);
                    if (c != null) oldTotal = c;
                }
                long newTotal = oldTotal + coinAmount;
                int newVip = getVipFromCoins(newTotal);
                int oldVip = 0;
                if (snap.hasChild("vipLevel")) {
                    Integer v = snap.child("vipLevel").getValue(Integer.class);
                    if (v != null) oldVip = v;
                }
                // Total coin update
                ref.child("totalCoinsReceived").setValue(newTotal);
                // Jodi notun VIP boro hoy - Tahole VIP barabe - Auto Unlock
                if (newVip > oldVip) {
                    ref.child("vipLevel").setValue(newVip);
                    ref.child("vipByAdmin").setValue(false);
                    ref.child("vipUnlockedBy").setValue("COIN");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ===== 3. ADMIN / COMPANY - Tumi / Company VIP diba - Test er jonno =====
    // Sudhu ADMIN_UID diyei ei ta cholbe - User nije call korleo cholbe NA
    public static void grantVipByAdmin(String adminUid, String targetUserId, int vipLevel, VipCallback callback) {
        if (adminUid == null || !adminUid.equals(ADMIN_UID)) {
            // User nije VIP nite chaile - BLOCK
            if (callback != null) callback.onResult(0, false, false);
            return;
        }
        if (targetUserId == null || vipLevel < 0 || vipLevel > 8) return;
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(targetUserId);
        ref.child("vipLevel").setValue(vipLevel);
        ref.child("vipByAdmin").setValue(true);
        ref.child("vipUnlockedBy").setValue("ADMIN_COMPANY");
        ref.child("vipGivenBy").setValue(adminUid);
        if (callback != null) callback.onResult(vipLevel, vipLevel > 0, true);
    }

    // ===== 4. ADMIN VIP SORANO - Test sesh hole =====
    public static void removeVipByAdmin(String adminUid, String targetUserId) {
        if (adminUid == null || !adminUid.equals(ADMIN_UID)) return;
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(targetUserId);
        ref.child("vipLevel").setValue(0);
        ref.child("vipByAdmin").setValue(false);
        ref.child("vipUnlockedBy").setValue("REMOVED");
    }

    // ===== 5. USER VIP PABE KINA - UI te check korar jonno =====
    // VIP 1 = Emblem, VIP 2 = Frame, VIP 5 = Chat Bubble, VIP 7 = Entry etc.
    public static boolean canUseEmblem(int vipLevel) { return vipLevel >= 1; }
    public static boolean canUseFrame(int vipLevel) { return vipLevel >= 1; }
    public static boolean canUseEntry(int vipLevel) { return vipLevel >= 3; }
    public static boolean canUseChatBubble(int vipLevel) { return vipLevel >= 2; }
    public static boolean canUseProfileCard(int vipLevel) { return vipLevel >= 1; }
    public static boolean canUseSpecialRoom(int vipLevel) { return vipLevel >= 5; }
}
