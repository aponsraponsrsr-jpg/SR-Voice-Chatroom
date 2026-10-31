package com.sr.voicechat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_me);

        // Profile image
        if (findViewById(R.id.ivProfile) != null) {
            // safe
        }

        // Logout button
        if (findViewById(R.id.btnLogout) != null) {
            findViewById(R.id.btnLogout).setOnClickListener(v -> {
                finish();
            });
        }
    }
}
