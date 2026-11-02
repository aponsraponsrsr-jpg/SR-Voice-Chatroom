package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    CircleImageView imgProfile;
    EditText editName;
    RadioGroup radioGender;
    Button btnSave;
    Uri imageUri;
    int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        imgProfile = findViewById(R.id.imgProfile);
        editName = findViewById(R.id.editName);
        radioGender = findViewById(R.id.radioGender);
        btnSave = findViewById(R.id.btnSave);

        imgProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                editName.setError("নাম লেখো বন্ধু!");
                return;
            }
            String gender = "Male";
            if (radioGender.getCheckedRadioButtonId() == R.id.radioFemale) {
                gender = "Female";
            }
            SharedPreferences pref = getSharedPreferences("SRVoice", MODE_PRIVATE);
            pref.edit().putString("userName", name).putString("userGender", gender).putString("userImage", imageUri != null ? imageUri.toString() : "").putBoolean("isRegistered", true).apply();
            Toast.makeText(this, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
        }
    }
}
