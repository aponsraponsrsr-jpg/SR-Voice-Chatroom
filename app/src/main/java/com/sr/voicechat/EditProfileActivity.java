package com.srvoicechatroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView imgProfile;
    private Uri selectedImageUri = null;
    private FirebaseFirestore db;

    // Gallery খোলার জন্য Launcher
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                selectedImageUri = uri;
                imgProfile.setImageURI(uri);
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        imgProfile = findViewById(R.id.imgProfile);
        EditText etName = findViewById(R.id.etName);
        RadioGroup rgGender = findViewById(R.id.rgGender);
        TextView tvId = findViewById(R.id.tvId);
        Button btnSave = findViewById(R.id.btnSave);

        // Random ID তৈরি, একবারই হবে
        String randomId = "ID: 999" + new Random().nextInt(900000);
        tvId.setText(randomId);

        // 1. Profile Pic এ Click করলে Gallery খুলবে
        findViewById(R.id.layoutProfilePic).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        // 2. Save করলে Permanent হয়ে যাবে
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String gender = rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "Male" : "Female";
            String userId = tvId.getText().toString();
            String firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // A. ফোনের ভিতরে Save (SharedPreferences) - যাতে App Delete করলেও মনে থাকে
            SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
            prefs.edit()
                .putBoolean("isProfileSaved", true)
                .putString("myName", name)
                .putString("myId", userId)
                .putString("myGender", gender)
                .putString("myPic", selectedImageUri != null ? selectedImageUri.toString() : "")
                .apply();

            // B. Firebase এ Save - যাতে সব ফোনে একই ID থাকে
            Map<String, Object> user = new HashMap<>();
            user.put("name", name);
            user.put("gender", gender);
            user.put("srId", userId);
            user.put("uid", firebaseUid);
            
            db.collection("users").document(firebaseUid).set(user);

            // C. সোজা Home Page এ যাবে
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}
