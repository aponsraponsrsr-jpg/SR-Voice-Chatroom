package com.sr.voicechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {
    ImageView profileImage;
    EditText nameEdit, bioEdit;
    RadioGroup genderGroup;
    TextView idShowText;
    Button confirmBtn;
    Uri imageUri;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    String myPermanentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImage = findViewById(R.id.profileImage);
        nameEdit = findViewById(R.id.nameEdit);
        bioEdit = findViewById(R.id.bioEdit);
        genderGroup = findViewById(R.id.genderGroup);
        idShowText = findViewById(R.id.idShowText);
        confirmBtn = findViewById(R.id.confirmBtn);

        // Permanent 9 Digit Mix ID Generate - একবারই হবে
        myPermanentId = generatePermanentId();
        idShowText.setText("Your Permanent ID: " + myPermanentId);

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        confirmBtn.setOnClickListener(v -> savePermanentProfile());
    }

    private String generatePermanentId() {
        Random rand = new Random();
        // 9 Digit Mix ID - 100000000 to 999999999
        int id = 100000000 + rand.nextInt(900000000);
        return String.valueOf(id);
    }

    private void savePermanentProfile() {
        String name = nameEdit.getText().toString().trim();
        String bio = bioEdit.getText().toString().trim();
        int selectedId = genderGroup.getCheckedRadioButtonId();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedId == -1) {
            Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show();
            return;
        }
        
        RadioButton radio = findViewById(selectedId);
        String gender = radio.getText().toString();
        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        confirmBtn.setText("Saving Permanently...");
        confirmBtn.setEnabled(false);

        // যদি ছবি থাকে তাহলে Storage এ Upload করবে
        if (imageUri != null) {
            storage.getReference().child("profile_images/" + uid + ".jpg")
                    .putFile(imageUri)
                    .addOnSuccessListener(task -> task.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> saveToFirestore(uid, name, bio, gender, email, uri.toString())))
                    .addOnFailureListener(e -> saveToFirestore(uid, name, bio, gender, email, ""));
        } else {
            saveToFirestore(uid, name, bio, gender, email, "");
        }
    }

    private void saveToFirestore(String uid, String name, String bio, String gender, String email, String photoUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("email", email);
        user.put("name", name);
        user.put("bio", bio);
        user.put("gender", gender);
        user.put("my_id", myPermanentId); // PERMANENT ID - কখনো চেঞ্জ হবে না
        user.put("photoUrl", photoUrl);
        user.put("coins", 0);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Saved Permanently!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    confirmBtn.setText("Confirm & Go");
                    confirmBtn.setEnabled(true);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}
