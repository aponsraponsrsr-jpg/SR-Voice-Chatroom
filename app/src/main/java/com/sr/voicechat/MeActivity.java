package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
                        // Auto save new image
                        saveProfile();
                    }
                }
            }
        );

        // Find Views - সব ID Try করবে, Fail হবে না
        imgProfile = findView(R.id.imgProfile, "ivProfile", "profile_image", "imgAvatar", "imageView");
        tvName = findView(R.id.tvName, "tvUserName", "etName", "name");
        tvId = findView(R.id.tvId, "tvUserId", "idView");
        
        btnEdit = findView(R.id.btnEdit, "btnEditProfile", "btnChange");
        btnSave = findView(R.id.btnSave, "btnSaveProfile", "btnUpdate");
        btnBack = findView(R.id.btnBack, "btnGoHome", "ivBack", "btnBackHome");
        btnLogout = findView(R.id.btnLogout, "btnLogOut", "btnSignOut");

        loadProfile();

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(intent);
            });
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                if (imgProfile != null) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickImageLauncher.launch(intent);
                }
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                // Go to Home
                try {
                    Intent intent = new Intent(MeActivity.this, HomeActivity.class);
                    startActivity(intent);
                } catch (Exception e) {}
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                prefs.edit().clear().apply();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
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
            try {
                imgProfile.setImageURI(Uri.parse(image));
            } catch (Exception e) {}
        }

        // Also load from Firebase for update
        db.collection("users").document(myId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists() && tvName != null) {
                    String fbName = doc.getString("name");
                    if (fbName != null) {
                        tvName.setText(fbName);
                        prefs.edit().putString("userName", fbName).apply();
                    }
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
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                // If doc not exists, create it
                map.put("id", myId);
                db.collection("users").document(myId).set(map);
                Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
            });
    }

    // Safe View Finder - ID না থাকলেও Crash করবে না
    private <T> T findView(int primaryId, String... altNames) {
        T view = findViewById(primaryId);
        if (view != null) return view;
        for (String name : altNames) {
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id != 0) {
                view = findViewById(id);
                if (view != null) return view;
            }
        }
        return null;
    }
}
