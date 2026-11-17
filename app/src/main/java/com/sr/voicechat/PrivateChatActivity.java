package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class PrivateChatActivity extends AppCompatActivity {

    private LinearLayout chatContainer;
    private EditText edtMsg;
    private TextView txtTitle;
    private String myId = "", myName = "User", otherId = "", otherName = "Chat", chatId = "";
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = getIntent().getStringExtra("MY_ID");
        if (myId == null || myId.isEmpty()) myId = prefs.getString("MY_9_DIGIT_ID", "");
        myName = getIntent().getStringExtra("MY_NAME");
        if (myName == null || myName.isEmpty()) myName = prefs.getString("USER_NAME", "User");
        otherId = getIntent().getStringExtra("OTHER_ID");
        if (otherId == null) otherId = "";
        otherName = getIntent().getStringExtra("OTHER_NAME");
        if (otherName == null || otherName.isEmpty()) otherName = "Chat";
        chatId = getIntent().getStringExtra("CHAT_ID");

        // GLOBAL Chat ID - 2 জনের একই ID হবে, ছোট ID আগে
        if ((chatId == null || chatId.isEmpty()) && !myId.isEmpty() && !otherId.isEmpty()) {
            chatId = myId.compareTo(otherId) < 0 ? myId + "_" + otherId : otherId + "_" + myId;
        }
        if (chatId == null) chatId = "chat_" + System.currentTimeMillis();

        // UI - সব Code দিয়ে বানানো, xml লাগবে না
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackgroundColor(Color.parseColor("#120A2A"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 30, 30, 30);
        sv.addView(root);

        TextView btnBack = new TextView(this);
        btnBack.setText("← Back");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(14);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(10, 10, 10, 10);
        btnBack.setOnClickListener(v -> finish());
        root.addView(btnBack);

        txtTitle = new TextView(this);
        txtTitle.setText("💬 " + otherName + "  (ID: " + otherId + ")");
        txtTitle.setTextColor(Color.parseColor("#FFD700"));
        txtTitle.setTextSize(19);
        txtTitle.setTypeface(null, Typeface.BOLD);
        txtTitle.setPadding(0, 10, 0, 10);
        root.addView(txtTitle);

        TextView txtStatus = new TextView(this);
        txtStatus.setText("✅ You are connected - Realtime SMS");
        txtStatus.setTextColor(Color.parseColor("#00E676"));
        txtStatus.setTextSize(12);
        root.addView(txtStatus);

        chatContainer = new LinearLayout(this);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        chatContainer.setPadding(0, 20, 0, 20);
        root.addView(chatContainer);

        // Say something... Box - নিচে
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 20, 0, 0);
        root.addView(row);

        edtMsg = new EditText(this);
        edtMsg.setHint("Say something...");
        edtMsg.setHintTextColor(Color.parseColor("#888888"));
        edtMsg.setTextColor(Color.WHITE);
        edtMsg.setTextSize(15);
        edtMsg.setPadding(25, 25, 25, 25);
        edtMsg.setBackgroundColor(Color.parseColor("#2A1B4E"));
        edtMsg.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(edtMsg);

        Button btnSend = new Button(this);
        btnSend.setText("Send");
        btnSend.setTextColor(Color.WHITE);
        btnSend.setBackgroundColor(Color.parseColor("#FF4081"));
        btnSend.setOnClickListener(v -> sendMsg());
        row.addView(btnSend);

        setContentView(sv);

        // Firebase - Realtime Listen
        chatRef = FirebaseDatabase.getInstance().getReference("private_chats").child(chatId).child("messages");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                chatContainer.removeAllViews();
                if (!snap.exists()) {
                    TextView empty = new TextView(PrivateChatActivity.this);
                    empty.setText("No messages yet.\nSay Hi 👋");
                    empty.setTextColor(Color.parseColor("#888888"));
                    empty.setTextSize(14);
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(0, 40, 0, 0);
                    chatContainer.addView(empty);
                    return;
                }
                for (DataSnapshot m : snap.getChildren()) {
                    String sender = m.child("senderId").getValue(String.class);
                    String text = m.child("text").getValue(String.class);
                    if (text == null) text = "";
                    boolean isMe = myId.equals(sender);
                    TextView tv = new TextView(PrivateChatActivity.this);
                    tv.setText((isMe ? "You: " : otherName + ": ") + text);
                    tv.setTextColor(Color.parseColor(isMe ? "#00E676" : "#FFFFFF"));
                    tv.setTextSize(15);
                    tv.setPadding(20, 12, 20, 12);
                    tv.setBackgroundColor(Color.parseColor(isMe ? "#1E3A2E" : "#2A1B4E"));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 6, 0, 6);
                    tv.setLayoutParams(lp);
                    chatContainer.addView(tv);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void sendMsg() {
        String text = edtMsg.getText().toString().trim();
        if (text.isEmpty()) return;
        if (chatRef == null) return;
        String key = chatRef.push().getKey();
        if (key == null) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", myId);
        msg.put("senderName", myName);
        msg.put("receiverId", otherId);
        msg.put("text", text);
        msg.put("timestamp", System.currentTimeMillis());
        chatRef.child(key).setValue(msg);
        edtMsg.setText("");
    }
}
