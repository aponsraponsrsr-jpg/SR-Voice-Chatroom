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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(v -> signIn());
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
                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
                
                // ১. পার্মানেন্ট ৯+ ডিজিটের র্যান্ডম ইউনিক আইডি জেনারেশন (যদি আগে না থাকে)
                if (!prefs.contains("MY_9_DIGIT_ID")) {
                    int randomId = 100000000 + (int)(Math.random() * 900000000);
                    prefs.edit().putString("MY_9_DIGIT_ID", String.valueOf(randomId)).apply();
                }

                // ২. চেক করা ইউজার কি প্রোফাইল সেটআপ করেছে কি না
                boolean isProfileCompleted = prefs.getBoolean("PROFILE_COMPLETED", false);

                if (!isProfileCompleted) {
                    // নতুন ইউজার হলে প্রথমে EditProfileActivity-তে পাঠাবে
                    startActivity(new Intent(LoginActivity.this, EditProfileActivity.class));
                } else {
                    // পুরোনো ইউজার হলে সরাসরি HomeActivity-তে পাঠাবে
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Auth Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
            boolean isProfileCompleted = prefs.getBoolean("PROFILE_COMPLETED", false);

            if (!isProfileCompleted) {
                startActivity(new Intent(this, EditProfileActivity.class));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
            finish();
        }
    }
}
