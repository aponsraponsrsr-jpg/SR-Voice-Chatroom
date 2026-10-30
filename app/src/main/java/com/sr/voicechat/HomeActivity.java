package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // বেসিক সেফটি চেক ও বাটন হ্যান্ডলিং
        Button btnCreate = findViewById(R.id.btnCreateFirst);
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                Toast.makeText(this, "SR Voice Chat Room Ready!", Toast.LENGTH_SHORT).show();
            });
        }

        Button btnSearch = findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                Toast.makeText(this, "Searching Room...", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
