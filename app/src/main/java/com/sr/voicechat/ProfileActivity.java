package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private EditText etNickname, etBirthday, etInvitationCode;
    private Button btnMale, btnFemale, btnConfirm;
    private String selectedGender = "Male";
    private String generatedUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // সেফলি ভিউগুলো ইনিশিয়ালাইজ করা
        etNickname = findViewById(R.id.etNickname);
        etBirthday = findViewById(R.id.etBirthday);
        etInvitationCode = findViewById(R.id.etInvitationCode);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);
        btnConfirm = findViewById(R.id.btnConfirm);

        // অটোমেটিক ৭ ডিজিটের ইউনিক আইডি জেনারেট করা
        generatedUserId = generateUniqueId();

        // জেন্ডার মেল ক্লিক
        if (btnMale != null) {
            btnMale.setOnClickListener(v -> selectedGender = "Male");
        }

        // জেন্ডার ফিমেল ক্লিক
        if (btnFemale != null) {
            btnFemale.setOnClickListener(v -> selectedGender = "Female");
        }

        // কনফার্ম বাটন ক্লিক
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String nickname = "";
                if (etNickname != null && etNickname.getText() != null) {
                    nickname = etNickname.getText().toString().trim();
                }

                if (nickname.isEmpty()) {
                    if (etNickname != null) {
                        etNickname.setError("Please enter your nickname");
                        etNickname.requestFocus();
                    }
                    return;
                }

                Toast.makeText(ProfileActivity.this, "Profile Setup Successful! ID: " + generatedUserId, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.putExtra("user_id", generatedUserId);
                intent.putExtra("user_name", nickname);
                intent.putExtra("user_gender", selectedGender);
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
