package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Google Sign In Setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Permanent - Find Sign in Button - Your photo button
        // We try all possible IDs - so click will work 100%
        android.view.View signInBtn = null;
        try { signInBtn = findViewById(R.id.sign_in_button); } catch (Exception e) {}
        if (signInBtn == null) {
            try { signInBtn = findViewById(R.id.btnGoogleSignIn); } catch (Exception e) {}
        }
        if (signInBtn == null) {
            try { signInBtn = findViewById(R.id.btnSignIn); } catch (Exception e) {}
        }
        if (signInBtn == null) {
            try { signInBtn = findViewById(R.id.google_sign_in_button); } catch (Exception e) {}
        }

        if (signInBtn != null) {
            signInBtn.setOnClickListener(v -> signIn());
        } else {
            // If ID not found, make whole screen clickable as backup
            findViewById(android.R.id.content).setOnClickListener(v -> signIn());
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Save 9 Digit ID First Time - Lifetime
                        SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
                        if (!prefs.contains("MY_9_DIGIT_ID")) {
                            int randomId = 100000000 + (int)(Math.random() * 900000000);
                            prefs.edit().putString("MY_9_DIGIT_ID", String.valueOf(randomId)).apply();
                            prefs.edit().putString("MY_NAME", user.getDisplayName()).apply();
                            prefs.edit().putString("MY_PHOTO", String.valueOf(user.getPhotoUrl())).apply();
                        }
                        // Go Home
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }
}
