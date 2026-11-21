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
import com.google.firebase.database.*;

public class HostPolicyActivity extends AppCompatActivity {
    private LinearLayout listBox, root, lockBox;
    private TextView tvMyProgress, tvMyLevel, tvMySalary, tvNext;
    private ProgressBar progressBar;
    private String myId = "";
    private SharedPreferences prefs;
    private boolean isHost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = getIntent().getStringExtra("MY_ID");
        if (myId == null || myId.isEmpty()) myId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myId == null) myId = "";
        isHost = prefs.getBoolean("IS_HOST_" + myId, false);
        if (!isHost) isHost = prefs.getBoolean("HOST_ACCEPTED_" + myId, false);
        if (getIntent().getBooleanExtra("FROM_HOST_ACCEPT", false)) isHost = true;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 28);
        scroll.addView(root);

        TextView btnBack = new TextView(this);
        btnBack.setText("← Back to Host Center");
        btnBack.setTextColor(Color.WHITE); btnBack.setTypeface(null, Typeface.BOLD); btnBack.setTextSize(14);
        btnBack.setOnClickListener(v -> finish());
        root.addView(btnBack);

        // LOCK BOX
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

        // TITLE
        TextView title = new TextView(this); title.setText("🎯 HOST SALARY POLICY");
        title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(20); title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        title.setVisibility(isHost ? View.VISIBLE : View.GONE); title.setTag("policy_title"); root.addView(title);
        TextView sub = new TextView(this); sub.setText("Official Policy 1-22 • Monthly Settlement\nCompany Database থেকে হিসাব হবে • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#B0BEC5")); sub.setTextSize(12); sub.setGravity(Gravity.CENTER); sub.setPadding(0, 8, 0, 18);
        sub.setVisibility(isHost ? View.VISIBLE : View.GONE); sub.setTag("policy_sub"); root.addView(sub);

        // MY PROGRESS - PREMIUM CARD
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

        // LIST - PREMIUM BANNER CARDS
        listBox = new LinearLayout(this); listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setVisibility(isHost ? View.VISIBLE : View.GONE); listBox.setTag("policy_list"); root.addView(listBox);
        if (isHost) buildCards(0);

        TextView note = new TextView(this);
        note.setText("• Target পুরা Fill হইলেই Salary Counting হবে - 1 Coin কম থাকলেও 0$\n• আপনি শুধু নিজের Coin + নিজের Dollar দেখছেন\n• এই পলিসি Company থেকে আসে, Host বদলাতে পারবে না");
        note.setTextColor(Color.parseColor("#B0BEC5")); note.setTextSize(12); note.setPadding(0, 18, 0, 0);
        note.setVisibility(isHost ? View.VISIBLE : View.GONE); note.setTag("policy_note"); root.addView(note);
        setContentView(scroll);
        checkHostFromFirebase();
        if (isHost) { updateCached(); loadMyProgress(); }
    }

    private void buildCards(int myLevel) {
        listBox.removeAllViews();
        for (int i = 0; i < SalaryPolicy.LEVELS.length; i++) {
            boolean done = myLevel > 0 && SalaryPolicy.LEVELS[i] <= myLevel;
            boolean current = myLevel + 1 == SalaryPolicy.LEVELS[i];
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(22, 18, 22, 18);
            GradientDrawable bg = new GradientDrawable();
            if (done) bg.setColors(new int[]{Color.parseColor("#00C853"), Color.parseColor("#0F5132")});
            else if (current) bg.setColors(new int[]{Color.parseColor("#FF6A00"), Color.parseColor("#7B2FF7")});
            else bg.setColors(new int[]{Color.parseColor("#2A1B4A"), Color.parseColor("#140B26")});
            bg.setOrientation(GradientDrawable.Orientation.TL_BR); bg.setCornerRadius(20); bg.setStroke(2, done ? Color.parseColor("#00E676") : current ? Color.parseColor("#FFD700") : Color.parseColor("#3A2A5A"));
            card.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 0, 12); card.setLayoutParams(lp);
            TextView t1 = new TextView(this); t1.setText((done ? "✅ " : current ? "🎯 " : "🔒 ") + "LEVEL " + SalaryPolicy.LEVELS[i]); t1.setTextColor(Color.WHITE); t1.setTypeface(null, Typeface.BOLD); t1.setTextSize(16); card.addView(t1);
            TextView t2 = new TextView(this); t2.setText("Target: " + String.format("%,d", SalaryPolicy.TARGET_COINS[i]) + " Coins"); t2.setTextColor(Color.parseColor("#FFD700")); t2.setTypeface(null, Typeface.BOLD); t2.setTextSize(14); card.addView(t2);
            TextView t3 = new TextView(this); t3.setText("My Salary: " + SalaryPolicy.money(SalaryPolicy.ANCHOR_SALARY[i])); t3.setTextColor(Color.parseColor("#00E676")); t3.setTypeface(null, Typeface.BOLD); t3.setTextSize(16); card.addView(t3);
            // Agency Commission ekhane kokhono dekhano hobe na
            listBox.addView(card);
        }
    }

    private void updateCached() {
        long c = prefs.getLong("CACHED_COINS_" + myId, 0);
        updateUI(c);
    }

    private void updateUI(long received) {
        int lvl = SalaryPolicy.getAchievedLevel(received);
        double sal = SalaryPolicy.getHostSalary(received);
        if (tvMyProgress != null) tvMyProgress.setText("My Received: " + String.format("%,d", received) + " Coins");
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
        prefs.edit().putBoolean("IS_HOST_" + myId, true).putBoolean("HOST_ACCEPTED_" + myId, true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        for (int i = 0; i < root.getChildCount(); i++) { View v = root.getChildAt(i); if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("policy_")) v.setVisibility(View.VISIBLE); }
        loadMyProgress();
    }

    private void checkHostFromFirebase() {
        if (myId.isEmpty()) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        if (snap.child("agencyId").getValue() != null && !String.valueOf(snap.child("agencyId").getValue()).isEmpty()) return true;
        return false;
    }
    private boolean isTrue(DataSnapshot snap, String key) { if (!snap.hasChild(key)) return false; Object v = snap.child(key).getValue(); return v != null && (v.toString().equalsIgnoreCase("true") || v.toString().equals("1")); }
    private void checkInAgencyHosts() {
        FirebaseDatabase.getInstance().getReference("agencies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) { for (DataSnapshot ag : snap.getChildren()) if (ag.child("hosts").hasChild(myId)) { showPolicyUnlocked(); return; } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void loadMyProgress() {
        if (myId.isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("users").child(myId).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) { FirebaseDatabase.getInstance().getReference("users").orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() { @Override public void onDataChange(@NonNull DataSnapshot s2) { for (DataSnapshot ds : s2.getChildren()) { applySnapshot(ds); return; } } @Override public void onCancelled(@NonNull DatabaseError e) {} }); return; }
                applySnapshot(snap);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void applySnapshot(DataSnapshot snap) {
        long received = 0; String[] keys = {"totalReceivedCoins", "receivedCoins", "totalCoins", "coins"};
        for (String k : keys) if (snap.hasChild(k) && snap.child(k).getValue() != null) { try { received = Long.parseLong(String.valueOf(snap.child(k).getValue())); break; } catch (Exception ignored) {} }
        prefs.edit().putLong("CACHED_COINS_" + myId, received).apply();
        updateUI(received);
    }
}
