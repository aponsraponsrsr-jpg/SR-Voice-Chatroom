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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.Calendar;

public class HostPolicyActivity extends AppCompatActivity {
    private LinearLayout listBox, root, lockBox;
    private TextView tvMyProgress, tvMyLevel, tvMySalary, tvNext;
    private ProgressBar progressBar;
    private String myId = "";
    private String firebaseUid = "";
    private String myUid = "";
    private String monthKey = "";
    private SharedPreferences prefs;
    private boolean isHost = false;

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
        if (!isHost) isHost = prefs.getBoolean("IS_HOST_" + myId, false);
        if (!isHost) isHost = prefs.getBoolean("HOST_ACCEPTED_" + myId, false);
        if (getIntent().getBooleanExtra("FROM_HOST_ACCEPT", false)) isHost = true;

        // ===== FIX - AGENCY MANEI AUTO HOST - ALADA HOST NITE HOBE NA =====
        boolean isAgencyOwner = prefs.getBoolean("IS_AGENCY_PERMANENT", false) || prefs.getBoolean("IS_AGENCY", false);
        if (isAgencyOwner) isHost = true;
        if (getIntent().getBooleanExtra("FROM_AGENCY_DASHBOARD", false)) isHost = true;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#FFFDE7"));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 28);
        scroll.addView(root);

        TextView btnBack = new TextView(this);
        btnBack.setText("← Back to Host Center");
        btnBack.setTextColor(Color.parseColor("#1B5E20")); btnBack.setTypeface(null, Typeface.BOLD); btnBack.setTextSize(14);
        btnBack.setOnClickListener(v -> finish());
        root.addView(btnBack);

        lockBox = new LinearLayout(this);
        lockBox.setOrientation(LinearLayout.VERTICAL); lockBox.setGravity(Gravity.CENTER);
        lockBox.setPadding(30, 60, 30, 60);
        GradientDrawable lockBg = new GradientDrawable();
        lockBg.setColor(Color.parseColor("#1B1035")); lockBg.setCornerRadius(22);
        lockBg.setStroke(2, Color.parseColor("#FFD700"));
        lockBox.setBackground(lockBg);
        lockBox.setVisibility(isHost ? View.GONE : View.VISIBLE);
        root.addView(lockBox);
        TextView lockIcon = new TextView(this); lockIcon.setText("🔒"); lockIcon.setTextSize(48); lockIcon.setGravity(Gravity.CENTER); lockBox.addView(lockIcon);
        TextView lockTitle = new TextView(this); lockTitle.setText("Host Policy Locked"); lockTitle.setTextColor(Color.WHITE); lockTitle.setTextSize(20); lockTitle.setTypeface(null, Typeface.BOLD); lockTitle.setGravity(Gravity.CENTER); lockBox.addView(lockTitle);
        TextView lockMsg = new TextView(this); lockMsg.setText("Hosting নিলে Policy দেখতে পাবেন\n\nAgency থেকে Host Invite Accept করলে\nএই Policy Automatic খুলে যাবে।"); lockMsg.setTextColor(Color.parseColor("#B0BEC5")); lockMsg.setTextSize(14); lockMsg.setGravity(Gravity.CENTER); lockBox.addView(lockMsg);

