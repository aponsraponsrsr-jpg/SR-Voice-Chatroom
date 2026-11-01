package com.srvoice.chatroom;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SRVoicePrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if(isLoggedIn){
            // Already have Permanent ID - Go to Home
            // For now show simple home, next file we will load real home
            setContentView(R.layout.activity_main);
            // If bottom_menu missing, fallback to simple text
            try {
                // Check if bottom menu exists
                findViewById(R.id.bottomNavigation);
            } catch (Exception e){
                // Fallback safe
                showSimpleHome();
            }
        } else {
            // First Time - Show our new Gmail Login Design
            setContentView(R.layout.activity_login);
            LinearLayout btnGmail = findViewById(R.id.btnGmailLogin);
            btnGmail.setOnClickListener(v -> {
                // This will go to Profile Edit in next file
                // For now save temporary Gmail and go to home
                prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("userGmail", "user@gmail.com")
                    .putString("userName", "SR Voice User")
                    .apply();
                recreate(); // Reload to show home
            });
        }
    }
    
    void showSimpleHome(){
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("SR Voice - Welcome!\n\nPermanent ID Saved!\n" + prefs.getString("userGmail",""));
        tv.setTextSize(20f);
        tv.setTextColor(0xFFFFFFFF);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(50,100,50,100);
        tv.setBackgroundColor(0xFF121212);
        setContentView(tv);
    }
}
