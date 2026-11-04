package com.sr.voicechat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Search button - Top Right
        View search = findViewById(R.id.btnSearch);
        if(search != null){
            search.setOnClickListener(v -> {
                Toast.makeText(this, "SR Official Search - Coming Soon", Toast.LENGTH_SHORT).show();
            });
        }

        // 4 Rooms Click - SR Official
        View room1 = findViewById(R.id.room1);
        View room2 = findViewById(R.id.room2);
        View room3 = findViewById(R.id.room3);
        View room4 = findViewById(R.id.room4);

        View.OnClickListener roomClick = v -> {
            Toast.makeText(this, "SR Official Room - Coming Soon", Toast.LENGTH_SHORT).show();
        };

        if(room1 != null) room1.setOnClickListener(roomClick);
        if(room2 != null) room2.setOnClickListener(roomClick);
        if(room3 != null) room3.setOnClickListener(roomClick);
        if(room4 != null) room4.setOnClickListener(roomClick);
    }
}
