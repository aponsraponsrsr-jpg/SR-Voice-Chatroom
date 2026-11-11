package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
    private EditText editEmail;
    private EditText editPassword;
    private Button btnLogin;
    private Button btnGoogle;
    private Button btnGoRegister;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SharedPreferences prefs;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginWithEmail());
        }
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> signInWithGoogle());
        }
        if (btnGoRegister != null) {
            btnGoRegister.setOnClickListener(v -> {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            });
        }

        FirebaseUser cur = mAuth.getCurrentUser();
        if (cur != null) {
            verifyAndGoHome(cur.getUid(), cur.getDisplayName(), cur.getEmail());
        }
    }

    private void loginWithEmail() {
        String email = editEmail.getText().toString().trim();
        String pass = editPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser u = task.getResult().getUser();
                        if (u != null) {
                            verifyAndGoHome(u.getUid(), u.getDisplayName(), u.getEmail());
                        }
                    } else {
                        String msg = "Login Failed";
                        if (task.getException() != null) {
                            msg = task.getException().getMessage();
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        String token = acct.getIdToken();
        if (token == null || token.isEmpty()) {
            String gName = acct.getDisplayName();
            String gEmail = acct.getEmail();
            String fakeUid = "google_" + acct.getId();
            verifyAndGoHome(fakeUid, gName, gEmail);
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser u = task.getResult().getUser();
                        if (u != null) {
                            verifyAndGoHome(u.getUid(), u.getDisplayName(), u.getEmail());
                        }
                    } else {
                        Toast.makeText(this, "Google Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // অত্যন্ত গুরুত্বপূর্ণ: ফায়ারবেস ডেটাবেস চেক করবে ইউজারের পার্মানেন্ট আইডি আগে থেকেই আছে কি না
    private void verifyAndGoHome(String uid, String gName, String email) {
        if (uid == null || uid.isEmpty()) return;
        if (email == null) email = "";

        final String finalEmail = email;
        final String rawName = (gName != null && !gName.isEmpty()) ? gName : "User";

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String existingPermanentId = "";
                String existingName = rawName;

                if (snap.exists()) {
                    if (snap.hasChild("my9Id") && snap.child("my9Id").getValue() != null) {
                        existingPermanentId = String.valueOf(snap.child("my9Id").getValue());
                    } else if (snap.hasChild("my9DigitId") && snap.child("my9DigitId").getValue() != null) {
                        existingPermanentId = String.valueOf(snap.child("my9DigitId").getValue());
                    }
                    if (snap.hasChild("name") && snap.child("name").getValue() != null) {
                        existingName = String.valueOf(snap.child("name").getValue());
                    }
                }

                // যদি ফায়ারবেসে আইডি না থাকে, তবে লোকাল prefs চেক করবো
                if (existingPermanentId.isEmpty()) {
                    existingPermanentId = prefs.getString("MY_9_DIGIT_ID", "");
                }

                // যদি কোথাও না থাকে, তবেই কেবল নতুন ৯-ডিজিট পার্মানেন্ট আইডি জেনারেট হবে
                if (existingPermanentId.isEmpty()) {
                    int r = (int) (Math.random() * 900000000) + 100000000;
                    existingPermanentId = String.valueOf(r);
                }

                // ফায়ারবেসে পার্মানেন্ট ডেটা স্থায়ীভাবে লক বা সিঙ্ক করে রাখা
                usersRef.child(uid).child("email").setValue(finalEmail);
                usersRef.child(uid).child("name").setValue(existingName);
                usersRef.child(uid).child("uid").setValue(uid);
                usersRef.child(uid).child("my9Id").setValue(existingPermanentId);
                usersRef.child(uid).child("my9DigitId").setValue(existingPermanentId);

                // লোকাল SharedPreferences-এ চিরতরে সেভ রাখা যাতে লগআউট বা রিমুভ হলেও হারায় না
                prefs.edit().putString("MY_9_DIGIT_ID", existingPermanentId).apply();
                prefs.edit().putString("USER_NAME", existingName).apply();
                prefs.edit().putString("USER_EMAIL", finalEmail).apply();
                prefs.edit().putString("FIREBASE_UID", uid).apply();

                // হোম বা মেইন এক্টিভিটিতে প্রবেশ
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
