package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvRooms;
    private FloatingActionButton fabCreateRoom;
    private TextView tvUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvRooms = findViewById(R.id.rvRooms);
        fabCreateRoom = findViewById(R.id.fabCreateRoom);
        tvUserId = findViewById(R.id.tvUserId);

        // Permanent Base - Room List Setup
        rvRooms.setLayoutManager(new LinearLayoutManager(this));

        // Lifetime ID - Migo Style - Will come from Firebase later
        tvUserId.setText("ID: 100254789");

        // Create Room Button
        fabCreateRoom.setOnClickListener(v -> {
            // Next Step - Create Voice Room
        });
    }
}
