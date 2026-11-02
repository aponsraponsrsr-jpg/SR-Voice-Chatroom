package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        String roomName = getIntent().getStringExtra("roomName");
        TextView txtRoom = findViewById(R.id.txtRoomName);
        txtRoom.setText(roomName);
        RecyclerView recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new GridLayoutManager(this, 4));
        List<UserModel> users = new ArrayList<>();
        users.add(new UserModel("Apon", true));
        users.add(new UserModel("SR", false));
        recyclerUsers.setAdapter(new UserAdapter(users));
    }
}
