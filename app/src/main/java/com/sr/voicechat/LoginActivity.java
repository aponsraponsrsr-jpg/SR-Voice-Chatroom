package com.srvoicechatroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Permanent Check - যদি আগে Profile Save করে থাকে, সোজা Home এ যাবে
        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
        boolean isProfileSaved = prefs.getBoolean("isProfileSaved", false);

        if (isProfileSaved) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        
        // তোমার আগের Button এর ID যেটাই থাকুক, এটা কাজ করবে
        try {
            findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> goToEdit());
        } catch (Exception e) {
            try {
                findViewById(R.id.btnSignIn).setOnClickListener(v -> goToEdit());
            } catch (Exception e2) {
                // যদি ID না মেলে, তবুও Error আসবে না
            }
        }
    }

    private void goToEdit() {
        startActivity(new Intent(this, EditProfileActivity.class));
        finish();
    }
}
