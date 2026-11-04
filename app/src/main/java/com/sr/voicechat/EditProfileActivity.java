package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private EditText editName;
    private TextView textUserId;
    private RadioGroup genderGroup;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private String imageUriString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        profileImage = findViewById(R.id.profileImage);
        editName = findViewById(R.id.editName);
        textUserId = findViewById(R.id.textUserId);
        genderGroup = findViewById(R.id.genderGroup);
        btnSave = findViewById(R.id.btnSave);

        // পার্মানেন্ট ইউনিক আইডি লোড করা
        String uniqueId = prefs.getString("MY_9_DIGIT_ID", "77788899");
        textUserId.setText("ID: " + uniqueId);

        // আগের সেভ করা ছবি বা নাম থাকলে তা লোড করে দেখানো
        String savedImage = prefs.getString("USER_IMAGE", "");
        if (!savedImage.isEmpty()) {
            imageUriString = savedImage;
            try {
                profileImage.setImageURI(Uri.parse(savedImage));
                profileImage.setPadding(0, 0, 0, 0); // ক্যামেরা আইকনের প্যাডিং রিমুভ করে ফুল ছবি সেট করা
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null) {
            editName.setText(mAuth.getCurrentUser().getDisplayName());
        }

        // প্রোফাইল ছবির ওপর ক্লিক করলে গ্যালারি ওপেন হবে
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            String gender = (selectedGenderId == R.id.radioFemale) ? "Female" : "Male";

            // লোকাল SharedPreferences-এ পার্মানেন্টলি সেভ করা
            prefs.edit()
                    .putString("USER_NAME", newName)
                    .putString("USER_GENDER", gender)
                    .putString("USER_IMAGE", imageUriString)
                    .putBoolean("PROFILE_COMPLETED", true)
                    .apply();

            if (mAuth.getCurrentUser() != null) {
                Map<String, Object> update = new HashMap<>();
                update.put("name", newName);
                update.put("gender", gender);
                update.put("uniqueId", uniqueId);
                update.put("profileImage", imageUriString);

                db.collection("users").document(mAuth.getCurrentUser().getUid())
                        .set(update, SetOptions.merge());
            }

            Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageUriString = imageUri.toString();
            try {
                profileImage.setImageURI(imageUri);
                profileImage.setPadding(0, 0, 0, 0); // গ্যালারি থেকে সিলেক্ট করা ছবি বক্সে পারফেক্ট দেখানোর জন্য
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