        TextView title = new TextView(this); title.setText("🎯 HOST SALARY POLICY - " + monthKey);
        title.setTextColor(Color.parseColor("#1B5E20")); title.setTextSize(20); title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        title.setVisibility(isHost ? View.VISIBLE : View.GONE); title.setTag("policy_title"); root.addView(title);
        TextView sub = new TextView(this); sub.setText("Official Policy • Monthly Settlement\nCompany Database থেকে হিসাব হবে • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#555555")); sub.setTextSize(12); sub.setGravity(Gravity.CENTER); sub.setPadding(0, 8, 0, 18);
        sub.setVisibility(isHost ? View.VISIBLE : View.GONE); sub.setTag("policy_sub"); root.addView(sub);

        LinearLayout myBox = new LinearLayout(this); myBox.setOrientation(LinearLayout.VERTICAL); myBox.setPadding(26, 22, 26, 22);
        GradientDrawable myBg = new GradientDrawable(); myBg.setColors(new int[]{Color.parseColor("#FF6A00"), Color.parseColor("#7B2FF7"), Color.parseColor("#1B1035")}); myBg.setOrientation(GradientDrawable.Orientation.TL_BR); myBg.setCornerRadius(22); myBg.setStroke(2, Color.parseColor("#FFD700"));
        myBox.setBackground(myBg);
        LinearLayout.LayoutParams myLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); myLp.setMargins(0, 0, 0, 18); myBox.setLayoutParams(myLp);
        myBox.setVisibility(isHost ? View.VISIBLE : View.GONE); myBox.setTag("policy_mybox"); root.addView(myBox);
        tvMyProgress = new TextView(this); tvMyProgress.setTextColor(Color.WHITE); tvMyProgress.setTypeface(null, Typeface.BOLD); tvMyProgress.setTextSize(15); myBox.addView(tvMyProgress);
        tvMyLevel = new TextView(this); tvMyLevel.setTextColor(Color.parseColor("#FFD700")); tvMyLevel.setTypeface(null, Typeface.BOLD); tvMyLevel.setTextSize(15); myBox.addView(tvMyLevel);
        tvMySalary = new TextView(this); tvMySalary.setTextColor(Color.parseColor("#00E676")); tvMySalary.setTypeface(null, Typeface.BOLD); tvMySalary.setTextSize(16); myBox.addView(tvMySalary);
        tvNext = new TextView(this); tvNext.setTextColor(Color.WHITE); tvNext.setTextSize(13); tvNext.setPadding(0, 10, 0, 0); myBox.addView(tvNext);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); progressBar.setMax(100); progressBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 18)); myBox.addView(progressBar);

        listBox = new LinearLayout(this); listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setVisibility(isHost ? View.VISIBLE : View.GONE); listBox.setTag("policy_list"); root.addView(listBox);
        if (isHost) buildCards(0);

        TextView note = new TextView(this);
        note.setText("• Target পুরা Fill হইলেই Salary Counting হবে - 1 Coin কম থাকলেও 0$\n• আপনি শুধু নিজের Coin + নিজের Dollar দেখছেন\n• ID te Gift Receive holei Count - Self / Other - Any Amount\n• Room Target (Weekly) alada - Policy er sathe milbe na\n• এই পলিসি Company থেকে আসে, Host বদলাতে পারবে না");
        note.setTextColor(Color.parseColor("#555555")); note.setTextSize(12); note.setPadding(0, 18, 0, 0);
        note.setVisibility(isHost ? View.VISIBLE : View.GONE); note.setTag("policy_note"); root.addView(note);
        setContentView(scroll);
        checkHostFromFirebase();
        if (isHost) { resolveUidAndLoad(); }
    }

    private void buildCards(int myLevel) {
        if (listBox == null) return;
        listBox.removeAllViews();
        for (int i = 0; i < SalaryPolicy.LEVELS.length; i++) {
            boolean done = myLevel > 0 && SalaryPolicy.LEVELS[i] <= myLevel;
            boolean current = myLevel + 1 == SalaryPolicy.LEVELS[i];
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(22, 18, 22, 18);
            GradientDrawable bg = new GradientDrawable();
            if (done) { bg.setColor(Color.parseColor("#E8F5E9")); bg.setStroke(2, Color.parseColor("#00C853")); }
            else if (current) { bg.setColors(new int[]{Color.parseColor("#FFF3E0"), Color.parseColor("#FFE0B2")}); bg.setOrientation(GradientDrawable.Orientation.TL_BR); bg.setStroke(2, Color.parseColor("#FF6A00")); }
            else { bg.setColor(Color.parseColor("#FFFFFF")); bg.setStroke(2, Color.parseColor("#4CAF50")); }
            bg.setCornerRadius(20);
            card.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 0, 14); card.setLayoutParams(lp);
            TextView t1 = new TextView(this); t1.setText((done ? "✅ " : current ? "🎯 " : "🔒 ") + "Level." + SalaryPolicy.LEVELS[i]); t1.setTextColor(Color.parseColor("#1B5E20")); t1.setTypeface(null, Typeface.BOLD); t1.setTextSize(18); card.addView(t1);
            LinearLayout h = new LinearLayout(this); h.setOrientation(LinearLayout.HORIZONTAL);
            h.addView(policyCell("Target", true)); h.addView(policyCell("Anchor Salary", true)); h.addView(policyCell("Agent Salary", true)); card.addView(h);
            LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL);
            v.addView(policyCell(String.format("%,d", SalaryPolicy.TARGET_COINS[i]), false));
            v.addView(policyCell(SalaryPolicy.money(SalaryPolicy.ANCHOR_SALARY[i]), false));
            v.addView(policyCell(SalaryPolicy.money(SalaryPolicy.AGENCY_COMMISSION[i]), false)); card.addView(v);
            TextView t3 = new TextView(this); t3.setText("Total Salary: " + SalaryPolicy.money(SalaryPolicy.TOTAL_SALARY[i]) + (done ? "  •  ✅ Achieved" : current ? "  •  🎯 Current" : "")); t3.setTextColor(Color.parseColor("#1B5E20")); t3.setTypeface(null, Typeface.BOLD); t3.setTextSize(14); t3.setPadding(0,10,0,0); card.addView(t3);
            listBox.addView(card);
        }
    }
    private TextView policyCell(String txt, boolean header) {
        TextView tv = new TextView(this); tv.setText(txt); tv.setGravity(Gravity.CENTER); tv.setPadding(8,12,8,12);
        tv.setBackgroundColor(header ? Color.parseColor("#4CAF50") : Color.WHITE);
        tv.setTextColor(header ? Color.WHITE : Color.BLACK); tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,-2,1f); p.setMargins(2,2,2,2); tv.setLayoutParams(p); return tv;
    }

    private void updateUI(long received) {
        int lvl = SalaryPolicy.getAchievedLevel(received);
        double sal = SalaryPolicy.getHostSalary(received);
        if (tvMyProgress != null) tvMyProgress.setText("My Received (" + monthKey + "): " + String.format("%,d", received) + " Coins");
        if (tvMyLevel != null) tvMyLevel.setText("My Level: " + (lvl == 0 ? "No Level Yet" : "Level " + lvl));
        if (tvMySalary != null) tvMySalary.setText("My Host Salary: " + SalaryPolicy.money(sal) + "  (Only Yours)");
        long next = SalaryPolicy.getNextTarget(received);
        if (tvNext != null) tvNext.setText(next == -1 ? "🎉 All Levels Complete!" : "Next: Level " + (lvl + 1) + " - Need " + String.format("%,d", SalaryPolicy.getRemainingToNext(received)) + " more coins");
        if (progressBar != null) {
            if (next == -1) progressBar.setProgress(100);
            else { long prev = lvl == 0 ? 0 : SalaryPolicy.TARGET_COINS[lvl - 1]; progressBar.setProgress((int) ((received - prev) * 100 / Math.max(1, next - prev))); }
        }
        buildCards(lvl);
    }

    private void showPolicyUnlocked() {
        if (isHost) return; isHost = true;
        prefs.edit().putBoolean("IS_HOST_" + myId, true).putBoolean("HOST_ACCEPTED_" + myId, true).putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        for (int i = 0; i < root.getChildCount(); i++) { View v = root.getChildAt(i); if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("policy_")) v.setVisibility(View.VISIBLE); }
        resolveUidAndLoad();
    }

    // ===== FIX - AGENCY CHECK - agencies/ID thaklei Host Unlock =====
    private void checkHostFromFirebase() {
        if (myId.isEmpty() && firebaseUid.isEmpty()) return;
        String key = myId.isEmpty() ? firebaseUid : myId;
        FirebaseDatabase.getInstance().getReference("agencies").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot agSnap) {
                if (agSnap.exists()) { showPolicyUnlocked(); return; }
                if (!firebaseUid.isEmpty() && !firebaseUid.equals(key)) {
                    FirebaseDatabase.getInstance().getReference("agencies").child(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot ag2) { if (ag2.exists()) { showPolicyUnlocked(); return; } checkHostNormal(); }
                        @Override public void onCancelled(@NonNull DatabaseError e) { checkHostNormal(); }
                    });
                    return;
                }
                checkHostNormal();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { checkHostNormal(); }
        });
    }

    private void checkHostNormal() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        String key = myId.isEmpty() ? firebaseUid : myId;
        usersRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (isHostFromSnapshot(snap)) { showPolicyUnlocked(); return; }
                usersRef.orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) { for (DataSnapshot ds : s2.getChildren()) if (isHostFromSnapshot(ds)) { showPolicyUnlocked(); return; } checkInAgencyHosts(); }
                    @Override public void onCancelled(@NonNull DatabaseError e) { checkInAgencyHosts(); }
                });
            }
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
    private void checkInAgencyHosts() {
        FirebaseDatabase.getInstance().getReference("agencies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { for (DataSnapshot ag : snap.getChildren()) if (ag.child("hosts").hasChild(myId)) { showPolicyUnlocked(); return; } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void resolveUidAndLoad() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        String key = myId.isEmpty() ? firebaseUid : myId;
        usersRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) { myUid = key; loadMyProgress(myUid); return; }
                usersRef.orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        for (DataSnapshot ds : s2.getChildren()) { myUid = ds.getKey(); loadMyProgress(myUid); return; }
                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot all) {
                                for (DataSnapshot u : all.getChildren()) {
                                    Object pid = u.child("profileId").getValue(); if (pid != null && String.valueOf(pid).equals(myId)) { myUid = u.getKey(); loadMyProgress(myUid); return; }
                                }
                                myUid = !firebaseUid.isEmpty() ? firebaseUid : key; loadMyProgress(myUid);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) {}
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void loadMyProgress(String uid) {
        if (uid == null || uid.isEmpty()) return;
        // Monthly Only - Agency ID te Receiving holeo - Host ID te Receiving holeo - eki UID er Monthly tei Count hobe
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("monthly").child(monthKey).child("receivedCoins").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long received = 0;
                if (snap.exists() && snap.getValue() != null) { try { received = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {} }
                prefs.edit().putLong("CACHED_COINS_" + myId + "_" + monthKey, received).putString("CACHED_MONTH_" + myId, monthKey).apply();
                updateUI(received);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
