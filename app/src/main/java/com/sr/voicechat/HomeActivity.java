package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView tvRoundHomeId;
    private RecyclerView recyclerRooms;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvRoundHomeId = findViewById(R.id.tvRoundHomeId);
        recyclerRooms = findViewById(R.id.recyclerRooms);

        loadRoundHomeId();
        loadRooms();
    }

    private void loadRoundHomeId() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) return;

            Long displayId = doc.getLong("displayId");
            Long nineDigitId = doc.getLong("nineDigitId");
            String globalId = doc.getString("globalId");
            Boolean isShort = doc.getBoolean("isShortId");
            Long shortId = doc.getLong("shortId");

            // Round Home ID - Permanent Display
            if (isShort != null && isShort && shortId != null) {
                // VIP Short ID - তুমি Panel থেকে দিয়েছো
                tvRoundHomeId.setText("ID: " + shortId);
                tvRoundHomeId.setBackgroundColor(0xFFFFD700); // Gold for VIP
            } else if (displayId != null) {
                tvRoundHomeId.setText("ID: " + displayId);
            } else if (nineDigitId != null) {
                tvRoundHomeId.setText("ID: " + nineDigitId);
            } else if (globalId != null) {
                tvRoundHomeId.setText(globalId); // SR_100254789 Mix ID
            } else {
                tvRoundHomeId.setText("ID: Generating...");
            }
        });
    }

    private void loadRooms() {
        // Room List Logic - তোমার Group Chat List
        // এখানে পরে Room Adapter বসবে
        Toast.makeText(this, "Round Home - Rooms Loading...", Toast.LENGTH_SHORT).show();
    }
}
