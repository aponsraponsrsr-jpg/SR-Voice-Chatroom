package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            // Firebase init failed, ignore
        }

        SharedPreferences prefs = getSharedPreferences("SR_APP", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null && isLoggedIn) {
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        } catch (Exception e) {
            // যদি Firebase এ সমস্যা থাকে, তাও Login এ যাবে, Crash করবে না
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
