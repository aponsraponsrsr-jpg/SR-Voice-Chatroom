package com.sr.voicechat;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private Button btnGoogleSignIn;
    private SharedPreferences prefs;
    private DatabaseReference usersRef;
    private static final int RC_ACCOUNT_PICKER = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        if (isLoggedIn) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        // When user clicks "Sign in with Google", show phone's Google Account Picker
        btnGoogleSignIn.setOnClickListener(v -> {
            try {
                Intent intent = AccountManager.newChooseAccountIntent(
                        null, null, new String[]{"com.google"},
                        null, null, null, null
                );
                startActivityForResult(intent, RC_ACCOUNT_PICKER);
            } catch (Exception e) {
                Toast.makeText(this, "Account picker error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_ACCOUNT_PICKER && resultCode == RESULT_OK && data != null) {
            String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            if (email != null && email.contains("@")) {
                checkDeviceAndEmailSecurity(email.toLowerCase());
            } else {
                Toast.makeText(this, "Please select a valid Gmail account!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkDeviceAndEmailSecurity(String email) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("device_bindings").child(deviceId);

        deviceRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String boundEmail = snapshot.child("email").getValue(String.class);
                String boundId = snapshot.child("userId").getValue(String.class);

                if (boundEmail != null && boundEmail.equals(email)) {
                    saveUserSession(boundEmail, boundId, snapshot.child("name").getValue(String.class));
                    Toast.makeText(this, "Welcome Back! Login Successful.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Security Alert: This device is already bound to another account!", Toast.LENGTH_LONG).show();
                }
            } else {
                // Check if this Gmail is already registered on another device
                usersRef.orderByChild("email").equalTo(email).get().addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.exists()) {
                        Toast.makeText(this, "This Gmail is already registered on another device!", Toast.LENGTH_LONG).show();
                    } else {
                        // New user, go to EditProfileActivity to setup profile and generate permanent ID
                        Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                        intent.putExtra("USER_EMAIL", email);
                        intent.putExtra("DEVICE_ID", deviceId);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserSession(String email, String userId, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.putString("USER_EMAIL", email);
        editor.putString("MY_9_DIGIT_ID", userId);
        editor.putString("USER_NAME", name != null ? name : "User");
        editor.apply();
    }
}
