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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private Button btnGoogleSignIn;
    private SharedPreferences prefs;
    private DatabaseReference usersRef, deviceRef;
    private static final int RC_ACCOUNT_PICKER = 1000;
    
    private static final String OWNER_EMAIL = "aponsraponsrsr@gmail.com";

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
                handleLoginProcess(email.toLowerCase());
            } else {
                Toast.makeText(this, "Please select a valid Gmail account!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleLoginProcess(String email) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference currentDeviceRef = FirebaseDatabase.getInstance().getReference("device_bindings").child(deviceId);

        // Owner Bypass: Always allow owner email
        if (OWNER_EMAIL.equalsIgnoreCase(email)) {
            usersRef.orderByChild("email").equalTo(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        String userId = snapshot.child("userId").getValue(String.class);
                        String name = snapshot.child("name").getValue(String.class);
                        saveSessionAndProceed(email, userId, name != null ? name : "SR Owner");
                        return;
                    }
                } else {
                    // First time owner setup
                    Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                    intent.putExtra("USER_EMAIL", email);
                    intent.putExtra("DEVICE_ID", deviceId);
                    startActivity(intent);
                    finish();
                }
            });
            return;
        }

        // For other users: Check device binding first
        currentDeviceRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String boundEmail = snapshot.child("email").getValue(String.class);
                    String boundId = snapshot.child("userId").getValue(String.class);

                    if (boundEmail != null && boundEmail.equals(email)) {
                        saveSessionAndProceed(boundEmail, boundId, snapshot.child("name").getValue(String.class));
                    } else {
                        Toast.makeText(this, "Security Alert: This device is bound to another email!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Check if email already used on another device
                    usersRef.orderByChild("email").equalTo(email).get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult().exists()) {
                            Toast.makeText(this, "This Gmail is already registered on another device!", Toast.LENGTH_LONG).show();
                        } else {
                            // New user registration
                            Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                            intent.putExtra("USER_EMAIL", email);
                            intent.putExtra("DEVICE_ID", deviceId);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Connection error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSessionAndProceed(String email, String userId, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.putString("USER_EMAIL", email);
        editor.putString("MY_9_DIGIT_ID", userId);
        editor.putString("USER_NAME", name);
        editor.apply();

        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }
}
