package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.Calendar;

public class AgencyPolicyActivity extends AppCompatActivity {
    private LinearLayout listBox, hostListBox, root, lockBox;
    private TextView tvMyTotal, tvTotalHosts, tvTotalDollar;
    private String myAgencyId = "", myName = "Agency", monthKey = "";
    private String firebaseUid = "";
    private SharedPreferences prefs;
    private boolean isAgency = false;

    private static String getMonthKey() {
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + (m < 10 ? "0" + m : "" + m);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        monthKey = getMonthKey();

        // ===== GLOBAL - ID RESOLVE - সব Key Support =====
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu != null) firebaseUid = fu.getUid();
        if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");

        String intentId = getIntent().getStringExtra("AGENCY_OWNER_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("AGENCY_UID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("AGENCY_PROFILE_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("PROFILE_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("MY_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("AGENCY_ID");

        myAgencyId = prefs.getString("MY_PROFILE_ID", "");
        if (myAgencyId == null || myAgencyId.isEmpty()) myAgencyId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myAgencyId == null) myAgencyId = "";
        if (intentId != null && !intentId.isEmpty()) myAgencyId = intentId.trim();
        myName = prefs.getString("CACHED_AGENCY_NAME", prefs.getString("USER_NAME", "Agency"));

        isAgency = prefs.getBoolean("IS_AGENCY_PERMANENT", false);
        if (!isAgency) isAgency = prefs.getBoolean("IS_AGENCY", false) && prefs.getBoolean("AGENCY_ACTIVE", false);
        if (!isAgency) isAgency = prefs.getBoolean("IS_AGENCY", false) && prefs.getString("MY_PROFILE_ID", "").equals(myAgencyId);
        if (getIntent().getBooleanExtra("FROM_AGENCY_DASHBOARD", false)) isAgency = true;

        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(22, 28, 22, 28); scroll.addView(root);
        TextView btnBack = new TextView(this); btnBack.setText("← Back to Agency Dashboard"); btnBack.setTextColor(Color.WHITE); btnBack.setTypeface(null, Typeface.BOLD); btnBack.setTextSize(14);
        btnBack.setOnClickListener(v -> finish()); root.addView(btnBack);

        // LOCK
        lockBox = new LinearLayout(this); lockBox.setOrientation(LinearLayout.VERTICAL); lockBox.setGravity(Gravity.CENTER); lockBox.setPadding(30, 60, 30, 60);
        GradientDrawable lockBg = new GradientDrawable(); lockBg.setColor(Color.parseColor("#1B1035")); lockBg.setCornerRadius(22); lockBg.setStroke(2, Color.parseColor("#FFD700")); lockBox.setBackground(lockBg);
        lockBox.setVisibility(isAgency ? View.GONE : View.VISIBLE); root.addView(lockBox);
        TextView lockIcon = new TextView(this); lockIcon.setText("🔒"); lockIcon.setTextSize(48); lockIcon.setGravity(Gravity.CENTER); lockBox.addView(lockIcon);
        TextView lockTitle = new TextView(this); lockTitle.setText("Agency Policy Locked"); lockTitle.setTextColor(Color.WHITE); lockTitle.setTextSize(20); lockTitle.setTypeface(null, Typeface.BOLD); lockTitle.setGravity(Gravity.CENTER); lockBox.addView(lockTitle);
        TextView lockMsg = new TextView(this); lockMsg.setText("Only Agency Owner can see this Policy\n\nAgency পেলে এই Policy Automatic খুলে যাবে।"); lockMsg.setTextColor(Color.parseColor("#B0BEC5")); lockMsg.setTextSize(14); lockMsg.setGravity(Gravity.CENTER); lockBox.addView(lockMsg);

        TextView title = new TextView(this); title.setText("🏛️ AGENCY POLICY - FULL - " + monthKey); title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(20); title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        title.setVisibility(isAgency ? View.VISIBLE : View.GONE); title.setTag("agency_title"); root.addView(title);
        TextView sub = new TextView(this); sub.setText("Only Agency Owner can see this • Host NEVER sees Agency Commission\nCompany Database থেকে হিসাব • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#B0BEC5")); sub.setTextSize(12); sub.setGravity(Gravity.CENTER); sub.setPadding(0, 8, 0, 18);
        sub.setVisibility(isAgency ? View.VISIBLE : View.GONE); sub.setTag("agency_sub"); root.addView(sub);

        // SUMMARY - PREMIUM
        LinearLayout sumBox = new LinearLayout(this); sumBox.setOrientation(LinearLayout.VERTICAL); sumBox.setPadding(26, 22, 26, 22);
        GradientDrawable sumBg = new GradientDrawable(); sumBg.setColors(new int[]{Color.parseColor("#FF6A00"), Color.parseColor("#7B2FF7")}); sumBg.setOrientation(GradientDrawable.Orientation.TL_BR); sumBg.setCornerRadius(22); sumBg.setStroke(2, Color.parseColor("#FFD700")); sumBox.setBackground(sumBg);
        LinearLayout.LayoutParams sumLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); sumLp.setMargins(0, 0, 0, 18); sumBox.setLayoutParams(sumLp);
        sumBox.setVisibility(isAgency ? View.VISIBLE : View.GONE); sumBox.setTag("agency_sum"); root.addView(sumBox);
        tvTotalHosts = new TextView(this); tvTotalHosts.setText("My Hosts: " + prefs.getInt("CACHED_HOST_COUNT_" + myAgencyId, 0)); tvTotalHosts.setTextColor(Color.WHITE); tvTotalHosts.setTypeface(null, Typeface.BOLD); tvTotalHosts.setTextSize(15); sumBox.addView(tvTotalHosts);
        tvTotalDollar = new TextView(this); tvTotalDollar.setText("My Agency Commission Total (" + monthKey + "): $" + formatDouble(prefs.getFloat("CACHED_AGENCY_TOTAL_" + myAgencyId, 0))); tvTotalDollar.setTextColor(Color.parseColor("#FFD700")); tvTotalDollar.setTypeface(null, Typeface.BOLD); tvTotalDollar.setTextSize(15); sumBox.addView(tvTotalDollar);
        tvMyTotal = new TextView(this); tvMyTotal.setText("Total Payout (Host + Agency): $" + formatDouble(prefs.getFloat("CACHED_TOTAL_PAYOUT_" + myAgencyId, 0))); tvMyTotal.setTextColor(Color.parseColor("#00E676")); tvMyTotal.setTypeface(null, Typeface.BOLD); tvMyTotal.setTextSize(15); sumBox.addView(tvMyTotal);

