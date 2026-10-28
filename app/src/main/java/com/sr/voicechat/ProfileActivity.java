package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
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

        // ভিউগুলো ইনিশিয়ালাইজ করা
        etNickname = findViewById(R.id.etNickname);
        etBirthday = findViewById(R.id.etBirthday);
        etInvitationCode = findViewById(R.id.etInvitationCode);
        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);
        btnConfirm = findViewById(R.id.btnConfirm);

        // অটোমেটিক ৭ থেকে ৮ ডিজিটের ইউনিক মিক্সড আইডি জেনারেট করা
        generatedUserId = generateUniqueId();

        // জেন্ডার সিলেকশন বাটন লজিক
        btnMale.setOnClickListener(v -> {
            selectedGender = "Male";
            btnMale.setBackgroundTintColor(getResources().getColor(android.R.color.holo_purple));
            btnFemale.setBackgroundTintColor(getResources().getColor(android.R.color.darker_gray));
        });

        btnFemale.setOnClickListener(v -> {
            selectedGender = "Female";
            btnFemale.setBackgroundTintColor(getResources().getColor(android.R.color.holo_purple));
            btnMale.setBackgroundTintColor(getResources().getColor(android.R.color.darker_gray));
        });

        // কনফার্ম বাটন ক্লিক লজিক
        btnConfirm.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();

            if (nickname.isEmpty()) {
                etNickname.setError("Please enter your nickname");
                etNickname.requestFocus();
                return;
            }

            // সফলভাবে প্রোফাইল সেটআপ সম্পন্ন হওয়ার পর MainActivity-তে পাঠানো
            Toast.makeText(ProfileActivity.this, "Profile Setup Successful! ID: " + generatedUserId, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.putExtra("user_id", generatedUserId);
            intent.putExtra("user_name", nickname);
            intent.putExtra("user_gender", selectedGender);
            startActivity(intent);
            finish();
        });
    }

    // ৭ বা ৮ ডিজিটের র্যান্ডম ইউনিক আইডি তৈরির মেথড
    private String generateUniqueId() {
        Random random = new Random();
        int idNumber = 1000000 + random.nextInt(9000000);
        return String.valueOf(idNumber);
    }
}
