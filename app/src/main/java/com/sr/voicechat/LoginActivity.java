package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private EditText etEmail, etPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check already logged in
        if (mAuth.getCurrentUser() != null) {
            goToMain();
            return;
        }

        // Build UI without R.id - 100% safe
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(60, 100, 60, 60);

        TextView title = new TextView(this);
        title.setText("SR Voice Chatroom\nHeavy Global Login");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 60);
        main.addView(title);

        etEmail = new EditText(this);
        etEmail.setHint("Email");
        main.addView(etEmail);

        etPass = new EditText(this);
        etPass.setHint("Password");
        main.addView(etPass);

        Button btnLogin = new Button(this);
        btnLogin.setText("Login");
        main.addView(btnLogin);

        Button btnRegister = new Button(this);
        btnRegister.setText("Register New Account");
        main.addView(btnRegister);

        Button btnGoogle = new Button(this);
        btnGoogle.setText("Login with Google - Heavy");
        main.addView(btnGoogle);

        setContentView(main);

        // Google Config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> loginEmail());
        btnRegister.setOnClickListener(v -> registerEmail());
        btnGoogle.setOnClickListener(v -> signInGoogle());
    }

    private void loginEmail() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Fill all", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(a -> goToMain())
                .addOnFailureListener(e -> Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void registerEmail() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Fill all", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(a -> {
                    saveUserToFirestore();
                    goToMain();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Register Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void signInGoogle() {
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
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(a -> {
                    saveUserToFirestore();
                    goToMain();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Auth Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserToFirestore() {
        if (mAuth.getCurrentUser() == null) return;
        Map<String, Object> user = new HashMap<>();
        user.put("uid", mAuth.getCurrentUser().getUid());
        user.put("name", mAuth.getCurrentUser().getDisplayName() != null ? mAuth.getCurrentUser().getDisplayName() : etEmail.getText().toString());
        user.put("email", mAuth.getCurrentUser().getEmail());
        user.put("globalId", "SR_" + System.currentTimeMillis()); // Heavy Global Permanent ID
        db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user);
    }

    private void goToMain() {
        // Try MainActivity or HomeActivity - whichever exists
        try {
            startActivity(new Intent(this, Class.forName("com.sr.voicechat.MainActivity")));
        } catch (Exception e) {
            try {
                startActivity(new Intent(this, Class.forName("com.sr.voicechat.HomeActivity")));
            } catch (Exception ex) {
                Toast.makeText(this, "Login Success! No Home Activity found", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
