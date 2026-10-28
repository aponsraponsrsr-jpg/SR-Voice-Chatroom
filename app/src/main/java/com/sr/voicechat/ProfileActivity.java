package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private EditText etNickname;
    private Button btnConfirm;
    private String generatedUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etNickname = findViewById(R.id.etNickname);
        btnConfirm = findViewById(R.id.btnConfirm);

        // ৭ ডিজিটের র্যান্ডম আইডি জেনারেট করা
        generatedUserId = generateUniqueId();

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String nickname = "User";
                if (etNickname != null && etNickname.getText() != null) {
                    nickname = etNickname.getText().toString().trim();
                    if (nickname.isEmpty()) {
                        nickname = "SR User";
                    }
                }

                Toast.makeText(ProfileActivity.this, "Welcome! ID: " + generatedUserId, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("user_id", generatedUserId);
                intent.putExtra("user_name", nickname);
                startActivity(intent);
                finish();
            });
        }
    }

    private String generateUniqueId() {
        Random random = new Random();
        int idNumber = 1000000 + random.nextInt(9000000);
        return String.valueOf(idNumber);
    }
}
