package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();

        // ১. চেক করবে ইউজার আগে থেকেই জিমেইল দিয়ে লগইন করা আছে কিনা
        if (mAuth.getCurrentUser() != null) {
            // যদি লগইন করা থাকে, সরাসরি ভয়েস রুমে চলে যাবে (আর জিমেইল চাইবে না)
            startActivity(new Intent(LoginActivity.this, VoiceRoomActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // ২. Google Sign In Options কনফিগারেশন
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // ফায়ারবেস থেকে নেওয়া আইডি
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ৩. আপনার ক্রিয়েট রুম বা জিমেইল সাইন-ইন বাটন
        Button btnGoogleLogin = findViewById(R.id.btnCreateRoom); 
        btnGoogleLogin.setText("Sign in with Gmail"); // বাটনের লেখা পরিবর্তন করে দিতে পারেন

        btnGoogleLogin.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInFromIntent(requestCode, data);
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
                        // জিমেইল লগইন সফল হলে সরাসরি ভয়েস রুমে চলে যাবে
                        Toast.makeText(this, "Gmail Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, VoiceRoomActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
