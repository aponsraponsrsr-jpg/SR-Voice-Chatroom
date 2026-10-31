package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView tvProfileName, tvProfileId, tvProfileGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        imgProfileMe = findViewById(R.id.imgProfileMe);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileId = findViewById(R.id.tvProfileId);
        tvProfileGender = findViewById(R.id.tvProfileGender);

        // SharedPreferences থেকে ইউজারের সেভ করা ডেটা লোড করা
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "User Name");
        String id = prefs.getString("USER_ID", "0000000");
        String gender = prefs.getString("USER_GENDER", "Male");
        String imageUriStr = prefs.getString("USER_IMAGE", null);

        if (tvProfileName != null) {
            tvProfileName.setText(name);
        }
        if (tvProfileId != null) {
            tvProfileId.setText("ID: " + id);
        }
        if (tvProfileGender != null) {
            tvProfileGender.setText("Gender: " + gender);
        }

        if (imgProfileMe != null && imageUriStr != null) {
            try {
                Uri imageUri = Uri.parse(imageUriStr);
                imgProfileMe.setImageURI(imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
