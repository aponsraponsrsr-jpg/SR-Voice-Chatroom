package com.sr.voicechat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    EditText etName;
    RadioGroup rgGender;
    Button btnSave;
    ImageView ivProfile; // ছবির জন্য
    FirebaseFirestore db;
    FirebaseAuth auth;
    String base64Image = ""; // Compressed ছবি এখানে থাকবে
    int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        etName = findViewById(R.id.etName);
        rgGender = findViewById(R.id.rgGender);
        btnSave = findViewById(R.id.btnSave);
        ivProfile = findViewById(R.id.ivProfile); // তোমার xml এ id টা ivProfile দিতে হবে
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ছবিতে ক্লিক করলে গ্যালারি খুলবে
        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if(name.isEmpty()){
                Toast.makeText(this,"Name লিখুন",Toast.LENGTH_SHORT).show();
                return;
            }
            String gender = "Male";
            int id = rgGender.getCheckedRadioButtonId();
            if(id == R.id.rbFemale) gender = "Female";

            String uid = auth.getCurrentUser().getUid();
            Map<String,Object> user = new HashMap<>();
            user.put("uid", uid);
            user.put("name", name);
            user.put("gender", gender);
            user.put("email", auth.getCurrentUser().getEmail());
            user.put("photoUrl", base64Image); // Compressed ছবি save হবে, ফাঁকা থাকবে না

            db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,"Profile Saved! Blaze ছাড়াই!",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            try {
                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // COMPRESS - এটাই তোমার Normal (Compressed) অপশন, 70% ছোট
                Bitmap resized = Bitmap.createScaledBitmap(original, 500, 500, true);
                ivProfile.setImageBitmap(resized);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 40, baos); // 40% quality
                byte[] bytes = baos.toByteArray();
                base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
