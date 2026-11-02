package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    Button btnGmail;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        btnGmail = findViewById(R.id.btnGmail);

        if(auth.getCurrentUser() != null){
            checkUserExists();
            return;
        }

        btnGmail.setOnClickListener(v -> {
            auth.signInAnonymously().addOnSuccessListener(r -> checkUserExists());
        });
    }

    private void checkUserExists(){
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if(doc.exists()){
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                startActivity(new Intent(this, EditProfileActivity.class));
            }
            finish();
        }).addOnFailureListener(e -> {
            // যদি internet না থাকে বা doc না পায়, তাও Edit এ নিয়ে যাবে
            startActivity(new Intent(this, EditProfileActivity.class));
            finish();
        });
    }
}
