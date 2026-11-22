package com.sr.voicechat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * PART 7 - VipProfileIdManager.java
 * PROFILE ID <-> UID LINK SYSTEM - PERMANENT
 *
 * RULE:
 * 1. First Login e User 9 Digit / Random ID pabe - Eita Permanent.
 * 2. Pore se 3 Digit / 2 Digit Fancy ID nileo - Puraton ID UID er sathe thakbe.
 * 3. Admin Profile ID diye VIP dile - Puraton / Notun - Jekono ID diye dilei - Oi UID tei jabe.
 * 4. Kokhono Alada hobe NA - UID = 1 ta, Profile ID = 2 ta (Old + New) - Kintu Manush 1 jon.
 *
 * ADMIN PANEL THEKE USE:
 * VipProfileIdManager.grantVipByProfileId("123456789", 7, callback)
 * VipProfileIdManager.grantVipByProfileId("123", 7, callback) // Fancy ID dileo same lok pabe
 */
public class VipProfileIdManager {

    public interface IdCallback {
        void onFound(String uid, String currentProfileId, String originalProfileId);
        void onNotFound();
    }

    public interface VipGrantCallback {
        void onSuccess(String uid, String profileId, int vipLevel);
        void onFail(String reason);
    }

    // ===== 1. FIRST LOGIN - User er Permanent ID banano =====
    // Register / Login er pore - 1 bar call korba
    // Jodi age theke ID thake - Tahole change korba NA
    public static void ensurePermanentId(String uid, String currentProfileId) {
        if (uid == null || currentProfileId == null) return;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                Map<String, Object> update = new HashMap<>();
                // 1. Current ID - Jeta ekhon dekhay
                update.put("profileId", currentProfileId);
                // 2. Original ID - First Time er ta - Jodi na thake - Tahole ekhoner tai Original
                if (!snap.hasChild("originalProfileId") || snap.child("originalProfileId").getValue() == null) {
                    update.put("originalProfileId", currentProfileId);
                }
                // 3. Mapping - Firebase e ID -> UID map - Admin khujte parbe
                userRef.updateChildren(update);

                // Mapping Table - 2 ta ID diyei khuje pawa jabe
                DatabaseReference mapRef = FirebaseDatabase.getInstance().getReference("profileIdToUid");
                mapRef.child(currentProfileId).setValue(uid);
                // Original ID tao map kore rakho - Kokhono harabe NA
                String original = snap.hasChild("originalProfileId") ?
                        String.valueOf(snap.child("originalProfileId").getValue()) : currentProfileId;
                mapRef.child(original).setValue(uid);
            }
            @Override public void onCancelled(DatabaseError e) {}
        });
    }

    // ===== 2. FANCY ID CHANGE - 3 Digit / 2 Digit kinle =====
    // Puraton ID muche jabe NA - UID er sathe 2 tai thakbe
    public static void changeToFancyId(String uid, String oldProfileId, String newFancyId, IdCallback callback) {
        if (uid == null || newFancyId == null) return;
        DatabaseReference mapRef = FirebaseDatabase.getInstance().getReference("profileIdToUid");
        // Dekho Notun ID ta onno karo kina
        mapRef.child(newFancyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {
                if (snap.exists() && !String.valueOf(snap.getValue()).equals(uid)) {
                    if (callback != null) callback.onNotFound(); // ID ta onno karo - Neya jabe NA
                    return;
                }
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                Map<String, Object> update = new HashMap<>();
                update.put("profileId", newFancyId); // Notun ta dekhabe
                // originalProfileId - Change korba NA - Puraton tai thakbe
                update.put("previousProfileId", oldProfileId);
                userRef.updateChildren(update);
                // 2 ta ID diyei map - Puraton + Notun
                mapRef.child(newFancyId).setValue(uid);
                if (oldProfileId != null) mapRef.child(oldProfileId).setValue(uid);
                if (callback != null) callback.onFound(uid, newFancyId, oldProfileId);
            }
            @Override public void onCancelled(DatabaseError e) { if (callback != null) callback.onNotFound(); }
        });
    }

    // ===== 3. PROFILE ID DIYE UID KHUJA - Puraton / Notun - Jekono ta dilei pabe =====
    public static void findUidByProfileId(String anyProfileId, IdCallback callback) {
        if (anyProfileId == null) { if (callback != null) callback.onNotFound(); return; }
        String cleanId = anyProfileId.trim();
        // 1. Age Mapping Table e khujo - Fast
        FirebaseDatabase.getInstance().getReference("profileIdToUid").child(cleanId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        if (snap.exists()) {
                            String uid = String.valueOf(snap.getValue());
                            loadUserIds(uid, callback);
                        } else {
                            // 2. Mapping e na pele - Users e profileId / originalProfileId diye khujo
                            searchInUsers(cleanId, callback);
                        }
                    }
                    @Override public void onCancelled(DatabaseError e) { if (callback != null) callback.onNotFound(); }
                });
    }

    private static void searchInUsers(String profileId, IdCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        // Current profileId diye khujo
        usersRef.orderByChild("profileId").equalTo(profileId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        if (snap.exists()) {
                            for (DataSnapshot child : snap.getChildren()) {
                                loadUserIds(child.getKey(), callback);
                                return;
                            }
                        } else {
                            // Original ID diye khujo - Puraton ID dileo pabe
                            usersRef.orderByChild("originalProfileId").equalTo(profileId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override public void onDataChange(DataSnapshot s2) {
                                            if (s2.exists()) {
                                                for (DataSnapshot child : s2.getChildren()) {
                                                    loadUserIds(child.getKey(), callback);
                                                    return;
                                                }
                                            } else {
                                                if (callback != null) callback.onNotFound();
                                            }
                                        }
                                        @Override public void onCancelled(DatabaseError e) { if (callback != null) callback.onNotFound(); }
                                    });
                        }
                    }
                    @Override public void onCancelled(DatabaseError e) { if (callback != null) callback.onNotFound(); }
                });
    }

    private static void loadUserIds(String uid, IdCallback callback) {
        FirebaseDatabase.getInstance().getReference("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        String current = snap.hasChild("profileId") ? String.valueOf(snap.child("profileId").getValue()) : "";
                        String original = snap.hasChild("originalProfileId") ? String.valueOf(snap.child("originalProfileId").getValue()) : current;
                        if (callback != null) callback.onFound(uid, current, original);
                    }
                    @Override public void onCancelled(DatabaseError e) { if (callback != null) callback.onNotFound(); }
                });
    }

    // ===== 4. ADMIN PANEL - PROFILE ID DIYE VIP DEWA - MAIN FUNCTION =====
    // Tumi Admin Panel theke - Jekono Profile ID diba - Puraton / Notun - Oi UID tei VIP jabe
    public static void grantVipByProfileId(String anyProfileId, int vipLevel, VipGrantCallback callback) {
        if (vipLevel < 0 || vipLevel > 8) { if (callback != null) callback.onFail("VIP 0-8 hote hobe"); return; }
        findUidByProfileId(anyProfileId, new IdCallback() {
            @Override public void onFound(String uid, String currentId, String originalId) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                Map<String, Object> update = new HashMap<>();
                update.put("vipLevel", vipLevel);
                update.put("vipByAdmin", true);
                update.put("vipUnlockedBy", "ADMIN_PANEL_PROFILE_ID");
                update.put("vipGivenToProfileId", currentId);
                update.put("vipGivenToOriginalId", originalId);
                update.put("vipGivenAt", System.currentTimeMillis());
                userRef.updateChildren(update);
                // Mapping tao update - Jate poreo khuje pawa jay
                FirebaseDatabase.getInstance().getReference("profileIdToUid").child(currentId).setValue(uid);
                FirebaseDatabase.getInstance().getReference("profileIdToUid").child(originalId).setValue(uid);
                if (callback != null) callback.onSuccess(uid, currentId, vipLevel);
            }
            @Override public void onNotFound() { if (callback != null) callback.onFail("Profile ID pawa jay nai - Puraton / Notun ID check koro"); }
        });
    }

    // ===== 5. ADMIN - VIP SORANO - Profile ID diye =====
    public static void removeVipByProfileId(String anyProfileId, VipGrantCallback callback) {
        findUidByProfileId(anyProfileId, new IdCallback() {
            @Override public void onFound(String uid, String currentId, String originalId) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                userRef.child("vipLevel").setValue(0);
                userRef.child("vipByAdmin").setValue(false);
                userRef.child("vipUnlockedBy").setValue("REMOVED_BY_ADMIN");
                if (callback != null) callback.onSuccess(uid, currentId, 0);
            }
            @Override public void onNotFound() { if (callback != null) callback.onFail("Profile ID pawa jay nai"); }
        });
    }
}
