package com.sr.voice.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        View btnSearch = findViewById(R.id.btnSearch);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, "SR Official Search Coming Soon", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
