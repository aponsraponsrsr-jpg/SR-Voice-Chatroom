package com.sr.voicechat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvRooms;
    private FloatingActionButton fabCreateRoom;
    private TextView tvUserId;
    private TextView tabMine, tabHot, tabDiscover;
    private List<RoomModel> allRooms;
    private RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvRooms = findViewById(R.id.rvRooms);
        fabCreateRoom = findViewById(R.id.fabCreateRoom);
        tvUserId = findViewById(R.id.tvUserId);
        tabMine = findViewById(R.id.tabMine);
        tabHot = findViewById(R.id.tabHot);
        tabDiscover = findViewById(R.id.tabDiscover);

        // 2 কলাম গ্রিড - তোমার Beach ডেমোর মতো
        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
        tvUserId.setText("ID: 100254789");

        // 10 টা রঙিন রুম - Premium Blue Pink Purple
        allRooms = new ArrayList<>();
        allRooms.add(new RoomModel("Bangla Adda ❤️", "125 talking", 0xFFFF2E7E));
        allRooms.add(new RoomModel("Voice Party Night", "98 talking", 0xFF7C4DFF));
        allRooms.add(new RoomModel("Dhaka Friends", "210 talking", 0xFF00BCD4));
        allRooms.add(new RoomModel("Chill Zone", "56 talking", 0xFF4CAF50));
        allRooms.add(new RoomModel("Love Talks", "342 talking", 0xFFFF9800));
        allRooms.add(new RoomModel("Gaming Voice", "78 talking", 0xFFE91E63));
        allRooms.add(new RoomModel("Midnight Masti", "189 talking", 0xFF9C27B0));
        allRooms.add(new RoomModel("SR Official Room", "999+ talking", 0xFF2196F3));
        allRooms.add(new RoomModel("Funny Jokes", "67 talking", 0xFF009688));
        allRooms.add(new RoomModel("Saudi to BD Live", "45 talking", 0xFF795548));

        adapter = new RoomAdapter(allRooms);
        rvRooms.setAdapter(adapter);

        // Mine / Hot / Discover - কালার চেঞ্জ
        tabMine.setOnClickListener(v -> {
            tabMine.setTextColor(Color.parseColor("#FFD700"));
            tabHot.setTextColor(Color.parseColor("#888888"));
            tabDiscover.setTextColor(Color.parseColor("#888888"));
            rvRooms.setAdapter(new RoomAdapter(allRooms)); // Mine = সব রুম
        });

        tabHot.setOnClickListener(v -> {
            tabHot.setTextColor(Color.parseColor("#FFD700"));
            tabMine.setTextColor(Color.parseColor("#888888"));
            tabDiscover.setTextColor(Color.parseColor("#888888"));
            // Popular = বেশি talking
            List<RoomModel> hot = new ArrayList<>();
            for (RoomModel r : allRooms) if (r.members.contains("342") || r.members.contains("999") || r.members.contains("210")) hot.add(r);
            rvRooms.setAdapter(new RoomAdapter(hot));
        });

        tabDiscover.setOnClickListener(v -> {
            tabDiscover.setTextColor(Color.parseColor("#FFD700"));
            tabMine.setTextColor(Color.parseColor("#888888"));
            tabHot.setTextColor(Color.parseColor("#888888"));
            rvRooms.setAdapter(new RoomAdapter(allRooms));
        });

        fabCreateRoom.setOnClickListener(v -> {
            // Next Step - Create Room Page - তোমার Mosque Theme সহ
            startActivity(new Intent(this, CreateRoomActivity.class));
        });
    }
}
