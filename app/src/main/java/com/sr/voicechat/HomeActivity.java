package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerRooms;
    private Button btnCreateRoom;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<DocumentSnapshot> roomList = new ArrayList<>();
    private RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerRooms = findViewById(R.id.recycler_rooms);
        btnCreateRoom = findViewById(R.id.btn_create_room);

        recyclerRooms.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new RoomAdapter(roomList, room -> {
            // Room এ ঢুকবে
            Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
            intent.putExtra("roomId", room.getId());
            startActivity(intent);
        });
        recyclerRooms.setAdapter(adapter);

        btnCreateRoom.setOnClickListener(v -> showCreateRoomDialog());

        loadRooms();
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        
        EditText etRoomName = dialog.findViewById(R.id.et_room_name);
        Button btnCreate = dialog.findViewById(R.id.btn_dialog_create);
        Button btnCancel = dialog.findViewById(R.id.btn_dialog_cancel);

        btnCreate.setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString().trim();
            if (roomName.isEmpty()) {
                Toast.makeText(this, "Room name দিন", Toast.LENGTH_SHORT).show();
                return;
            }
            createRoom(roomName);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void createRoom(String roomName) {
        String roomId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        String userName = auth.getCurrentUser() != null ? auth.getCurrentUser().getDisplayName() : "Guest";

        Map<String, Object> room = new HashMap<>();
        room.put("roomId", roomId);
        room.put("roomName", roomName);
        room.put("ownerId", userId);
        room.put("ownerName", userName);
        room.put("createdAt", System.currentTimeMillis());
        room.put("memberCount", 1);

        db.collection("rooms").document(roomId).set(room)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Room Created!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, VoiceRoomActivity.class);
                intent.putExtra("roomId", roomId);
                startActivity(intent);
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }

    private void loadRooms() {
        db.collection("rooms")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) return;
                if (value != null) {
                    roomList.clear();
                    roomList.addAll(value.getDocuments());
                    adapter.notifyDataSetChanged();
                }
            });
    }
}
