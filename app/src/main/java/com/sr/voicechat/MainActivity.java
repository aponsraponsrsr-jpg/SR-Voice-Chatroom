package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
        String name = prefs.getString("userName","");

        // যদি আগেই Profile Setup করা থাকে, Direct Home
        if(!name.isEmpty() && !name.equals("SR User")){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        Button btn = findViewById(getResources().getIdentifier("btnGoogle","id",getPackageName()));
        if(btn==null) btn = findViewById(getResources().getIdentifier("btnSignInWithGoogle","id",getPackageName()));
        if(btn==null) btn = findViewById(getResources().getIdentifier("sign_in_button","id",getPackageName()));

        if(btn!=null){
            btn.setOnClickListener(v -> {
                // Google Successful -> এখন ID Edit Page এ যাও
                startActivity(new Intent(this, ProfileSetupActivity.class));
                finish();
            });
        }
    }
}
