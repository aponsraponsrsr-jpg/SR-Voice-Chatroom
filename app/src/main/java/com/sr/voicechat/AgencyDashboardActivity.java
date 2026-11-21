package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.*;

public class AgencyDashboardActivity extends AppCompatActivity {
    private TextView txtAgencyTitle, txtTotalHosts, txtTotalEarnings, txtMonth;
    private Button btnOpenInviteDialog, btnAgencyPolicy;
    private LinearLayout hostListContainer;
    private String agencyOwnerId = "";
    private String agencyOwnerProfileId = "";
    private String agencyName = "SR Agency", monthKey = "";
    private DatabaseReference agencyRef, invitationRef, usersRef;
    private double totalCommission = 0;
    private SharedPreferences prefs;

    private final Map<String, Long> hostCoinsMap = new HashMap<>();
    private final Map<String, String> hostNamesMap = new HashMap<>();
    private final Map<String, String> hostProfileIdMap = new HashMap<>();
    private final Map<String, ValueEventListener> hostListeners = new HashMap<>();
    private boolean dashboardStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        monthKey = SalaryPolicy.getCurrentMonthKey();

        ScrollView scrollView = new ScrollView(this); scrollView.setFillViewport(true); scrollView.setBackgroundColor(android.graphics.Color.parseColor("#120A2A"));
        LinearLayout rootLayout = new LinearLayout(this); rootLayout.setOrientation(LinearLayout.VERTICAL); rootLayout.setPadding(40, 40, 40, 40);
        txtAgencyTitle = new TextView(this); agencyName = prefs.getString("CACHED_AGENCY_NAME", "SR Agency");
        if (agencyName == null || agencyName.isEmpty()) agencyName = prefs.getString("AGENCY_NAME", "SR Agency");
        txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard"); txtAgencyTitle.setTextColor(android.graphics.Color.parseColor("#FFD700")); txtAgencyTitle.setTextSize(22); txtAgencyTitle.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(txtAgencyTitle);
        txtMonth = new TextView(this); txtMonth.setText("📅 Month: " + monthKey + " • Monthly - মাস শেষে ZERO হবে"); txtMonth.setTextColor(android.graphics.Color.WHITE); txtMonth.setTextSize(13); rootLayout.addView(txtMonth);
        txtTotalHosts = new TextView(this); txtTotalHosts.setText("👥 Active Hosts: 0"); txtTotalHosts.setTextColor(android.graphics.Color.WHITE); txtTotalHosts.setTextSize(16); txtTotalHosts.setPadding(0, 20, 0, 5); rootLayout.addView(txtTotalHosts);
        txtTotalEarnings = new TextView(this); txtTotalEarnings.setText("💰 Total Agency Earnings: $0"); txtTotalEarnings.setTextColor(android.graphics.Color.parseColor("#00E676")); txtTotalEarnings.setTextSize(16); txtTotalEarnings.setPadding(0, 0, 0, 30); rootLayout.addView(txtTotalEarnings);
        btnAgencyPolicy = new Button(this); btnAgencyPolicy.setText("📜 Agency Policy - Full Table"); btnAgencyPolicy.setBackgroundColor(android.graphics.Color.parseColor("#FFD700")); btnAgencyPolicy.setTextColor(android.graphics.Color.BLACK); btnAgencyPolicy.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(btnAgencyPolicy);
        btnOpenInviteDialog = new Button(this); btnOpenInviteDialog.setText("➕ Invite New Host (Any Digit ID)"); btnOpenInviteDialog.setBackgroundColor(android.graphics.Color.parseColor("#FF4081")); btnOpenInviteDialog.setTextColor(android.graphics.Color.WHITE); btnOpenInviteDialog.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(btnOpenInviteDialog);
        TextView txtHostListHeader = new TextView(this); txtHostListHeader.setText("\n📋 Agency Host Performance & Targets (" + monthKey + "):"); txtHostListHeader.setTextColor(android.graphics.Color.parseColor("#00B0FF")); txtHostListHeader.setTextSize(18); txtHostListHeader.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(txtHostListHeader);
        hostListContainer = new LinearLayout(this); hostListContainer.setOrientation(LinearLayout.VERTICAL); rootLayout.addView(hostListContainer);
        scrollView.addView(rootLayout); setContentView(scrollView);

