package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView tvNameMe, tvIdMe, tvGenderMe, tvFans, tvFollowing, tvVisitors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_me);

        imgProfileMe = findViewById(R.id.imgProfileMe);
        tvNameMe = findViewById(R.id.tvNameMe);
        tvIdMe = findViewById(R.id.tvIdMe);
        tvGenderMe = findViewById(R.id.tvGenderMe);
        tvFans = findViewById(R.id.tvFans);
        tvFollowing = findViewById(R.id.tvFollowing);
        tvVisitors = findViewById(R.id.tvVisitors);

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "SR User");
        String gender = prefs.getString("USER_GENDER", "Not Specified");
        String imageUriStr = prefs.getString("USER_IMAGE", null);

        // পার্মানেন্ট আইডি জেনারেট বা ফেচ করা (একবার তৈরি হলে আর বদলাবে না)
        String userId = prefs.getString("USER_UNIQUE_ID", null);
        if (userId == null) {
            int randomId = 1000000 + new Random().nextInt(9000000);
            userId = String.valueOf(randomId);
            prefs.edit().putString("USER_UNIQUE_ID", userId).apply();
        }

        if (tvNameMe != null) tvNameMe.setText(name);
        if (tvIdMe != null) tvIdMe.setText("ID: " + userId);
        if (tvGenderMe != null) tvGenderMe.setText("Gender: " + gender);
        
        if (tvFans != null) tvFans.setText("0\nFans");
        if (tvFollowing != null) tvFollowing.setText("0\nFollowing");
        if (tvVisitors != null) tvVisitors.setText("0\nVisitors");

        if (imageUriStr != null && imgProfileMe != null) {
            imgProfileMe.setImageURI(Uri.parse(imageUriStr));
        }
    }
}
