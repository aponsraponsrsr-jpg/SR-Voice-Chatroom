package com.sr.voicechat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String roomId;
    private EditText etMessage;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        roomId = getIntent().getStringExtra("roomId");

        recyclerView = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        ImageView btnSend = findViewById(R.id.btnSend);
        TextView tvRoomName = findViewById(R.id.tvRoomName);

        if (roomId != null) tvRoomName.setText("Room: " + roomId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Map<String, Object>> messages = new ArrayList<>();
        // Simple adapter - will show messages
        ChatAdapter adapter = new ChatAdapter(messages);
        recyclerView.setAdapter(adapter);

        // HEAVY GLOBAL LISTENER - Real time chat
        db.collection("rooms").document(roomId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        messages.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            messages.add(doc.getData());
                        }
                        adapter.notifyDataSetChanged();
                        if (messages.size() > 0) recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty() || mAuth.getCurrentUser() == null) return;

            Map<String, Object> msg = new HashMap<>();
            msg.put("text", text);
            msg.put("uid", mAuth.getCurrentUser().getUid());
            msg.put("name", mAuth.getCurrentUser().getDisplayName());
            msg.put("timestamp", System.currentTimeMillis());

            db.collection("rooms").document(roomId).collection("messages").add(msg);
            etMessage.setText("");
        });
    }

    // Simple inner adapter to avoid other file error
    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        List<Map<String, Object>> list;
        ChatAdapter(List<Map<String, Object>> list) { this.list = list; }
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(24, 24, 24, 24);
            return new ViewHolder(tv);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map<String, Object> m = list.get(position);
            ((TextView) holder.itemView).setText(m.get("name") + ": " + m.get("text"));
        }
        @Override public int getItemCount() { return list.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(android.view.View itemView) { super(itemView); }
        }
    }
}
