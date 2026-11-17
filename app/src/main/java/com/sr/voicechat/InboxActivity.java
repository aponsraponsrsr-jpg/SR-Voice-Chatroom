package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private String myName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(android.graphics.Color.parseColor("#120A2A"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);

        TextView btnBack = new TextView(this);
        btnBack.setText("← Back");
        btnBack.setTextColor(android.graphics.Color.WHITE);
        btnBack.setTextSize(14);
        btnBack.setTypeface(null, android.graphics.Typeface.BOLD);
        btnBack.setPadding(10, 10, 10, 10);
        btnBack.setOnClickListener(v -> finish());
        root.addView(btnBack);

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

        // FIX - My ID - SR_USER / MY_9_DIGIT_ID থেকে নেওয়া, Profile এর সাথে মিলবে, Global এ খালি থাকবে না
        myId = getIntent().getStringExtra("MY_ID");
        if (myId == null || myId.isEmpty()) myId = getIntent().getStringExtra("USER_ID");
        if (myId == null || myId.isEmpty()) {
            SharedPreferences p1 = getSharedPreferences("SR_USER", MODE_PRIVATE);
            myId = p1.getString("MY_9_DIGIT_ID", "");
            myName = p1.getString("USER_NAME", "");
            if (myName == null || myName.isEmpty()) myName = p1.getString("MY_NAME", "User");
            if (myName == null || myName.isEmpty()) myName = p1.getString("name", "User");
        }
        if (myId == null || myId.isEmpty()) {
            myId = getSharedPreferences("SR_PREFS", MODE_PRIVATE).getString("MY_ID", "");
        }
        if (myId == null) myId = "";
        if (myName == null || myName.isEmpty()) myName = "User";

        systemNoticeRef = FirebaseDatabase.getInstance().getReference("system_notice");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        myInboxRef = usersRef.child(myId).child("inbox");

        loadSystemNotice();
        loadMyInbox();
    }

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
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // GLOBAL - Inbox লিস্ট - Agency + Friend + Follow + Message সব
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
                    } else if (type.equals("FRIEND_REQUEST")) {
                        showFriendRequestCard(key, msgSnap);
                    } else if (type.equals("FOLLOW_REQUEST")) {
                        showFollowRequestCard(key, msgSnap);
                    } else {
                        showNormalMessageCard(key, msgSnap);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ============ FRIEND REQUEST - Accept করলেই 2 জন Friend + SMS খুলবে ============
    private void showFriendRequestCard(String key, DataSnapshot snap) {
        String fromId = snap.child("fromId").getValue(String.class);
        String fromName = snap.child("fromName").getValue(String.class);
        String msg = snap.child("message").getValue(String.class);
        String status = snap.child("status").getValue(String.class);
        if (fromId == null) fromId = "";
        if (fromName == null) fromName = "User";
        if (msg == null) msg = "";
        if (status == null) status = "Pending";

        LinearLayout card = makeCard("#00B0FF");
        TextView tv = new TextView(this);
        tv.setText("👥 Friend Request\n" + fromName + " (ID: " + fromId + ")\n" + msg + "\nStatus: " + status);
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
            final String fId = fromId;
            final String fName = fromName;
            btnAccept.setOnClickListener(v -> acceptFriendRequest(key, fId, fName));

            Button btnReject = new Button(this);
            btnReject.setText("❌ Reject");
            btnReject.setBackgroundColor(android.graphics.Color.parseColor("#FF1744"));
            btnReject.setTextColor(android.graphics.Color.WHITE);
            btnReject.setOnClickListener(v -> rejectInvite(key));

            btnRow.addView(btnAccept);
            btnRow.addView(btnReject);
            card.addView(btnRow);
        } else if (status.equals("Accepted")) {
            Button btnChat = new Button(this);
            btnChat.setText("💬 Message - Say something...");
            btnChat.setBackgroundColor(android.graphics.Color.parseColor("#00C853"));
            btnChat.setTextColor(android.graphics.Color.WHITE);
            final String fId2 = fromId;
            final String fName2 = fromName;
            btnChat.setOnClickListener(v -> openChat(fId2, fName2));
            card.addView(btnChat);
        }
        inboxListContainer.addView(card);
    }

    private void acceptFriendRequest(String inviteKey, String fromId, String fromName) {
        if (fromId.isEmpty()) return;
        // 1. 2 জনের friends লিস্টে একে অপরকে যোগ - Global
        usersRef.child(myId).child("friends").child(fromId).setValue(true);
        usersRef.child(fromId).child("friends").child(myId).setValue(true);
        // 2. Status Accepted
        myInboxRef.child(inviteKey).child("status").setValue("Accepted");
        // 3. ওই লোককে Notification - তোমার Inbox এ যাবে
        String notifyKey = usersRef.child(fromId).child("inbox").push().getKey();
        if (notifyKey != null) {
            Map<String, Object> notify = new HashMap<>();
            notify.put("type", "MESSAGE");
            notify.put("title", "✅ Friend Request Accepted");
            notify.put("message", myName + " (ID: " + myId + ") accepted your Friend Request - Now you can Chat");
            notify.put("fromId", myId);
            notify.put("fromName", myName);
            notify.put("timestamp", System.currentTimeMillis());
            usersRef.child(fromId).child("inbox").child(notifyKey).setValue(notify);
        }
        Toast.makeText(this, "You are now Friends with " + fromName + " ❤️", Toast.LENGTH_LONG).show();
        openChat(fromId, fromName);
    }

    // ============ FOLLOW REQUEST - Accept করলেই Follow Back + Mutual Follow + SMS ============
    private void showFollowRequestCard(String key, DataSnapshot snap) {
        String fromId = snap.child("fromId").getValue(String.class);
        String fromName = snap.child("fromName").getValue(String.class);
        String msg = snap.child("message").getValue(String.class);
        String status = snap.child("status").getValue(String.class);
        if (fromId == null) fromId = "";
        if (fromName == null) fromName = "User";
        if (msg == null) msg = "";
        if (status == null) status = "Pending";

        LinearLayout card = makeCard("#FF9800");
        TextView tv = new TextView(this);
        tv.setText("➕ Follow Request\n" + fromName + " (ID: " + fromId + ")\n" + msg + "\nStatus: " + status);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(14);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tv);

        if (status.equals("Pending")) {
            LinearLayout btnRow = new LinearLayout(this);
            btnRow.setOrientation(LinearLayout.HORIZONTAL);
            btnRow.setPadding(0, 15, 0, 0);

            Button btnAccept = new Button(this);
            btnAccept.setText("✅ Follow Back");
            btnAccept.setBackgroundColor(android.graphics.Color.parseColor("#00E676"));
            btnAccept.setTextColor(android.graphics.Color.WHITE);
            final String fId = fromId;
            final String fName = fromName;
            btnAccept.setOnClickListener(v -> acceptFollowRequest(key, fId, fName));

            Button btnReject = new Button(this);
            btnReject.setText("❌ Reject");
            btnReject.setBackgroundColor(android.graphics.Color.parseColor("#FF1744"));
            btnReject.setTextColor(android.graphics.Color.WHITE);
            btnReject.setOnClickListener(v -> rejectInvite(key));

            btnRow.addView(btnAccept);
            btnRow.addView(btnReject);
            card.addView(btnRow);
        } else if (status.equals("Accepted")) {
            Button btnChat = new Button(this);
            btnChat.setText("💬 Message - Say something...");
            btnChat.setBackgroundColor(android.graphics.Color.parseColor("#00C853"));
            btnChat.setTextColor(android.graphics.Color.WHITE);
            final String fId2 = fromId;
            final String fName2 = fromName;
            btnChat.setOnClickListener(v -> openChat(fId2, fName2));
            card.addView(btnChat);
        }
        inboxListContainer.addView(card);
    }

    private void acceptFollowRequest(String inviteKey, String fromId, String fromName) {
        if (fromId.isEmpty()) return;
        // Mutual Follow - আমি তাকে Follow করি + সে আমাকে Follow করে
        usersRef.child(myId).child("following").child(fromId).setValue(true);
        usersRef.child(myId).child("followers").child(fromId).setValue(true);
        usersRef.child(fromId).child("followers").child(myId).setValue(true);
        usersRef.child(fromId).child("following").child(myId).setValue(true);
        myInboxRef.child(inviteKey).child("status").setValue("Accepted");
        String notifyKey = usersRef.child(fromId).child("inbox").push().getKey();
        if (notifyKey != null) {
            Map<String, Object> notify = new HashMap<>();
            notify.put("type", "MESSAGE");
            notify.put("title", "✅ Follow Back - Mutual Follow");
            notify.put("message", myName + " (ID: " + myId + ") followed you back - Now you can Chat");
            notify.put("fromId", myId);
            notify.put("fromName", myName);
            notify.put("timestamp", System.currentTimeMillis());
            usersRef.child(fromId).child("inbox").child(notifyKey).setValue(notify);
        }
        Toast.makeText(this, "Mutual Follow with " + fromName + " ✅ Now you can Chat", Toast.LENGTH_LONG).show();
        openChat(fromId, fromName);
    }

    private void openChat(String otherId, String otherName) {
        if (otherId == null || otherId.isEmpty()) return;
        String chatId = myId.compareTo(otherId) < 0 ? myId + "_" + otherId : otherId + "_" + myId;
        Intent i = new Intent(InboxActivity.this, PrivateChatActivity.class);
        i.putExtra("MY_ID", myId);
        i.putExtra("MY_NAME", myName);
        i.putExtra("OTHER_ID", otherId);
        i.putExtra("OTHER_NAME", otherName);
        i.putExtra("CHAT_ID", chatId);
        startActivity(i);
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
        String fromId = snap.child("fromId").getValue(String.class);
        String fromName = snap.child("fromName").getValue(String.class);
        if (title == null) title = "📩 Message";
        if (msg == null) msg = "";
        LinearLayout card = makeCard("#1E1E2E");
        TextView tv = new TextView(this);
        tv.setText(title + "\n" + msg);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setTextSize(14);
        card.addView(tv);
        // Friend Accepted হলে Message কার্ড থেকেও Chat খুলবে
        if (fromId != null && !fromId.isEmpty() && title.contains("Accepted")) {
            Button btnChat = new Button(this);
            btnChat.setText("💬 Message - Say something...");
            btnChat.setBackgroundColor(android.graphics.Color.parseColor("#00C853"));
            btnChat.setTextColor(android.graphics.Color.WHITE);
            final String fId = fromId;
            final String fName = fromName != null ? fromName : "Chat";
            btnChat.setOnClickListener(v -> openChat(fId, fName));
            card.addView(btnChat);
        }
        inboxListContainer.addView(card);
    }

    private void acceptAgencyInvite(String inviteKey, String agencyId, String agencyName) {
        if (agencyId.isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("agencies").child(agencyId).child("hosts").child(myId).setValue(true);
        usersRef.child(myId).child("agencyId").setValue(agencyId);
        usersRef.child(myId).child("agencyName").setValue(agencyName);
        usersRef.child(myId).child("isHost").setValue(true);
        myInboxRef.child(inviteKey).child("status").setValue("Accepted");
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
