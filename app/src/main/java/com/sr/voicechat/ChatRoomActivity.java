package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {
    
    private String roomId, roomName;
    private TextView txtRoomName;
    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private List<UserModel> seatList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        db = FirebaseFirestore.getInstance();
        roomId = getIntent().getStringExtra("roomId");
        roomName = getIntent().getStringExtra("roomName");

        txtRoomName = findViewById(R.id.txtRoomName);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        
        if (roomName != null) {
            txtRoomName.setText(roomName);
        }

        // 10 সিটের জন্য - 5 কলাম করে 2 লাইন
        recyclerUsers.setLayoutManager(new GridLayoutManager(this, 5));
        
        // প্রথমে 10 টা খালি সিট বানাবো
        for (int i = 0; i < 10; i++) {
            seatList.add(new UserModel("Empty Seat " + (i+1), false));
        }
        
        userAdapter = new UserAdapter(seatList);
        recyclerUsers.setAdapter(userAdapter);

        loadRoomUsers();
    }

    private void loadRoomUsers() {
        if (roomId == null) return;
        
        // রুমের ভিতরে কে কে আছে Firestore থেকে লোড হবে
        db.collection("rooms").document(roomId).collection("seats")
          .addSnapshotListener((value, error) -> {
              if (error != null) {
                  Toast.makeText(this, "Seat load failed", Toast.LENGTH_SHORT).show();
                  return;
              }
              if (value != null) {
                  // খালি সিট রিসেট
                  for (int i = 0; i < 10; i++) {
                      seatList.set(i, new UserModel("Empty", false));
                  }
                  // Firestore থেকে ইউজার বসানো
                  for (QueryDocumentSnapshot doc : value) {
                      try {
                          int seatNo = Integer.parseInt(doc.getId()); // 0-9
                          UserModel user = doc.toObject(UserModel.class);
                          if (seatNo >= 0 && seatNo < 10) {
                              seatList.set(seatNo, user);
                          }
                      } catch (Exception e) {}
                  }
                  userAdapter.notifyDataSetChanged();
              }
          });
    }
}
