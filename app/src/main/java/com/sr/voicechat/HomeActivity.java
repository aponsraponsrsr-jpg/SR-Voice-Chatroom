package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // হোমপেজের নিচের 'Me' ট্যাবের সঠিক আইডি দিয়ে MeActivity-তে যাওয়ার লজিক
        TextView tvMe = findViewById(R.id.tvMe);
        if (tvMe != null) {
            tvMe.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }
    }
}
