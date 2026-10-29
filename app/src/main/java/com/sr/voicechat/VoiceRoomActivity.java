package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    String userName, userImage, groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userName = prefs.getString("user_name", "User");
        userImage = prefs.getString("user_image", "");
        groupId = prefs.getString("group_id", "11458875");
        String roomName = prefs.getString("room_name", userName);

        TextView tvRoomName = findViewById(R.id.tvRoomName);
        TextView tvRoomIds = findViewById(R.id.tvRoomIds);
        TextView tvSeat3Name = findViewById(R.id.tvSeat3Name);
        TextView tvEntryLog = findViewById(R.id.tvEntryLog);
        ImageView seat3 = findViewById(R.id.seat3);
        ImageView ivHome = findViewById(R.id.ivHomeSettings);

        // Group Name = যে নাম দিয়েছে সেটাই অটো গ্রুপ নেম
        tvRoomName.setText(roomName);
        tvRoomIds.setText("ID: " + groupId + " | Room ID: " + groupId);
        tvSeat3Name.setText(userName);
        tvEntryLog.setText(userName + " entered the room");

        // 3 নাম্বার সিটে তোমার পিক
        if (userImage != null && !userImage.isEmpty()) {
            try {
                seat3.setImageURI(Uri.parse(userImage));
            } catch (Exception e) {
                // ignore
            }
        }

        // সিটে টাচ করলে নাম শো
        seat3.setOnClickListener(v -> {
            Toast.makeText(this, "You are on seat 3: " + userName, Toast.LENGTH_SHORT).show();
        });

        // Home Settings - বামে আইকনে ক্লিক
        ivHome.setOnClickListener(v -> {
            Toast.makeText(this, "Home Settings - ID: " + groupId, Toast.LENGTH_SHORT).show();
        });

        // বাকি সিটগুলোতে ক্লিক করলে এন্ট্রি
        for (int i = 1; i <= 10; i++) {
            if (i == 3) continue;
            int resId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            ImageView seat = findViewById(resId);
            if (seat != null) {
                int finalI = i;
                seat.setOnClickListener(v -> {
                    tvEntryLog.setText(userName + " want to sit on " + finalI);
                    Toast.makeText(this, "Seat " + finalI, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }
}
