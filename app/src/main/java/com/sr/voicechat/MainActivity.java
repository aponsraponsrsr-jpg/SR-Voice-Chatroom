package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // এটা এখন কিছু করবে না, সোজা Splash এ পাঠাবে
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }
}
