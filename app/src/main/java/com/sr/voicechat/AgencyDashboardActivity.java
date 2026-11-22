package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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

        ScrollView scrollView = new ScrollView(this); scrollView.setFillViewport(true); scrollView.setBackgroundColor(Color.parseColor("#120A2A"));
        LinearLayout rootLayout = new LinearLayout(this); rootLayout.setOrientation(LinearLayout.VERTICAL); rootLayout.setPadding(40, 40, 40, 40);
        txtAgencyTitle = new TextView(this); agencyName = prefs.getString("CACHED_AGENCY_NAME", "SR Agency");
        if (agencyName == null || agencyName.isEmpty()) agencyName = prefs.getString("AGENCY_NAME", "SR Agency");
        txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard"); txtAgencyTitle.setTextColor(Color.parseColor("#FFD700")); txtAgencyTitle.setTextSize(22); txtAgencyTitle.setTypeface(null, Typeface.BOLD); rootLayout.addView(txtAgencyTitle);
        txtMonth = new TextView(this); txtMonth.setText("📅 Month: " + monthKey + " • Monthly - মাস শেষে ZERO হবে"); txtMonth.setTextColor(Color.WHITE); txtMonth.setTextSize(13); rootLayout.addView(txtMonth);
        txtTotalHosts = new TextView(this); txtTotalHosts.setText("👥 Active Hosts: 0"); txtTotalHosts.setTextColor(Color.WHITE); txtTotalHosts.setTextSize(16); txtTotalHosts.setPadding(0, 20, 0, 5); rootLayout.addView(txtTotalHosts);
        txtTotalEarnings = new TextView(this); txtTotalEarnings.setText("💰 Total Agency Earnings: $0"); txtTotalEarnings.setTextColor(Color.parseColor("#00E676")); txtTotalEarnings.setTextSize(16); txtTotalEarnings.setPadding(0, 0, 0, 30); rootLayout.addView(txtTotalEarnings);
        btnAgencyPolicy = new Button(this); btnAgencyPolicy.setText("📜 Agency Policy - Full Table"); btnAgencyPolicy.setBackgroundColor(Color.parseColor("#FFD700")); btnAgencyPolicy.setTextColor(Color.BLACK); btnAgencyPolicy.setTypeface(null, Typeface.BOLD); rootLayout.addView(btnAgencyPolicy);
        btnOpenInviteDialog = new Button(this); btnOpenInviteDialog.setText("➕ Invite New Host (Any Digit ID)"); btnOpenInviteDialog.setBackgroundColor(Color.parseColor("#FF4081")); btnOpenInviteDialog.setTextColor(Color.WHITE); btnOpenInviteDialog.setTypeface(null, Typeface.BOLD); rootLayout.addView(btnOpenInviteDialog);
        TextView txtHostListHeader = new TextView(this); txtHostListHeader.setText("\n📋 Agency Host Performance & Targets (" + monthKey + "):"); txtHostListHeader.setTextColor(Color.parseColor("#00B0FF")); txtHostListHeader.setTextSize(18); txtHostListHeader.setTypeface(null, Typeface.BOLD); rootLayout.addView(txtHostListHeader);
        hostListContainer = new LinearLayout(this); hostListContainer.setOrientation(LinearLayout.VERTICAL); rootLayout.addView(hostListContainer);
        scrollView.addView(rootLayout); setContentView(scrollView);

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
            } catch (Exception e) { showPolicyPremiumDialog(); }
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
        // FIX 2 PARTY - agencies/hosts e na thakleo - agencyId millei auto add hobe
        discoverHostsByAgencyId();
    }

    // FIX - Premium Box Table - NiuChat er moto
    private void showPolicyPremiumDialog() {
        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,32,32,32); root.setBackgroundColor(Color.parseColor("#FFFDE7"));
        TextView t = new TextView(this); t.setText("🏛️ SR AGENCY POLICY - " + monthKey); t.setTextSize(18); t.setTypeface(null, Typeface.BOLD); t.setTextColor(Color.parseColor("#2E7D32")); t.setGravity(android.view.Gravity.CENTER); root.addView(t);
        String[] TARGET = {"288000","588000","1190000","1780000","3280000","5780000","7880000","9960000"};
        String[] ANCHOR = {"$1","$2","$4.5","$7","$14","$24","$32","$40"};
        String[] AGENT = {"$0.5","$1","$1.5","$2","$3","$6","$8","$10"};
        String[] TOTAL = {"$1.5","$3","$6","$9","$17","$30","$40","$50"};
        for (int i=0;i<TARGET.length;i++) {
            LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(16,16,16,16); box.setBackgroundColor(Color.parseColor("#E8F5E9"));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,16,0,0); box.setLayoutParams(lp);
            TextView lv = new TextView(this); lv.setText("Level."+(i+1)); lv.setTypeface(null, Typeface.BOLD); lv.setTextSize(16); box.addView(lv);
            LinearLayout h = new LinearLayout(this); h.setOrientation(LinearLayout.HORIZONTAL);
            h.addView(policyCell("Target", true)); h.addView(policyCell("Anchor", true)); h.addView(policyCell("Agent", true)); box.addView(h);
            LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL);
            v.addView(policyCell(TARGET[i], false)); v.addView(policyCell(ANCHOR[i], false)); v.addView(policyCell(AGENT[i], false)); box.addView(v);
            TextView tot = new TextView(this); tot.setText("Total Salary: " + TOTAL[i]); tot.setTypeface(null, Typeface.BOLD); tot.setPadding(0,8,0,0); box.addView(tot);
            root.addView(box);
        }
        TextView note = new TextView(this); note.setText("\n1. Room Seat e Gift Receive holei Count\n2. Monthly - Month seshe ZERO\n3. Level pura Fill holei Salary"); root.addView(note);
        sv.addView(root);
        new androidx.appcompat.app.AlertDialog.Builder(this).setView(sv).setPositiveButton("OK", (d,w)->d.dismiss()).show();
    }
    private TextView policyCell(String txt, boolean header) {
        TextView tv = new TextView(this); tv.setText(txt); tv.setGravity(android.view.Gravity.CENTER); tv.setPadding(8,10,8,10);
        tv.setBackgroundColor(header ? Color.parseColor("#4CAF50") : Color.WHITE);
        tv.setTextColor(header ? Color.WHITE : Color.BLACK); tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,-2,1f); p.setMargins(2,2,2,2); tv.setLayoutParams(p); return tv;
    }

    private void showHostInviteInputDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this); builder.setTitle("📨 Send Host Invitation");
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(40, 30, 40, 30);
        final EditText inputTargetId = new EditText(this); inputTargetId.setHint("Enter Host Profile ID (Any Digit) / UID..."); inputTargetId.setTextColor(Color.WHITE); layout.addView(inputTargetId); builder.setView(layout);
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
                usersRef.child(targetUid).child("pendingHostInvite").setValue(true);
                usersRef.child(targetUid).child("pendingAgencyId").setValue(agencyOwnerId);
                usersRef.child(targetUid).child("pendingAgencyName").setValue(agencyName);
                if (!typedId.equals(targetUid)) {
                    usersRef.child(typedId).child("pendingHostInvite").setValue(true);
                    usersRef.child(typedId).child("pendingAgencyId").setValue(agencyOwnerId);
                    usersRef.child(typedId).child("pendingAgencyName").setValue(agencyName);
                }
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

    // NEW - 2 Party Fix - users table e agencyId / agencyOwnerId millei Host hisebe dhorbe
    private void discoverHostsByAgencyId() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot all) {
                for (DataSnapshot u : all.getChildren()) {
                    if (u.getKey() == null) continue;
                    if (u.getKey().equals(agencyOwnerId)) continue;
                    String a1 = u.child("agencyId").getValue(String.class);
                    String a2 = u.child("agencyOwnerId").getValue(String.class);
                    String a3 = u.child("agency").getValue(String.class);
                    boolean match = (a1 != null && (a1.equals(agencyOwnerId) || a1.equals(agencyOwnerProfileId)))
                        || (a2 != null && (a2.equals(agencyOwnerId) || a2.equals(agencyOwnerProfileId)))
                        || (a3 != null && (a3.equals(agencyOwnerId) || a3.equals(agencyOwnerProfileId)));
                    if (match && !hostListeners.containsKey(u.getKey())) {
                        hostProfileIdMap.put(u.getKey(), getProfileIdFromSnapshot(u, u.getKey()));
                        attachHostRealtimeListener(u.getKey());
                    }
                }
                renderHostList();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void attachHostRealtimeListener(final String hostUid) {
        if (hostListeners.containsKey(hostUid)) return;
        // FIX - 3 ta Path theke Coin porbe - jetai besi oitai nibe
        ValueEventListener coinsListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long coins = 0; if (snap.exists() && snap.getValue() != null) try { coins = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                // 2nd path - hostWorkData
                usersRef.child(hostUid).child("hostWorkData").child(monthKey).child("receivedCoins").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        long c2 = 0; if (s2.exists() && s2.getValue() != null) try { c2 = Long.parseLong(String.valueOf(s2.getValue())); } catch (Exception ignored) {}
                        long best = Math.max(coins, c2);
                        // 3rd path - totalReceived
                        usersRef.child(hostUid).child("monthly").child(monthKey).child("totalReceived").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot s3) {
                                long c3 = 0; if (s3.exists() && s3.getValue() != null) try { c3 = Long.parseLong(String.valueOf(s3.getValue())); } catch (Exception ignored) {}
                                hostCoinsMap.put(hostUid, Math.max(best, c3));
                                renderHostList();
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) { hostCoinsMap.put(hostUid, best); renderHostList(); }
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { hostCoinsMap.put(hostUid, coins); renderHostList(); }
                });
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
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable(); bg.setColor(Color.parseColor(isOwn ? "#3D2A00" : "#2A1B4E")); bg.setCornerRadius(15); card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); p.setMargins(0, 0, 0, 15); card.setLayoutParams(p);
        TextView tvName = new TextView(this); tvName.setText(title + " (" + idLine + ")"); tvName.setTextColor(Color.WHITE); tvName.setTextSize(15); tvName.setTypeface(null, Typeface.BOLD); card.addView(tvName);
        TextView tvTarget = new TextView(this); int lvl = SalaryPolicy.getAchievedLevel(coins); tvTarget.setText("🎯 Target (" + monthKey + "): " + String.format("%,d", coins) + " Coins • Level: " + (lvl == 0 ? "No Level Yet" : "Level " + lvl)); tvTarget.setTextColor(Color.parseColor("#00E676")); tvTarget.setTextSize(13); card.addView(tvTarget);
        TextView tvDollar = new TextView(this);
        if (isOwn) tvDollar.setText("💵 My Earnings: " + SalaryPolicy.money(hostEarnings));
        else tvDollar.setText("💵 Host Gets: " + SalaryPolicy.money(hostEarnings) + "  •  Agency Commission: " + SalaryPolicy.money(agencyCommissionOrOwn));
        tvDollar.setTextColor(Color.parseColor("#FFD700")); tvDollar.setTextSize(13); card.addView(tvDollar);
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
