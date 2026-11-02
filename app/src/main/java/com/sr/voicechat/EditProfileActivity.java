package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    EditText etName;
    RadioGroup rgGender;
    Button btnSave;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        etName = findViewById(R.id.etName);
        rgGender = findViewById(R.id.rgGender);
        btnSave = findViewById(R.id.btnSave);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

            db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
        });
    }
}
