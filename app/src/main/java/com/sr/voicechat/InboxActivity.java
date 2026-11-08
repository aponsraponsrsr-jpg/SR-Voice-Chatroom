package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InboxActivity extends AppCompatActivity {

    private TextView txtSystemTitle, txtSystemMessage;
    private DatabaseReference systemNoticeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        txtSystemTitle = findViewById(R.id.txtSystemTitle);
        txtSystemMessage = findViewById(R.id.txtSystemMessage);

        // ফায়ারবেস থেকে রিয়েল-টাইম সিস্টেম নোটিশ লোড করার রেফারেন্স
        systemNoticeRef = FirebaseDatabase.getInstance().getReference("system_notice");

        loadSystemNotice();
    }

    private void loadSystemNotice() {
        systemNoticeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);

                    if (title != null) txtSystemTitle.setText(title);
                    if (message != null) txtSystemMessage.setText(message);
                } else {
                    // ডিফল্ট অফিশিয়াল নোটিশ যদি ফায়ারবেসে না থাকে
                    txtSystemTitle.setText("🌟 SR Voice Chatroom Official System");
                    txtSystemMessage.setText("No new system announcements right now. Enjoy your voice chatting!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InboxActivity.this, "Failed to load system notice", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
