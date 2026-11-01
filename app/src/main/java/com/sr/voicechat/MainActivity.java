package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // প্রথমে চেক করা ইউজার প্রোফাইল সেটআপ করেছে কি না
        SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isComplete = pref.getBoolean("isProfileSetupComplete", false);

        if (!isComplete) {
            // যদি প্রোফাইল সেটআপ করা না থাকে, তবে শুধু প্রথমবার প্রোফাইল সেটআপ পেজে পাঠাবে
            Intent intent = new Intent(MainActivity.this, ProfileSetupActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // যদি অলরেডি সেটআপ করা থাকে, তবে সরাসরি হোম পেজ লোড হবে
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        
        // ডিফল্টভাবে হোম ফ্র্যাগমেন্ট লোড করা
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_explore) {
                selectedFragment = new ExploreFragment();
            } else if (itemId == R.id.nav_message) {
                selectedFragment = new MessageFragment();
            } else if (itemId == R.id.nav_me) {
                selectedFragment = new MeFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
            }
            return true;
        });
    }
}
