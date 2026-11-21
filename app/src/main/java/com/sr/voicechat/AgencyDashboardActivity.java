package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.*;

public class AgencyDashboardActivity extends AppCompatActivity {
    private TextView txtAgencyTitle, txtTotalHosts, txtTotalEarnings, txtHostListHeader, txtMonth;
    private Button btnOpenInviteDialog, btnAgencyPolicy;
    private LinearLayout hostListContainer;
    private String agencyOwnerId = "", agencyName = "SR Agency", monthKey = "";
    private DatabaseReference agencyRef, invitationRef, usersRef;
    private double totalCommission = 0;
    private int loadedHostCount = 0, expectedHostCount = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        monthKey = SalaryPolicy.getCurrentMonthKey();

        ScrollView scrollView = new ScrollView(this); scrollView.setFillViewport(true); scrollView.setBackgroundColor(android.graphics.Color.parseColor("#120A2A"));
        LinearLayout rootLayout = new LinearLayout(this); rootLayout.setOrientation(LinearLayout.VERTICAL); rootLayout.setPadding(40, 40, 40, 40);
        txtAgencyTitle = new TextView(this); agencyName = prefs.getString("CACHED_AGENCY_NAME", "SR Agency");
        txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard"); txtAgencyTitle.setTextColor(android.graphics.Color.parseColor("#FFD700")); txtAgencyTitle.setTextSize(22); txtAgencyTitle.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(txtAgencyTitle);
        txtMonth = new TextView(this); txtMonth.setText("📅 Month: " + monthKey + " • Monthly - মাস শেষে ZERO হবে"); txtMonth.setTextColor(android.graphics.Color.WHITE); txtMonth.setTextSize(13); rootLayout.addView(txtMonth);
        txtTotalHosts = new TextView(this); txtTotalHosts.setText("👥 Active Hosts: 0"); txtTotalHosts.setTextColor(android.graphics.Color.WHITE); txtTotalHosts.setTextSize(16); txtTotalHosts.setPadding(0, 20, 0, 5); rootLayout.addView(txtTotalHosts);
        txtTotalEarnings = new TextView(this); txtTotalEarnings.setText("💰 Total Agency Earnings: $0"); txtTotalEarnings.setTextColor(android.graphics.Color.parseColor("#00E676")); txtTotalEarnings.setTextSize(16); txtTotalEarnings.setPadding(0, 0, 0, 30); rootLayout.addView(txtTotalEarnings);
        btnAgencyPolicy = new Button(this); btnAgencyPolicy.setText("📜 Agency Policy - Full Table"); btnAgencyPolicy.setBackgroundColor(android.graphics.Color.parseColor("#FFD700")); btnAgencyPolicy.setTextColor(android.graphics.Color.BLACK); btnAgencyPolicy.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(btnAgencyPolicy);
        btnOpenInviteDialog = new Button(this); btnOpenInviteDialog.setText("➕ Invite New Host (Any Digit ID)"); btnOpenInviteDialog.setBackgroundColor(android.graphics.Color.parseColor("#FF4081")); btnOpenInviteDialog.setTextColor(android.graphics.Color.WHITE); btnOpenInviteDialog.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(btnOpenInviteDialog);
        txtHostListHeader = new TextView(this); txtHostListHeader.setText("\n📋 Agency Host Performance & Targets (" + monthKey + "):"); txtHostListHeader.setTextColor(android.graphics.Color.parseColor("#00B0FF")); txtHostListHeader.setTextSize(18); txtHostListHeader.setTypeface(null, android.graphics.Typeface.BOLD); rootLayout.addView(txtHostListHeader);
        hostListContainer = new LinearLayout(this); hostListContainer.setOrientation(LinearLayout.VERTICAL); rootLayout.addView(hostListContainer);
        scrollView.addView(rootLayout); setContentView(scrollView);

