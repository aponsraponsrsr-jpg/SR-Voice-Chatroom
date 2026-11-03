package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String roomId;
    private int myCurrentSeat = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        
        db = FirebaseFirestore.getInstance();
        roomId = getIntent().getStringExtra("roomId");
        if (roomId == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                roomId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }
    }

    public void handleSeatClick(int seatPos) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("rooms").document(roomId)
          .collection("seats").document(String.valueOf(seatPos))
          .get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                if (myCurrentSeat != -1 && myCurrentSeat != seatPos) {
                    new AlertDialog.Builder(this)
                        .setTitle("Change Seat?")
                        .setMessage("Do you want to change your seat?")
                        .setPositiveButton("Take Mic", (d, w) -> { leaveCurrentSeat(); joinSeat(seatPos); })
                        .setNegativeButton("Exit", (d, w) -> leaveCurrentSeat())
                        .setNeutralButton("Cancel", null).show();
                } else {
                    joinSeat(seatPos);
                }
            } else {
                String occupier = doc.getString("userId");
                if (occupier != null && occupier.equals(myUid)) {
                    new AlertDialog.Builder(this)
                        .setTitle("Seat Options")
                        .setMessage("Your seat")
                        .setPositiveButton("Exit", (d, w) -> leaveCurrentSeat())
                        .setNeutralButton("Cancel", null).show();
                } else {
                    Toast.makeText(this, "Everything is alone right now", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void joinSeat(int pos) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        m.put("seatNo", pos);
        db.collection("rooms").document(roomId).collection("seats").document(String.valueOf(pos)).set(m);
        myCurrentSeat = pos;
        Toast.makeText(this, "Joined Seat " + pos, Toast.LENGTH_SHORT).show();
    }

    private void leaveCurrentSeat() {
        if (myCurrentSeat != -1) {
            db.collection("rooms").document(roomId).collection("seats").document(String.valueOf(myCurrentSeat)).delete();
            myCurrentSeat = -1;
        }
    }
}