        // ===== GLOBAL - Owner ID Resolve - Me থেকে আসা সব Key Support =====
        String rawOwner = getIntent().getStringExtra("AGENCY_OWNER_ID");
        if (rawOwner == null || rawOwner.isEmpty()) rawOwner = getIntent().getStringExtra("AGENCY_UID");
        if (rawOwner == null || rawOwner.isEmpty()) rawOwner = getIntent().getStringExtra("AGENCY_PROFILE_ID");
        if (rawOwner == null || rawOwner.isEmpty()) rawOwner = getIntent().getStringExtra("PROFILE_ID");
        if (rawOwner == null || rawOwner.isEmpty()) rawOwner = getIntent().getStringExtra("MY_ID");
        if (rawOwner == null || rawOwner.isEmpty()) rawOwner = prefs.getString("MY_PROFILE_ID", "");
        if (rawOwner == null || rawOwner.isEmpty()) rawOwner = prefs.getString("MY_9_DIGIT_ID", "");
        if (rawOwner == null || rawOwner.isEmpty()) {
            FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
            if (fu != null) rawOwner = fu.getUid();
        }
        if (rawOwner == null) rawOwner = "";
        agencyOwnerProfileId = rawOwner;

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        invitationRef = FirebaseDatabase.getInstance().getReference("agency_invitations");

        btnAgencyPolicy.setOnClickListener(v -> {
            try {
                Intent i = new Intent(this, AgencyPolicyActivity.class);
                i.putExtra("AGENCY_OWNER_ID", agencyOwnerId); i.putExtra("AGENCY_UID", agencyOwnerId); i.putExtra("AGENCY_PROFILE_ID", agencyOwnerProfileId); i.putExtra("PROFILE_ID", agencyOwnerProfileId); i.putExtra("MY_ID", agencyOwnerId); i.putExtra("FROM_AGENCY_DASHBOARD", true);
                startActivity(i);
            } catch (Exception e) { showPolicyFallbackDialog(); }
        });
        btnOpenInviteDialog.setOnClickListener(v -> showHostInviteInputDialog());

