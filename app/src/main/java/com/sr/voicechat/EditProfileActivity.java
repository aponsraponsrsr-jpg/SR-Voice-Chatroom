package com.srvoicechatroom; // তোমার package নামটা যদি অন্য হয়, এটা চেঞ্জ করো না, আগেরটাই রাখো

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.security.SecureRandom;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText etName, etBio;
    private TextView tvIdShow;
    private Uri selectedUri;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // তোমার xml এর নাম

        prefs = getSharedPreferences("SR_PREF", MODE_PRIVATE);

        imgProfile = findViewById(R.id.profileImage); // তোমার xml এ id টা profileImage / imgProfile / ivProfile হতে পারে
        if (imgProfile == null) imgProfile = findViewById(R.id.imgProfile);
        if (imgProfile == null) imgProfile = findViewById(R.id.ivProfile);
        if (imgProfile == null) imgProfile = findViewById(R.id.addProfileImage);

        etName = findViewById(R.id.etName);
        if (etName == null) etName = findViewById(R.id.nameEdit);
        if (etName == null) etName = findViewById(R.id.editName);

        etBio = findViewById(R.id.etBio);
        if (etBio == null) etBio = findViewById(R.id.bioEdit);
        if (etBio == null) etBio = findViewById(R.id.editBio);

        tvIdShow = findViewById(R.id.tvId);
        if (tvIdShow == null) tvIdShow = findViewById(R.id.idShowText);
        if (tvIdShow == null) tvIdShow = findViewById(R.id.txtId);

        Button btnConfirm = findViewById(R.id.btnConfirm);
        if (btnConfirm == null) btnConfirm = findViewById(R.id.confirmBtn);
        if (btnConfirm == null) btnConfirm = findViewById(R.id.btnSave);

        // 1. অটো 8 Digit Mix ID - যেমন 78294105, 77381902
        String myId = prefs.getString("my_id", null);
        if (myId == null) {
            myId = generateMixId();
            prefs.edit().putString("my_id", myId).apply();
        }
        if (tvIdShow != null) tvIdShow.setText("ID: " + myId);

        // আগের নাম থাকলে দেখাবে
        if (etName != null) etName.setText(prefs.getString("my_name", ""));
        if (etBio != null) etBio.setText(prefs.getString("my_bio", ""));

        // 2. ফটোতে ট্যাপ করলেই গ্যালারি খুলবে
        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> openGallery());
        }

        // 3. Confirm করলে Permanent Save - আর কখনো Edit করতে হবে না
        String finalMyId = myId;
        btnConfirm.setOnClickListener(v -> {
            String name = etName != null ? etName.getText().toString().trim() : "User";
            String bio = etBio != null ? etBio.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(this, "Name লিখো বন্ধু", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                .putString("my_name", name)
                .putString("my_bio", bio)
                .putString("my_id", finalMyId)
                .putString("my_image", selectedUri != null ? selectedUri.toString() : prefs.getString("my_image", ""))
                .putBoolean("profile_created", true)
                .apply();

            Toast.makeText(this, "Saved! ID: " + finalMyId, Toast.LENGTH_SHORT).show();
            // Home এ যাবে
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
                return;
            }
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK && data != null) {
            selectedUri = data.getData();
            if (imgProfile != null) imgProfile.setImageURI(selectedUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }

    private String generateMixId() {
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(1 + r.nextInt(9)); // প্রথম digit 0 না
        for (int i = 1; i < 8; i++) sb.append(r.nextInt(10));
        String id = sb.toString();
        // সব digit একই হলে আবার বানাবে - যেমন 77777777 হবে না
        if (id.matches("^(.)\\1+$")) return generateMixId();
        return id;
    }
}
