package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private TextView tvName, tvId;
    private Button btnEdit, btnSave, btnBack, btnLogout;
    private Uri newImageUri = null;
    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);

        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    newImageUri = result.getData().getData();
                    if (imgProfile != null) {
                        imgProfile.setImageURI(newImageUri);
                        saveProfile();
                    }
                }
            }
        );

        // Direct IDs - No Generic Error
        try { imgProfile = findViewById(R.id.imgProfile); } catch(Exception e){}
        if(imgProfile==null) imgProfile = getImageViewByName("ivProfile", "profile_image", "imgAvatar", "imageView", "imgPick");
        
        try { tvName = findViewById(R.id.tvName); } catch(Exception e){}
        if(tvName==null) tvName = getTextViewByName("tvUserName", "name", "etName");
        
        try { tvId = findViewById(R.id.tvId); } catch(Exception e){}
        if(tvId==null) tvId = getTextViewByName("tvUserId", "idView");
        
        try { btnEdit = findViewById(R.id.btnEdit); } catch(Exception e){}
        try { btnSave = findViewById(R.id.btnSave); } catch(Exception e){}
        try { btnBack = findViewById(R.id.btnBack); } catch(Exception e){}
        try { btnLogout = findViewById(R.id.btnLogout); } catch(Exception e){}

        loadProfile();

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            });
        }
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            });
        }
        if (btnSave != null) btnSave.setOnClickListener(v -> saveProfile());
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                prefs.edit().clear().apply();
                Intent intent = new Intent(MeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadProfile() {
        String name = prefs.getString("userName", "real CEO");
        String myId = IdManager.getOrCreateUserId(this);
        String image = prefs.getString("userImage", "");
        if (tvName != null) tvName.setText(name);
        if (tvId != null) tvId.setText("ID: " + myId);
        if (imgProfile != null && !image.isEmpty()) {
            try { imgProfile.setImageURI(Uri.parse(image)); } catch (Exception e) {}
        }
        db.collection("users").document(myId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists() && tvName != null) {
                    String fbName = doc.getString("name");
                    if (fbName != null) tvName.setText(fbName);
                }
            });
    }

    private void saveProfile() {
        String myId = IdManager.getOrCreateUserId(this);
        String currentName = tvName != null ? tvName.getText().toString() : prefs.getString("userName", "real CEO");
        Map<String, Object> map = new HashMap<>();
        map.put("name", currentName);
        if (newImageUri != null) {
            map.put("image", newImageUri.toString());
            prefs.edit().putString("userImage", newImageUri.toString()).apply();
        }
        db.collection("users").document(myId).update(map)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> {
                map.put("id", myId);
                db.collection("users").document(myId).set(map);
                Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
            });
    }

    private ImageView getImageViewByName(String... names) {
        for (String n : names) {
            int id = getResources().getIdentifier(n, "id", getPackageName());
            if (id != 0) {
                View v = findViewById(id);
                if (v instanceof ImageView) return (ImageView) v;
            }
        }
        return null;
    }
    private TextView getTextViewByName(String... names) {
        for (String n : names) {
            int id = getResources().getIdentifier(n, "id", getPackageName());
            if (id != 0) {
                View v = findViewById(id);
                if (v instanceof TextView) return (TextView) v;
            }
        }
        return null;
    }
}
