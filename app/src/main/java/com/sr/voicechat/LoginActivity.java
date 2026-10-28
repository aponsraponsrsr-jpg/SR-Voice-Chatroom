package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private Button googleLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // এখানে আপনার ডিজাইন অনুযায়ী বাটনের আইডি যুক্ত করা হলো
        googleLoginBtn = findViewById(R.id.googleLoginBtn); // যদি লেআউটে আইডি আলাদা হয় এটি পরে ঠিক করা যাবে

        if (googleLoginBtn != null) {
            googleLoginBtn.setOnClickListener(v -> {
                Toast.makeText(LoginActivity.this, "Google Login Clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
