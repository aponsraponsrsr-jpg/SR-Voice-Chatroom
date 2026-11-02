package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerRooms;
    private TextView btnCreate;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerRooms = findViewById(R.id.recyclerRooms);
        btnCreate = findViewById(R.id.btnCreate);

        recyclerRooms.setLayoutManager(new GridLayoutManager(this, 2));

        btnCreate.setOnClickListener(v -> showCreateRoomDialog());
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        
        // তোমার XML অনুযায়ী ID
        EditText etRoomName = dialog.findViewById(R.id.etRoomName);
        EditText etRoomBio = dialog.findViewById(R.id.etRoomBio);
        Button btnSaveRoom = dialog.findViewById(R.id.btnSaveRoom);

        btnSaveRoom.setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString().trim();
            String roomBio = etRoomBio.getText().toString().trim();
            
            if (roomName.isEmpty()) {
                Toast.makeText(this, "Room Name দিন", Toast.LENGTH_SHORT).show();
                return;
            }
            createRoom(roomName, roomBio);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createRoom(String roomName, String roomBio) {
        String roomId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";

        Map<String, Object> room = new HashMap<>();
        room.put("roomId", roomId);
        room.put("roomName", roomName);
        room.put("roomBio", roomBio);
        room.put("ownerId", userId);
        room.put("createdAt", System.currentTimeMillis());

        db.collection("rooms").document(roomId).set(room)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Room Created!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, VoiceRoomActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }
}
