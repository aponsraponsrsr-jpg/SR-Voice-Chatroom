package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ১. চেক করা ইউজার কি আগে থেকেই লগইন বা প্রোফাইল সেটআপ করেছে কি না
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);

        if (isLoggedIn) {
            // যদি ইতিমধ্যেই লগইন করা থাকে, তবে সরাসরি HomeActivity-তে চলে যাবে
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        Button btnLoginDirect = findViewById(R.id.btnLoginDirect);
        if (btnLoginDirect != null) {
            btnLoginDirect.setOnClickListener(v -> {
                // প্রথমবারের মতো লগইন করে ProfileActivity-তে যাবে
                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
