package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String mySrId = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = FirebaseFirestore.getInstance();
        TextView tvCoin = findViewById(R.id.tvCoin);
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                mySrId = doc.getString("sr_id");
                if(mySrId==null) mySrId = doc.getString("room_id");
                if(mySrId!=null && tvCoin!=null) tvCoin.setText("ID: " + mySrId);
            }
        });
        findViewById(R.id.navCreate).setOnClickListener(v -> createMyRoom());
        findViewById(R.id.btnCreateFirst).setOnClickListener(v -> createMyRoom());
        findViewById(R.id.navMe).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            EditText et = findViewById(R.id.etSearchId);
            String s = et.getText().toString().trim();
            if(s.length() < 5){ Toast.makeText(this,"ID likho!",Toast.LENGTH_SHORT).show(); return; }
            joinRoom(s);
        });
        loadActiveRooms();
    }
    void createMyRoom(){ if(mySrId==null||mySrId.isEmpty()){Toast.makeText(this,"ID Loading...",Toast.LENGTH_SHORT).show();return;} joinRoom(mySrId); }
    void joinRoom(String roomId){ Intent i = new Intent(this, VoiceRoomActivity.class); i.putExtra("room_id", roomId); i.putExtra("is_owner", roomId.equals(mySrId)); startActivity(i); }
    void loadActiveRooms(){
        db.collection("rooms").addSnapshotListener((value, error) -> {
            if(value==null) return;
            LinearLayout container = findViewById(R.id.roomListContainer);
            if(container==null) return;
            container.removeAllViews();
            for(DocumentSnapshot doc : value.getDocuments()){
                String roomId = doc.getId();
                String name = doc.getString("owner_name"); if(name==null) name="User "+roomId;
                LinearLayout item = new LinearLayout(this); item.setOrientation(LinearLayout.HORIZONTAL); item.setPadding(24,24,24,24); item.setBackgroundColor(0xFF2A2A2A);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,16); item.setLayoutParams(lp);
                TextView tv = new TextView(this); tv.setText("🎙️ "+name+" - LIVE"); tv.setTextColor(0xFFFFFFFF); tv.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
                Button btn = new Button(this); btn.setText("JOIN"); btn.setOnClickListener(v->joinRoom(roomId));
                item.addView(tv); item.addView(btn); item.setOnClickListener(v->joinRoom(roomId)); container.addView(item);
            }
        });
    }
}
