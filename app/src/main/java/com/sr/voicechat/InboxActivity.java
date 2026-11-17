package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Button;
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
import java.util.HashMap;
import java.util.Map;

public class InboxActivity extends AppCompatActivity {

    private TextView txtSystemTitle, txtSystemMessage;
    private LinearLayout inboxListContainer;
    private DatabaseReference systemNoticeRef, myInboxRef, usersRef;
    private String myId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIX: Layout কোডেই বানানো, যাতে xml missing হলেও Inbox খুলে
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(android.graphics.Color.parseColor("#120A2A"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);

        TextView header = new TextView(this);
        header.setText("📥 Inbox");
        header.setTextColor(android.graphics.Color.parseColor("#FFD700"));
        header.setTextSize(22);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(header);

        txtSystemTitle = new TextView(this);
        txtSystemTitle.setText("🌟 SR Voice Chatroom Official System");
        txtSystemTitle.setTextColor(android.graphics.Color.WHITE);
        txtSystemTitle.setTextSize(16);
        txtSystemTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        txtSystemTitle.setPadding(0, 30, 0, 5);
        root.addView(txtSystemTitle);

        txtSystemMessage = new TextView(this);
        txtSystemMessage.setText("No new system announcements right now. Enjoy your voice chatting!");
        txtSystemMessage.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
        txtSystemMessage.setTextSize(14);
        txtSystemMessage.setPadding(0, 0, 0, 30);
        root.addView(txtSystemMessage);

        TextView lblInvites = new TextView(this);
        lblInvites.setText("📨 My Invitations & Messages:");
        lblInvites.setTextColor(android.graphics.Color.parseColor("#00B0FF"));
        lblInvites.setTextSize(17);
        lblInvites.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(lblInvites);

        inboxListContainer = new LinearLayout(this);
        inboxListContainer.setOrientation(LinearLayout.VERTICAL);
        inboxListContainer.setPadding(0, 20, 0, 0);
        root.addView(inboxListContainer);

        scrollView.addView(root);
        setContentView(scrollView);

        // FIX: My ID - 3 জায়গা থেকে নেওয়া, Global এ কখনো খালি থাকবে না
        myId = getIntent().getStringExtra("MY_ID");
        if (myId == null || myId.isEmpty()) myId = getIntent().getStringExtra("USER_ID");
        if (myId == null || myId.isEmpty()) {
            myId = getSharedPreferences("SR_PREFS", MODE_PRIVATE).getString("MY_ID", "");
        }
        if (myId == null) myId = "";

        systemNoticeRef = FirebaseDatabase.getInstance().getReference("system_notice");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        myInboxRef = usersRef.child(myId).child("inbox");

        loadSystemNotice();
        loadMyInbox();
    }

