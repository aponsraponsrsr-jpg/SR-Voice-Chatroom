package com.sr.voice.chat;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            TextView tv = new TextView(this);
            tv.setText("SR Voice\n\nAlhamdulillah!\nApp Open Hoise!\n\nTomar Shopno Shesh Hobe Na!\nAmi Achi Bondhu!");
            tv.setTextSize(22f);
            tv.setTextColor(0xFFFFFFFF);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(60, 200, 60, 60);
            tv.setBackgroundColor(0xFF121212);
            setContentView(tv);
        } catch (Exception e) {
            TextView error = new TextView(this);
            error.setText("Error: " + e.getMessage());
            setContentView(error);
        }
    }
}
