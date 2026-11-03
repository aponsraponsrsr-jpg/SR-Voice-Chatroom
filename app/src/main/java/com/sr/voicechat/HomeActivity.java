package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView tvRoomName, tvRoundHomeId, tvMyIdDisplay;
    private RecyclerView recyclerRooms;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoundHomeId = findViewById(R.id.tvRoundHomeId);
        tvMyIdDisplay = findViewById(R.id.tvMyIdDisplay);
        recyclerRooms = findViewById(R.id.recyclerRooms);

        if (recyclerRooms != null) {
            recyclerRooms.setLayoutManager(new LinearLayoutManager(this));
        }

        if (tvRoomName != null) {
            tvRoomName.setText("SR Voice Chat - Home");
        }

        loadMyPermanentId();
        
        if (tvMyIdDisplay != null) {
            tvMyIdDisplay.setOnClickListener(v -> {
                startActivity(new Intent(this, VoiceRoomActivity.class));
            });
        }
    }

    private void loadMyPermanentId() {
        if (mAuth.getCurrentUser() == null) return;
        if (tvRoundHomeId == null) return;
        
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) {
                tvRoundHomeId.setText("ID: Loading...");
                return;
            }

            Long displayId = doc.getLong("displayId");
            Long nineDigitId = doc.getLong("nineDigitId");
            Boolean isShort = doc.getBoolean("isShortId");
            Long shortId = doc.getLong("shortId");
            String name = doc.getString("name");

            if (isShort != null && isShort && shortId != null) {
                tvRoundHomeId.setText("ID: " + shortId + " [VIP]");
            } else if (displayId != null) {
                tvRoundHomeId.setText("ID: " + displayId);
            } else if (nineDigitId != null) {
                tvRoundHomeId.setText("ID: " + nineDigitId);
            } else {
                tvRoundHomeId.setText("ID: 100000000+");
            }

            if (tvMyIdDisplay != null && name != null) {
                tvMyIdDisplay.setText("Welcome " + name + " - Tap to Join Voice Room");
            }
        });
    }
}