        agencyOwnerId = getIntent().getStringExtra("AGENCY_OWNER_ID");
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) agencyOwnerId = getIntent().getStringExtra("MY_ID");
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) agencyOwnerId = prefs.getString("MY_9_DIGIT_ID", "");
        if (agencyOwnerId == null) agencyOwnerId = "";
        if (!agencyOwnerId.isEmpty()) prefs.edit().putString("AGENCY_OWNER_ID", agencyOwnerId).putBoolean("IS_AGENCY", true).putBoolean("IS_AGENCY_PERMANENT", true).apply();

        agencyRef = FirebaseDatabase.getInstance().getReference("agencies").child(agencyOwnerId);
        invitationRef = FirebaseDatabase.getInstance().getReference("agency_invitations");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        agencyRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { if (!snap.exists()) return; prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("IS_AGENCY_PERMANENT", true).apply(); String n = snap.child("agencyName").getValue(String.class); if (n == null) n = snap.child("name").getValue(String.class); if (n != null && !n.isEmpty()) { agencyName = n; prefs.edit().putString("CACHED_AGENCY_NAME", n).apply(); txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard"); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        loadAgencyDataAndHosts();
        btnAgencyPolicy.setOnClickListener(v -> { try { Intent i = new Intent(this, AgencyPolicyActivity.class); i.putExtra("AGENCY_OWNER_ID", agencyOwnerId); i.putExtra("MY_ID", agencyOwnerId); i.putExtra("FROM_AGENCY_DASHBOARD", true); startActivity(i); } catch (Exception ignored) {} });
        btnOpenInviteDialog.setOnClickListener(v -> showHostInviteInputDialog());
    }

    private void showHostInviteInputDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this); builder.setTitle("📨 Send Host Invitation");
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(40, 30, 40, 30);
        final EditText inputTargetId = new EditText(this); inputTargetId.setHint("Enter Host ID (Any Digit ID)..."); inputTargetId.setTextColor(android.graphics.Color.WHITE); layout.addView(inputTargetId); builder.setView(layout);
        builder.setPositiveButton("Send Invite", (d, w) -> { String t = inputTargetId.getText().toString().trim(); if (!t.isEmpty()) sendRealtimeHostInvitation(t); else Toast.makeText(this, "Please enter a valid User ID!", Toast.LENGTH_SHORT).show(); });
        builder.setNegativeButton("Cancel", (d, w) -> d.dismiss()); builder.show();
    }
    private void sendRealtimeHostInvitation(String targetUserId) {
        if (targetUserId.equals(agencyOwnerId)) { Toast.makeText(this, "You cannot invite yourself!", Toast.LENGTH_SHORT).show(); return; }
        String inviteKey = invitationRef.child(targetUserId).push().getKey(); if (inviteKey == null) return;
        Map<String, Object> m = new HashMap<>(); m.put("agencyId", agencyOwnerId); m.put("agencyName", agencyName); m.put("inviterId", agencyOwnerId); m.put("targetId", targetUserId); m.put("type", "AGENCY_INVITE"); m.put("status", "Pending"); m.put("timestamp", System.currentTimeMillis());
        invitationRef.child(targetUserId).child(inviteKey).setValue(m);
        usersRef.child(targetUserId).child("inbox").child(inviteKey).setValue(m).addOnCompleteListener(t -> { if (t.isSuccessful()) Toast.makeText(this, "Host invitation sent to ID: " + targetUserId, Toast.LENGTH_LONG).show(); else Toast.makeText(this, "Failed to send invitation!", Toast.LENGTH_SHORT).show(); });
    }

    private void loadAgencyDataAndHosts() {
        agencyRef.child("hosts").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                long hostCount = snapshot.getChildrenCount(); txtTotalHosts.setText("👥 Active Hosts: " + hostCount); hostListContainer.removeAllViews(); totalCommission = 0; loadedHostCount = 0; expectedHostCount = (int) hostCount;
                if (hostCount == 0) { fetchOwnTargetCard(); return; }
                for (DataSnapshot h : snapshot.getChildren()) if (h.getKey() != null) fetchHostDetailsAndDisplay(h.getKey());
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // MONTHLY: users/{id}/monthly/{monthKey}/receivedCoins - মাস Change = 0
    private void fetchOwnTargetCard() {
        usersRef.child(agencyOwnerId).child("monthly").child(monthKey).child("receivedCoins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long myCoins = 0; if (snap.exists() && snap.getValue() != null) try { myCoins = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                double myEarnings = SalaryPolicy.getHostSalary(myCoins);
                double total = myEarnings + totalCommission;
                txtTotalEarnings.setText("💰 Total Agency Earnings (" + monthKey + "): " + SalaryPolicy.money(total) + " (Own " + SalaryPolicy.money(myEarnings) + " + Commission " + SalaryPolicy.money(totalCommission) + ")");
                addHostCard("👑 " + agencyName + " (You - Agency Owner)", agencyOwnerId, myCoins, myEarnings, 100, true);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // ===== STRONG FIX - FINAL COPY - আজীবনের জন্য Error আসবে না =====
    private void fetchHostDetailsAndDisplay(final String hostUserId) {
        // hostUserId কে final করলাম - ভিতরের Listener এ ব্যবহার করা যাবে
        final String fHostId = hostUserId;
        usersRef.child(fHostId).child("monthly").child(monthKey).child("receivedCoins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long tmpCoins = 0; if (snap.exists() && snap.getValue() != null) try { tmpCoins = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                // ===== KEY FIX =====
                // hostCoins কে সরাসরি ভিতরে ব্যবহার করলে Error আসে।
                // তাই final copy বানালাম - প্রতিটা Host এর নিজের Copy থাকবে, ভুল Host এর Coin অন্য Host এ যাবে না।
                final long fHostCoins = tmpCoins;
                final String fHostId2 = fHostId;

                usersRef.child(fHostId2).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot nSnap) {
                        String tmpName = nSnap.getValue(String.class); if (tmpName == null || tmpName.isEmpty()) tmpName = "Host ID: " + fHostId2;
                        final String fHostName = tmpName;
                        // Calculation ভিতরে করলাম - বাইরে না - তাই Final Error আর আসবে না
                        final double fHostGets = SalaryPolicy.getHostSalary(fHostCoins);
                        final double fAgencyCommission = SalaryPolicy.getAgencyCommission(fHostCoins); // 20% STRICT
                        synchronized (AgencyDashboardActivity.this) {
                            totalCommission += fAgencyCommission; loadedHostCount++;
                        }
                        addHostCard("👤 " + fHostName, fHostId2, fHostCoins, fHostGets, fAgencyCommission, false);
                        if (loadedHostCount >= expectedHostCount) fetchOwnTargetCard();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        // Name না পাইলেও Card দেখাবে - Dashboard ফাঁকা থাকবে না
                        final double fHostGets2 = SalaryPolicy.getHostSalary(fHostCoins);
                        final double fAgencyCommission2 = SalaryPolicy.getAgencyCommission(fHostCoins);
                        synchronized (AgencyDashboardActivity.this) {
                            totalCommission += fAgencyCommission2; loadedHostCount++;
                        }
                        addHostCard("👤 Host ID: " + fHostId2, fHostId2, fHostCoins, fHostGets2, fAgencyCommission2, false);
                        if (loadedHostCount >= expectedHostCount) fetchOwnTargetCard();
                    }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addHostCard(String title, String id, long coins, double hostEarnings, double agencyCommissionOrOwn, boolean isOwn) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(25, 20, 25, 20);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable(); bg.setColor(android.graphics.Color.parseColor(isOwn ? "#3D2A00" : "#2A1B4E")); bg.setCornerRadius(15); card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); p.setMargins(0, 0, 0, 15); card.setLayoutParams(p);
        TextView tvName = new TextView(this); tvName.setText(title + " (ID: " + id + ")"); tvName.setTextColor(android.graphics.Color.WHITE); tvName.setTextSize(15); tvName.setTypeface(null, android.graphics.Typeface.BOLD); card.addView(tvName);
        TextView tvTarget = new TextView(this); int lvl = SalaryPolicy.getAchievedLevel(coins); tvTarget.setText("🎯 Target (" + monthKey + "): " + String.format("%,d", coins) + " Coins • Level: " + (lvl == 0 ? "No Level Yet" : "Level " + lvl)); tvTarget.setTextColor(android.graphics.Color.parseColor("#00E676")); tvTarget.setTextSize(13); card.addView(tvTarget);
        TextView tvDollar = new TextView(this);
        if (isOwn) tvDollar.setText("💵 My Earnings: " + SalaryPolicy.money(hostEarnings));
        else tvDollar.setText("💵 Host Gets: " + SalaryPolicy.money(hostEarnings) + "  •  Agency Commission: " + SalaryPolicy.money(agencyCommissionOrOwn));
        tvDollar.setTextColor(android.graphics.Color.parseColor("#FFD700")); tvDollar.setTextSize(13); card.addView(tvDollar);
        hostListContainer.addView(card);
    }
}
