package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView tvNameMe, tvIdMe, tvGenderMe, tvFans, tvFollowing, tvVisitors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        imgProfileMe = findViewById(R.id.imgProfileMe);
        tvNameMe = findViewById(R.id.tvNameMe);
        tvIdMe = findViewById(R.id.tvIdMe);
        tvGenderMe = findViewById(R.id.tvGenderMe);
        tvFans = findViewById(R.id.tvFans);
        tvFollowing = findViewById(R.id.tvFollowing);
        tvVisitors = findViewById(R.id.tvVisitors);

        // Load permanent profile data from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "SR User");
        String id = prefs.getString("USER_ID", "1000000");
        String gender = prefs.getString("USER_GENDER", "Not Specified");
        String imageUriStr = prefs.getString("USER_IMAGE", null);

        // Set data to views
        tvNameMe.setText(name);
        tvIdMe.setText("ID: " + id);
        tvGenderMe.setText("Gender: " + gender);
        
        tvFans.setText("0\nFans");
        tvFollowing.setText("0\nFollowing");
        tvVisitors.setText("0\nVisitors");

        if (imageUriStr != null) {
            imgProfileMe.setImageURI(Uri.parse(imageUriStr));
        }
    }
}
