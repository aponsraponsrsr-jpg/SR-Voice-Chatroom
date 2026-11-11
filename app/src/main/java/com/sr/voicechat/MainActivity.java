package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private LinearLayout tabHome, tabMoments, tabMessage, tabMe;
    private ImageView btnCreateRoomPlus;
    private SharedPreferences prefs;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 🌟 অটো-লগইন ও সেশন সিকিউরিটি চেক
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null || !isLoggedIn) {
            // যদি লগইন করা না থাকে, তবে সোজা LoginActivity তে পাঠিয়ে দেব
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // পার্মানেন্ট ৯-ডিজিট আইডি নিশ্চিত করা
        currentUserId = prefs.getString("MY_9_DIGIT_ID", "");
        if (currentUserId.isEmpty()) {
            int r = (int) (Math.random() * 900000000) + 100000000;
            currentUserId = String.valueOf(r);
            prefs.edit().putString("MY_9_DIGIT_ID", currentUserId).apply();
        }

        tabHome = findViewById(R.id.tabHome);
        tabMoments = findViewById(R.id.tabMoments);
        tabMessage = findViewById(R.id.tabMessage);
        tabMe = findViewById(R.id.tabMe);
        btnCreateRoomPlus = findViewById(R.id.btnCreateRoomPlus);

        // উপরে ছোট প্লাস (+) আইকনে ক্লিক করলে নিজের ভয়েস রুমে এন্ট্রি হবে
        if (btnCreateRoomPlus != null) {
            btnCreateRoomPlus.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, VoiceRoomActivity.class);
                intent.putExtra("ROOM_OWNER_ID", currentUserId);
                startActivity(intent);
            });
        }

        // ট্যাব ক্লিক হ্যান্ডলার
        if (tabHome != null) {
            tabHome.setOnClickListener(v -> {
                // Home ফ্রেম বা রুম লিস্ট অ্যাক্টিভিটি
                Toast.makeText(this, "Home Tab Active", Toast.LENGTH_SHORT).show();
            });
        }

        if (tabMoments != null) {
            tabMoments.setOnClickListener(v -> {
                Toast.makeText(this, "Moments Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (tabMessage != null) {
            tabMessage.setOnClickListener(v -> {
                Toast.makeText(this, "Messages Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (tabMe != null) {
            tabMe.setOnClickListener(v -> {
                // 'Me' ট্যাবে ক্লিক করলে MeActivity তে যাবে
                startActivity(new Intent(MainActivity.this, MeActivity.class));
            });
        }
    }
}
