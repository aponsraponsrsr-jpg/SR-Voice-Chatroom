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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class HostWorkDataActivity extends AppCompatActivity {
    private TextView tvReceived, tvLevel, tvSalary, tvNext, tvMonth;
    private LinearLayout root, lockBox;
    private String myId = "", monthKey = "";
    private String firebaseUid = "";
    private SharedPreferences prefs;
    private boolean isHost = false;

    // MONTHLY KEY: 2026-11 -> 2026-12 হইলেই Data Automatic ZERO
    public static String getMonthKey() {
        Calendar c = Calendar.getInstance();
        int m = c.get(Calendar.MONTH) + 1;
        return c.get(Calendar.YEAR) + "-" + (m < 10 ? "0" + m : "" + m);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        monthKey = getMonthKey();

        // ===== GLOBAL + PERMANENT - ID RESOLVE - একটাই ID =====
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu != null) firebaseUid = fu.getUid();
        if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");

        String intentId = getIntent().getStringExtra("HOST_PROFILE_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("HOST_ANY_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("PROFILE_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("HOST_ID");
        if (intentId == null || intentId.isEmpty()) intentId = getIntent().getStringExtra("MY_ID");

        myId = prefs.getString("MY_PROFILE_ID", "");
        if (myId == null || myId.isEmpty()) myId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myId == null) myId = "";
        // Intent ID কে Priority - Host Center থেকে যেটা আসছে ওইটাই আসল ID
        if (intentId != null && !intentId.isEmpty()) myId = intentId.trim();

        isHost = prefs.getBoolean("IS_HOST", false) && prefs.getBoolean("HOST_ACTIVE", false);
        if (!isHost) isHost = prefs.getBoolean("IS_HOST_" + myId, false) || prefs.getBoolean("HOST_ACCEPTED_" + myId, false);
        if (getIntent().getBooleanExtra("FROM_HOST_ACCEPT", false)) isHost = true;

        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24, 24, 24, 24); scroll.addView(root);
        TextView btnBack = new TextView(this); btnBack.setText("← Back to Host Center"); btnBack.setTextColor(Color.WHITE); btnBack.setTextSize(14); btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setOnClickListener(v -> finish()); root.addView(btnBack);

        // LOCK
        lockBox = new LinearLayout(this); lockBox.setOrientation(LinearLayout.VERTICAL); lockBox.setGravity(Gravity.CENTER); lockBox.setPadding(30, 60, 30, 60);
        android.graphics.drawable.GradientDrawable lockBg = new android.graphics.drawable.GradientDrawable(); lockBg.setColor(Color.parseColor("#1B1035")); lockBg.setCornerRadius(22); lockBg.setStroke(1, Color.parseColor("#FFD700")); lockBox.setBackground(lockBg);
        lockBox.setVisibility(isHost ? View.GONE : View.VISIBLE); root.addView(lockBox);
        TextView lockIcon = new TextView(this); lockIcon.setText("🔒"); lockIcon.setTextSize(48); lockIcon.setGravity(Gravity.CENTER); lockBox.addView(lockIcon);
        TextView lockTitle = new TextView(this); lockTitle.setText("Work Data Locked"); lockTitle.setTextColor(Color.WHITE); lockTitle.setTextSize(20); lockTitle.setTypeface(null, Typeface.BOLD); lockTitle.setGravity(Gravity.CENTER); lockBox.addView(lockTitle);
        TextView lockMsg = new TextView(this); lockMsg.setText("Hosting নিলে Work Data দেখতে পাবেন\n\nAgency থেকে Host Invite Accept করলে\nএই Work Data Automatic খুলে যাবে।"); lockMsg.setTextColor(Color.parseColor("#B0BEC5")); lockMsg.setTextSize(14); lockMsg.setGravity(Gravity.CENTER); lockBox.addView(lockMsg);

        TextView title = new TextView(this); title.setText("📊 Work Data - " + monthKey); title.setTextColor(Color.WHITE); title.setTextSize(20); title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        title.setVisibility(isHost ? View.VISIBLE : View.GONE); title.setTag("work_title"); root.addView(title);
        TextView tvId = new TextView(this); tvId.setText("My ID: " + myId + "  (Only Yours)"); tvId.setTextColor(Color.parseColor("#FFD700")); tvId.setTextSize(13); tvId.setTypeface(null, Typeface.BOLD); tvId.setGravity(Gravity.CENTER); tvId.setPadding(0, 0, 0, 20);
        tvId.setVisibility(isHost ? View.VISIBLE : View.GONE); tvId.setTag("work_id"); root.addView(tvId);

        // CACHE - Sudhu EKI MASER cache dekhabe, onno maser ta dekhabe na
        String savedMonth = prefs.getString("CACHED_MONTH_" + myId, "");
        long cachedCoins = 0;
        if (savedMonth.equals(monthKey)) cachedCoins = prefs.getLong("CACHED_COINS_" + myId + "_" + monthKey, 0);

        tvMonth = makeBig(root, "📅 Month: " + monthKey + " • Monthly Target • মাস শেষে Data ZERO হবে", isHost);
        tvReceived = makeBig(root, "", isHost);
        tvLevel = makeBig(root, "", isHost);
        tvSalary = makeBig(root, "", isHost);
        tvNext = makeBig(root, "", isHost);
        updateUI(cachedCoins);

        TextView note = new TextView(this); note.setText("\n• Ekhane sudhu tomar nijer Target dekhba\n• Agency Commission ekhane dekhba na\n• Onno Host er kichu ekhane dekhba na\n• মাস Change হইলেই এই পেজ 0 থেকে শুরু হবে");
        note.setTextColor(Color.parseColor("#B0BEC5")); note.setTextSize(12); note.setGravity(Gravity.CENTER); note.setVisibility(isHost ? View.VISIBLE : View.GONE); note.setTag("work_note"); root.addView(note);
        setContentView(scroll);
        checkHostFromFirebase();
        if (isHost) loadData();
    }

    private void updateUI(long received) {
        int lvl = SalaryPolicy.getAchievedLevel(received);
        double sal = SalaryPolicy.getHostSalary(received);
        String fmt = NumberFormat.getNumberInstance(Locale.US).format(received);
        if (tvReceived != null) tvReceived.setText("My Received (" + monthKey + "): " + fmt + " Coins");
        if (tvLevel != null) tvLevel.setText("My Level: " + (lvl == 0 ? "No Level Yet" : "Level " + lvl));
        if (tvSalary != null) tvSalary.setText("My Salary: " + SalaryPolicy.money(sal) + "  (Only Yours)");
        if (tvNext != null) {
            long next = SalaryPolicy.getNextTarget(received);
            if (next == -1) tvNext.setText("Next Target: Max Level Done! 🎉");
            else tvNext.setText("Next Target: Level " + (lvl + 1) + " - " + NumberFormat.getNumberInstance(Locale.US).format(next) + " Coins\nBaki: " + NumberFormat.getNumberInstance(Locale.US).format(SalaryPolicy.getRemainingToNext(received)) + " Coins");
        }
    }

    private void showUnlocked() {
        if (isHost) return; isHost = true;
        prefs.edit().putBoolean("IS_HOST_" + myId, true).putBoolean("HOST_ACCEPTED_" + myId, true).putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        for (int i = 0; i < root.getChildCount(); i++) { View v = root.getChildAt(i); if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("work_")) v.setVisibility(View.VISIBLE); }
        loadData();
    }
    private void checkHostFromFirebase() {
        if (myId.isEmpty() && firebaseUid.isEmpty()) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        String key = myId.isEmpty() ? firebaseUid : myId;
        usersRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { if (isHostFromSnapshot(snap)) { showUnlocked(); return; } usersRef.orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() { @Override public void onDataChange(@NonNull DataSnapshot s2) { for (DataSnapshot ds : s2.getChildren()) if (isHostFromSnapshot(ds)) { showUnlocked(); return; } checkInAgencyHosts(); } @Override public void onCancelled(@NonNull DatabaseError e) { checkInAgencyHosts(); } }); }
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

    // --- MONTHLY LOAD: users/{id}/monthly/{2026-11}/receivedCoins ---
    private void loadData() {
        if (myId.isEmpty() && firebaseUid.isEmpty()) return;
        String key = myId.isEmpty() ? firebaseUid : myId;
        FirebaseDatabase.getInstance().getReference("users").child(key).child("monthly").child(monthKey).child("receivedCoins").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long received = 0;
                if (snap.exists() && snap.getValue() != null) { try { received = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {} }
                prefs.edit().putLong("CACHED_COINS_" + myId + "_" + monthKey, received).putString("CACHED_MONTH_" + myId, monthKey).apply();
                updateUI(received);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        // Fallback: Firebase UID দিয়ে ও - UID আলাদা হলেও Coin মিস হবে না
        if (!firebaseUid.isEmpty() && !firebaseUid.equals(key)) {
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUid).child("monthly").child(monthKey).child("receivedCoins").addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!snap.exists() || snap.getValue() == null) return;
                    try { long c = Long.parseLong(String.valueOf(snap.getValue())); if (c > 0) { prefs.edit().putLong("CACHED_COINS_" + myId + "_" + monthKey, c).putString("CACHED_MONTH_" + myId, monthKey).apply(); updateUI(c); } } catch (Exception ignored) {}
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
    }
}
