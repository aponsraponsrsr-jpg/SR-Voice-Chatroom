package com.srvoice.chatroom;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
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
            // Permanent ID - Direct Home
            showHomeScreen();
        } else {
            // First Time - Show Gmail Login
            showLoginScreen();
        }
    }
    
    void showLoginScreen(){
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50,100,50,100);
        layout.setBackgroundColor(0xFF121212);
        
        TextView title = new TextView(this);
        title.setText("SR Voice");
        title.setTextSize(28f);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0,0,0,50);
        
        TextView sub = new TextView(this);
        sub.setText("Continue with Gmail to create Permanent ID");
        sub.setTextSize(16f);
        sub.setTextColor(0xFFBBBBBB);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0,0,0,100);
        
        Button btn = new Button(this);
        btn.setText("Continue with Gmail");
        btn.setTextSize(18f);
        
        btn.setOnClickListener(v -> {
            // Temporary: Save as logged in and go to profile edit
            // Next file we will add real Gmail picker
            prefs.edit().putBoolean("isLoggedIn", true).putString("userName", "SR Voice User").apply();
            showHomeScreen();
        });
        
        layout.addView(title);
        layout.addView(sub);
        layout.addView(btn);
        setContentView(layout);
    }
    
    void showHomeScreen(){
        String name = prefs.getString("userName", "User");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFF121212);
        
        TextView tv = new TextView(this);
        tv.setText("Welcome " + name + "\n\nPermanent ID Saved!\n\nNext time no login needed\nDirect Home");
        tv.setTextSize(20f);
        tv.setTextColor(0xFFFFFFFF);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(50,100,50,100);
        
        Button logout = new Button(this);
        logout.setText("Logout (For Testing)");
        logout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            showLoginScreen();
        });
        
        layout.addView(tv);
        layout.addView(logout);
        setContentView(layout);
    }
}
