package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String roomId;
    private LinearLayout chatContainer;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) roomId = "global_room";

        // Create UI without R.id - 100% No Error
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(20, 20, 20, 20);

        TextView title = new TextView(this);
        title.setText("Room: " + roomId + " - Heavy Global Chat");
        title.setTextSize(18);
        main.addView(title);

        ScrollView scroll = new ScrollView(this);
        chatContainer = new LinearLayout(this);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(chatContainer);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        main.addView(scroll, scrollParams);

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        etMessage = new EditText(this);
        etMessage.setHint("Type message...");
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputRow.addView(etMessage, etParams);
        Button btnSend = new Button(this);
        btnSend.setText("Send");
        inputRow.addView(btnSend);
        main.addView(inputRow);

        setContentView(main);

        // HEAVY GLOBAL LISTENER - Firestore
        db.collection("rooms").document(roomId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        chatContainer.removeAllViews();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Map<String, Object> data = doc.getData();
                            if (data == null) continue;
                            String name = data.get("name") != null ? data.get("name").toString() : "User";
                            String text = data.get("text") != null ? data.get("text").toString() : "";
                            TextView tv = new TextView(this);
                            tv.setText(name + ": " + text);
                            tv.setPadding(10, 10, 10, 10);
                            chatContainer.addView(tv);
                        }
                    }
                });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty() || mAuth.getCurrentUser() == null) return;
            Map<String, Object> msg = new HashMap<>();
            msg.put("text", text);
            msg.put("uid", mAuth.getCurrentUser().getUid());
            msg.put("name", mAuth.getCurrentUser().getDisplayName() != null ? mAuth.getCurrentUser().getDisplayName() : "User");
            msg.put("timestamp", System.currentTimeMillis());
            db.collection("rooms").document(roomId).collection("messages").add(msg);
            etMessage.setText("");
        });
    }
}