        resolveAnyIdToUid(rawOwner, new UidCallback() {
            @Override public void onResolved(String uid, String profileId) {
                agencyOwnerId = uid;
                if (profileId != null && !profileId.isEmpty()) agencyOwnerProfileId = profileId;
                startDashboard();
            }
        });
    }

    private interface UidCallback { void onResolved(String uid, String profileId); }

    private void startDashboard() {
        if (dashboardStarted) return; dashboardStarted = true;
        if (!agencyOwnerId.isEmpty()) prefs.edit().putString("AGENCY_OWNER_ID", agencyOwnerId).putBoolean("IS_AGENCY", true).putBoolean("IS_AGENCY_PERMANENT", true).apply();
        agencyRef = FirebaseDatabase.getInstance().getReference("agencies").child(agencyOwnerId);
        agencyRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { if (!snap.exists()) return; String n = snap.child("agencyName").getValue(String.class); if (n == null) n = snap.child("name").getValue(String.class); if (n != null && !n.isEmpty()) { agencyName = n; prefs.edit().putString("CACHED_AGENCY_NAME", n).apply(); txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard"); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        loadAgencyDataAndHosts();
    }

    private void showPolicyFallbackDialog() {
        String policy = "🏛️ SR AGENCY POLICY - " + monthKey + "\n\n"
            + "1. Receiving = Counting. Room Seat এ Gift Receive হলেই Count হবে - Self / Other / Agency, যেখান থেকে আসুক।\n\n"
            + "2. Profile ID Any Digit হতে পারে - 1, 2, 9, Any Digit। Gift যেই Profile ID তে পড়বে, ওই Profile ID + Firebase UID মিলিয়ে Counting হবে।\n\n"
            + "3. Monthly Target. Month শেষে ZERO হবে।\n\n"
            + "4. Level Salary: যে Level পুরা Fill হবে, শুধু সেই Level এর Salary পাবে। 5 Fill + 6 এ 1 Coin বাকি = শুধু 5 এর Salary, 6 এর পাবে না।\n\n"
            + "5. Dollar মিস হবে না, বেশিও পাবে না - Policy Target অনুযায়ী ঠিক ততটুকুই।\n\n"
            + "6. সবার জন্য সমান নিয়ম - Permanent।";
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("📜 Agency Policy - Full Table").setMessage(policy).setPositiveButton("OK", (d, w) -> d.dismiss()).show();
    }

    private void showHostInviteInputDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this); builder.setTitle("📨 Send Host Invitation");
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(40, 30, 40, 30);
        final EditText inputTargetId = new EditText(this); inputTargetId.setHint("Enter Host Profile ID (Any Digit) / UID..."); inputTargetId.setTextColor(android.graphics.Color.WHITE); layout.addView(inputTargetId); builder.setView(layout);
        builder.setPositiveButton("Send Invite", (d, w) -> { String t = inputTargetId.getText().toString().trim(); if (!t.isEmpty()) resolveAnyIdToUid(t, (uid, profileId) -> sendRealtimeHostInvitation(uid, t)); else Toast.makeText(this, "Please enter a valid User ID!", Toast.LENGTH_SHORT).show(); });
        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss()); builder.show();
    }

    private void resolveAnyIdToUid(final String inputId, final UidCallback cb) {
        if (inputId == null || inputId.isEmpty()) { cb.onResolved("", ""); return; }
        usersRef.child(inputId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    String pid = getProfileIdFromSnapshot(snap, inputId);
                    cb.onResolved(inputId, pid); return;
                }
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot all) {
                        for (DataSnapshot u : all.getChildren()) {
                            if (u.getKey() == null) continue;
                            if (matchesAnyProfileId(u, inputId)) { cb.onResolved(u.getKey(), inputId); return; }
                        }
                        if (cb != null) cb.onResolved(inputId, inputId);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { cb.onResolved(inputId, inputId); }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { cb.onResolved(inputId, inputId); }
        });
    }

    private boolean matchesAnyProfileId(DataSnapshot u, String inputId) {
        String[] fields = {"profileId", "profileID", "profile_id", "userProfileId", "displayId", "displayID", "shortId", "shortID", "MY_9_DIGIT_ID", "nineDigitId", "my9DigitId", "my9DigitID", "userShortId", "short_id", "id", "userId", "profileNumber", "publicId", "anyId", "myId", "idNumber", "srId"};
        for (String f : fields) {
            Object v = u.child(f).getValue();
            if (v != null && String.valueOf(v).trim().equals(inputId.trim())) return true;
        }
        for (DataSnapshot sub : u.getChildren()) {
            if (sub.hasChild("profileId") && inputId.trim().equals(String.valueOf(sub.child("profileId").getValue()))) return true;
            if (sub.hasChild("shortId") && inputId.trim().equals(String.valueOf(sub.child("shortId").getValue()))) return true;
        }
        return false;
    }

    private String getProfileIdFromSnapshot(DataSnapshot u, String fallback) {
        String[] fields = {"profileId", "profileID", "displayId", "shortId", "MY_9_DIGIT_ID", "my9DigitId", "anyId"};
        for (String f : fields) {
            Object v = u.child(f).getValue();
            if (v != null && !String.valueOf(v).trim().isEmpty()) return String.valueOf(v).trim();
        }
        return fallback;
    }

    private void sendRealtimeHostInvitation(String targetUid, String typedId) {
        if (targetUid.equals(agencyOwnerId)) { Toast.makeText(this, "You cannot invite yourself!", Toast.LENGTH_SHORT).show(); return; }
        usersRef.child(targetUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) { Toast.makeText(AgencyDashboardActivity.this, "No user found for ID: " + typedId, Toast.LENGTH_LONG).show(); return; }
                String inviteKey = invitationRef.child(targetUid).push().getKey(); if (inviteKey == null) return;
                Map<String, Object> m = new HashMap<>(); m.put("agencyId", agencyOwnerId); m.put("agencyName", agencyName); m.put("inviterId", agencyOwnerId); m.put("targetId", targetUid); m.put("targetProfileId", typedId); m.put("typedId", typedId); m.put("type", "AGENCY_INVITE"); m.put("status", "Pending"); m.put("timestamp", System.currentTimeMillis());
                invitationRef.child(targetUid).child(inviteKey).setValue(m);
                // ===== FIX - Host এর Me Page এ Invite দেখানোর জন্য - MeActivity এইটা শুনে =====
                // Host এর users/{targetUid} তে pendingHostInvite = true লিখতে হবে
                // নইলে Host কোনোদিন Invite দেখবে না
                String targetProfileKey = typedId;
                // Target এর Profile ID Key - users/{typedId} তেও লিখি - MeActivity যেই Key দিয়ে শুনে
                usersRef.child(targetUid).child("pendingHostInvite").setValue(true);
                usersRef.child(targetUid).child("pendingAgencyId").setValue(agencyOwnerId);
                usersRef.child(targetUid).child("pendingAgencyName").setValue(agencyName);
                if (!targetProfileKey.equals(targetUid)) {
                    usersRef.child(targetProfileKey).child("pendingHostInvite").setValue(true);
                    usersRef.child(targetProfileKey).child("pendingAgencyId").setValue(agencyOwnerId);
                    usersRef.child(targetProfileKey).child("pendingAgencyName").setValue(agencyName);
                }
                // hostInvites - MeActivity Accept করলে এইটা Accepted হবে
                Map<String, Object> hi = new HashMap<>(); hi.put("hostUid", targetUid); hi.put("hostProfileId", typedId); hi.put("agencyId", agencyOwnerId); hi.put("agencyName", agencyName); hi.put("status", "pending"); hi.put("timestamp", System.currentTimeMillis());
                FirebaseDatabase.getInstance().getReference("hostInvites").child(inviteKey).setValue(hi);
                usersRef.child(targetUid).child("inbox").child(inviteKey).setValue(m).addOnCompleteListener(t -> { if (t.isSuccessful()) Toast.makeText(AgencyDashboardActivity.this, "Host invitation sent to ID: " + typedId, Toast.LENGTH_LONG).show(); else Toast.makeText(AgencyDashboardActivity.this, "Failed to send invitation!", Toast.LENGTH_SHORT).show(); });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void loadAgencyDataAndHosts() {
        agencyRef.child("hosts").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> current = new HashSet<>();
                for (DataSnapshot h : snapshot.getChildren()) if (h.getKey() != null) current.add(h.getKey());
                for (String key : current) {
                    if (hostListeners.containsKey(key) || hostListeners.containsKey("UID_" + key)) continue;
                    resolveAnyIdToUid(key, (uid, pid) -> { hostProfileIdMap.put(uid, pid); attachHostRealtimeListener(uid); });
                }
                attachOwnRealtimeListener();
                renderHostList();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void attachHostRealtimeListener(final String hostUid) {
        if (hostListeners.containsKey(hostUid)) return;
        ValueEventListener coinsListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long coins = 0; if (snap.exists() && snap.getValue() != null) try { coins = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                hostCoinsMap.put(hostUid, coins);
                renderHostList();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        hostListeners.put(hostUid, coinsListener);
        usersRef.child(hostUid).child("monthly").child(monthKey).child("receivedCoins").addValueEventListener(coinsListener);
        usersRef.child(hostUid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot nSnap) { String n = nSnap.getValue(String.class); if (n != null && !n.isEmpty()) hostNamesMap.put(hostUid, n); renderHostList(); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        usersRef.child(hostUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { String pid = getProfileIdFromSnapshot(s, hostUid); hostProfileIdMap.put(hostUid, pid); renderHostList(); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void attachOwnRealtimeListener() {
        if (hostListeners.containsKey("OWN_" + agencyOwnerId)) return;
        ValueEventListener ownListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long coins = 0; if (snap.exists() && snap.getValue() != null) try { coins = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                hostCoinsMap.put("OWN_" + agencyOwnerId, coins);
                renderHostList();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        hostListeners.put("OWN_" + agencyOwnerId, ownListener);
        usersRef.child(agencyOwnerId).child("monthly").child(monthKey).child("receivedCoins").addValueEventListener(ownListener);
    }

    private void renderHostList() {
        hostListContainer.removeAllViews();
        totalCommission = 0;
        double ownEarnings = 0;
        int hostCount = 0;
        for (Map.Entry<String, Long> e : hostCoinsMap.entrySet()) {
            if (e.getKey().startsWith("OWN_")) { ownEarnings = SalaryPolicy.getHostSalary(e.getValue()); continue; }
            hostCount++;
            totalCommission += SalaryPolicy.getAgencyCommission(e.getValue());
        }
        txtTotalHosts.setText("👥 Active Hosts: " + hostCount);
        double total = ownEarnings + totalCommission;
        txtTotalEarnings.setText("💰 Total Agency Earnings (" + monthKey + "): " + SalaryPolicy.money(total) + " (Own " + SalaryPolicy.money(ownEarnings) + " + Commission " + SalaryPolicy.money(totalCommission) + ")");
        Long ownCoins = hostCoinsMap.get("OWN_" + agencyOwnerId);
        if (ownCoins != null) {
            String ownPid = hostProfileIdMap.get(agencyOwnerId); if (ownPid == null) ownPid = agencyOwnerProfileId;
            addHostCard("👑 " + agencyName + " (You - Agency Owner)", "Profile ID: " + ownPid, ownCoins, ownEarnings, 100, true);
        }
        for (Map.Entry<String, Long> e : hostCoinsMap.entrySet()) {
            if (e.getKey().startsWith("OWN_")) continue;
            String hostUid = e.getKey();
            String name = hostNamesMap.get(hostUid); if (name == null || name.isEmpty()) name = "Host";
            String pid = hostProfileIdMap.get(hostUid); if (pid == null || pid.isEmpty()) pid = hostUid;
            double hostGets = SalaryPolicy.getHostSalary(e.getValue());
            double commission = SalaryPolicy.getAgencyCommission(e.getValue());
            addHostCard("👤 " + name, "Profile ID: " + pid, e.getValue(), hostGets, commission, false);
        }
    }

    private void addHostCard(String title, String idLine, long coins, double hostEarnings, double agencyCommissionOrOwn, boolean isOwn) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(25, 20, 25, 20);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable(); bg.setColor(android.graphics.Color.parseColor(isOwn ? "#3D2A00" : "#2A1B4E")); bg.setCornerRadius(15); card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); p.setMargins(0, 0, 0, 15); card.setLayoutParams(p);
        TextView tvName = new TextView(this); tvName.setText(title + " (" + idLine + ")"); tvName.setTextColor(android.graphics.Color.WHITE); tvName.setTextSize(15); tvName.setTypeface(null, android.graphics.Typeface.BOLD); card.addView(tvName);
        TextView tvTarget = new TextView(this); int lvl = SalaryPolicy.getAchievedLevel(coins); tvTarget.setText("🎯 Target (" + monthKey + "): " + String.format("%,d", coins) + " Coins • Level: " + (lvl == 0 ? "No Level Yet" : "Level " + lvl)); tvTarget.setTextColor(android.graphics.Color.parseColor("#00E676")); tvTarget.setTextSize(13); card.addView(tvTarget);
        TextView tvDollar = new TextView(this);
        if (isOwn) tvDollar.setText("💵 My Earnings: " + SalaryPolicy.money(hostEarnings));
        else tvDollar.setText("💵 Host Gets: " + SalaryPolicy.money(hostEarnings) + "  •  Agency Commission: " + SalaryPolicy.money(agencyCommissionOrOwn));
        tvDollar.setTextColor(android.graphics.Color.parseColor("#FFD700")); tvDollar.setTextSize(13); card.addView(tvDollar);
        hostListContainer.addView(card);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        for (Map.Entry<String, ValueEventListener> e : hostListeners.entrySet()) {
            if (e.getKey().startsWith("OWN_")) usersRef.child(agencyOwnerId).child("monthly").child(monthKey).child("receivedCoins").removeEventListener(e.getValue());
            else usersRef.child(e.getKey()).child("monthly").child(monthKey).child("receivedCoins").removeEventListener(e.getValue());
        }
    }
}
