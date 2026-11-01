package com.srvoice.chatroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("SRVoicePrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Permanent ID আছে - সরাসরি Home (আপাতত Profile এ যাবে, পরে Home বানাবো)
            startActivity(new Intent(this, ProfileSetupActivity.class));
        } else {
            // নতুন ইউজার - Login এ যাবে
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
