package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView tvRoundHomeId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_home);
            
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Safe Find - যদি ID না থাকে Crash করবে না
            tvRoundHomeId = findViewById(R.id.tvRoundHomeId);
            if (tvRoundHomeId == null) {
                // Fallback - Layout এ ID না থাকলে TextView খুঁজবে
                tvRoundHomeId = findViewById(R.id.tvId);
            }

            loadRoundHomeId();

        } catch (Exception e) {
            Toast.makeText(this, "Home Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadRoundHomeId() {
        if (mAuth == null || mAuth.getCurrentUser() == null) return;
        if (tvRoundHomeId == null) return;
        
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) {
                tvRoundHomeId.setText("ID: Loading...");
                return;
            }

            try {
                Long displayId = doc.getLong("displayId");
                Long nineDigitId = doc.getLong("nineDigitId");
                Boolean isShort = doc.getBoolean("isShortId");
                Long shortId = doc.getLong("shortId");

                if (isShort != null && isShort && shortId != null) {
                    tvRoundHomeId.setText("ID: " + shortId + " [VIP]");
                } else if (displayId != null) {
                    tvRoundHomeId.setText("ID: " + displayId);
                } else if (nineDigitId != null) {
                    tvRoundHomeId.setText("ID: " + nineDigitId);
                } else {
                    tvRoundHomeId.setText("ID: 100000000+");
                }
            } catch (Exception e) {
                tvRoundHomeId.setText("ID: 100000000+");
            }
        });
    }
}
