package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("SR_APP", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && isLoggedIn) {
            // আগে সেভ করা আছে, সোজা Home এ যাবে
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            // নতুন ইউজার
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
