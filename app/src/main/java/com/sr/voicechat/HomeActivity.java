package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class HomeActivity extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String mySrId = "";
    TextView tvCoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvCoin = findViewById(R.id.tvCoin);

        // 1. Load My Permanent ID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                mySrId = doc.getString("sr_id");
                if(mySrId==null) mySrId = doc.getString("room_id");
                if(tvCoin!=null) tvCoin.setText("ID: " + mySrId);
                Toast.makeText(this, "My Permanent ID: " + mySrId, Toast.LENGTH_LONG).show();
            }
        });

        // 2. + Create Button -> My ID = My Room
        findViewById(R.id.navCreate).setOnClickListener(v -> {
            if(mySrId.isEmpty()){
                Toast.makeText(this, "ID Loading...", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, VoiceRoomActivity.class);
            i.putExtra("room_id", mySrId);
            i.putExtra("is_owner", true);
            startActivity(i);
        });

        // First create button also
        if(findViewById(R.id.btnCreateFirst)!=null){
            findViewById(R.id.btnCreateFirst).setOnClickListener(v -> {
                if(mySrId.isEmpty()) return;
                Intent i = new Intent(this, VoiceRoomActivity.class);
                i.putExtra("room_id", mySrId);
                i.putExtra("is_owner", true);
                startActivity(i);
            });
        }

        // 3. Me Button
        findViewById(R.id.navMe).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // 4. SEARCH - Long press on Banner
        findViewById(R.id.bannerArea).setOnLongClickListener(v -> {
            showSearchDialog();
            return true;
        });
        // Also click on Popular to search
        if(findViewById(R.id.tvPopular)!=null){
            findViewById(R.id.tvPopular).setOnLongClickListener(v -> {
                showSearchDialog();
                return true;
            });
        }
    }

    void showSearchDialog(){
        EditText input = new EditText(this);
        input.setHint("Enter ID e.g. 10052384");
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔍 Search by Permanent ID")
            .setMessage("7-8 Digit ID লিখো - বন্ধুর রুমে ঢুকে যাবে")
            .setView(input)
            .setPositiveButton("Join Room", (d,w)->{
                String searchId = input.getText().toString().trim();
                if(searchId.length() < 6){
                    Toast.makeText(this, "সঠিক ID দাও!", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.collection("rooms").document(searchId).get().addOnSuccessListener(doc -> {
                    if(doc.exists()){
                        Intent i = new Intent(this, VoiceRoomActivity.class);
                        i.putExtra("room_id", searchId);
                        i.putExtra("is_owner", false);
                        startActivity(i);
                    } else {
                        // Allow join even if not created yet - ID is valid
                        Toast.makeText(this, "ID: "+searchId+" তে Join করছি...", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, VoiceRoomActivity.class);
                        i.putExtra("room_id", searchId);
                        i.putExtra("is_owner", false);
                        startActivity(i);
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
