package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button btnGoogleLogin;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 🌟 অটো-লগইন চেক: অলরেডি লগইন করা থাকলে জিমেইল স্ক্রিন স্কিপ করে সরাসরি MainActivity-তে চলে যাবে
        if (currentUser != null && isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> {
                // সফল লগইনের পর সেশন সেভ করে MainActivity তে পাঠানো হবে
                prefs.edit().putBoolean("IS_LOGGED_IN", true).apply();
                
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
