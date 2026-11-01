package com.srvoice.chatroom;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SRVoicePrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if(isLoggedIn){
            // Show Home - Permanent ID Already Saved
            TextView tv = new TextView(this);
            tv.setText("SR Voice\n\nWelcome!\n\nPermanent ID:\n" + prefs.getString("userGmail","user@gmail.com") + "\n\nLogin Saved!");
            tv.setTextSize(20f);
            tv.setTextColor(0xFFFFFFFF);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(50,150,50,150);
            tv.setBackgroundColor(0xFF121212);
            setContentView(tv);
        } else {
            // First Time - Show Gmail Login Design
            setContentView(R.layout.activity_login);
            LinearLayout btnGmail = findViewById(R.id.btnGmailLogin);
            if(btnGmail != null){
                btnGmail.setOnClickListener(v -> {
                    prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("userGmail", "user@gmail.com")
                        .putString("userName", "SR Voice User")
                        .apply();
                    recreate();
                });
            }
        }
    }
}
