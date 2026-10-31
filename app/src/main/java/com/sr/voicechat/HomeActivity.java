package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvRooms;
    private FirebaseFirestore db;
    private List<RoomModel> roomList = new ArrayList();
    private RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        rvRooms = findViewById(R.id.rvRooms);
        
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter(roomList, room -> {
            // Room এ ক্লিক করলে Voice Room এ যাবে
            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            intent.putExtra("roomId", room.roomId);
            intent.putExtra("roomName", room.roomName);
            startActivity(intent);
        });
        rvRooms.setAdapter(adapter);

        // Permanent Room Load from Firebase
        loadRooms();

        FloatingActionButton fab = findViewById(R.id.fabCreate);
        if(fab != null){
            fab.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
            });
        }
    }

    private void loadRooms() {
        db.collection("rooms")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (value != null) {
                    roomList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        RoomModel room = doc.toObject(RoomModel.class);
                        roomList.add(room);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }
}
