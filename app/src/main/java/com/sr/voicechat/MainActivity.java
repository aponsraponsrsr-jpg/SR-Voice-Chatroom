package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        String savedName = prefs.getString("USER_NAME", null);

        // যদি ইউজার আগে থেকেই নাম এবং লগইন স্ট্যাটাস সেভ করে থাকে, সরাসরি HomeActivity-তে যাবে
        if (isLoggedIn && savedName != null && !savedName.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
