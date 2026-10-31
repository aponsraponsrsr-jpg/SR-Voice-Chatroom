package com.sr.voicechat;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // আমরা যে ফাইলটা বানালাম ওটা লোড হবে
        setContentView(R.layout.fragment_me);

        // Follow / Fans / Visitor = 0 (Dynamic)
        TextView tvFollow = findViewById(R.id.tvFollow);
        TextView tvFans = findViewById(R.id.tvFans);
        TextView tvVisitor = findViewById(R.id.tvVisitor);
        
        if(tvFollow != null) tvFollow.setText("0\nFollow");
        if(tvFans != null) tvFans.setText("0\nFans");
        if(tvVisitor != null) tvVisitor.setText("0\nVisitor");

        // Coming Soon Logic - All Locked
        View.OnClickListener comingSoon = v -> {
            Toast.makeText(MeActivity.this, "Coming Soon - SR Official", Toast.LENGTH_SHORT).show();
        };

        // সব Item Lock
        findViewById(R.id.itemAgency).setOnClickListener(comingSoon);
        findViewById(R.id.itemInvite).setOnClickListener(comingSoon);
        findViewById(R.id.itemMedal).setOnClickListener(comingSoon);
        findViewById(R.id.itemFamily).setOnClickListener(comingSoon);
        findViewById(R.id.itemHost).setOnClickListener(comingSoon);
        findViewById(R.id.itemTask).setOnClickListener(comingSoon);
        findViewById(R.id.itemSetting).setOnClickListener(comingSoon);
    }
}
