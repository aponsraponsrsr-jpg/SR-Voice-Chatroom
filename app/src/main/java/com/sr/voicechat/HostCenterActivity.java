package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HostCenterActivity extends AppCompatActivity {
    private String myId = "";
    private String name = "User";
    private String agency = "SR Agency";
    private SharedPreferences prefs;
    private boolean isHost = false;
    private LinearLayout policyRow, workRow;
    private TextView tvPolicySub, tvWorkSub, tvBal, tvLockMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        name = prefs.getString("USER_NAME", "User");
        myId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myId == null || myId.isEmpty()) myId = prefs.getString("MY_ID", "");
        if (myId == null || myId.isEmpty()) myId = prefs.getString("USER_ID", "");
        if (myId == null || myId.isEmpty()) {
            myId = getSharedPreferences("SR_PREFS", MODE_PRIVATE).getString("MY_ID", "");
        }
        if (myId == null) myId = "";
        if (name == null || name.isEmpty()) name = prefs.getString("MY_NAME", "User");
        if (name == null || name.isEmpty()) name = "User";
        agency = prefs.getString("AGENCY_NAME", "SR Agency");
        if (agency == null || agency.isEmpty()) agency = "SR Agency";

        String intentId = getIntent().getStringExtra("MY_ID");
        if (intentId != null && !intentId.isEmpty()) myId = intentId;

        // GATE - Local Cache: Ei ID Host kina? Instant check, Loading nai
        isHost = prefs.getBoolean("IS_HOST_" + myId, false);
        if (!isHost) isHost = prefs.getBoolean("HOST_ACCEPTED_" + myId, false);
        if (!isHost) isHost = prefs.getBoolean("IS_HOST", false) && prefs.getString("MY_9_DIGIT_ID", "").equals(myId);
        if (getIntent().getBooleanExtra("FROM_HOST_ACCEPT", false)) isHost = true;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Host Center");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(30, 28, 30, 28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1B1035"));
        bg.setCornerRadius(24);
        top.setBackground(bg);
        root.addView(top);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(18);
        tvName.setTypeface(null, Typeface.BOLD);
        top.addView(tvName);

        TextView tvId = new TextView(this);
        tvId.setText("ID: " + myId + "  |  Agency: " + (isHost ? agency : "No Agency Yet"));
        tvId.setTextColor(Color.parseColor("#FFD700"));
        tvId.setTextSize(13);
        top.addView(tvId);

        tvBal = new TextView(this);
        // Hosting nai hole Dollar dekhabe na - sudhu Lock msg
        if (isHost) {
            long cachedCoins = prefs.getLong("CACHED_COINS_" + myId, 0);
            tvBal.setText("My Coins: " + String.format("%,d", cachedCoins) + "  -  Work Data te dekhte parba");
        } else {
            tvBal.setText("🔒 Hosting নিলে এখানে তোমার Coin + Dollar দেখতে পাবা");
        }
        tvBal.setTextColor(Color.parseColor("#B0BEC5"));
        tvBal.setTextSize(12);
        tvBal.setPadding(0, 12, 0, 0);
        top.addView(tvBal);

        // LOCK MSG - Hosting nai hole eitai dekhabe
        tvLockMsg = new TextView(this);
        tvLockMsg.setText(isHost ? "" : "\n🔒 Host Policy Locked\nHosting নিলে Policy Automatic খুলে যাবে।\nAgency থেকে Invite Accept করো।");
        tvLockMsg.setTextColor(Color.parseColor("#FFD700"));
        tvLockMsg.setTextSize(13);
        tvLockMsg.setTypeface(null, Typeface.BOLD);
        tvLockMsg.setGravity(Gravity.CENTER);
        root.addView(tvLockMsg);

        // Option 1 - Host Policy - LOCKED if not Host
        policyRow = makeRow("📜  Host Policy", isHost ? "Official Policy 1-22 - Target + Salary" : "🔒 Locked - Hosting নিলে খুলবে", v -> {
            if (!isHost) {
                Toast.makeText(this, "🔒 Hosting নিলে Policy দেখতে পাবেন", Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(this, HostPolicyActivity.class);
            i.putExtra("MY_ID", myId);
            i.putExtra("HOST_ID", myId);
            i.putExtra("USER_ID", myId);
            i.putExtra("FROM_HOST_ACCEPT", true);
            startActivity(i);
        });
        // Lock hole Row ta halka fade kore dao - bujhbe Lock
        policyRow.setAlpha(isHost ? 1.0f : 0.55f);
        root.addView(policyRow);

        // Option 2 - Work Data - LOCKED if not Host
        workRow = makeRow("📊  Work Data", isHost ? "Koto Target hoiche, Koto Dollar hoiche - ekhane dekhba" : "🔒 Locked - Hosting নিলে খুলবে", v -> {
            if (!isHost) {
                Toast.makeText(this, "🔒 Hosting নিলে Work Data দেখতে পাবেন", Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(this, HostWorkDataActivity.class);
            i.putExtra("MY_ID", myId);
            i.putExtra("HOST_ID", myId);
            i.putExtra("USER_ID", myId);
            startActivity(i);
        });
        workRow.setAlpha(isHost ? 1.0f : 0.55f);
        root.addView(workRow);

        TextView note = new TextView(this);
        note.setText("\nHost Center e sudhu ei 2 ta option thakbe.");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        root.addView(note);

        setContentView(scroll);
        // GATE 2 - Firebase theke asol Host check - Accept hole auto unlock
        checkHostFromFirebase();
    }

    private void checkHostFromFirebase() {
        if (myId.isEmpty()) return;
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (isHostFromSnapshot(snap)) { unlockHostCenter(); return; }
                usersRef.orderByChild("my9DigitId").equalTo(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        for (DataSnapshot ds : s2.getChildren()) {
                            if (isHostFromSnapshot(ds)) { unlockHostCenter(); return; }
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
                    if (ag.child("hosts").hasChild(myId)) { unlockHostCenter(); return; }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void unlockHostCenter() {
        if (isHost) return;
        isHost = true;
        prefs.edit().putBoolean("IS_HOST_" + myId, true).putBoolean("HOST_ACCEPTED_" + myId, true).apply();
        if (tvLockMsg != null) tvLockMsg.setText("");
        if (tvBal != null) {
            long cachedCoins = prefs.getLong("CACHED_COINS_" + myId, 0);
            tvBal.setText("My Coins: " + String.format("%,d", cachedCoins) + "  -  Work Data te dekhte parba");
        }
        if (policyRow != null) {
            policyRow.setAlpha(1.0f);
            // Row er sub text update - makeRow er 2nd child
            if (policyRow.getChildCount() > 1 && policyRow.getChildAt(1) instanceof TextView) {
                ((TextView) policyRow.getChildAt(1)).setText("Official Policy 1-22 - Target + Salary");
            }
        }
        if (workRow != null) {
            workRow.setAlpha(1.0f);
            if (workRow.getChildCount() > 1 && workRow.getChildAt(1) instanceof TextView) {
                ((TextView) workRow.getChildAt(1)).setText("Koto Target hoiche, Koto Dollar hoiche - ekhane dekhba");
            }
        }
        Toast.makeText(this, "🎉 Hosting Accepted! Policy Unlock হয়ে গেছে ✅", Toast.LENGTH_LONG).show();
    }

    private LinearLayout makeRow(String t1, String t2, android.view.View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(30, 26, 30, 26);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1B1035"));
        bg.setCornerRadius(20);
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        row.setLayoutParams(lp);
        row.setOnClickListener(click);
        TextView a = new TextView(this);
        a.setText(t1);
        a.setTextColor(Color.WHITE);
        a.setTextSize(16);
        a.setTypeface(null, Typeface.BOLD);
        row.addView(a);
        TextView b = new TextView(this);
        b.setText(t2);
        b.setTextColor(Color.parseColor("#B0BEC5"));
        b.setTextSize(12);
        row.addView(b);
        return row;
    }
}
