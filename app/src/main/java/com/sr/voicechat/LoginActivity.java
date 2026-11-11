package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private Button btnGoogleLogin;
    private SharedPreferences prefs;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        boolean isProfileDone = prefs.getBoolean("IS_PROFILE_SETUP_DONE", false);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && isLoggedIn && isProfileDone) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        // আসল Google Sign-In সেটআপ
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> {
                // আগের ফেক সেশন মুছে নতুন করে Gmail চাইবো
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Login Failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) return;
                String uid = user.getUid();
                // আপনার ৯ ডিজিটের ID টা আলাদা রাখলাম, Firebase UID দিয়ে ওভাররাইট করলাম না
                String my9Digit = prefs.getString("MY_9_DIGIT_ID", "");
                if (my9Digit.isEmpty()) {
                    int r = (int) (Math.random() * 900000000) + 100000000;
                    my9Digit = String.valueOf(r);
                    prefs.edit().putString("MY_9_DIGIT_ID", my9Digit).apply();
                }
                prefs.edit().putBoolean("IS_LOGGED_IN", true).apply();
                prefs.edit().putString("USER_NAME", user.getDisplayName() != null ? user.getDisplayName() : "User").apply();
                prefs.edit().putString("USER_EMAIL", user.getEmail() != null ? user.getEmail() : "").apply();

                // Firebase users/uid তে ডাটা সেভ
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                userRef.child("name").setValue(user.getDisplayName());
                userRef.child("email").setValue(user.getEmail());
                userRef.child("my9DigitId").setValue(my9Digit);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("gender") && snapshot.hasChild("country")) {
                            prefs.edit().putBoolean("IS_PROFILE_SETUP_DONE", true).apply();
                            goToMain();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) { goToMain(); }
                });
            } else {
                Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