        // POLICY CARDS - PREMIUM BANNER - FULL TABLE
        TextView tableTitle = new TextView(this); tableTitle.setText("FULL POLICY TABLE - 22 Levels"); tableTitle.setTextColor(Color.parseColor("#FFD700")); tableTitle.setTypeface(null, Typeface.BOLD); tableTitle.setTextSize(14); tableTitle.setPadding(0, 4, 0, 10);
        tableTitle.setVisibility(isAgency ? View.VISIBLE : View.GONE); tableTitle.setTag("agency_table_title"); root.addView(tableTitle);
        listBox = new LinearLayout(this); listBox.setOrientation(LinearLayout.VERTICAL); listBox.setVisibility(isAgency ? View.VISIBLE : View.GONE); listBox.setTag("agency_list"); root.addView(listBox);
        if (isAgency) buildCards();

        TextView hostTitle = new TextView(this); hostTitle.setText("👥 MY HOSTS - Management - " + monthKey); hostTitle.setTextColor(Color.parseColor("#FFD700")); hostTitle.setTypeface(null, Typeface.BOLD); hostTitle.setTextSize(15); hostTitle.setPadding(0, 22, 0, 10);
        hostTitle.setVisibility(isAgency ? View.VISIBLE : View.GONE); hostTitle.setTag("agency_host_title"); root.addView(hostTitle);
        TextView hostSub = new TextView(this); hostSub.setText("কোন Host কত Target করছে, তার Total Dollar কত - সব এখানে দেখা যাবে"); hostSub.setTextColor(Color.parseColor("#B0BEC5")); hostSub.setTextSize(12); hostSub.setPadding(0, 0, 0, 10);
        hostSub.setVisibility(isAgency ? View.VISIBLE : View.GONE); hostSub.setTag("agency_host_sub"); root.addView(hostSub);
        hostListBox = new LinearLayout(this); hostListBox.setOrientation(LinearLayout.VERTICAL); hostListBox.setVisibility(isAgency ? View.VISIBLE : View.GONE); hostListBox.setTag("agency_host_list"); root.addView(hostListBox);
        TextView note = new TextView(this); note.setText("• Host শুধু তার Host Salary দেখে, Agency Commission দেখে না\n• Agency Commission শুধু Agency Owner দেখে\n• সব Counting Database থেকে, কেউ নিজে থেকে Dollar লিখতে পারবে না");
        note.setTextColor(Color.parseColor("#B0BEC5")); note.setTextSize(12); note.setPadding(0, 18, 0, 0); note.setVisibility(isAgency ? View.VISIBLE : View.GONE); note.setTag("agency_note"); root.addView(note);
        setContentView(scroll);
        checkAgencyAccess();
        if (isAgency) loadMyHosts();
    }

    private void buildCards() {
        if (listBox == null) return;
        listBox.removeAllViews();
        for (int i = 0; i < SalaryPolicy.LEVELS.length; i++) {
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(22, 18, 22, 18);
            GradientDrawable bg = new GradientDrawable(); bg.setColors(new int[]{Color.parseColor("#2A1B4A"), Color.parseColor("#140B26")}); bg.setOrientation(GradientDrawable.Orientation.TL_BR); bg.setCornerRadius(20); bg.setStroke(2, Color.parseColor("#3A2A5A")); card.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 0, 12); card.setLayoutParams(lp);
            TextView t1 = new TextView(this); t1.setText("LEVEL " + SalaryPolicy.LEVELS[i]); t1.setTextColor(Color.WHITE); t1.setTypeface(null, Typeface.BOLD); t1.setTextSize(16); card.addView(t1);
            TextView t2 = new TextView(this); t2.setText("Target: " + String.format("%,d", SalaryPolicy.TARGET_COINS[i]) + " Coins"); t2.setTextColor(Color.parseColor("#FFD700")); t2.setTypeface(null, Typeface.BOLD); t2.setTextSize(14); card.addView(t2);
            TextView t3 = new TextView(this); t3.setText("Host (80%): " + SalaryPolicy.money(SalaryPolicy.ANCHOR_SALARY[i]) + "  •  Agency (20%): " + SalaryPolicy.money(SalaryPolicy.AGENCY_COMMISSION[i]) + "  •  Total: " + SalaryPolicy.money(SalaryPolicy.TOTAL_SALARY[i])); t3.setTextColor(Color.parseColor("#00E676")); t3.setTypeface(null, Typeface.BOLD); t3.setTextSize(13); card.addView(t3);
            listBox.addView(card);
        }
    }

    private void showUnlocked() {
        if (isAgency) return; isAgency = true;
        prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("IS_AGENCY_PERMANENT", true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        for (int i = 0; i < root.getChildCount(); i++) { View v = root.getChildAt(i); if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("agency_")) v.setVisibility(View.VISIBLE); }
        if (listBox != null && listBox.getChildCount() == 0) buildCards();
        loadMyHosts();
    }

    private void checkAgencyAccess() {
        if (myAgencyId.isEmpty() || isAgency) return;
        // UID দিয়ে Check - agencies/{uid}
        FirebaseDatabase.getInstance().getReference("agencies").child(myAgencyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) { showUnlocked(); return; }
                // Firebase UID দিয়ে Check
                if (!firebaseUid.isEmpty() && !firebaseUid.equals(myAgencyId)) {
                    FirebaseDatabase.getInstance().getReference("agencies").child(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot s3) { if (s3.exists()) { showUnlocked(); return; } checkByProfileId(); }
                        @Override public void onCancelled(@NonNull DatabaseError e) { checkByProfileId(); }
                    });
                    return;
                }
                checkByProfileId();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void checkByProfileId() {
        FirebaseDatabase.getInstance().getReference("users").orderByChild("my9DigitId").equalTo(myAgencyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s2) { for (DataSnapshot ds : s2.getChildren()) if (ds.hasChild("isAgency") && Boolean.TRUE.equals(ds.child("isAgency").getValue(Boolean.class))) { showUnlocked(); return; } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void loadMyHosts() {
        // Agency Owner UID দিয়ে hosts - agencies/{uid}/hosts
        String key = myAgencyId;
        FirebaseDatabase.getInstance().getReference("agencies").child(key).child("hosts").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                hostListBox.removeAllViews();
                if (!snap.exists() || snap.getChildrenCount() == 0) {
                    // Fallback UID দিয়ে
                    if (!firebaseUid.isEmpty() && !firebaseUid.equals(key)) { loadMyHostsByUid(firebaseUid); return; }
                    TextView e = new TextView(AgencyPolicyActivity.this); e.setText("No Host yet under your Agency"); e.setTextColor(Color.parseColor("#B0BEC5")); e.setGravity(Gravity.CENTER); e.setPadding(0, 16, 0, 16); hostListBox.addView(e); if (tvTotalHosts != null) tvTotalHosts.setText("My Hosts: 0"); return;
                }
                int[] count = {0}, serial = {0}; double[] agencyTotal = {0}, hostTotal = {0};
                for (DataSnapshot h : snap.getChildren()) { String hid = h.getKey(); if (hid == null || hid.equals(myAgencyId)) continue; fetchHostRow(hid, serial, count, agencyTotal, hostTotal, (int) snap.getChildrenCount()); }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void loadMyHostsByUid(String uid) {
        FirebaseDatabase.getInstance().getReference("agencies").child(uid).child("hosts").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                hostListBox.removeAllViews();
                if (!snap.exists() || snap.getChildrenCount() == 0) { TextView e = new TextView(AgencyPolicyActivity.this); e.setText("No Host yet under your Agency"); e.setTextColor(Color.parseColor("#B0BEC5")); e.setGravity(Gravity.CENTER); e.setPadding(0, 16, 0, 16); hostListBox.addView(e); if (tvTotalHosts != null) tvTotalHosts.setText("My Hosts: 0"); return; }
                int[] count = {0}, serial = {0}; double[] agencyTotal = {0}, hostTotal = {0};
                for (DataSnapshot h : snap.getChildren()) { String hid = h.getKey(); if (hid == null) continue; fetchHostRow(hid, serial, count, agencyTotal, hostTotal, (int) snap.getChildrenCount()); }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void fetchHostRow(String hostId, int[] serial, int[] count, double[] agencyTotal, double[] hostTotal, int expected) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(hostId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) { ref.orderByChild("my9DigitId").equalTo(hostId).addListenerForSingleValueEvent(new ValueEventListener() { @Override public void onDataChange(@NonNull DataSnapshot s2) { for (DataSnapshot ds : s2.getChildren()) { showHostRow(ds, hostId, serial, count, agencyTotal, hostTotal, expected); return; } count[0]++; if (count[0] >= expected) updateTotals(count[0], agencyTotal[0], hostTotal[0]); } @Override public void onCancelled(@NonNull DatabaseError e) {} }); return; }
                showHostRow(snap, hostId, serial, count, agencyTotal, hostTotal, expected);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void showHostRow(DataSnapshot child, String hostId, int[] serial, int[] count, double[] agencyTotal, double[] hostTotal, int expected) {
        String name = child.hasChild("name") ? String.valueOf(child.child("name").getValue()) : "Host";
        // ===== MONTHLY - users/{host}/monthly/{month}/receivedCoins - মাস Change = ZERO =====
        long received = 0;
        if (child.hasChild("monthly") && child.child("monthly").hasChild(monthKey) && child.child("monthly").child(monthKey).hasChild("receivedCoins")) {
            try { received = Long.parseLong(String.valueOf(child.child("monthly").child(monthKey).child("receivedCoins").getValue())); } catch (Exception ignored) {}
        } else {
            String[] keys = {"totalReceivedCoins", "totalCoins", "coins", "receivedCoins"};
            for (String k : keys) if (child.hasChild(k) && child.child(k).getValue() != null) { try { received = Long.parseLong(String.valueOf(child.child(k).getValue())); break; } catch (Exception ignored) {} }
        }
        int lvl = SalaryPolicy.getAchievedLevel(received); double hSal = SalaryPolicy.getHostSalary(received); double aSal = SalaryPolicy.getAgencyCommission(received);
        hostTotal[0] += hSal; agencyTotal[0] += aSal; count[0]++; serial[0]++;
        addHostRow(serial[0], hostId, name, received, lvl, hSal, aSal);
        if (count[0] >= expected) updateTotals(count[0], agencyTotal[0], hostTotal[0]);
    }
    private void updateTotals(int count, double agencyTotal, double hostTotal) {
        if (tvTotalHosts != null) tvTotalHosts.setText("My Hosts (" + monthKey + "): " + count);
        if (tvTotalDollar != null) tvTotalDollar.setText("My Agency Commission Total: $" + formatDouble(agencyTotal));
        if (tvMyTotal != null) tvMyTotal.setText("Total Payout (Host + Agency): $" + formatDouble(hostTotal + agencyTotal));
        prefs.edit().putInt("CACHED_HOST_COUNT_" + myAgencyId, count).putFloat("CACHED_AGENCY_TOTAL_" + myAgencyId, (float) agencyTotal).putFloat("CACHED_TOTAL_PAYOUT_" + myAgencyId, (float) (hostTotal + agencyTotal)).apply();
    }
    private void addHostRow(int serial, String hostId, String name, long received, int level, double hSal, double aSal) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(22, 16, 22, 16);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.parseColor("#1B1035")); bg.setCornerRadius(16); row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 0, 10); row.setLayoutParams(lp); hostListBox.addView(row);
        TextView t1 = new TextView(this); t1.setText(serial + ". " + name + " (ID: " + hostId + ") • Level " + (level == 0 ? "-" : level)); t1.setTextColor(Color.WHITE); t1.setTypeface(null, Typeface.BOLD); t1.setTextSize(14); row.addView(t1);
        TextView t2 = new TextView(this); t2.setText("Received (" + monthKey + "): " + String.format("%,d", received) + " Coins  |  Host: $" + formatDouble(hSal) + "  |  Agency: $" + formatDouble(aSal)); t2.setTextColor(Color.parseColor("#FFD700")); t2.setTextSize(12); row.addView(t2);
    }
    private String formatDouble(double d) { if (d == (long) d) return String.valueOf((long) d); return String.valueOf(d); }
}
