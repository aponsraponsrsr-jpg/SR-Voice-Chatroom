package com.sr.voicechat;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_room);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        EditText etRoomName = dialog.findViewById(R.id.etRoomName);
        EditText etRoomBio = dialog.findViewById(R.id.etRoomBio);
        RadioGroup rgGender = dialog.findViewById(R.id.rgGender);
        Button btnSaveRoom = dialog.findViewById(R.id.btnSaveRoom);
        ImageView ivProfile = dialog.findViewById(R.id.ivProfile);
        
        if(mAuth.getCurrentUser() == null){
            Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        btnSaveRoom.setOnClickListener(v -> {
            String name = etRoomName.getText().toString().trim();
            String bio = etRoomBio.getText().toString().trim();
            int genderId = rgGender.getCheckedRadioButtonId();
            if(name.isEmpty()){ etRoomName.setError("Required"); return; }
            if(genderId==-1){ Toast.makeText(this, "Gender is Mandatory", Toast.LENGTH_SHORT).show(); return; }
            if(bio.isEmpty()){ etRoomBio.setError("Announcement Required"); return; }
            String gender = genderId==R.id.rbMale?"Male":"Female";
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", uid);
            map.put("roomName", name);
            map.put("roomBio", bio);
            map.put("roomNotice", bio);
            map.put("gender", gender);
            map.put("ownerId", uid);
            map.put("roomImage", "");
            map.put("timestamp", System.currentTimeMillis());
            db.collection("rooms").document(uid).set(map).addOnSuccessListener(a->{
                Toast.makeText(this, "Saved Permanently!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }
}
