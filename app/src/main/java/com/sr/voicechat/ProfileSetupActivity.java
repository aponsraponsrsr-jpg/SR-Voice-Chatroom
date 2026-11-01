package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(ProfileSetupActivity.this, "দয়া করে আপনার নাম লিখুন", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedId = rgGender.getCheckedRadioButtonId();
                String gender = "Male";
                if (selectedId == R.id.rbFemale) {
                    gender = "Female";
                }

                int randomId = 1000000 + new Random().nextInt(9000000);
                String userId = String.valueOf(randomId);

                SharedPreferences pref = getSharedPreferences("UserProfile", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("userName", name);
                editor.putString("gender", gender);
                editor.putString("userId", userId);
                editor.putInt("coins", 1000);
                editor.putBoolean("isProfileSetupComplete", true);
                editor.apply();

                Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
