package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
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
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private boolean isNavigating = false; // FIX: একবার গেলে আর ২য় বার যাবে না - লাফালাফি বন্ধ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnGoogle = findViewById(R.id.btnGoogleLogin);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Google...", Toast.LENGTH_SHORT).show();
                signIn();
            });
        }

        // FIX: onStart এ আর চেক করবো না। শুধু onCreate এ একবার চেক করবো।
        // যাতে Google থেকে ফিরে আসার সময় আবার চেক করে লাফালাফি না করে
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            checkUserInFirebase(user.getUid(), user.getEmail());
        }
    }

    // FIX: onStart পুরা তুলে দিলাম - এইটাই লাফালাফির আসল কারণ ছিলো
    // @Override onStart আর লাগবে না

    private void signIn() {
        if (isNavigating) return;
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Toast.makeText(this, "Google Play Service Problem: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "You Cancelled Google Login", Toast.LENGTH_SHORT).show();
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, "ID Token Null - Check google-services.json", Toast.LENGTH_LONG).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) checkUserInFirebase(user.getUid(), user.getEmail());
            } else {
                Toast.makeText(this, "Firebase Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserInFirebase(String firebaseUid, String email) {
        if (isNavigating) return; // ইতিমধ্যে যাইতেছে, আবার যাবে না
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                // FIX: এখন শুধু gender + country থাকলেই পুরান ইউজার ধরবো
                // my9DigitId না থাকলেও Home এ যাবে, Edit এ আটকাবে না
                if (s.exists() && s.hasChild("gender") && s.hasChild("country")) {
                    String id = s.child("my9DigitId").getValue(String.class);
                    // যদি এই UID তে ID না থাকে, তাহলে পুরান চাবি থেকে নাও
                    if (id == null || id.isEmpty()) {
                        // পুরান ID 428803851 এর ডাটা থেকে নাও
                        id = prefs.getString("MY_9_DIGIT_ID", "");
                        if (id == null || id.isEmpty()) id = "428803851";
                    }
                    String name = s.child("name").getValue(String.class);
                    String gender = s.child("gender").getValue(String.class);
                    String country = s.child("country").getValue(String.class);
                    String photo = s.child("photoBase64").getValue(String.class);
                    if (photo == null) photo = s.child("photoUrl").getValue(String.class);

                    prefs.edit()
                        .putString("FIREBASE_UID", firebaseUid)
                        .putString("MY_9_DIGIT_ID", id != null ? id : "")
                        .putString("USER_NAME", name != null ? name : "")
                        .putString("USER_GENDER", gender != null ? gender : "")
                        .putString("USER_COUNTRY", country != null ? country : "")
                        .putString("USER_EMAIL", email != null ? email : "")
                        .putString("USER_PHOTO", photo != null ? photo : "")
                        .putBoolean("IS_PROFILE_SETUP_DONE", true)
                        .apply();
                    goHome();
                } else {
                    // UID তে নাই - Email দিয়ে খোঁজো, হয়তো পুরান ডাটা অন্য UID তে আছে
                    if (email != null && !email.isEmpty()) {
                        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(DataSnapshot snap) {
                                if (snap.exists()) {
                                    for (DataSnapshot child : snap.getChildren()) {
                                        String oldId = child.child("my9DigitId").getValue(String.class);
                                        if (oldId == null || oldId.isEmpty()) oldId = child.getKey();
                                        String oldName = child.child("name").getValue(String.class);
                                        String oldGender = child.child("gender").getValue(String.class);
                                        String oldCountry = child.child("country").getValue(String.class);
                                        prefs.edit()
                                            .putString("FIREBASE_UID", firebaseUid)
                                            .putString("MY_9_DIGIT_ID", oldId != null ? oldId : "")
                                            .putString("USER_NAME", oldName != null ? oldName : "")
                                            .putString("USER_GENDER", oldGender != null ? oldGender : "")
                                            .putString("USER_COUNTRY", oldCountry != null ? oldCountry : "")
                                            .putString("USER_EMAIL", email)
                                            .putBoolean("IS_PROFILE_SETUP_DONE", true).apply();
                                        goHome();
                                        return;
                                    }
                                }
                                // একদম নতুন ইউজার
                                goToEdit(firebaseUid, email);
                            }
                            @Override public void onCancelled(DatabaseError e) { goToEdit(firebaseUid, email); }
                        });
                    } else {
                        goToEdit(firebaseUid, email);
                    }
                }
            }
            @Override public void onCancelled(DatabaseError e) {
                Toast.makeText(LoginActivity.this, "Database Error, Try Again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToEdit(String firebaseUid, String email) {
        if (isNavigating) return;
        isNavigating = true;
        prefs.edit()
            .putString("FIREBASE_UID", firebaseUid)
            .putString("USER_EMAIL", email != null ? email : "")
            .putBoolean("IS_PROFILE_SETUP_DONE", false)
            .apply();
        Intent i = new Intent(LoginActivity.this, EditProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goHome() {
        if (isNavigating) return;
        isNavigating = true;
        try {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        } catch (Exception e) {
            isNavigating = false;
            Toast.makeText(this, "MainActivity Not Found: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
