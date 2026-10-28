package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // গুগল সাইন-ইন অপশন কনফিগার করা
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // গুগল সাইন-ইন বাটনে ক্লিক লজিক যুক্ত করা
        Button googleSignInButton = findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this, "Login Successful: " + account.getDisplayName(), Toast.LENGTH_LONG).show();
                
                // সফল লগইনের পর সরাসরি MainActivity-তে নিয়ে যাওয়ার কোড
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("user_name", account.getDisplayName());
                intent.putExtra("user_email", account.getEmail());
                startActivity(intent);
                finish(); // যাতে ব্যাক করলে আবার লগইন পেজে ফিরে না আসে
                
            } catch (ApiException e) {
                Toast.makeText(this, "Login Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
