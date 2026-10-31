package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Uri profileImageUri = null;
    private Uri roomImageUri = null;
    private ImageView imgProfile, imgRoomPick;
    private EditText etName;
    private Button btnSaveEnter;
    private RadioButton rbMale, rbFemale;
    private FirebaseFirestore db;
    
    private ActivityResultLauncher<Intent> pickProfileImageLauncher;
    private ActivityResultLauncher<Intent> pickRoomImageLauncher;
    private boolean isPickingProfile = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        db = FirebaseFirestore.getInstance();

        // Check if already setup - then go to Home
        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
        // যদি আগে Save করা থাকে, তাও এই Page দেখাবো, Skip করবো না তোমার Testing এর জন্য

        // Launchers
        pickProfileImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageUri = result.getData().getData();
                    if (imgProfile != null) {
                        imgProfile.setImageURI(profileImageUri);
                    }
                }
            }
        );

        pickRoomImageLauncher = registerForActivityResult(
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

        // Find Views Dynamically - ID যাই হোক কাজ করবে
        imgProfile = findViewById(R.id.imgProfile);
        if (imgProfile == null) imgProfile = findViewById(R.id.imgRoomPick);
        if (imgProfile == null) {
             int id = getResources().getIdentifier("profileImage", "id", getPackageName());
             if(id!=0) imgProfile = findViewById(id);
        }
        // activity_main এর camera icon টা
        if (imgProfile == null) {
            // Try common image id from your layout
            int camId = getResources().getIdentifier("imgPick", "id", getPackageName());
            if(camId!=0) imgProfile = findViewById(camId);
        }

        etName = findViewById(R.id.etName);
        if (etName == null) etName = findViewById(R.id.etUserName);
        if (etName == null) {
            int id = getResources().getIdentifier("etRoomName", "id", getPackageName());
            if(id!=0) etName = findViewById(id);
        }

        btnSaveEnter = findViewById(R.id.btnSave);
        if (btnSaveEnter == null) btnSaveEnter = findViewById(R.id.btnSaveAndEnter);
        if (btnSaveEnter == null) btnSaveEnter = findViewById(R.id.btnSaveEnterChatroom);
        if (btnSaveEnter == null) btnSaveEnter = findViewById(R.id.btnEnter);
        if (btnSaveEnter == null) {
            int id = getResources().getIdentifier("btnSave", "id", getPackageName());
            if(id!=0) btnSaveEnter = findViewById(id);
        }
        // Last try: Find button by layout - if still null, use btnOpenRoom id if your layout still has it
        if (btnSaveEnter == null) {
            int id = getResources().getIdentifier("btnOpenRoom", "id", getPackageName());
            if(id!=0) btnSaveEnter = findViewById(id);
        }

        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        if(rbMale == null){
            int id = getResources().getIdentifier("male", "id", getPackageName());
            if(id!=0) rbMale = findViewById(id);
        }

        // Profile Image Click -> Gallery Open
        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickProfileImageLauncher.launch(intent);
            });
        }

        // Save & Enter Chatroom Click
        if (btnSaveEnter != null) {
            btnSaveEnter.setOnClickListener(v -> {
                String name = "real CEO";
                if (etName != null) {
                    name = etName.getText().toString().trim();
                }
                if (name.isEmpty()) {
                    if(etName!=null) etName.setError("Enter name");
                    Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
                    return;
                }

                String gender = "Male";
                if (rbFemale != null && rbFemale.isChecked()) gender = "Female";

                // Save Locally
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("userName", name);
                editor.putString("userGender", gender);
                if (profileImageUri != null) editor.putString("userImage", profileImageUri.toString());
                editor.apply();

                // Save to Firebase (Optional but good for CEO)
                String myId = IdManager.getOrCreateUserId(this);
                // Save user profile permanently
                UserModel user = new UserModel(myId, name, gender, profileImageUri != null ? profileImageUri.toString() : "");
                db.collection("users").document(myId).set(user);

                Toast.makeText(this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();

                // Go to Home / Voice Room List
                try {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    // If HomeActivity not found, show Create Room Dialog
                    showCreateRoomDialog();
                }
            });
        }
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        imgRoomPick = dialog.findViewById(R.id.imgRoomPick);
        EditText etRoomName = dialog.findViewById(R.id.etRoomName);
        EditText etBio = dialog.findViewById(R.id.etRoomBio);
        Button btnSaveRoom = dialog.findViewById(R.id.btnSaveRoom);

        if(imgRoomPick != null){
            imgRoomPick.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickRoomImageLauncher.launch(intent);
            });
        }

        if(btnSaveRoom != null){
            btnSaveRoom.setOnClickListener(v -> {
                String name = etRoomName.getText().toString().trim();
                String bio = etBio.getText().toString().trim();

                if (name.isEmpty()) {
                    etRoomName.setError("Enter room name");
                    return;
                }

                String roomId = UUID.randomUUID().toString();
                String myId = IdManager.getOrCreateUserId(this);

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
        }

        dialog.show();
    }
}
