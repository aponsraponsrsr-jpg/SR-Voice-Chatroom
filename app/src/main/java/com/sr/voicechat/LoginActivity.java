package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private DatabaseReference userDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        // যদি অলরেডি লগইন করা থাকে এবং পার্মানেন্ট আইডি থাকে, তবে সরাসরি হোম পেজে পাঠিয়ে দেব
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        String savedPermanentId = prefs.getString("MY_9_DIGIT_ID", "");
        if (isLoggedIn && !savedPermanentId.isEmpty()) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // XML ফাইলের সঠিক আইডি (btnGoogleSignIn) এখানে যুক্ত করা হয়েছে
        Button btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> signIn());
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken(), account.getEmail());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken, String email) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkExistingUserInDatabase(email);
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // জিমেইল চেক করে পুরাতন পার্মানেন্ট আইডি রিকভার করার লজিক
    private void checkExistingUserInDatabase(String email) {
        if (email == null) return;
        String safeEmailKey = email.replace(".", "_").replace("@", "_");

        userDatabaseRef.child(safeEmailKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("USER_EMAIL", email);

                if (snapshot.exists()) {
                    // পুরাতন ইউজার! ডাটাবেস থেকে পার্মানেন্ট আইডি উদ্ধার করা হচ্ছে
                    String existingId = snapshot.child("userId").getValue(String.class);
                    String existingName = snapshot.child("name").getValue(String.class);
                    String existingImage = snapshot.child("profileImage").getValue(String.class);

                    if (existingId != null && !existingId.isEmpty()) {
                        editor.putString("MY_9_DIGIT_ID", existingId);
                        editor.putString("USER_ID", existingId);
                    }
                    if (existingName != null) {
                        editor.putString("USER_NAME", existingName);
                    }
                    if (existingImage != null) {
                        editor.putString("USER_IMAGE", existingImage);
                    }

                    editor.putBoolean("IS_LOGGED_IN", true);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Welcome Back! ID: " + existingId, Toast.LENGTH_SHORT).show();
                    
                    // সরাসরি হোম পেজে চলে যাবে, আর কখনোই এডিট প্রোফাইল পেজে যাবে না
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    // একদম নতুন ইউজার হলে তবেই প্রথমবার প্রোফাইল সেটআপ পেজে পাঠাবে
                    editor.putBoolean("IS_LOGGED_IN", false);
                    editor.apply();

                    String deviceId = android.provider.Settings.Secure.getString(
                            getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

                    Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                    intent.putExtra("USER_EMAIL", email);
                    intent.putExtra("DEVICE_ID", deviceId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
