package com.sr.voicechat;

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
        setContentView(R.layout.activity_login);
        
        mAuth = FirebaseAuth.getInstance();
        
        // Button clicks will be handled here - keeping old layout IDs
    }

    @Override
    protected void onStart() {
        super.onStart();
        // PERMANENT FIX - Auto Login Check - No More Repeated Login!
        FirebaseUser currentUser = mAuth.getCurrentUser();
        SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        boolean hasId = prefs.contains("MY_9_DIGIT_ID");

        if (currentUser != null) {
            // Already logged in - Check if new user or old user
            if (hasId) {
                // Old User - Direct Home - No Edit Profile
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            } else {
                // New User - Go to Edit Profile Once -> Lifetime ID
                // For now direct Home - We will add Edit Profile next
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            }
            finish();
        }
        // If not logged in, stay on login page
    }
}
