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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// HOST POLICY - GATED - READ ONLY
// RULE: Hosting nai = Policy kokhono dekhbe na. Hosting nile auto Policy khulbe.
// Host sees ONLY: Own Target Coins + Own Host Salary (80%)
// Host NEVER sees: Agency Commission, Agency Benefit, Onno Host er kichu
public class HostPolicyActivity extends AppCompatActivity {
    private LinearLayout listBox, root, lockBox;
    private TextView tvMyProgress, tvMyLevel, tvMySalary;
    private String myId = "";
    private SharedPreferences prefs;
    private boolean isHost = false;

    private final int[][] POLICY = {
        {1, 288000, 1},
        {2, 588000, 2},
        {3, 1190000, 4},
        {4, 1780000, 7},
        {5, 3280000, 14},
        {6, 5780000, 24},
        {7, 7880000, 32},
        {8, 9980000, 40},
        {9, 13850000, 56},
        {10, 18850000, 76},
        {11, 26500000, 104},
        {12, 34500000, 132},
        {13, 43500000, 168},
        {14, 56500000, 220},
        {15, 76500000, 292},
        {16, 88500000, 348},
        {17, 115500000, 440},
        {18, 155000000, 584},
        {19, 210000000, 752},
        {20, 288000000, 1024},
        {21, 350000000, 1240},
        {22, 460000000, 1600}
    };
    private final String[] HOST_SALARY_TEXT = {
        "$1", "$2", "$4.5", "$7", "$14", "$24", "$32", "$40", "$56", "$76",
        "$104", "$132", "$168", "$220", "$292", "$348", "$440", "$584", "$752", "$1024", "$1240", "$1600"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        myId = getIntent().getStringExtra("MY_ID");
        if (myId == null || myId.isEmpty()) myId = getIntent().getStringExtra("HOST_ID");
        if (myId == null || myId.isEmpty()) myId = getIntent().getStringExtra("USER_ID");
        if (myId == null || myId.isEmpty()) {
            myId = prefs.getString("MY_9_DIGIT_ID", "");
            if (myId == null || myId.isEmpty()) myId = prefs.getString("MY_ID", "");
            if (myId == null || myId.isEmpty()) myId = prefs.getString("USER_ID", "");
        }
        if (myId == null || myId.isEmpty()) myId = getSharedPreferences("SR_PREFS", MODE_PRIVATE).getString("MY_ID", "");
        if (myId == null) myId = "";

        // GATE 1 - Local Cache: Age dekho ei ID Host kina - Instant, Loading nai
        isHost = prefs.getBoolean("IS_HOST_" + myId, false);
        if (!isHost) isHost = prefs.getBoolean("IS_HOST", false) && prefs.getString("MY_9_DIGIT_ID", "").equals(myId);
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
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(14);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(10, 10, 10, 10);
        btnBack.setOnClickListener(v -> finish());
        root.addView(btnBack);

        // LOCK BOX - Hosting nai hole eitai dekhabe, Policy kokhono dekhbe na
        lockBox = new LinearLayout(this);
        lockBox.setOrientation(LinearLayout.VERTICAL);
        lockBox.setGravity(Gravity.CENTER);
        lockBox.setPadding(30, 60, 30, 60);
        GradientDrawable lockBg = new GradientDrawable();
        lockBg.setColor(Color.parseColor("#1B1035"));
        lockBg.setCornerRadius(22);
        lockBg.setStroke(1, Color.parseColor("#FFD700"));
        lockBox.setBackground(lockBg);
        lockBox.setVisibility(isHost ? View.GONE : View.VISIBLE);
        root.addView(lockBox);

        TextView lockIcon = new TextView(this);
        lockIcon.setText("🔒");
        lockIcon.setTextSize(48);
        lockIcon.setGravity(Gravity.CENTER);
        lockBox.addView(lockIcon);

        TextView lockTitle = new TextView(this);
        lockTitle.setText("Host Policy Locked");
        lockTitle.setTextColor(Color.WHITE);
        lockTitle.setTextSize(20);
        lockTitle.setTypeface(null, Typeface.BOLD);
        lockTitle.setGravity(Gravity.CENTER);
        lockTitle.setPadding(0, 16, 0, 8);
        lockBox.addView(lockTitle);

        TextView lockMsg = new TextView(this);
        lockMsg.setText("Hosting নিলে Policy দেখতে পাবেন\n\nAgency থেকে Host Invite Accept করলে\nএই Policy Automatic খুলে যাবে।");
        lockMsg.setTextColor(Color.parseColor("#B0BEC5"));
        lockMsg.setTextSize(14);
        lockMsg.setGravity(Gravity.CENTER);
        lockBox.addView(lockMsg);

        TextView lockBtn = new TextView(this);
        lockBtn.setText("📨 Hosting নিতে চাই - Agency খুঁজুন");
        lockBtn.setTextColor(Color.parseColor("#FFD700"));
        lockBtn.setTypeface(null, Typeface.BOLD);
        lockBtn.setTextSize(14);
        lockBtn.setGravity(Gravity.CENTER);
        lockBtn.setPadding(0, 24, 0, 0);
        lockBtn.setOnClickListener(v -> finish());
        lockBox.addView(lockBtn);

        // POLICY UI - Sudhu Host dekhbe
        TextView title = new TextView(this);
        title.setText("🎯 HOST SALARY POLICY");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setVisibility(isHost ? View.VISIBLE : View.GONE);
        root.addView(title);
        title.setTag("policy_title");

        TextView sub = new TextView(this);
        sub.setText("Official Policy 1-22 • Monthly Settlement\nCompany Database থেকে হিসাব হবে • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 18);
        sub.setVisibility(isHost ? View.VISIBLE : View.GONE);
        sub.setTag("policy_sub");
        root.addView(sub);

        LinearLayout myBox = new LinearLayout(this);
        myBox.setOrientation(LinearLayout.VERTICAL);
        myBox.setPadding(26, 22, 26, 22);
        GradientDrawable myBg = new GradientDrawable();
        myBg.setColor(Color.parseColor("#1B1035"));
        myBg.setCornerRadius(22);
        myBg.setStroke(1, Color.parseColor("#FFD700"));
        myBox.setBackground(myBg);
        LinearLayout.LayoutParams myLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myLp.setMargins(0, 0, 0, 18);
        myBox.setLayoutParams(myLp);
        myBox.setVisibility(isHost ? View.VISIBLE : View.GONE);
        myBox.setTag("policy_mybox");
        root.addView(myBox);

        // FIXED - Loading uthay dilam. Cached instantly.
        long cachedCoins = prefs.getLong("CACHED_COINS_" + myId, 0);
        int cachedLevel = 0; String cachedSalary = "$0";
        for (int i = 0; i < POLICY.length; i++) {
            if (cachedCoins >= POLICY[i][1]) { cachedLevel = POLICY[i][0]; cachedSalary = HOST_SALARY_TEXT[i]; }
        }

        tvMyProgress = new TextView(this);
        tvMyProgress.setText("My Received: " + formatCoins((int) cachedCoins) + " Coins");
        tvMyProgress.setTextColor(Color.WHITE);
        tvMyProgress.setTypeface(null, Typeface.BOLD);
        tvMyProgress.setTextSize(15);
        myBox.addView(tvMyProgress);

        tvMyLevel = new TextView(this);
        tvMyLevel.setText("My Level: " + (cachedLevel == 0 ? "No Level Yet" : "Level " + cachedLevel));
        tvMyLevel.setTextColor(Color.parseColor("#FFD700"));
        tvMyLevel.setTypeface(null, Typeface.BOLD);
        tvMyLevel.setTextSize(15);
        myBox.addView(tvMyLevel);

        tvMySalary = new TextView(this);
        tvMySalary.setText("My Host Salary: " + cachedSalary + "  (Only Yours)");
        tvMySalary.setTextColor(Color.parseColor("#00E676"));
        tvMySalary.setTypeface(null, Typeface.BOLD);
        tvMySalary.setTextSize(16);
        myBox.addView(tvMySalary);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(18, 14, 18, 14);
        GradientDrawable hBg = new GradientDrawable();
        hBg.setColor(Color.parseColor("#0F5132"));
        hBg.setCornerRadius(14);
        header.setBackground(hBg);
        header.setVisibility(isHost ? View.VISIBLE : View.GONE);
        header.setTag("policy_header");
        root.addView(header);
        addHeaderCell(header, "Level", 1);
        addHeaderCell(header, "Target Coins", 2);
        addHeaderCell(header, "Host Salary", 2);

        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setVisibility(isHost ? View.VISIBLE : View.GONE);
        listBox.setTag("policy_list");
        root.addView(listBox);

        if (isHost) {
            for (int i = 0; i < POLICY.length; i++) addRow(i, POLICY[i][0], POLICY[i][1], HOST_SALARY_TEXT[i]);
        }

        TextView note = new TextView(this);
        note.setText("• Target Complete হলেই Salary Counting হবে\n• 30% Exchange Coin আলাদা ভাবে Wallet এ যাবে\n• এই পলিসি Company থেকে আসে, Host বদলাতে পারবে না\n• আপনি শুধু নিজের Coin + নিজের Dollar দেখছেন");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setPadding(0, 18, 0, 0);
        note.setVisibility(isHost ? View.VISIBLE : View.GONE);
        note.setTag("policy_note");
        root.addView(note);

        setContentView(scroll);
        // GATE 2 - Firebase theke asol Host check - Hosting peye gele auto unlock hobe
        checkHostFromFirebase();
        if (isHost) loadMyProgress();
    }

    private void showPolicyUnlocked() {
        if (isHost) return;
        isHost = true;
        prefs.edit().putBoolean("IS_HOST_" + myId, true).putBoolean("HOST_ACCEPTED_" + myId, true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        // Sob policy view show koro
        for (int i = 0; i < root.getChildCount(); i++) {
            View v = root.getChildAt(i);
            if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("policy_")) v.setVisibility(View.VISIBLE);
        }
        if (listBox != null && listBox.getChildCount() == 0) {
            for (int i = 0; i < POLICY.length; i++) addRow(i, POLICY[i][0], POLICY[i][1], HOST_SALARY_TEXT[i]);
        }
        loadMyProgress();
    }

    private void checkHostFromFirebase() {
        if (myId.isEmpty()) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        // 1. Direct key diye check
        usersRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (isHostFromSnapshot(snap)) { showPolicyUnlocked(); return; }
                // 2. my9DigitId diye check - Global e 2 vabei kaj korbe
                usersRef.orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        for (DataSnapshot ds : s2.getChildren()) {
                            if (isHostFromSnapshot(ds)) { showPolicyUnlocked(); return; }
                        }
                        // 3. Agency hosts e ache kina check
                        checkInAgencyHosts();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { checkInAgencyHosts(); }
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private boolean isHostFromSnapshot(DataSnapshot snap) {
        if (snap == null || !snap.exists()) return false;
        if (isTrue(snap, "isHost")) return true;
        if (isTrue(snap, "isHostAccepted")) return true;
        if (isTrue(snap, "hostAccepted")) return true;
        if (isTrue(snap, "isAnchor")) return true;
        if (snap.child("hostStatus").getValue() != null && String.valueOf(snap.child("hostStatus").getValue()).equalsIgnoreCase("accepted")) return true;
        if (snap.child("agencyId").getValue() != null && !String.valueOf(snap.child("agencyId").getValue()).isEmpty()) return true;
        if (snap.child("agency").getValue() != null && !String.valueOf(snap.child("agency").getValue()).isEmpty()) return true;
        return false;
    }

    private boolean isTrue(DataSnapshot snap, String key) {
        if (!snap.hasChild(key)) return false;
        Object v = snap.child(key).getValue();
        if (v == null) return false;
        return v.toString().equalsIgnoreCase("true") || v.toString().equals("1");
    }

    private void checkInAgencyHosts() {
        FirebaseDatabase.getInstance().getReference("agencies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                for (DataSnapshot ag : snap.getChildren()) {
                    if (ag.child("hosts").hasChild(myId)) { showPolicyUnlocked(); return; }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addHeaderCell(LinearLayout row, String txt, float weight) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setTextColor(Color.WHITE);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        row.addView(t);
    }

    private void addRow(int index, int level, int target, String salary) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(18, 16, 18, 16);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(index % 2 == 0 ? Color.parseColor("#1B1035") : Color.parseColor("#140B26"));
        bg.setCornerRadius(12);
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 4);
        row.setLayoutParams(lp);
        listBox.addView(row);
        TextView t1 = new TextView(this);
        t1.setText("Level " + level);
        t1.setTextColor(Color.WHITE);
        t1.setTypeface(null, Typeface.BOLD);
        t1.setTextSize(13);
        t1.setGravity(Gravity.CENTER);
        t1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(t1);
        TextView t2 = new TextView(this);
        t2.setText(formatCoins(target));
        t2.setTextColor(Color.parseColor("#FFD700"));
        t2.setTypeface(null, Typeface.BOLD);
        t2.setTextSize(13);
        t2.setGravity(Gravity.CENTER);
        t2.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        row.addView(t2);
        TextView t3 = new TextView(this);
        t3.setText(salary);
        t3.setTextColor(Color.parseColor("#00E676"));
        t3.setTypeface(null, Typeface.BOLD);
        t3.setTextSize(14);
        t3.setGravity(Gravity.CENTER);
        t3.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        row.addView(t3);
    }

    private void loadMyProgress() {
        if (myId.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(myId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    FirebaseDatabase.getInstance().getReference("users").orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot s2) {
                            for (DataSnapshot ds : s2.getChildren()) { updateProgress(ds); return; }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                    return;
                }
                updateProgress(snap);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void updateProgress(DataSnapshot snap) {
        long received = 0;
        String[] coinKeys = {"totalReceivedCoins", "receivedCoins", "totalCoins", "coins"};
        for (String k : coinKeys) {
            if (snap.hasChild(k) && snap.child(k).getValue() != null) {
                try { received = Long.parseLong(String.valueOf(snap.child(k).getValue())); break; } catch (Exception ignored) {}
            }
        }
        int myLevel = 0; String mySalary = "$0";
        for (int i = 0; i < POLICY.length; i++) {
            if (received >= POLICY[i][1]) { myLevel = POLICY[i][0]; mySalary = HOST_SALARY_TEXT[i]; }
        }
        // Cache - porer bar Loading chara sathe sathe dekhabe
        prefs.edit().putLong("CACHED_COINS_" + myId, received).apply();
        if (tvMyProgress != null) tvMyProgress.setText("My Received: " + formatCoins((int) received) + " Coins");
        if (tvMyLevel != null) tvMyLevel.setText("My Level: " + (myLevel == 0 ? "No Level Yet" : "Level " + myLevel));
        if (tvMySalary != null) tvMySalary.setText("My Host Salary: " + mySalary + "  (Only Yours)");
    }

    private String formatCoins(int n) {
        return String.format("%,d", n);
    }
}
