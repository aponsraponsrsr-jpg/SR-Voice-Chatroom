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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.util.Locale;

public class HostWorkDataActivity extends AppCompatActivity {
    private TextView tvReceived, tvLevel, tvSalary, tvNext;
    private LinearLayout root, lockBox;
    private String myId = "";
    private SharedPreferences prefs;
    private boolean isHost = false;

    private final long[] TARGETS = {288000L,588000L,1190000L,1780000L,3280000L,5780000L,7880000L,9980000L,13850000L,18850000L,26500000L,34500000L,43500000L,56500000L,76500000L,88500000L,115500000L,155000000L,210000000L,288000000L,350000000L,460000000L};
    private final String[] SALARY = {"$1","$2","$4.5","$7","$14","$24","$32","$40","$56","$76","$104","$132","$168","$220","$292","$348","$440","$584","$752","$1024","$1240","$1600"};

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
        if (myId == null || myId.isEmpty()) {
            myId = getSharedPreferences("SR_PREFS", MODE_PRIVATE).getString("MY_ID", "");
        }
        if (myId == null) myId = "";

        // GATE 1 - Local Cache: Instant, Loading nai
        isHost = prefs.getBoolean("IS_HOST_" + myId, false);
        if (!isHost) isHost = prefs.getBoolean("HOST_ACCEPTED_" + myId, false);
        if (!isHost) isHost = prefs.getBoolean("IS_HOST", false) && prefs.getString("MY_9_DIGIT_ID", "").equals(myId);
        if (getIntent().getBooleanExtra("FROM_HOST_ACCEPT", false)) isHost = true;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        scroll.addView(root);

        TextView btnBack = new TextView(this);
        btnBack.setText("← Back to Host Center");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(14);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(10, 10, 10, 10);
        btnBack.setOnClickListener(v -> finish());
        root.addView(btnBack);

        // LOCK BOX - Hosting nai hole eitai dekhabe, Work Data kokhono dekhbe na
        lockBox = new LinearLayout(this);
        lockBox.setOrientation(LinearLayout.VERTICAL);
        lockBox.setGravity(Gravity.CENTER);
        lockBox.setPadding(30, 60, 30, 60);
        android.graphics.drawable.GradientDrawable lockBg = new android.graphics.drawable.GradientDrawable();
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
        lockTitle.setText("Work Data Locked");
        lockTitle.setTextColor(Color.WHITE);
        lockTitle.setTextSize(20);
        lockTitle.setTypeface(null, Typeface.BOLD);
        lockTitle.setGravity(Gravity.CENTER);
        lockTitle.setPadding(0, 16, 0, 8);
        lockBox.addView(lockTitle);
        TextView lockMsg = new TextView(this);
        lockMsg.setText("Hosting নিলে Work Data দেখতে পাবেন\n\nAgency থেকে Host Invite Accept করলে\nএই Work Data Automatic খুলে যাবে।");
        lockMsg.setTextColor(Color.parseColor("#B0BEC5"));
        lockMsg.setTextSize(14);
        lockMsg.setGravity(Gravity.CENTER);
        lockBox.addView(lockMsg);

