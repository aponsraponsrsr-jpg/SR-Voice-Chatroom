package com.sr.voicechat;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    String userName, userImage, groupId, roomName;
    TextView tvRoomName, tvRoomIds, tvSeat3Name, tvEntryLog;
    ImageView seat3, ivExit;
    LinearLayout headerProfileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userName = prefs.getString("user_name", "User");
        userImage = prefs.getString("user_image", "");
        groupId = prefs.getString("group_id", "11458875");
        roomName = prefs.getString("room_name", userName + " Room");

        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomIds = findViewById(R.id.tvRoomIds);
        tvSeat3Name = findViewById(R.id.tvSeat3Name);
        tvEntryLog = findViewById(R.id.tvEntryLog);
        seat3 = findViewById(R.id.seat3);
        ivExit = findViewById(R.id.ivHomeSettings);
        headerProfileLayout = findViewById(R.id.headerProfileLayout);

        tvRoomName.setText(roomName);
        tvRoomIds.setText("ID: " + groupId);
        tvSeat3Name.setText(userName);
        tvEntryLog.setText(userName + " entered the room");

        if (userImage != null && !userImage.isEmpty()) {
            try {
                seat3.setImageURI(Uri.parse(userImage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        seat3.setOnClickListener(v -> Toast.makeText(this, "You are on seat 3: " + userName, Toast.LENGTH_SHORT).show());

        if (headerProfileLayout != null) {
            headerProfileLayout.setOnClickListener(v -> showEditRoomDialog());
        }

        if (ivExit != null) {
            ivExit.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Exit Room")
                        .setMessage("আপনি কি রুম থেকে বের হতে চান?")
                        .setPositiveButton("হ্যাঁ", (dialog, which) -> finish())
                        .setNegativeButton("না", null)
                        .show();
            });
        }

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

    private void showEditRoomDialog() {
        final EditText inputRoomName = new EditText(this);
        inputRoomName.setHint("নতুন রুমের নাম লিখুন");
        inputRoomName.setText(roomName);
        inputRoomName.setPadding(40, 30, 40, 30);
        inputRoomName.setTextColor(getResources().getColor(android.R.color.white));

        new AlertDialog.Builder(this)
                .setTitle("Edit Room Name")
                .setView(inputRoomName)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = inputRoomName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        roomName = newName;
                        tvRoomName.setText(roomName);
                        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                        editor.putString("room_name", roomName);
                        editor.apply();
                        Toast.makeText(this, "রুমের নাম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
