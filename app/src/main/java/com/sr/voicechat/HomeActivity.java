package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    CircleImageView imgUser;
    TextView txtName;
    RecyclerView recyclerRooms;
    FloatingActionButton fabCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        imgUser = findViewById(R.id.imgUser);
        txtName = findViewById(R.id.txtName);
        recyclerRooms = findViewById(R.id.recyclerRooms);
        fabCreate = findViewById(R.id.fabCreate);

        SharedPreferences pref = getSharedPreferences("SRVoice", MODE_PRIVATE);
        String name = pref.getString("userName", "User");
        String imgStr = pref.getString("userImage", "");
        txtName.setText("Hi, " + name);
        if (!imgStr.isEmpty()) {
            try {
                imgUser.setImageURI(Uri.parse(imgStr));
            } catch (Exception e) {}
        }

        recyclerRooms.setLayoutManager(new LinearLayoutManager(this));
        List<RoomModel> rooms = new ArrayList<>();
        rooms.add(new RoomModel("SR Adda Room", "Bengali, Hindi, English", 12));
        rooms.add(new RoomModel("Voice Lovers", "Fun Chat", 5));
        rooms.add(new RoomModel("Night Chill", "Music & Talk", 8));
        recyclerRooms.setAdapter(new RoomAdapter(rooms, room -> {
            Intent i = new Intent(HomeActivity.this, ChatRoomActivity.class);
            i.putExtra("roomName", room.getName());
            startActivity(i);
        }));

        fabCreate.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, ChatRoomActivity.class);
            i.putExtra("roomName", "My Room - " + name);
            startActivity(i);
        });
    }
}
