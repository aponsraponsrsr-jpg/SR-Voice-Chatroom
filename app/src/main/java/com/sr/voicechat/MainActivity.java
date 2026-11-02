package com.sr.voicechat;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements RoomAdapter.OnRoomClick {

    private RecyclerView recyclerRooms;
    private RoomAdapter roomAdapter;
    private List<RoomModel> roomList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        recyclerRooms = findViewById(R.id.recyclerRooms);
        fabCreate = findViewById(R.id.fabCreateRoom);
        
        recyclerRooms.setLayoutManager(new LinearLayoutManager(this));
        roomAdapter = new RoomAdapter(roomList, this);
        recyclerRooms.setAdapter(roomAdapter);

        fabCreate.setOnClickListener(v -> showCreateRoomDialog());

        loadRooms();
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etRoomName = dialog.findViewById(R.id.etRoomName);
        EditText etRoomBio = dialog.findViewById(R.id.etRoomBio);
        Button btnSaveRoom = dialog.findViewById(R.id.btnSaveRoom);
        ImageView ivRoomImage = dialog.findViewById(R.id.ivRoomImage);

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("rooms").document(uid).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                etRoomName.setText(doc.getString("roomName"));
                // ২ টা ফিল্ডই চেক করবে যাতে আগের ডাটা মিস না হয়
                if(doc.contains("roomBio")){
                    etRoomBio.setText(doc.getString("roomBio"));
                } else if(doc.contains("roomNotice")){
                    etRoomBio.setText(doc.getString("roomNotice"));
                }
            }
        });

        btnSaveRoom.setOnClickListener(v -> {
            String roomName = etRoomName.getText().toString().trim();
            String roomBio = etRoomBio.getText().toString().trim();

            if(roomName.isEmpty()){
                etRoomName.setError("Group Name দাও ভাইজান");
                return;
            }

            Map<String, Object> roomMap = new HashMap<>();
            roomMap.put("roomId", uid);
            roomMap.put("roomName", roomName);
            roomMap.put("roomBio", roomBio);
            roomMap.put("roomNotice", roomBio); // ২ নামেই সেভ যাতে কোথাও Error না হয়
            roomMap.put("ownerId", uid);
            roomMap.put("roomImage", ""); 
            roomMap.put("timestamp", System.currentTimeMillis());

            db.collection("rooms").document(uid).set(roomMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Room Permanent Save হয়ে গেছে!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        dialog.show();
    }

    private void loadRooms() {
        db.collection("rooms").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Load Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
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
