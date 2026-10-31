package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText etName;
    private RadioGroup rgGender;
    private Button btnSaveAndEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgProfile = findViewById(R.id.imgProfile);
        etName = findViewById(R.id.etName);
        rgGender = findViewById(R.id.rgGender);
        btnSaveAndEnter = findViewById(R.id.btnSaveAndEnter);

        imgProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Gallery picker will open here", Toast.LENGTH_SHORT).show();
        });

        btnSaveAndEnter.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            int selectedGenderId = rgGender.getCheckedRadioButtonId();

            if (name.isEmpty()) {
                etName.setError("Please enter your name");
                return;
            }

            if (selectedGenderId == -1) {
                Toast.makeText(ProfileActivity.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedGender = findViewById(selectedGenderId);
            String genderText = selectedGender.getText().toString();

            // Generate 7-digit unique permanent ID
            int randomId = 1000000 + new Random().nextInt(9000000);

            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.putExtra("USER_NAME", name);
            intent.putExtra("USER_GENDER", genderText);
            intent.putExtra("USER_ID", String.valueOf(randomId));
            startActivity(intent);
            finish();
        });
    }
}
