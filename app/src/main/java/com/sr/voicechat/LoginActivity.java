package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailEditText.setError("Please enter your email");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Please enter your password");
                    return;
                }

                // Sign in with Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                // Navigate to ProfileActivity for profile setup after login
                                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If user is already logged in, check profile or redirect to ProfileActivity / MainActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
            finish();
        }
    }
}
