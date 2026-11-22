package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class HostWorkDataActivity extends AppCompatActivity {
    private TextView tvReceived, tvLevel, tvSalary, tvNext, tvMonth;
    private TextView tvBeforeMonth, tvBeforeReceived, tvBeforeLevel, tvBeforeSalary;
    private LinearLayout root, lockBox;
    private String myId = "", monthKey = "", beforeMonthKey = "";
    private SharedPreferences prefs;
    private boolean isHost = false;
    // GOLDEN RULE - Profile ID Only - Any Digit Number - First Saved ID
    // UID never used - UID only stays in Database

    public static String getMonthKey() {
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + (m < 10 ? "0" + m : "" + m);
    }

    public static String getBeforeMonthKey() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        int m = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + (m < 10 ? "0" + m : "" + m);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        monthKey = getMonthKey();
        beforeMonthKey = getBeforeMonthKey();

        // FIX - UID neoa bondho - sudhu Profile ID
        String intentId = getIntent().getStringExtra("HOST_PROFILE_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("HOST_ANY_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("PROFILE_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("HOST_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("MY_ID");

        myId = prefs.getString("MY_PROFILE_ID", "");
        if (myId == null || myId.isEmpty()) myId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myId == null) myId = "";
        if (intentId != null && !intentId.isEmpty()) myId = intentId.trim();

        isHost = prefs.getBoolean("IS_HOST", false) && prefs.getBoolean("HOST_ACTIVE", false);
        if (!isHost) isHost = prefs.getBoolean("IS_HOST_" + myId, false) || prefs.getBoolean("HOST_ACCEPTED_" + myId, false);
        if (getIntent().getBooleanExtra("FROM_HOST_ACCEPT", false)) isHost = true;

        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24, 24, 24, 24); scroll.addView(root);
        TextView btnBack = new TextView(this); btnBack.setText("Back to Host Center"); btnBack.setTextColor(Color.WHITE); btnBack.setTextSize(14); btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setOnClickListener(v -> finish()); root.addView(btnBack);

        lockBox = new LinearLayout(this); lockBox.setOrientation(LinearLayout.VERTICAL); lockBox.setGravity(Gravity.CENTER); lockBox.setPadding(30, 60, 30, 60);
        android.graphics.drawable.GradientDrawable lockBg = new android.graphics.drawable.GradientDrawable(); lockBg.setColor(Color.parseColor("#1B1035")); lockBg.setCornerRadius(22); lockBg.setStroke(1, Color.parseColor("#FFD700")); lockBox.setBackground(lockBg);
        lockBox.setVisibility(isHost ? View.GONE : View.VISIBLE); root.addView(lockBox);
        TextView lockIcon = new TextView(this); lockIcon.setText("🔒"); lockIcon.setTextSize(48); lockIcon.setGravity(Gravity.CENTER); lockBox.addView(lockIcon);
        TextView lockTitle = new TextView(this); lockTitle.setText("Work Data Locked"); lockTitle.setTextColor(Color.WHITE); lockTitle.setTextSize(20); lockTitle.setTypeface(null, Typeface.BOLD); lockTitle.setGravity(Gravity.CENTER); lockBox.addView(lockTitle);
        TextView lockMsg = new TextView(this); lockMsg.setText("After accepting Hosting, you will see Work Data.\n\nAfter accepting Host Invite from Agency,\nthis Work Data will open automatically."); lockMsg.setTextColor(Color.parseColor("#B0BEC5")); lockMsg.setTextSize(14); lockMsg.setGravity(Gravity.CENTER); lockBox.addView(lockMsg);

        TextView title = new TextView(this); title.setText("Work Data - " + monthKey); title.setTextColor(Color.WHITE); title.setTextSize(20); title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        title.setVisibility(isHost ? View.VISIBLE : View.GONE); title.setTag("work_title"); root.addView(title);
        TextView tvId = new TextView(this); tvId.setText("My ID: " + myId); tvId.setTextColor(Color.parseColor("#FFD700")); tvId.setTextSize(13); tvId.setTypeface(null, Typeface.BOLD); tvId.setGravity(Gravity.CENTER); tvId.setPadding(0, 0, 0, 20);
        tvId.setVisibility(isHost ? View.VISIBLE : View.GONE); tvId.setTag("work_id"); root.addView(tvId);

        String savedMonth = prefs.getString("CACHED_MONTH_" + myId, "");
        long cachedCoins = 0;
        if (savedMonth.equals(monthKey)) cachedCoins = prefs.getLong("CACHED_COINS_" + myId + "_" + monthKey, 0);
        long cachedBefore = prefs.getLong("CACHED_COINS_" + myId + "_" + beforeMonthKey, 0);

        // RUNNING MONTH - Current - ZERO theke suru
        tvMonth = makeBig(root, "Running Month: " + monthKey + " - Monthly Target - At month end Data will be ZERO", isHost);
        tvReceived = makeBig(root, "", isHost);
        tvLevel = makeBig(root, "", isHost);
        tvSalary = makeBig(root, "", isHost);
        tvNext = makeBig(root, "", isHost);
        updateUI(cachedCoins);

        // BEFORE MONTH - History - Alada - Kokhono milbe na
        tvBeforeMonth = makeBig(root, "Before Month: " + beforeMonthKey + " - History - Previous Month - Separate", isHost);
        tvBeforeReceived = makeBig(root, "", isHost);
        tvBeforeLevel = makeBig(root, "", isHost);
        tvBeforeSalary = makeBig(root, "", isHost);
        updateBeforeUI(cachedBefore);

        TextView note = new TextView(this); note.setText("\n• Here you will see only your own Target\n• You will not see other Host data here\n• When month changes, Running will start from 0 again - Before will show previous month\n• 2 Dollar amounts will never mix together - Separate");
        note.setTextColor(Color.parseColor("#B0BEC5")); note.setTextSize(12); note.setGravity(Gravity.CENTER); note.setVisibility(isHost ? View.VISIBLE : View.GONE); note.setTag("work_note"); root.addView(note);
        setContentView(scroll);
        checkHostFromFirebase();
        if (isHost) loadData();
    }

    // FIX - Crash Fix - getHostSalary use korlam - getHostOnlySalary chilo na
    private void updateUI(long received) {
        int lvl = SalaryPolicy.getAchievedLevel(received);
        double sal = SalaryPolicy.getHostSalary(received); // Host Only
        String fmt = NumberFormat.getNumberInstance(Locale.US).format(received);
        if (tvReceived != null) tvReceived.setText("My Received (" + monthKey + "): " + fmt + " Coins");
        if (tvLevel != null) tvLevel.setText("My Level: " + (lvl == 0 ? "No Level Yet" : "Level " + lvl));
        if (tvSalary != null) tvSalary.setText("My Host Salary: " + SalaryPolicy.money(sal));
        if (tvNext != null) {
            long next = SalaryPolicy.getNextTarget(received);
            if (next == -1) tvNext.setText("Next Target: Max Level Done!");
            else tvNext.setText("Next Target: Level " + (lvl + 1) + " - " + NumberFormat.getNumberInstance(Locale.US).format(next) + " Coins\nRemaining: " + NumberFormat.getNumberInstance(Locale.US).format(SalaryPolicy.getRemainingToNext(received)) + " Coins");
        }
    }

    private void updateBeforeUI(long received) {
        int lvl = SalaryPolicy.getAchievedLevel(received);
        double sal = SalaryPolicy.getHostSalary(received);
        String fmt = NumberFormat.getNumberInstance(Locale.US).format(received);
        if (tvBeforeReceived != null) tvBeforeReceived.setText("Before Received (" + beforeMonthKey + "): " + fmt + " Coins");
        if (tvBeforeLevel != null) tvBeforeLevel.setText("Before Level: " + (lvl == 0 ? "No Level" : "Level " + lvl));
        if (tvBeforeSalary != null) tvBeforeSalary.setText("Before Salary: " + SalaryPolicy.money(sal) + " (History - Separate)");
    }

    private void showUnlocked() {
        if (isHost) return; isHost = true;
        prefs.edit().putBoolean("IS_HOST_" + myId, true).putBoolean("HOST_ACCEPTED_" + myId, true).putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        for (int i = 0; i < root.getChildCount(); i++) { View v = root.getChildAt(i); if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("work_")) v.setVisibility(View.VISIBLE); }
        loadData();
    }
    private void checkHostFromFirebase() {
        if (myId.isEmpty()) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { if (isHostFromSnapshot(snap)) { showUnlocked(); return; } checkInAgencyHosts(); }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private boolean isHostFromSnapshot(DataSnapshot snap) {
        if (snap == null || !snap.exists()) return false;
        if (isTrue(snap, "isHost") || isTrue(snap, "isHostAccepted") || isTrue(snap, "hostAccepted") || isTrue(snap, "isAnchor")) return true;
        if (snap.child("hostStatus").getValue() != null && String.valueOf(snap.child("hostStatus").getValue()).equalsIgnoreCase("accepted")) return true;
        if (snap.child("hostStatus").getValue() != null && String.valueOf(snap.child("hostStatus").getValue()).equalsIgnoreCase("active")) return true;
        if (snap.child("agencyId").getValue() != null && !String.valueOf(snap.child("agencyId").getValue()).isEmpty()) return true;
        if (snap.child("hostAgencyId").getValue() != null && !String.valueOf(snap.child("hostAgencyId").getValue()).isEmpty()) return true;
        return false;
    }
    private boolean isTrue(DataSnapshot snap, String key) { if (!snap.hasChild(key)) return false; Object v = snap.child(key).getValue(); return v != null && (v.toString().equalsIgnoreCase("true") || v.toString().equals("1")); }
    private void checkInAgencyHosts() { FirebaseDatabase.getInstance().getReference("agencies").addListenerForSingleValueEvent(new ValueEventListener() { @Override public void onDataChange(@NonNull DataSnapshot snap) { for (DataSnapshot ag : snap.getChildren()) if (ag.child("hosts").hasChild(myId)) { showUnlocked(); return; } } @Override public void onCancelled(@NonNull DatabaseError e) {} }); }
    private TextView makeBig(LinearLayout root, String t, boolean visible) {
        TextView tv = new TextView(this); tv.setText(t); tv.setTextColor(Color.parseColor("#FFD700")); tv.setTextSize(16); tv.setTypeface(null, Typeface.BOLD); tv.setPadding(24, 24, 24, 24); tv.setBackgroundColor(Color.parseColor("#1B1035"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 0, 16); tv.setLayoutParams(lp); tv.setVisibility(visible ? View.VISIBLE : View.GONE); root.addView(tv); return tv;
    }

    // MONTHLY LOAD: users/{ProfileID}/monthly/{month}/receivedCoins - sudhu Profile ID
    private void loadData() {
        if (myId.isEmpty()) return;
        // Running Month
        FirebaseDatabase.getInstance().getReference("users").child(myId).child("monthly").child(monthKey).child("receivedCoins").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long received = 0;
                if (snap.exists() && snap.getValue() != null) { try { received = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {} }
                prefs.edit().putLong("CACHED_COINS_" + myId + "_" + monthKey, received).putString("CACHED_MONTH_" + myId, monthKey).apply();
                updateUI(received);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        // Before Month - History - Running er sathe milbe na
        FirebaseDatabase.getInstance().getReference("users").child(myId).child("monthly").child(beforeMonthKey).child("receivedCoins").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long received = 0;
                if (snap.exists() && snap.getValue() != null) { try { received = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {} }
                prefs.edit().putLong("CACHED_COINS_" + myId + "_" + beforeMonthKey, received).apply();
                updateBeforeUI(received);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        // FIX - UID Fallback BAD - UID diye Coin anbo na
    }
}
