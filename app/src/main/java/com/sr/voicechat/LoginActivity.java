package com.sr.voicechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnLogin;
    private SharedPreferences prefs;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        inputEmail = findViewById(R.id.inputEmail); // জিমেইল ইনপুট ফিল্ড (অথবা Google Sign-in বাটন)
        btnLogin = findViewById(R.id.btnLogin);

        // যদি ইউজার আগে থেকেই লগইন করা থাকে, তবে সরাসরি হোমপেজে পাঠিয়ে দিবে
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        if (isLoggedIn) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim().toLowerCase();
            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Please enter a valid Gmail address!", Toast.LENGTH_SHORT).show();
                return;
            }

            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            // চেক করা যাক এই ডিভাইসে ইতিমধ্যে অন্য কোনো অ্যাকাউন্ট বা জিমেইল রেজিস্টার্ড আছে কি না
            DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("device_bindings").child(deviceId);
            deviceRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // এই ডিভাইসে পূর্বে অ্যাকাউন্ট খোলা হয়েছে, ওই জিমেইলটি রিকভার করা হচ্ছে
                    String boundEmail = snapshot.child("email").getValue(String.class);
                    String boundId = snapshot.child("userId").getValue(String.class);

                    if (boundEmail != null && boundEmail.equals(email)) {
                        // পুরাতন ইউজার, সরাসরি হোমপেজে চলে যাবে
                        saveUserSession(boundEmail, boundId, snapshot.child("name").getValue(String.class));
                        Toast.makeText(this, "Welcome Back! Login Successful.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // সিকিউরিটি অ্যালার্ট: এক ডিভাইসে একাধিক জিমেইল এলাউড নয়!
                        Toast.makeText(this, "Security Alert: This device is already bound to another account (" + boundEmail + ")!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // একদম নতুন ইউজার বা নতুন ডিভাইস
                    checkEmailAlreadyExists(email, deviceId);
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void checkEmailAlreadyExists(String email, String deviceId) {
        usersRef.orderByChild("email").equalTo(email).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // এই জিমেইল দিয়ে অন্য ডিভাইসে অ্যাকাউন্ট আছে কিনা চেক করা
                Toast.makeText(this, "This Gmail is already registered on another device!", Toast.LENGTH_LONG).show();
            } else {
                // একদম নতুন ইউজার! এখন তাকে প্রোফাইল সেটআপ পেজে পাঠানো হবে (যেখানে নাম, পিকচার, জেন্ডার ও লোকেশন সেট করবে)
                Intent intent = new Intent(LoginActivity.this, ProfileSetupActivity.class);
                intent.putExtra("USER_EMAIL", email);
                intent.putExtra("DEVICE_ID", deviceId);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveUserSession(String email, String userId, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.setUserEmail("USER_EMAIL", email);
        editor.putString("MY_9_DIGIT_ID", userId);
        editor.putString("USER_NAME", name != null ? name : "User");
        editor.apply();
    }
}
