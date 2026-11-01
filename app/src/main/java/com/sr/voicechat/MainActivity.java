package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
        
        // আগে Login থাকলে Direct Home
        if (!prefs.getString("userName", "").isEmpty()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        // Google Button খোঁজো - ID যাই হোক পেয়ে যাবে
        int btnId = getResources().getIdentifier("btnGoogle", "id", getPackageName());
        if (btnId == 0) btnId = getResources().getIdentifier("sign_in_button", "id", getPackageName());
        if (btnId == 0) btnId = getResources().getIdentifier("btnSignInWithGoogle", "id", getPackageName());
        if (btnId == 0) btnId = getResources().getIdentifier("btnLogin", "id", getPackageName());

        if (btnId != 0) {
            Button btn = findViewById(btnId);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    // ID বানাও - IdManager না থাকলেও চলবে
                    String uid = "SR_" + System.currentTimeMillis();
                    try {
                        uid = IdManager.getOrCreateUserId(this);
                    } catch (Exception e) {}

                    prefs.edit()
                        .putString("userName", "SR User")
                        .putString("userId", uid)
                        .apply();

                    Toast.makeText(this, "Welcome! Entry Success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
            }
        }
    }
}
