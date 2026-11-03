package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {

    EditText editName;
    TextView textUserId;
    RadioGroup genderGroup;
    Button btnSave;
    String generatedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        textUserId = findViewById(R.id.textUserId);
        genderGroup = findViewById(R.id.genderGroup);
        btnSave = findViewById(R.id.btnSave);

        // Generate 9 Digit Permanent ID
        Random random = new Random();
        int id = 100000000 + random.nextInt(900000000);
        generatedId = String.valueOf(id);
        textUserId.setText("ID: " + generatedId);

        btnSave.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (genderGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show();
                return;
            }
            // Go to Home
            Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
            intent.putExtra("userName", name);
            intent.putExtra("userId", generatedId);
            startActivity(intent);
            finish();
        });
    }
}
