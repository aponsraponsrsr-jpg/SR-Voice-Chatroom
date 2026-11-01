package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Button btnGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                Toast.makeText(LoginActivity.this, "Sign in clicked successfully!", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
