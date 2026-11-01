package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Button btnGoogleSignIn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ফায়ারবেস অথ ইনিশিয়ালাইজ
        mAuth = FirebaseAuth.getInstance();

        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                Toast.makeText(LoginActivity.this, "Sign in clicked successfully!", Toast.LENGTH_SHORT).show();
                
                // সফল ক্লিকের পর পরবর্তী পেজে যাওয়ার জন্য নিচের কোডটি ব্যবহার করতে পারো:
                // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // startActivity(intent);
                // finish();
            });
        }
    }
}