        TextView title = new TextView(this);
        title.setText("📊 Work Data");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 10);
        title.setVisibility(isHost ? View.VISIBLE : View.GONE);
        title.setTag("work_title");
        root.addView(title);

        TextView tvId = new TextView(this);
        tvId.setText("My ID: " + myId + "  (Only Yours)");
        tvId.setTextColor(Color.parseColor("#FFD700"));
        tvId.setTextSize(13);
        tvId.setTypeface(null, Typeface.BOLD);
        tvId.setGravity(Gravity.CENTER);
        tvId.setPadding(0, 0, 0, 20);
        tvId.setVisibility(isHost ? View.VISIBLE : View.GONE);
        tvId.setTag("work_id");
        root.addView(tvId);

        // FIXED - Loading uthay dilam. Cache theke sathe sathe dekhabe.
        long cachedCoins = prefs.getLong("CACHED_COINS_" + myId, 0);
        int cLevel = 0; String cSalary = "$0";
        for (int i = 0; i < TARGETS.length; i++) {
            if (cachedCoins >= TARGETS[i]) { cLevel = i + 1; cSalary = SALARY[i]; }
        }
        String cFmt = NumberFormat.getNumberInstance(Locale.US).format(cachedCoins);

        tvReceived = makeBig(root, "My Received: " + cFmt + " Coins", isHost);
        tvLevel = makeBig(root, "My Level: " + (cLevel == 0 ? "No Level Yet" : "Level " + cLevel), isHost);
        tvSalary = makeBig(root, "My Salary: " + cSalary + "  (Only Yours)", isHost);
        String nextTxt;
        if (cLevel < 22) {
            long nextTarget = TARGETS[cLevel];
            long left = nextTarget - cachedCoins;
            if (left < 0) left = 0;
            nextTxt = "Next Target: Level " + (cLevel + 1) + " - " + NumberFormat.getNumberInstance(Locale.US).format(nextTarget) + " Coins\nBaki: " + NumberFormat.getNumberInstance(Locale.US).format(left) + " Coins";
        } else nextTxt = "Next Target: Max Level Done! 🎉";
        tvNext = makeBig(root, nextTxt, isHost);

        TextView note = new TextView(this);
        note.setText("\n• Ekhane sudhu tomar nijer Target dekhba\n• Agency Commission ekhane dekhba na\n• Onno Host er kichu ekhane dekhba na");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        note.setVisibility(isHost ? View.VISIBLE : View.GONE);
        note.setTag("work_note");
        root.addView(note);

        setContentView(scroll);
        // GATE 2 - Firebase theke asol Host check
        checkHostFromFirebase();
        if (isHost) loadData();
    }

    private void showUnlocked() {
        if (isHost) return;
        isHost = true;
        prefs.edit().putBoolean("IS_HOST_" + myId, true).putBoolean("HOST_ACCEPTED_" + myId, true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        for (int i = 0; i < root.getChildCount(); i++) {
            View v = root.getChildAt(i);
            if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("work_")) v.setVisibility(View.VISIBLE);
        }
        if (tvReceived != null) tvReceived.setVisibility(View.VISIBLE);
        if (tvLevel != null) tvLevel.setVisibility(View.VISIBLE);
        if (tvSalary != null) tvSalary.setVisibility(View.VISIBLE);
        if (tvNext != null) tvNext.setVisibility(View.VISIBLE);
        loadData();
    }

    private void checkHostFromFirebase() {
        if (myId.isEmpty()) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (isHostFromSnapshot(snap)) { showUnlocked(); return; }
                usersRef.orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        for (DataSnapshot ds : s2.getChildren()) {
                            if (isHostFromSnapshot(ds)) { showUnlocked(); return; }
                        }
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
                    if (ag.child("hosts").hasChild(myId)) { showUnlocked(); return; }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private TextView makeBig(LinearLayout root, String t, boolean visible) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextColor(Color.parseColor("#FFD700"));
        tv.setTextSize(16);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(24, 24, 24, 24);
        tv.setBackgroundColor(Color.parseColor("#1B1035"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        tv.setLayoutParams(lp);
        tv.setVisibility(visible ? View.VISIBLE : View.GONE);
        root.addView(tv);
        return tv;
    }

    private void loadData() {
        if (myId.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(myId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    FirebaseDatabase.getInstance().getReference("users").orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot s2) {
                            for (DataSnapshot ds : s2.getChildren()) { updateUI(ds); return; }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                    return;
                }
                updateUI(snap);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void updateUI(DataSnapshot snap) {
        long received = 0;
        String[] coinKeys = {"totalReceivedCoins", "receivedCoins", "totalCoins", "coins"};
        for (String k : coinKeys) {
            if (snap.hasChild(k) && snap.child(k).getValue() != null) {
                try { received = Long.parseLong(String.valueOf(snap.child(k).getValue())); break; } catch (Exception ignored) {}
            }
        }
        int myLevel = 0; String mySalary = "$0";
        for (int i = 0; i < TARGETS.length; i++) {
            if (received >= TARGETS[i]) { myLevel = i + 1; mySalary = SALARY[i]; }
        }
        // Cache - porer bar Loading chara sathe sathe dekhabe
        prefs.edit().putLong("CACHED_COINS_" + myId, received).apply();
        String fmt = NumberFormat.getNumberInstance(Locale.US).format(received);
        if (tvReceived != null) tvReceived.setText("My Received: " + fmt + " Coins");
        if (tvLevel != null) tvLevel.setText("My Level: " + (myLevel == 0 ? "No Level Yet" : "Level " + myLevel));
        if (tvSalary != null) tvSalary.setText("My Salary: " + mySalary + "  (Only Yours)");
        if (tvNext != null) {
            if (myLevel < 22) {
                long nextTarget = TARGETS[myLevel];
                long left = nextTarget - received;
                tvNext.setText("Next Target: Level " + (myLevel + 1) + " - " + NumberFormat.getNumberInstance(Locale.US).format(nextTarget) + " Coins\nBaki: " + NumberFormat.getNumberInstance(Locale.US).format(left) + " Coins");
            } else {
                tvNext.setText("Next Target: Max Level Done! 🎉");
            }
        }
    }
}
