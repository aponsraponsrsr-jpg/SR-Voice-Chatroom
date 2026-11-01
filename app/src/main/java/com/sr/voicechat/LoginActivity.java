package com.srvoice.chatroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> {
            // Permanent 8 Digit ID Generate
            String permanentId = "SR-" + (10000000 + new Random().nextInt(90000000));
            
            // Save Permanent ID
            SharedPreferences prefs = getSharedPreferences("SRVoicePrefs", MODE_PRIVATE);
            prefs.edit().putString("permanent_id", permanentId).putBoolean("is_first_login", true).apply();

            Toast.makeText(this, "ID Created: " + permanentId, Toast.LENGTH_SHORT).show();

            // Go to Profile Setup
            Intent intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
