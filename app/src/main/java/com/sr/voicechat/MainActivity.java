package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Uri roomImageUri = null;
    private ImageView imgRoomPick;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        db = FirebaseFirestore.getInstance();

        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    roomImageUri = result.getData().getData();
                    if (imgRoomPick != null) {
                        imgRoomPick.setImageURI(roomImageUri);
                    }
                }
            }
        );

        Button btnOpenRoom = findViewById(R.id.btnOpenRoom);
        btnOpenRoom.setOnClickListener(v -> {
            showCreateRoomDialog();
        });
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        imgRoomPick = dialog.findViewById(R.id.imgRoomPick);
        EditText etName = dialog.findViewById(R.id.etRoomName);
        EditText etBio = dialog.findViewById(R.id.etRoomBio);
        Button btnSave = dialog.findViewById(R.id.btnSaveRoom);

        imgRoomPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Enter room name");
                return;
            }

            // Permanent ID Create
            String roomId = UUID.randomUUID().toString();
            String myId = IdManager.getUserId(this); // তোমার IdManager থেকে ID নেবে

            // RoomModel Save to Firebase
            RoomModel room = new RoomModel(roomId, name, bio, roomImageUri != null ? roomImageUri.toString() : "", myId);
            
            db.collection("rooms").document(roomId).set(room)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Room Created Permanently!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Intent roomIntent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                    roomIntent.putExtra("roomId", roomId);
                    roomIntent.putExtra("roomName", name);
                    startActivity(roomIntent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        dialog.show();
    }
}
