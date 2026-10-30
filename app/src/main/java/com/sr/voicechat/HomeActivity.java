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
    EditText etSearchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvCoin = findViewById(R.id.tvCoin);
        etSearchId = findViewById(R.id.etSearchId);
        Button btnSearch = findViewById(R.id.btnSearch);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                mySrId = doc.getString("sr_id");
                if(mySrId==null) mySrId = doc.getString("room_id");
                if(mySrId!=null && tvCoin!=null){
                    tvCoin.setText("ID: " + mySrId);
                    Toast.makeText(this, "My Permanent ID: " + mySrId, Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.navCreate).setOnClickListener(v -> createMyRoom());
        findViewById(R.id.btnCreateFirst).setOnClickListener(v -> createMyRoom());
        findViewById(R.id.navMe).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        btnSearch.setOnClickListener(v -> doSearch());
        findViewById(R.id.bannerArea).setOnLongClickListener(v -> {
            showSearchDialog();
            return true;
        });
    }

    void createMyRoom(){
        if(mySrId==null || mySrId.isEmpty()){
            Toast.makeText(this, "ID Loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, VoiceRoomActivity.class);
        i.putExtra("room_id", mySrId);
        i.putExtra("is_owner", true);
        startActivity(i);
    }

    void doSearch(){
        String searchId = etSearchId.getText().toString().trim();
        if(searchId.length() < 6){
            Toast.makeText(this, "7-8 Digit ID লিখো!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, VoiceRoomActivity.class);
        i.putExtra("room_id", searchId);
        i.putExtra("is_owner", false);
        startActivity(i);
        Toast.makeText(this, "Joining Room: " + searchId, Toast.LENGTH_SHORT).show();
    }

    void showSearchDialog(){
        EditText input = new EditText(this);
        input.setHint("Enter ID e.g. 10052384");
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔍 Search by Permanent ID")
            .setView(input)
            .setPositiveButton("Join Room", (d,w)->{
                String searchId = input.getText().toString().trim();
                Intent i = new Intent(this, VoiceRoomActivity.class);
                i.putExtra("room_id", searchId);
                i.putExtra("is_owner", false);
                startActivity(i);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
