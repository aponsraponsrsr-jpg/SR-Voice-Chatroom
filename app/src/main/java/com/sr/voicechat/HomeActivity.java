package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

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

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        tvUserId.setText("ID: 100254789");

        // 10 টা রঙিন রুম - এইখানেই বসবে বন্ধু
        List<RoomModel> rooms = new ArrayList<>();
        rooms.add(new RoomModel("Bangla Adda ❤️", "125 talking", 0xFFFF2E7E));
        rooms.add(new RoomModel("Voice Party Night", "98 talking", 0xFF7C4DFF));
        rooms.add(new RoomModel("Dhaka Friends", "210 talking", 0xFF00BCD4));
        rooms.add(new RoomModel("Chill Zone", "56 talking", 0xFF4CAF50));
        rooms.add(new RoomModel("Love Talks", "342 talking", 0xFFFF9800));
        rooms.add(new RoomModel("Gaming Voice", "78 talking", 0xFFE91E63));
        rooms.add(new RoomModel("Midnight Masti", "189 talking", 0xFF9C27B0));
        rooms.add(new RoomModel("SR Official Room", "999+ talking", 0xFF2196F3));
        rooms.add(new RoomModel("Funny Jokes", "67 talking", 0xFF009688));
        rooms.add(new RoomModel("Saudi to BD Live", "45 talking", 0xFF795548));

        rvRooms.setAdapter(new RoomAdapter(rooms));

        fabCreateRoom.setOnClickListener(v -> {
            // Next Step - Create Voice Room
        });
    }
}
