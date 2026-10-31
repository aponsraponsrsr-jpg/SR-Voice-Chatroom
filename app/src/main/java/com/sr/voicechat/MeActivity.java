package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView tvNameMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_me);

        imgProfileMe = findViewById(R.id.imgProfileMe);
        tvNameMe = findViewById(R.id.tvNameMe);

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "SR User");
        String imageUriStr = prefs.getString("USER_IMAGE", null);

        if (tvNameMe != null) {
            tvNameMe.setText(name);
        }

        if (imageUriStr != null && imgProfileMe != null) {
            imgProfileMe.setImageURI(Uri.parse(imageUriStr));
        }
    }
}
