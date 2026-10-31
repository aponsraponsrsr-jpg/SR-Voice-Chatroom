package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView tvNameMe, tvIdMe, tvGenderMe;
    private Button btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        imgProfileMe = findViewById(R.id.imgProfileMe);
        tvNameMe = findViewById(R.id.tvNameMe);
        tvIdMe = findViewById(R.id.tvIdMe);
        tvGenderMe = findViewById(R.id.tvGenderMe);
        btnBackHome = findViewById(R.id.btnBackHome);

        // SharedPreferences থেকে ইউজারের সেভ করা ডেটা লোড করা
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "User Name");
        String id = prefs.getString("USER_ID", "1000000");
        String gender = prefs.getString("USER_GENDER", "Male");
        String imageUriStr = prefs.getString("USER_IMAGE", null);

        if (tvNameMe != null) tvNameMe.setText(name);
        if (tvIdMe != null) tvIdMe.setText("ID: " + id);
        if (tvGenderMe != null) tvGenderMe.setText("Gender: " + gender);

        if (imgProfileMe != null && imageUriStr != null) {
            try {
                Uri imageUri = Uri.parse(imageUriStr);
                imgProfileMe.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
