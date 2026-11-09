package com.sr.voicechat;

import android.os.Bundle;
import android.view.Gravity;
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

public class AgencyDashboardActivity extends AppCompatActivity {

    private TextView txtAgencyTitle, txtTotalHosts, txtTotalEarnings, txtHostListHeader;
    private Button btnOpenInviteDialog;
    private LinearLayout hostListContainer;
    private String agencyOwnerId = "";
    private String agencyName = "";
    private DatabaseReference agencyRef, invitationRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // প্রিমিয়াম ড্যাশবোর্ড লেআউট
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(android.graphics.Color.parseColor("#120A2A"));

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(40, 40, 40, 40);

        txtAgencyTitle = new TextView(this);
        txtAgencyTitle.setText("🏛️ Agency Dashboard");
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

        // হোস্ট ইনভাইট পাঠানোর প্রিমিয়াম বাটন
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

        // হোস্টদের তালিকা দেখানোর কন্টেইনার
        hostListContainer = new LinearLayout(this);
        hostListContainer.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(hostListContainer);

        scrollView.addView(rootLayout);
        setContentView(scrollView);

        // ইনটেন্ট থেকে ডাটা রিসিভ
        agencyOwnerId = getIntent().getStringExtra("AGENCY_OWNER_ID");
        agencyName = getIntent().getStringExtra("AGENCY_NAME");

        if (agencyOwnerId == null) agencyOwnerId = "";
        if (agencyName == null) agencyName = "SR Agency";

        if (txtAgencyTitle != null) {
            txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard");
        }

        agencyRef = FirebaseDatabase.getInstance().getReference("agencies").child(agencyOwnerId);
        invitationRef = FirebaseDatabase.getInstance().getReference("agency_invitations");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadAgencyDataAndHosts();

        // ইনভাইট বাটনে ক্লিক ইভেন্ট
        btnOpenInviteDialog.setOnClickListener(v -> showHostInviteInputDialog());
    }

    // হোস্ট ইনভাইট পাঠানোর পপআপ (যেখানে যেকোনো ডিজিটের আইডি দেওয়া যাবে)
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
            if (!targetUserId.isEmpty()) {
                sendRealtimeHostInvitation(targetUserId);
            } else {
                Toast.makeText(this, "Please enter a valid User ID!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // রিয়েল-টাইমে ডাইনামিক এজেন্সি নামসহ ইনভাইট পাঠানোর লজিক
    private void sendRealtimeHostInvitation(String targetUserId) {
        String inviteKey = invitationRef.child(targetUserId).push().getKey();
        if (inviteKey == null) return;

        java.util.Map<String, Object> inviteMap = new java.util.HashMap<>();
        inviteMap.put("agencyId", agencyOwnerId);
        inviteMap.put("agencyName", agencyName); // ডাইনামিক এজেন্সি নাম
        inviteMap.put("inviterId", agencyOwnerId);
        inviteMap.put("status", "Pending"); 
        inviteMap.put("timestamp", System.currentTimeMillis());

        invitationRef.child(targetUserId).child(inviteKey).setValue(inviteMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Host invitation sent successfully to ID: " + targetUserId, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to send invitation!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // এজেন্সির হোস্ট এবং তাদের টার্গেট/আর্নিং লোড করার মেথড
    private void loadAgencyDataAndHosts() {
        agencyRef.child("hosts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long hostCount = snapshot.getChildrenCount();
                if (txtTotalHosts != null) {
                    txtTotalHosts.setText("👥 Active Hosts: " + hostCount);
                }

                hostListContainer.removeAllViews();

                for (DataSnapshot hostSnap : snapshot.getChildren()) {
                    String hostUserId = hostSnap.getKey();
                    if (hostUserId != null) {
                        fetchHostDetailsAndDisplay(hostUserId);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // প্রতিটি হোস্টের কয়েন টার্গেট ও ডলার হিসাব করে ড্যাশবোর্ডে শো করার লজিক
    private void fetchHostDetailsAndDisplay(String hostUserId) {
        usersRef.child(hostUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String hostName = snapshot.child("name").getValue(String.class);
                if (hostName == null) hostName = "Host ID: " + hostUserId;

                long hostCoins = 0;
                if (snapshot.child("receivedCoins").exists() && snapshot.child("receivedCoins").getValue(Long.class) != null) {
                    hostCoins = snapshot.child("receivedCoins").getValue(Long.class);
                }

                // পলিসি অনুযায়ী কয়েন থেকে ডলারে রূপান্তর (যেমন: ১০,০০০ কয়েন = ১ ডলার বা পলিসি রেট)
                double hostEarnings = hostCoins / 10000.0; 

                LinearLayout card = new LinearLayout(AgencyDashboardActivity.this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(25, 20, 25, 20);
                
                android.graphics.drawable.GradientDrawable cardBg = new android.graphics.drawable.GradientDrawable();
                cardBg.setColor(android.graphics.Color.parseColor("#2A1B4E"));
                cardBg.setCornerRadius(15);
                card.setBackground(cardBg);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 15);
                card.setLayoutParams(params);

                TextView tvName = new TextView(AgencyDashboardActivity.this);
                tvName.setText("👤 " + hostName + " (ID: " + hostUserId + ")");
                tvName.setTextColor(android.graphics.Color.WHITE);
                tvName.setTextSize(15);
                tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                card.addView(tvName);

                TextView tvTarget = new TextView(AgencyDashboardActivity.this);
                tvTarget.setText("🎯 Target Coins: " + hostCoins + " Coins");
                tvTarget.setTextColor(android.graphics.Color.parseColor("#00E676"));
                tvTarget.setTextSize(13);
                card.addView(tvTarget);

                TextView tvDollar = new TextView(AgencyDashboardActivity.this);
                tvDollar.setText("💵 Host Earnings: $" + String.format("%.2f", hostEarnings));
                tvDollar.setTextColor(android.graphics.Color.parseColor("#FFD700"));
                tvDollar.setTextSize(13);
                card.addView(tvDollar);

                hostListContainer.addView(card);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
