package com.sr.voicechat;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    private TextView txtAgencyTitle, txtTotalHosts, txtTotalEarnings;
    private Button btnOpenInviteDialog;
    private String agencyOwnerId = "";
    private String agencyName = "";
    private DatabaseReference agencyRef, invitationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ড্যাশবোর্ডের লেআউট ইনিশিয়ালাইজেশন
        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(40, 40, 40, 40);
        rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#120A2A"));

        txtAgencyTitle = new TextView(this);
        txtAgencyTitle.setText("🏛️ Agency Dashboard");
        txtAgencyTitle.setTextColor(android.graphics.Color.parseColor("#FFD700"));
        txtAgencyTitle.setTextSize(20);
        txtAgencyTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        rootLayout.addView(txtAgencyTitle);

        txtTotalHosts = new TextView(this);
        txtTotalHosts.setText("👥 Active Hosts: 0");
        txtTotalHosts.setTextColor(android.graphics.Color.WHITE);
        txtTotalHosts.setTextSize(16);
        txtTotalHosts.setPadding(0, 20, 0, 10);
        rootLayout.addView(txtTotalHosts);

        txtTotalEarnings = new TextView(this);
        txtTotalEarnings.setText("💰 Total Earnings: 0 Coins ($0.00)");
        txtTotalEarnings.setTextColor(android.graphics.Color.parseColor("#00E676"));
        txtTotalEarnings.setTextSize(16);
        txtTotalEarnings.setPadding(0, 0, 0, 40);
        rootLayout.addView(txtTotalEarnings);

        // হোস্ট ইনভাইট পাঠানোর প্রিমিয়াম বাটন ("Invite" অপশন)
        btnOpenInviteDialog = new Button(this);
        btnOpenInviteDialog.setText("➕ Invite New Host");
        btnOpenInviteDialog.setBackgroundColor(android.graphics.Color.parseColor("#FF4081"));
        btnOpenInviteDialog.setTextColor(android.graphics.Color.WHITE);
        btnOpenInviteDialog.setTypeface(null, android.graphics.Typeface.BOLD);
        rootLayout.addView(btnOpenInviteDialog);

        setContentView(rootLayout);

        // ইনটেন্ট থেকে এজেন্সি ওনারের আইডি এবং নাম রিসিভ করা
        agencyOwnerId = getIntent().getStringExtra("AGENCY_OWNER_ID");
        agencyName = getIntent().getStringExtra("AGENCY_NAME");

        if (agencyOwnerId == null) agencyOwnerId = "";
        if (agencyName == null) agencyName = "SR Agency";

        if (txtAgencyTitle != null) {
            txtAgencyTitle.setText("🏛️ " + agencyName + " - Dashboard");
        }

        agencyRef = FirebaseDatabase.getInstance().getReference("agencies").child(agencyOwnerId);
        invitationRef = FirebaseDatabase.getInstance().getReference("agency_invitations");

        loadAgencyStats();

        // ইনভাইট বাটনে ক্লিক করলে আইডি ইনপুট ডায়ালগ ওপেন হবে
        btnOpenInviteDialog.setOnClickListener(v -> showHostInviteInputDialog());
    }

    // হোস্টের আইডি দিয়ে ইনভাইট পাঠানোর পপআপ ডায়ালগ
    private void showHostInviteInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📨 Send Host Invitation");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        final EditText inputTargetId = new EditText(this);
        inputTargetId.setHint("Enter Host ID (Any Digit ID)...");
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

    // রিয়েল-টাইমে ইউজারের কাছে ডাইনামিক এজেন্সি নামসহ ইনভাইট পাঠানোর লজিক
    private void sendRealtimeHostInvitation(String targetUserId) {
        String inviteKey = invitationRef.child(targetUserId).push().getKey();
        if (inviteKey == null) return;

        java.util.Map<String, Object> inviteMap = new java.util.HashMap<>();
        inviteMap.put("agencyId", agencyOwnerId);
        inviteMap.put("agencyName", agencyName); // ডাইনামিক এজেন্সি নাম
        inviteMap.put("inviterId", agencyOwnerId);
        inviteMap.put("status", "Pending"); // Pending, Accepted, Refused
        inviteMap.put("timestamp", System.currentTimeMillis());

        invitationRef.child(targetUserId).child(inviteKey).setValue(inviteMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Host invitation sent successfully to ID: " + targetUserId, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to send invitation!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // এজেন্সির হোস্ট সংখ্যা এবং ডাটা লোড করার মেথড
    private void loadAgencyStats() {
        agencyRef.child("hosts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long hostCount = snapshot.getChildrenCount();
                if (txtTotalHosts != null) {
                    txtTotalHosts.setText("👥 Active Hosts: " + hostCount);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
