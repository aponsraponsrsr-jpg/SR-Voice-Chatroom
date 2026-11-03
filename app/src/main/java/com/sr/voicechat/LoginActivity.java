package com.srvoicechatroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
        boolean isProfileSaved = prefs.getBoolean("isProfileSaved", false);

        // Permanent System
        if (currentUser != null && isProfileSaved) {
            // পুরাতন User - সোজা Home এ যাবে
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        } else if (currentUser != null) {
            // Gmail Login আছে কিন্তু Profile Save করে নাই
            startActivity(new Intent(this, EditProfileActivity.class));
            finish();
            return;
        }

        // নতুন User - Login Page দেখবে
        setContentView(R.layout.activity_login);
        
        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> {
            // তোমার আগের Google Sign In কোড এখানে থাকবে
            // Sign In Success হলে EditProfile এ যাবে
            startActivity(new Intent(this, EditProfileActivity.class));
            finish();
        });
    }
}
