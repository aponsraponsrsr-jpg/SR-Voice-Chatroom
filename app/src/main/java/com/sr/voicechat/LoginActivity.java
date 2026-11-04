package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Activity Result Launcher for Google Sign In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Login Button Click Listener (matched with xml id: sign_in_button)
        signInButton = findViewById(R.id.sign_in_button);
        if (signInButton != null) {
            signInButton.setOnClickListener(v -> signInWithGoogle());
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
                
                // --- পার্মানেন্ট ইউনিক আইডির লজিক ---
                if (user != null) {
                    String firebaseUid = user.getUid();
                    String savedUniqueId = prefs.getString("UID_" + firebaseUid, "");

                    if (savedUniqueId.isEmpty()) {
                        // প্রথমবার লগইন করলে নতুন ৯ ডিজিটের আইডি জেনারেট হবে এবং এই জিমেইল/ইউআইডি দিয়ে চিরতরে লক হয়ে যাবে
                        int randomId = 100000000 + (int)(Math.random() * 900000000);
                        savedUniqueId = String.valueOf(randomId);
                        prefs.edit()
                                .putString("UID_" + firebaseUid, savedUniqueId)
                                .putString("MY_9_DIGIT_ID", savedUniqueId)
                                .apply();
                    } else {
                        // পুরোনো ইউজার হলে আগের আইডিই ফিক্সড থাকবে, কখনোই বদলাবে না
                        prefs.edit().putString("MY_9_DIGIT_ID", savedUniqueId).apply();
                    }
                }
                // ----------------------------------------------------

                // চেক করা ইউজার কি প্রোফাইল সেটআপ করেছে কি না
                boolean isProfileCompleted = prefs.getBoolean("PROFILE_COMPLETED", false);

                if (!isProfileCompleted) {
                    startActivity(new Intent(LoginActivity.this, EditProfileActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Auth Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
