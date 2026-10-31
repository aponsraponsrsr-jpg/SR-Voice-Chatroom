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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Uri roomImageUri = null;
    private ImageView imgRoomPick;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Gallery Launcher
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

        // তোমার আগের Room Button - এটাতে ক্লিক করলেই Dialog আসবে
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

            dialog.dismiss();
            
            // 10 Seat Room Activity তে নিয়ে যাবে
            Intent roomIntent = new Intent(MainActivity.this, VoiceRoomActivity.class);
            roomIntent.putExtra("roomName", name);
            roomIntent.putExtra("roomBio", bio);
            roomIntent.putExtra("roomImage", roomImageUri != null ? roomImageUri.toString() : "");
            startActivity(roomIntent);
        });

        dialog.show();
    }
}
