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

        // Grid 2 column
        recyclerRooms.setLayoutManager(new GridLayoutManager(this, 2));
        // আপাতত খালি, Room লোড করার Adapter পরে যোগ করবো, এখন Crash করবে না

        btnCreate.setOnClickListener(v -> showCreateRoomDialog());
        
        // Popular Button
        findViewById(R.id.btnPopular).setOnClickListener(v -> 
            Toast.makeText(this, "Popular Coming Soon", Toast.LENGTH_SHORT).show()
        );
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        
        EditText etRoomName = dialog.findViewById(R.id.et_room_name);
        Button btnCreateRoom = dialog.findViewById(R.id.btn_dialog_create);
        Button btnCancel = dialog.findViewById(R.id.btn_dialog_cancel);

        if (etRoomName == null || btnCreateRoom == null) {
            Toast.makeText(this, "dialog_create_room.xml ID ভুল আছে", Toast.LENGTH_LONG).show();
            return;
        }

        btnCreateRoom.setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString().trim();
            if (roomName.isEmpty()) {
                Toast.makeText(this, "Room name দিন", Toast.LENGTH_SHORT).show();
                return;
            }
            createRoom(roomName);
            dialog.dismiss();
        });

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    private void createRoom(String roomName) {
        String roomId = UUID.randomUUID().toString();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";

        Map<String, Object> room = new HashMap<>();
        room.put("roomId", roomId);
        room.put("roomName", roomName);
        room.put("ownerId", userId);
        room.put("createdAt", System.currentTimeMillis());

        db.collection("rooms").document(roomId).set(room)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Room Created: " + roomName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, VoiceRoomActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
    }
}
