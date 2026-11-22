package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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
    private final Map<String, Long> ownPathCoins = new HashMap<>();
    // GLOBAL FIX - Own er Salary + Commission alada rakhar jonno - Policy onujayi
    private double ownSalary = 0;
    private double ownCommission = 0;

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
            Intent i = new Intent(this, AgencyPolicyActivity.class);
            i.putExtra("AGENCY_OWNER_ID", agencyOwnerId);
            i.putExtra("AGENCY_UID", agencyOwnerId);
            i.putExtra("AGENCY_PROFILE_ID", agencyOwnerProfileId);
            i.putExtra("PROFILE_ID", agencyOwnerProfileId);
            i.putExtra("MY_ID", agencyOwnerId);
            i.putExtra("FROM_AGENCY_DASHBOARD", true);
            startActivity(i);
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
        discoverHostsByAgencyId();
        attachOwnRealtimeListener();
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
                    if (hostListeners.containsKey(key)) continue;
                    resolveAnyIdToUid(key, (uid, pid) -> { hostProfileIdMap.put(uid, pid); attachHostRealtimeListener(uid); });
                }
                attachOwnRealtimeListener();
                renderHostList();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

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
        ValueEventListener coinsListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long coins = 0; if (snap.exists() && snap.getValue() != null) try { coins = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                final long c1 = coins;
                usersRef.child(hostUid).child("hostWorkData").child(monthKey).child("receivedCoins").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        long c2 = 0; if (s2.exists() && s2.getValue() != null) try { c2 = Long.parseLong(String.valueOf(s2.getValue())); } catch (Exception ignored) {}
                        long best = Math.max(c1, c2);
                        usersRef.child(hostUid).child("monthly").child(monthKey).child("totalReceived").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot s3) {
                                long c3 = 0; if (s3.exists() && s3.getValue() != null) try { c3 = Long.parseLong(String.valueOf(s3.getValue())); } catch (Exception ignored) {}
                                hostCoinsMap.put(hostUid, Math.max(best, c3));
                                renderHostList();
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) { hostCoinsMap.put(hostUid, best); renderHostList(); }
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { hostCoinsMap.put(hostUid, c1); renderHostList(); }
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
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) return;
        if (hostListeners.containsKey("OWN_" + agencyOwnerId)) return;
        hostListeners.put("OWN_" + agencyOwnerId, new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        Set<String> keys = new HashSet<>();
        keys.add(agencyOwnerId);
        if (agencyOwnerProfileId != null && !agencyOwnerProfileId.isEmpty() && !agencyOwnerProfileId.equals(agencyOwnerId)) keys.add(agencyOwnerProfileId);
        for (String key : keys) {
            attachOwnPath(key, "monthly/" + monthKey + "/receivedCoins");
            attachOwnPath(key, "monthly/" + monthKey + "/totalReceived");
            attachOwnPath(key, "hostWorkData/" + monthKey + "/receivedCoins");
        }
        String fu = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        if (!fu.isEmpty() && !keys.contains(fu)) {
            attachOwnPath(fu, "monthly/" + monthKey + "/receivedCoins");
            attachOwnPath(fu, "monthly/" + monthKey + "/totalReceived");
            attachOwnPath(fu, "hostWorkData/" + monthKey + "/receivedCoins");
        }
    }

    private void attachOwnPath(String uidKey, String path) {
        String mapKey = uidKey + "|" + path;
        if (hostListeners.containsKey("OWNPATH_" + mapKey)) return;
        ValueEventListener l = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long v = 0; if (snap.exists() && snap.getValue() != null) try { v = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                ownPathCoins.put(mapKey, v);
                long best = 0; for (long x : ownPathCoins.values()) best = Math.max(best, x);
                hostCoinsMap.put("OWN_" + agencyOwnerId, best);
                renderHostList();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        hostListeners.put("OWNPATH_" + mapKey, l);
        usersRef.child(uidKey).child(path).addValueEventListener(l);
    }

    private void renderHostList() {
        hostListContainer.removeAllViews();
        totalCommission = 0;
        double ownEarnings = 0;
        ownSalary = 0;
        ownCommission = 0;
        int hostCount = 0;
        for (Map.Entry<String, Long> e : hostCoinsMap.entrySet()) {
            if (e.getKey().startsWith("OWN_")) {
                // GLOBAL FIX - Policy onujayi - Agency Owner nijer ID te target korleo Salary + Commission 2 tai pabe - sobar jonno soman
                ownSalary = SalaryPolicy.getHostSalary(e.getValue());
                ownCommission = SalaryPolicy.getAgencyCommission(e.getValue());
                ownEarnings = ownSalary + ownCommission;
                continue;
            }
            hostCount++;
            totalCommission += SalaryPolicy.getAgencyCommission(e.getValue());
        }
        txtTotalHosts.setText("👥 Active Hosts: " + hostCount);
        double total = ownEarnings + totalCommission;
        // Total e Own er Commission o jog - Commission er jaygay Commission
        txtTotalEarnings.setText("💰 Total Agency Earnings (" + monthKey + "): " + SalaryPolicy.money(total) + " (Own " + SalaryPolicy.money(ownEarnings) + " + Commission " + SalaryPolicy.money(totalCommission) + ")");
        Long ownCoins = hostCoinsMap.get("OWN_" + agencyOwnerId);
        if (ownCoins != null) {
            String ownPid = hostProfileIdMap.get(agencyOwnerId); if (ownPid == null) ownPid = agencyOwnerProfileId;
            // Own Card e Commission alada pathai - jate Card e Salary + Commission 2 tai dekha jay
            addHostCard("👑 " + agencyName + " (You - Agency Owner)", "Profile ID: " + ownPid, ownCoins, ownEarnings, ownCommission, true);
        } else {
            String ownPid = hostProfileIdMap.get(agencyOwnerId); if (ownPid == null) ownPid = agencyOwnerProfileId;
            addHostCard("👑 " + agencyName + " (You - Agency Owner)", "Profile ID: " + ownPid, 0, 0, 0, true);
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
        // FIX - Own Card e Salary + Commission alada alada dekhabe - Commission er jaygay Commission
        if (isOwn) tvDollar.setText("💵 My Earnings: " + SalaryPolicy.money(hostEarnings) + " (Salary " + SalaryPolicy.money(hostEarnings - agencyCommissionOrOwn) + " + Commission " + SalaryPolicy.money(agencyCommissionOrOwn) + ")");
        else tvDollar.setText("💵 Host Gets: " + SalaryPolicy.money(hostEarnings) + "  •  Agency Commission: " + SalaryPolicy.money(agencyCommissionOrOwn));
        tvDollar.setTextColor(Color.parseColor("#FFD700")); tvDollar.setTextSize(13); card.addView(tvDollar);
        hostListContainer.addView(card);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        for (Map.Entry<String, ValueEventListener> e : hostListeners.entrySet()) {
            if (e.getKey().startsWith("OWNPATH_")) continue;
            if (e.getKey().startsWith("OWN_")) continue;
            else usersRef.child(e.getKey()).child("monthly").child(monthKey).child("receivedCoins").removeEventListener(e.getValue());
        }
    }
}
