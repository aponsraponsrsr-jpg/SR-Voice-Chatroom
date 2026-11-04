package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button btnHomeTab, btnMomentTab, btnCreateRoomPlus, btnMeTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // নিচের নেভিগেশন বার বা বাটনগুলো ইনিশিয়ালাইজেশন
        btnHomeTab = findViewById(R.id.btnHomeTab);
        btnMomentTab = findViewById(R.id.btnMomentTab);
        btnCreateRoomPlus = findViewById(R.id.btnCreateRoomPlus); // মাঝখানের প্লাস (+) আইকন
        btnMeTab = findViewById(R.id.btnMeTab);

        // ১. Home ট্যাব ক্লিক
        if (btnHomeTab != null) {
            btnHomeTab.setOnClickListener(v -> 
                Toast.makeText(this, "Welcome to Home Feed", Toast.LENGTH_SHORT).show()
            );
        }

        // ২. Moment ট্যাব ক্লিক
        if (btnMomentTab != null) {
            btnMomentTab.setOnClickListener(v -> 
                Toast.makeText(this, "Opening Moments...", Toast.LENGTH_SHORT).show()
            );
        }

        // ৩. মাঝখানের প্লাস (+) আইকন ক্লিক করলেই সরাসরি ১০টি সিটের ভয়েস রুম ওপেন হবে
        if (btnCreateRoomPlus != null) {
            btnCreateRoomPlus.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, VoiceRoomActivity.class);
                startActivity(intent);
            });
        }

        // ৪. Me ট্যাব (প্রোফাইল) ক্লিক
        if (btnMeTab != null) {
            btnMeTab.setOnClickListener(v -> 
                Toast.makeText(this, "Opening My Profile...", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
