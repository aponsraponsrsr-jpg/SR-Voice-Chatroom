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

        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(Color.parseColor("#FFFDE7"));
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(22, 28, 22, 28); scroll.addView(root);
        TextView btnBack = new TextView(this); btnBack.setText("← Back to Agency Dashboard"); btnBack.setTextColor(Color.parseColor("#1B5E20")); btnBack.setTypeface(null, Typeface.BOLD); btnBack.setTextSize(14);
        btnBack.setOnClickListener(v -> finish()); root.addView(btnBack);

        // LOCK
        lockBox = new LinearLayout(this); lockBox.setOrientation(LinearLayout.VERTICAL); lockBox.setGravity(Gravity.CENTER); lockBox.setPadding(30, 60, 30, 60);
        GradientDrawable lockBg = new GradientDrawable(); lockBg.setColor(Color.parseColor("#1B1035")); lockBg.setCornerRadius(22); lockBg.setStroke(2, Color.parseColor("#FFD700")); lockBox.setBackground(lockBg);
        lockBox.setVisibility(isAgency ? View.GONE : View.VISIBLE); root.addView(lockBox);
        TextView lockIcon = new TextView(this); lockIcon.setText("🔒"); lockIcon.setTextSize(48); lockIcon.setGravity(Gravity.CENTER); lockBox.addView(lockIcon);
        TextView lockTitle = new TextView(this); lockTitle.setText("Agency Policy Locked"); lockTitle.setTextColor(Color.WHITE); lockTitle.setTextSize(20); lockTitle.setTypeface(null, Typeface.BOLD); lockTitle.setGravity(Gravity.CENTER); lockBox.addView(lockTitle);
        TextView lockMsg = new TextView(this); lockMsg.setText("Only Agency Owner can see this Policy\n\nAgency পেলে এই Policy Automatic খুলে যাবে।"); lockMsg.setTextColor(Color.parseColor("#B0BEC5")); lockMsg.setTextSize(14); lockMsg.setGravity(Gravity.CENTER); lockBox.addView(lockMsg);

        TextView title = new TextView(this); title.setText("🏛️ AGENCY POLICY - FULL - " + monthKey); title.setTextColor(Color.parseColor("#1B5E20")); title.setTextSize(20); title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        title.setVisibility(isAgency ? View.VISIBLE : View.GONE); title.setTag("agency_title"); root.addView(title);
        TextView sub = new TextView(this); sub.setText("Only Agency Owner can see this • Host NEVER sees Agency Commission\nCompany Database থেকে হিসাব • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#555555")); sub.setTextSize(12); sub.setGravity(Gravity.CENTER); sub.setPadding(0, 8, 0, 18);
        sub.setVisibility(isAgency ? View.VISIBLE : View.GONE); sub.setTag("agency_sub"); root.addView(sub);

        // SUMMARY - PREMIUM
        LinearLayout sumBox = new LinearLayout(this); sumBox.setOrientation(LinearLayout.VERTICAL); sumBox.setPadding(26, 22, 26, 22);
        GradientDrawable sumBg = new GradientDrawable(); sumBg.setColors(new int[]{Color.parseColor("#FF6A00"), Color.parseColor("#7B2FF7")}); sumBg.setOrientation(GradientDrawable.Orientation.TL_BR); sumBg.setCornerRadius(22); sumBg.setStroke(2, Color.parseColor("#FFD700")); sumBox.setBackground(sumBg);
        LinearLayout.LayoutParams sumLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); sumLp.setMargins(0, 0, 0, 18); sumBox.setLayoutParams(sumLp);
        sumBox.setVisibility(isAgency ? View.VISIBLE : View.GONE); sumBox.setTag("agency_sum"); root.addView(sumBox);
        tvTotalHosts = new TextView(this); tvTotalHosts.setText("My Hosts: " + prefs.getInt("CACHED_HOST_COUNT_" + myAgencyId, 0)); tvTotalHosts.setTextColor(Color.WHITE); tvTotalHosts.setTypeface(null, Typeface.BOLD); tvTotalHosts.setTextSize(15); sumBox.addView(tvTotalHosts);
        tvTotalDollar = new TextView(this); tvTotalDollar.setText("My Agency Commission Total (" + monthKey + "): $" + formatDouble(prefs.getFloat("CACHED_AGENCY_TOTAL_" + myAgencyId, 0))); tvTotalDollar.setTextColor(Color.parseColor("#FFD700")); tvTotalDollar.setTypeface(null, Typeface.BOLD); tvTotalDollar.setTextSize(15); sumBox.addView(tvTotalDollar);
        tvMyTotal = new TextView(this); tvMyTotal.setText("Total Payout (Host + Agency): $" + formatDouble(prefs.getFloat("CACHED_TOTAL_PAYOUT_" + myAgencyId, 0))); tvMyTotal.setTextColor(Color.parseColor("#00E676")); tvMyTotal.setTypeface(null, Typeface.BOLD); tvMyTotal.setTextSize(15); sumBox.addView(tvMyTotal);

        // POLICY CARDS - PREMIUM - Demo er moto - Green Box
        TextView tableTitle = new TextView(this); tableTitle.setText("FULL POLICY TABLE - " + SalaryPolicy.LEVELS.length + " Levels"); tableTitle.setTextColor(Color.parseColor("#1B5E20")); tableTitle.setTypeface(null, Typeface.BOLD); tableTitle.setTextSize(14); tableTitle.setPadding(0, 4, 0, 10);
        tableTitle.setVisibility(isAgency ? View.VISIBLE : View.GONE); tableTitle.setTag("agency_table_title"); root.addView(tableTitle);
        listBox = new LinearLayout(this); listBox.setOrientation(LinearLayout.VERTICAL); listBox.setVisibility(isAgency ? View.VISIBLE : View.GONE); listBox.setTag("agency_list"); root.addView(listBox);
        if (isAgency) buildCards();

        TextView hostTitle = new TextView(this); hostTitle.setText("👥 MY HOSTS - Management - " + monthKey); hostTitle.setTextColor(Color.parseColor("#1B5E20")); hostTitle.setTypeface(null, Typeface.BOLD); hostTitle.setTextSize(15); hostTitle.setPadding(0, 22, 0, 10);
        hostTitle.setVisibility(isAgency ? View.VISIBLE : View.GONE); hostTitle.setTag("agency_host_title"); root.addView(hostTitle);
        TextView hostSub = new TextView(this); hostSub.setText("কোন Host কত Target করছে, তার Total Dollar কত - সব এখানে দেখা যাবে"); hostSub.setTextColor(Color.parseColor("#555555")); hostSub.setTextSize(12); hostSub.setPadding(0, 0, 0, 10);
        hostSub.setVisibility(isAgency ? View.VISIBLE : View.GONE); hostSub.setTag("agency_host_sub"); root.addView(hostSub);
        hostListBox = new LinearLayout(this); hostListBox.setOrientation(LinearLayout.VERTICAL); hostListBox.setVisibility(isAgency ? View.VISIBLE : View.GONE); hostListBox.setTag("agency_host_list"); root.addView(hostListBox);
        TextView note = new TextView(this); note.setText("• Host শুধু তার Host Salary দেখে, Agency Commission দেখে না\n• Agency Commission শুধু Agency Owner দেখে\n• সব Counting Database থেকে - Monthly Only - Room Target (Weekly) alada\n• ID te Gift Receive holei Count - Self / Other - Any Amount");
        note.setTextColor(Color.parseColor("#555555")); note.setTextSize(12); note.setPadding(0, 18, 0, 0); note.setVisibility(isAgency ? View.VISIBLE : View.GONE); note.setTag("agency_note"); root.addView(note);
        setContentView(scroll);
        checkAgencyAccess();
        if (isAgency) loadMyHosts();
    }

    // PREMIUM BOX - Demo er moto - Level alada alada Box
    private void buildCards() {
        if (listBox == null) return;
        listBox.removeAllViews();
        for (int i = 0; i < SalaryPolicy.LEVELS.length; i++) {
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(22, 18, 22, 18);
            GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.parseColor("#E8F5E9")); bg.setCornerRadius(20); bg.setStroke(2, Color.parseColor("#4CAF50")); card.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 0, 14); card.setLayoutParams(lp);
            TextView t1 = new TextView(this); t1.setText("Level." + SalaryPolicy.LEVELS[i]); t1.setTextColor(Color.parseColor("#1B5E20")); t1.setTypeface(null, Typeface.BOLD); t1.setTextSize(18); card.addView(t1);
            // Header Row
            LinearLayout h = new LinearLayout(this); h.setOrientation(LinearLayout.HORIZONTAL);
            h.addView(policyCell("Target", true)); h.addView(policyCell("Anchor Salary", true)); h.addView(policyCell("Agent Salary", true)); card.addView(h);
            LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL);
            v.addView(policyCell(String.format("%,d", SalaryPolicy.TARGET_COINS[i]), false));
            v.addView(policyCell(SalaryPolicy.money(SalaryPolicy.ANCHOR_SALARY[i]), false));
            v.addView(policyCell(SalaryPolicy.money(SalaryPolicy.AGENCY_COMMISSION[i]), false)); card.addView(v);
            TextView t3 = new TextView(this); t3.setText("Total Salary: " + SalaryPolicy.money(SalaryPolicy.TOTAL_SALARY[i])); t3.setTextColor(Color.parseColor("#1B5E20")); t3.setTypeface(null, Typeface.BOLD); t3.setTextSize(14); t3.setPadding(0,10,0,0); card.addView(t3);
            listBox.addView(card);
        }
    }
    private TextView policyCell(String txt, boolean header) {
        TextView tv = new TextView(this); tv.setText(txt); tv.setGravity(Gravity.CENTER); tv.setPadding(8,12,8,12);
        tv.setBackgroundColor(header ? Color.parseColor("#4CAF50") : Color.WHITE);
        tv.setTextColor(header ? Color.WHITE : Color.BLACK); tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,-2,1f); p.setMargins(2,2,2,2); tv.setLayoutParams(p); return tv;
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
        FirebaseDatabase.getInstance().getReference("agencies").child(myAgencyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) { showUnlocked(); return; }
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

    // FIX - hosts + agencyId diye join kora host + Own - sob dekhbe - Realtime
    private void loadMyHosts() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        // 1. agencies/hosts
        FirebaseDatabase.getInstance().getReference("agencies").child(myAgencyId).child("hosts").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) for (DataSnapshot h : snap.getChildren()) if (h.getKey() != null) attachHostRow(h.getKey());
                // 2. Firebase UID diyeo
                if (!firebaseUid.isEmpty() && !firebaseUid.equals(myAgencyId)) {
                    FirebaseDatabase.getInstance().getReference("agencies").child(firebaseUid).child("hosts").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot s2) { if (s2.exists()) for (DataSnapshot h : s2.getChildren()) if (h.getKey() != null) attachHostRow(h.getKey()); }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                }
                // 3. users table e agencyId == amar ID - 2 Party Fix
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot all) {
                        for (DataSnapshot u : all.getChildren()) {
                            if (u.getKey() == null) continue;
                            String a1 = u.child("agencyId").getValue(String.class);
                            String a2 = u.child("agencyOwnerId").getValue(String.class);
                            if ((a1 != null && (a1.equals(myAgencyId) || a1.equals(firebaseUid))) || (a2 != null && (a2.equals(myAgencyId) || a2.equals(firebaseUid)))) attachHostRow(u.getKey());
                        }
                        // Own o dekhabe - Self Gift Count er jonno
                        attachHostRow(myAgencyId);
                        if (!firebaseUid.isEmpty()) attachHostRow(firebaseUid);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private final java.util.Set<String> attachedHosts = new java.util.HashSet<>();
    private void attachHostRow(String rawId) {
        if (rawId == null || rawId.isEmpty()) return;
        // Profile ID -> UID resolve kore - tarpor Monthly porbe
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(rawId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                String uid = rawId;
                if (snap.exists()) uid = rawId;
                else {
                    // Profile ID diye khujo
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot all) {
                            for (DataSnapshot u : all.getChildren()) {
                                if (u.getKey() == null) continue;
                                Object pid = u.child("my9DigitId").getValue(); if (pid != null && String.valueOf(pid).equals(rawId)) { listenMonthly(u.getKey()); return; }
                                Object pid2 = u.child("profileId").getValue(); if (pid2 != null && String.valueOf(pid2).equals(rawId)) { listenMonthly(u.getKey()); return; }
                            }
                            listenMonthly(rawId);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                    return;
                }
                listenMonthly(uid);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // CORE - Monthly Only - Room Target porbe na - Self Gift o Count
    private void listenMonthly(String uid) {
        if (attachedHosts.contains(uid)) return; attachedHosts.add(uid);
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("monthly").child(monthKey).child("receivedCoins").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { refreshHostList(); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        // Name o ekbar niye refresh
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { refreshHostList(); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void refreshHostList() {
        if (hostListBox == null) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot all) {
                hostListBox.removeAllViews();
                int count = 0; double agencyTotal = 0; double hostTotal = 0; int serial = 0;
                for (String uid : attachedHosts) {
                    DataSnapshot child = all.child(uid);
                    if (!child.exists()) continue;
                    String name = child.hasChild("name") ? String.valueOf(child.child("name").getValue()) : "Host";
                    // FIX - Sudhu Monthly - Fallback NAI - Monthly na thakle 0
                    long received = 0;
                    if (child.hasChild("monthly") && child.child("monthly").hasChild(monthKey) && child.child("monthly").child(monthKey).hasChild("receivedCoins")) {
                        try { received = Long.parseLong(String.valueOf(child.child("monthly").child(monthKey).child("receivedCoins").getValue())); } catch (Exception ignored) {}
                    }
                    // Own keo Host hisebe dhorbo na - Own alada Summary te - kintu Row te dekhale valo - Own ke skip kori na - dekhai
                    int lvl = SalaryPolicy.getAchievedLevel(received); double hSal = SalaryPolicy.getHostSalary(received); double aSal = SalaryPolicy.getAgencyCommission(received);
                    // Own er Commission Agency Total e add korbo na - Own er Salary alada
                    boolean isOwn = uid.equals(myAgencyId) || uid.equals(firebaseUid);
                    if (!isOwn) { agencyTotal += aSal; hostTotal += hSal; count++; }
                    else { hostTotal += hSal; }
                    serial++;
                    addHostRow(serial, uid, name + (isOwn ? " (You)" : ""), received, lvl, hSal, aSal);
                }
                if (serial == 0) {
                    TextView e = new TextView(AgencyPolicyActivity.this); e.setText("No Host yet under your Agency"); e.setTextColor(Color.parseColor("#555555")); e.setGravity(Gravity.CENTER); e.setPadding(0, 16, 0, 16); hostListBox.addView(e);
                }
                updateTotals(count, agencyTotal, hostTotal);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void updateTotals(int count, double agencyTotal, double hostTotal) {
        if (tvTotalHosts != null) tvTotalHosts.setText("My Hosts (" + monthKey + "): " + count);
        if (tvTotalDollar != null) tvTotalDollar.setText("My Agency Commission Total: $" + formatDouble(agencyTotal));
        if (tvMyTotal != null) tvMyTotal.setText("Total Payout (Host + Agency): $" + formatDouble(hostTotal + agencyTotal));
        prefs.edit().putInt("CACHED_HOST_COUNT_" + myAgencyId, count).putFloat("CACHED_AGENCY_TOTAL_" + myAgencyId, (float) agencyTotal).putFloat("CACHED_TOTAL_PAYOUT_" + myAgencyId, (float) (hostTotal + agencyTotal)).apply();
    }
    private void addHostRow(int serial, String hostId, String name, long received, int level, double hSal, double aSal) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(22, 16, 22, 16);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.parseColor("#FFFFFF")); bg.setCornerRadius(16); bg.setStroke(1, Color.parseColor("#4CAF50")); row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 0, 10); row.setLayoutParams(lp); hostListBox.addView(row);
        TextView t1 = new TextView(this); t1.setText(serial + ". " + name + " (ID: " + hostId + ") • Level " + (level == 0 ? "-" : level)); t1.setTextColor(Color.BLACK); t1.setTypeface(null, Typeface.BOLD); t1.setTextSize(14); row.addView(t1);
        TextView t2 = new TextView(this); t2.setText("Received (" + monthKey + "): " + String.format("%,d", received) + " Coins  |  Host: $" + formatDouble(hSal) + "  |  Agency: $" + formatDouble(aSal)); t2.setTextColor(Color.parseColor("#1B5E20")); t2.setTextSize(12); row.addView(t2);
    }
    private String formatDouble(double d) { if (d == (long) d) return String.valueOf((long) d); return String.valueOf(d); }
}
