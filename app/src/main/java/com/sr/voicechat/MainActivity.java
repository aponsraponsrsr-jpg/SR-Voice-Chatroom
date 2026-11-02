package com.sr.voicechat;

import android.os.Bundle;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RoomAdapter.OnRoomClick {

    private RecyclerView recyclerRooms;
    private RoomAdapter roomAdapter;
    private List<RoomModel> roomList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerRooms = findViewById(R.id.recyclerRooms);
        recyclerRooms.setLayoutManager(new LinearLayoutManager(this));

        roomAdapter = new RoomAdapter(roomList, this);
        recyclerRooms.setAdapter(roomAdapter);

        db = FirebaseFirestore.getInstance();
        loadRooms();
    }

    private void loadRooms() {
        db.collection("rooms").addSnapshotListener((value, error) -> {
            if (value != null) {
                roomList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    RoomModel room = doc.toObject(RoomModel.class);
                    roomList.add(room);
                }
                roomAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRoomClick(RoomModel room) {
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("roomId", room.getRoomId());
        intent.putExtra("roomName", room.getRoomName());
        startActivity(intent);
    }
}
