package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private TextView tvChatMyId;
    private RecyclerView recyclerChat;
    private EditText etMessage;
    private Button btnSend;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvChatMyId = findViewById(R.id.tvChatMyId);
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) roomId = "global_chat";

        if (recyclerChat != null) {
            recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        }

        loadMyId();
        loadMessages();

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }
    }

    private void loadMyId() {
        if (mAuth.getCurrentUser() == null || tvChatMyId == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Long displayId = doc.getLong("displayId");
                Boolean isShort = doc.getBoolean("isShortId");
                Long shortId = doc.getLong("shortId");
                
                if (isShort != null && isShort && shortId != null) {
                    tvChatMyId.setText("My ID: " + shortId + " [VIP] - Chat");
                } else if (displayId != null) {
                    tvChatMyId.setText("My ID: " + displayId);
                }
            }
        });
    }

    private void loadMessages() {
        // Heavy Chat Loading - Firestore Real-time
        db.collection("chats").document(roomId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    // Adapter will be added later - For now just listening
                });
    }

    private void sendMessage() {
        if (etMessage == null) return;
        String msg = etMessage.getText().toString().trim();
        if (msg.isEmpty()) return;
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            Long displayId = userDoc.getLong("displayId");
            if (displayId == null) displayId = userDoc.getLong("nineDigitId");
            if (displayId == null) displayId = 100000000L;

            Map<String, Object> chatData = new HashMap<>();
            chatData.put("uid", uid);
            chatData.put("senderId", displayId); // 9 Digit ID দিয়েই Chat যাবে - Permanent
            chatData.put("message", msg);
            chatData.put("timestamp", System.currentTimeMillis());
            chatData.put("name", userDoc.getString("name"));

            db.collection("chats").document(roomId).collection("messages").add(chatData)
                    .addOnSuccessListener(a -> etMessage.setText(""))
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
