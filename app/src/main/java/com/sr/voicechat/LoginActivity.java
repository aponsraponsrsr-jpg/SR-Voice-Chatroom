package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // সাময়িকভাবে টোস্ট মেসেজ দিয়ে রাখা হলো যাতে ক্র্যাশ না করে
        Toast.makeText(LoginActivity.this, "Login Activity Loaded", Toast.LENGTH_SHORT).show();
    }
}
