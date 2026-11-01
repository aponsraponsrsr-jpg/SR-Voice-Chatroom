package com.sr.voice.chat;

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

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(80, 80, 80, 80);
        root.setBackgroundColor(0xFF121212);

        TextView title = new TextView(this);
        title.setText("SR Voice");
        title.setTextSize(32f);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 80);
        root.addView(title);

        if(isLoggedIn){
            String gmail = prefs.getString("userGmail","user@gmail.com");
            TextView tv = new TextView(this);
            tv.setText("Welcome!\n\nPermanent ID:\n" + gmail + "\n\nLogin Saved!");
            tv.setTextSize(18f);
            tv.setTextColor(0xFFFFFFFF);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0,0,0,40);
            root.addView(tv);
            
            Button logout = new Button(this);
            logout.setText("Logout Test");
            logout.setOnClickListener(v->{
                prefs.edit().clear().apply();
                recreate();
            });
            root.addView(logout);
        } else {
            TextView sub = new TextView(this);
            sub.setText("Create your Permanent ID\nVideo like Gmail Login");
            sub.setTextSize(16f);
            sub.setTextColor(0xFFAAAAAA);
            sub.setGravity(Gravity.CENTER);
            sub.setPadding(0,0,0,50);
            root.addView(sub);

            Button btn = new Button(this);
            btn.setText("Continue with Gmail");
            btn.setOnClickListener(v->{
                prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("userGmail","myvoice@gmail.com")
                    .apply();
                recreate();
            });
            root.addView(btn);
        }
        
        setContentView(root);
    }
}
