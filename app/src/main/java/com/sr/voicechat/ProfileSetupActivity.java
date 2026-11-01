package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etName;
    private RadioGroup rgGender;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        etName = findViewById(R.id.etName);
        rgGender = findViewById(R.id.rgGender);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            RadioButton rbSelected = findViewById(selectedGenderId);
            String gender = rbSelected != null ? rbSelected.getText().toString() : "Male";

            // ৭ ডিজিটের ইউনিক ID জেনারেট করা
            int randomId = 1000000 + new Random().nextInt(9000000);
            String userId = String.valueOf(randomId);

            // ইউজারের ডাটা লোকালি সেভ করা
            SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("userName", name);
            editor.putString("gender", gender);
            editor.putString("userId", userId);
            editor.putInt("coins", 1000); // প্রাথমিক অবস্থায় ১০০০ কয়েন ফ্রি
            editor.putBoolean("isProfileSetupComplete", true);
            editor.apply();

            // সরাসরি হোম পেজে রিডাইরেক্ট
            Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
