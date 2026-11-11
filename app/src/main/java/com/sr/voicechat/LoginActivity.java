package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private Button btnGoogleLogin;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        boolean isProfileDone = prefs.getBoolean("IS_PROFILE_SETUP_DONE", false);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // যদি অলরেডি লগইন করা থাকে এবং প্রোফাইল সেটআপ করা থাকে, সরাসরি MainActivity-তে যাবে
        if (currentUser != null && isLoggedIn && isProfileDone) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> {
                // গুগল লগইন সফল ধরে সেশন সেভ করা হলো
                prefs.edit().putBoolean("IS_LOGGED_IN", true).apply();

                // ফায়ারবেস থেকে চেক করব ইউজারের পার্মানেন্ট ডেটা সেট করা আছে কি না
                String uid = prefs.getString("MY_9_DIGIT_ID", "");
                if (uid.isEmpty()) {
                    int r = (int) (Math.random() * 900000000) + 100000000;
                    uid = String.valueOf(r);
                    prefs.edit().putString("MY_9_DIGIT_ID", uid).apply();
                }

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("gender") && snapshot.hasChild("country")) {
                            // অলরেডি পার্মানেন্ট ডেটা আছে
                            prefs.edit().putBoolean("IS_PROFILE_SETUP_DONE", true).apply();
                            goToMain();
                        } else {
                            // নতুন ইউজার বা প্রোফাইল সেটআপ হয়নি -> EditProfileActivity তে পাঠাবো
                            Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        goToMain();
                    }
                });
            });
        }
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
