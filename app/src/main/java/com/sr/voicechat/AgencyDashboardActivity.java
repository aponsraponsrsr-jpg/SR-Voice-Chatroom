package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class AgencyDashboardActivity extends AppCompatActivity {

    private TextView txtAgencyTitle, txtTotalHosts, txtTotalEarnings, txtHostListHeader;
    private Button btnOpenInviteDialog, btnAgencyPolicy;
    private LinearLayout hostListContainer;
    private String agencyOwnerId = "";
    private String agencyName = "SR Agency";
    private DatabaseReference agencyRef, invitationRef, usersRef;
    private double totalCommission = 0;
    private int loadedHostCount = 0;
    private int expectedHostCount = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(android.graphics.Color.parseColor("#120A2A"));

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(40, 40, 40, 40);

        txtAgencyTitle = new TextView(this);
        // FIX PERMANENT - Cached naam instantly, khali dekhabe na
        agencyName = prefs.getString("CACHED_AGENCY_NAME", "SR Agency");
        txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard");
        txtAgencyTitle.setTextColor(android.graphics.Color.parseColor("#FFD700"));
        txtAgencyTitle.setTextSize(22);
        txtAgencyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        rootLayout.addView(txtAgencyTitle);

        txtTotalHosts = new TextView(this);
        txtTotalHosts.setText("👥 Active Hosts: 0");
        txtTotalHosts.setTextColor(android.graphics.Color.WHITE);
        txtTotalHosts.setTextSize(16);
        txtTotalHosts.setPadding(0, 20, 0, 5);
        rootLayout.addView(txtTotalHosts);

        txtTotalEarnings = new TextView(this);
        txtTotalEarnings.setText("💰 Total Agency Earnings: $0.00");
        txtTotalEarnings.setTextColor(android.graphics.Color.parseColor("#00E676"));
        txtTotalEarnings.setTextSize(16);
        txtTotalEarnings.setPadding(0, 0, 0, 30);
        rootLayout.addView(txtTotalEarnings);

        btnAgencyPolicy = new Button(this);
        btnAgencyPolicy.setText("📜 Agency Policy - Full Table");
        btnAgencyPolicy.setBackgroundColor(android.graphics.Color.parseColor("#FFD700"));
        btnAgencyPolicy.setTextColor(android.graphics.Color.BLACK);
        btnAgencyPolicy.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams policyLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        policyLp.setMargins(0, 0, 0, 20);
        btnAgencyPolicy.setLayoutParams(policyLp);
        rootLayout.addView(btnAgencyPolicy);

        btnOpenInviteDialog = new Button(this);
        btnOpenInviteDialog.setText("➕ Invite New Host (Any Digit ID)");
        btnOpenInviteDialog.setBackgroundColor(android.graphics.Color.parseColor("#FF4081"));
        btnOpenInviteDialog.setTextColor(android.graphics.Color.WHITE);
        btnOpenInviteDialog.setTypeface(null, android.graphics.Typeface.BOLD);
        rootLayout.addView(btnOpenInviteDialog);

        txtHostListHeader = new TextView(this);
        txtHostListHeader.setText("\n📋 Agency Host Performance & Targets:");
        txtHostListHeader.setTextColor(android.graphics.Color.parseColor("#00B0FF"));
        txtHostListHeader.setTextSize(18);
        txtHostListHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        rootLayout.addView(txtHostListHeader);

        hostListContainer = new LinearLayout(this);
        hostListContainer.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(hostListContainer);

        scrollView.addView(rootLayout);
        setContentView(scrollView);

        // ID - Intent + SR_USER + SR_PREFS - 3 jayga theke, kokhono khali na
        agencyOwnerId = getIntent().getStringExtra("AGENCY_OWNER_ID");
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) agencyOwnerId = getIntent().getStringExtra("MY_ID");
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) agencyOwnerId = getIntent().getStringExtra("AGENCY_ID");
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) agencyOwnerId = prefs.getString("MY_9_DIGIT_ID", "");
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) agencyOwnerId = prefs.getString("MY_ID", "");
        if (agencyOwnerId == null || agencyOwnerId.isEmpty()) agencyOwnerId = getSharedPreferences("SR_PREFS", MODE_PRIVATE).getString("MY_ID", "");
        if (agencyOwnerId == null) agencyOwnerId = "";

        // PERMANENT AGENCY - Ekbar Agency mane sobsomoy Agency. Ekhane lock kore dilam.
        if (!agencyOwnerId.isEmpty()) {
            prefs.edit().putString("AGENCY_OWNER_ID", agencyOwnerId)
                    .putString("MY_AGENCY_ID", agencyOwnerId)
                    .putBoolean("IS_AGENCY", true)
                    .putBoolean("IS_AGENCY_PERMANENT", true)
                    .putLong("AGENCY_GRANTED_AT", prefs.getLong("AGENCY_GRANTED_AT", System.currentTimeMillis()))
                    .apply();
        }

        agencyRef = FirebaseDatabase.getInstance().getReference("agencies").child(agencyOwnerId);
        try { agencyRef.keepSynced(true); } catch (Exception ignored) {}
        invitationRef = FirebaseDatabase.getInstance().getReference("agency_invitations");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Agency naam LIVE - kintu kokhono khali / false diye overwrite korbe na
        agencyRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    // Firebase e na thakleo cache theke Agency thakbe - harabe na
                    return;
                }
                // Agency exist kore - Permanent lock abar pakka koro
                prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("IS_AGENCY_PERMANENT", true).apply();
                String liveName = snap.child("agencyName").getValue(String.class);
                if (liveName == null) liveName = snap.child("name").getValue(String.class);
                if (liveName != null && !liveName.isEmpty()) {
                    agencyName = liveName;
                    prefs.edit().putString("CACHED_AGENCY_NAME", liveName).apply();
                    txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        loadAgencyDataAndHosts();

        // POLICY - Crash-proof. Activity khulle valo, na khulle eki screen e Table khulbe. Back / Crash korbe na.
        btnAgencyPolicy.setOnClickListener(v -> openAgencyPolicySafe());

        btnOpenInviteDialog.setOnClickListener(v -> showHostInviteInputDialog());
    }

    private void openAgencyPolicySafe() {
        try {
            Intent i = new Intent(AgencyDashboardActivity.this, AgencyPolicyActivity.class);
            i.putExtra("AGENCY_OWNER_ID", agencyOwnerId);
            i.putExtra("MY_ID", agencyOwnerId);
            i.putExtra("AGENCY_ID", agencyOwnerId);
            i.putExtra("FROM_AGENCY_DASHBOARD", true);
            startActivity(i);
        } catch (Exception e) {
            // AgencyPolicyActivity na thakle / crash korle - ekhanei Policy Table dekhabe
            showInlineAgencyPolicy();
        }
    }

    private void showInlineAgencyPolicy() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("📜 Agency Policy - SR Voice Chat");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);
        ScrollView sv = new ScrollView(this);
        TextView tv = new TextView(this);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(14);
        tv.setText(
            "🏛️ AGENCY POLICY\n\n" +
            "✅ Agency Permanent - Ekbar Agency mane sobsomoy Agency\n\n" +
            "👑 Agency Owner: 100% Own Earnings\n" +
            "👤 Host: 80% Earnings\n" +
            "🏛️ Agency Commission: 20% from Host\n\n" +
            "🎯 TARGET TABLE:\n" +
            "• 10,000 Coins = $1.00\n" +
            "• Host 100K Coins = $10 (Host $8 + Agency $2)\n" +
            "• Host 1M Coins = $100 (Host $80 + Agency $20)\n\n" +
            "📜 RULES:\n" +
            "1. Host ke respect korte hobe\n" +
            "2. Fake coins / fraud korle Agency cancel\n" +
            "3. Payment maser 1-7 tarikhe\n" +
            "4. Host chaile Agency change korte parbe\n\n" +
            "Agency ID: " + agencyOwnerId
        );
        sv.addView(tv);
        layout.addView(sv);
        b.setView(layout);
        b.setPositiveButton("OK ✅", (d, w) -> d.dismiss());
        try { b.show(); } catch (Exception ignored) {}
    }

    private void showHostInviteInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📨 Send Host Invitation");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);
        final EditText inputTargetId = new EditText(this);
        inputTargetId.setHint("Enter Host ID (Any Digit ID)...");
        inputTargetId.setTextColor(android.graphics.Color.WHITE);
        layout.addView(inputTargetId);
        builder.setView(layout);
        builder.setPositiveButton("Send Invite", (dialog, which) -> {
            String targetUserId = inputTargetId.getText().toString().trim();
            if (!targetUserId.isEmpty()) sendRealtimeHostInvitation(targetUserId);
            else Toast.makeText(this, "Please enter a valid User ID!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void sendRealtimeHostInvitation(String targetUserId) {
        if (targetUserId.equals(agencyOwnerId)) {
            Toast.makeText(this, "You cannot invite yourself!", Toast.LENGTH_SHORT).show();
            return;
        }
        String inviteKey = invitationRef.child(targetUserId).push().getKey();
        if (inviteKey == null) return;
        Map<String, Object> inviteMap = new HashMap<>();
        inviteMap.put("agencyId", agencyOwnerId);
        inviteMap.put("agencyName", agencyName);
        inviteMap.put("inviterId", agencyOwnerId);
        inviteMap.put("targetId", targetUserId);
        inviteMap.put("type", "AGENCY_INVITE");
        inviteMap.put("status", "Pending");
        inviteMap.put("timestamp", System.currentTimeMillis());
        invitationRef.child(targetUserId).child(inviteKey).setValue(inviteMap);
        usersRef.child(targetUserId).child("inbox").child(inviteKey).setValue(inviteMap)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) Toast.makeText(this, "Host invitation sent to ID: " + targetUserId, Toast.LENGTH_LONG).show();
                else Toast.makeText(this, "Failed to send invitation!", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadAgencyDataAndHosts() {
        agencyRef.child("hosts").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                long hostCount = snapshot.getChildrenCount();
                txtTotalHosts.setText("👥 Active Hosts: " + hostCount);
                hostListContainer.removeAllViews();
                totalCommission = 0;
                loadedHostCount = 0;
                expectedHostCount = (int) hostCount;
                if (hostCount == 0) {
                    fetchOwnTargetCard();
                    return;
                }
                for (DataSnapshot hostSnap : snapshot.getChildren()) {
                    String hostUserId = hostSnap.getKey();
                    if (hostUserId != null) fetchHostDetailsAndDisplay(hostUserId);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchOwnTargetCard() {
        // Age direct key diye, na paile my9DigitId diye khujbo - Global e 2 vabei kaj korbe
        usersRef.child(agencyOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && (snapshot.child("receivedCoins").exists() || snapshot.child("totalCoins").exists())) {
                    showOwnCard(snapshot);
                } else {
                    usersRef.orderByChild("my9DigitId").equalTo(agencyOwnerId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot s2) {
                            for (DataSnapshot ds : s2.getChildren()) { showOwnCard(ds); return; }
                            showOwnCard(snapshot);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) { showOwnCard(snapshot); }
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showOwnCard(DataSnapshot snapshot) {
        long myCoins = getCoins(snapshot);
        double myEarnings = myCoins / 10000.0;
        double total = myEarnings + totalCommission;
        txtTotalEarnings.setText("💰 Total Agency Earnings: $" + String.format("%.2f", total) + " (Own $" + String.format("%.2f", myEarnings) + " + Commission $" + String.format("%.2f", totalCommission) + ")");
        addHostCard("👑 " + agencyName + " (You - Agency Owner)", agencyOwnerId, myCoins, myEarnings, 100, true);
    }

    private long getCoins(DataSnapshot snapshot) {
        if (snapshot.child("receivedCoins").exists() && snapshot.child("receivedCoins").getValue(Long.class) != null)
            return snapshot.child("receivedCoins").getValue(Long.class);
        if (snapshot.child("totalCoins").exists() && snapshot.child("totalCoins").getValue(Long.class) != null)
            return snapshot.child("totalCoins").getValue(Long.class);
        return 0;
    }

    private void fetchHostDetailsAndDisplay(String hostUserId) {
        usersRef.child(hostUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    usersRef.orderByChild("my9DigitId").equalTo(hostUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot s2) {
                            for (DataSnapshot ds : s2.getChildren()) { showHostCard(ds, hostUserId); return; }
                            loadedHostCount++; if (loadedHostCount >= expectedHostCount) fetchOwnTargetCard();
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                    return;
                }
                showHostCard(snapshot, hostUserId);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showHostCard(DataSnapshot snapshot, String hostUserId) {
        String hostName = snapshot.child("name").getValue(String.class);
        if (hostName == null) hostName = "Host ID: " + hostUserId;
        long hostCoins = getCoins(snapshot);
        double hostTotalEarnings = hostCoins / 10000.0;
        double hostGets = hostTotalEarnings * 0.80;
        double agencyCommission = hostTotalEarnings * 0.20;
        totalCommission += agencyCommission;
        loadedHostCount++;
        addHostCard("👤 " + hostName, hostUserId, hostCoins, hostGets, 80, false);
        if (loadedHostCount >= expectedHostCount) fetchOwnTargetCard();
    }

    private void addHostCard(String title, String id, long coins, double earnings, int percent, boolean isOwn) {
        LinearLayout card = new LinearLayout(AgencyDashboardActivity.this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(25, 20, 25, 20);
        android.graphics.drawable.GradientDrawable cardBg = new android.graphics.drawable.GradientDrawable();
        cardBg.setColor(android.graphics.Color.parseColor(isOwn ? "#3D2A00" : "#2A1B4E"));
        cardBg.setCornerRadius(15);
        card.setBackground(cardBg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 15);
        card.setLayoutParams(params);
        TextView tvName = new TextView(AgencyDashboardActivity.this);
        tvName.setText(title + " (ID: " + id + ")");
        tvName.setTextColor(android.graphics.Color.WHITE);
        tvName.setTextSize(15);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvName);
        TextView tvTarget = new TextView(AgencyDashboardActivity.this);
        tvTarget.setText("🎯 Target Coins: " + coins + " Coins");
        tvTarget.setTextColor(android.graphics.Color.parseColor("#00E676"));
        tvTarget.setTextSize(13);
        card.addView(tvTarget);
        TextView tvDollar = new TextView(AgencyDashboardActivity.this);
        tvDollar.setText("💵 Earnings (" + percent + "%): $" + String.format("%.2f", earnings));
        tvDollar.setTextColor(android.graphics.Color.parseColor("#FFD700"));
        tvDollar.setTextSize(13);
        card.addView(tvDollar);
        hostListContainer.addView(card);
    }
}