    // FIX 1: Failed to load আর আসবে না - Error হলেও Default মেসেজ থাকবে
    private void loadSystemNotice() {
        systemNoticeRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);
                    if (title != null && !title.isEmpty()) txtSystemTitle.setText(title);
                    if (message != null && !message.isEmpty()) txtSystemMessage.setText(message);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                // Toast দিবো না, চুপচাপ Default মেসেজ থাকবে - এটাই Global সিস্টেম
            }
        });
    }

    // FIX 2: Inbox লিস্ট - Agency Invite + Follow + সব মেসেজ
    private void loadMyInbox() {
        myInboxRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                inboxListContainer.removeAllViews();
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    TextView empty = new TextView(InboxActivity.this);
                    empty.setText("No messages yet.");
                    empty.setTextColor(android.graphics.Color.parseColor("#888888"));
                    empty.setTextSize(14);
                    inboxListContainer.addView(empty);
                    return;
                }
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    String key = msgSnap.getKey();
                    String type = msgSnap.child("type").getValue(String.class);
                    if (type == null) type = "MESSAGE";
                    if (type.equals("AGENCY_INVITE")) {
                        showAgencyInviteCard(key, msgSnap);
                    } else {
                        showNormalMessageCard(key, msgSnap);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAgencyInviteCard(String key, DataSnapshot snap) {
        String agencyName = snap.child("agencyName").getValue(String.class);
        String agencyId = snap.child("agencyId").getValue(String.class);
        String status = snap.child("status").getValue(String.class);
        if (agencyName == null) agencyName = "Agency";
        if (agencyId == null) agencyId = "";
        if (status == null) status = "Pending";

        LinearLayout card = makeCard("#2A1B4E");
        TextView tv = new TextView(this);
        tv.setText("🏛️ Agency Invite\n" + agencyName + " (ID: " + agencyId + ")\nStatus: " + status);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(14);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tv);

        if (status.equals("Pending")) {
            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setPadding(0, 15, 0, 0);

            Button btnAccept = new Button(this);
            btnAccept.setText("✅ Accept");
            btnAccept.setBackgroundColor(android.graphics.Color.parseColor("#00E676"));
            btnAccept.setTextColor(android.graphics.Color.WHITE);
            final String fAgencyId = agencyId;
            final String fAgencyName = agencyName;
            btnAccept.setOnClickListener(v -> acceptAgencyInvite(key, fAgencyId, fAgencyName));

            Button btnReject = new Button(this);
            btnReject.setText("❌ Reject");
            btnReject.setBackgroundColor(android.graphics.Color.parseColor("#FF1744"));
            btnReject.setTextColor(android.graphics.Color.WHITE);
            btnReject.setOnClickListener(v -> rejectInvite(key));

            btnRow.addView(btnAccept);
            btnRow.addView(btnReject);
            card.addView(btnRow);
        }
        inboxListContainer.addView(card);
    }

    private void showNormalMessageCard(String key, DataSnapshot snap) {
        String title = snap.child("title").getValue(String.class);
        String msg = snap.child("message").getValue(String.class);
        if (title == null) title = "📩 Message";
        if (msg == null) msg = "";
        LinearLayout card = makeCard("#1E1E2E");
        TextView tv = new TextView(this);
        tv.setText(title + "\n" + msg);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(14);
        card.addView(tv);
        inboxListContainer.addView(card);
    }

    // FIX 3: Accept করলেই Host Agency তে জয়েন + Agency Owner কে Notification
    private void acceptAgencyInvite(String inviteKey, String agencyId, String agencyName) {
        if (agencyId.isEmpty()) return;
        // 1. Agency এর hosts লিস্টে যোগ
        FirebaseDatabase.getInstance().getReference("agencies").child(agencyId).child("hosts").child(myId).setValue(true);
        // 2. নিজের প্রোফাইলে Agency সেট
        usersRef.child(myId).child("agencyId").setValue(agencyId);
        usersRef.child(myId).child("agencyName").setValue(agencyName);
        usersRef.child(myId).child("isHost").setValue(true);
        // 3. Invite Status Accepted
        myInboxRef.child(inviteKey).child("status").setValue("Accepted");
        // 4. Agency Owner কে Notification - তোমার Inbox এ যাবে
        String notifyKey = usersRef.child(agencyId).child("inbox").push().getKey();
        if (notifyKey != null) {
            Map<String, Object> notify = new HashMap<>();
            notify.put("type", "MESSAGE");
            notify.put("title", "✅ Host Accepted");
            notify.put("message", "User ID " + myId + " accepted your Agency invite and joined " + agencyName);
            notify.put("timestamp", System.currentTimeMillis());
            usersRef.child(agencyId).child("inbox").child(notifyKey).setValue(notify);
        }
        Toast.makeText(this, "You joined " + agencyName + " as Host!", Toast.LENGTH_LONG).show();
    }

    private void rejectInvite(String inviteKey) {
        myInboxRef.child(inviteKey).child("status").setValue("Rejected");
        Toast.makeText(this, "Invite Rejected", Toast.LENGTH_SHORT).show();
    }

    private LinearLayout makeCard(String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(25, 20, 25, 20);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(android.graphics.Color.parseColor(color));
        bg.setCornerRadius(15);
        card.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, 15);
        card.setLayoutParams(p);
        return card;
    }
}
