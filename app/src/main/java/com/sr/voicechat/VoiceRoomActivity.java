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
    TextView tvRoomName, tvRoomIds, tvUserInfoBottom, tvEntryLog;
    ImageView ivExit;
    LinearLayout headerProfileLayout;
    
    // সিটগুলোর অ্যারে এবং কারেন্ট সিট ট্র্যাক করার জন্য
    ImageView[] seats = new ImageView[11]; // 1 to 10
    int currentSeat = 3; // ডিফল্ট ৩ নাম্বার সিটে ইউজার থাকবে

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
        tvUserInfoBottom = findViewById(R.id.tvUserInfoBottom);
        tvEntryLog = findViewById(R.id.tvEntryLog);
        ivExit = findViewById(R.id.ivHomeSettings);
        headerProfileLayout = findViewById(R.id.headerProfileLayout);

        tvRoomName.setText(roomName);
        tvRoomIds.setText("ID: " + groupId);
        
        // নিচে নাম ও আইডি একসাথে শো করার জন্য
        tvUserInfoBottom.setText(userName + " (ID: " + groupId + ")");
        tvEntryLog.setText(userName + " joined the room on seat 3");

        // ১ থেকে ১০ পর্যন্ত সিট ইনিশিয়ালাইজ করা এবং ক্লিক লজিক (সিট এক্সচেঞ্জ)
        for (int i = 1; i <= 10; i++) {
            int resId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            seats[i] = findViewById(resId);
        }

        // প্রথমে ৩ নাম্বার সিটে ইউজারের ছবি বসানো
        updateSeatsDisplay();

        // যেকোনো সিটে ক্লিক করলে ইউজার সেই সিটে চলে যাবে (Seat Exchange)
        for (int i = 1; i <= 10; i++) {
            final int targetSeat = i;
            if (seats[i] != null) {
                seats[i].setOnClickListener(v -> {
                    currentSeat = targetSeat;
                    updateSeatsDisplay();
                    tvEntryLog.setText(userName + " moved to seat " + targetSeat);
                    Toast.makeText(this, "Moved to Seat " + targetSeat, Toast.LENGTH_SHORT).show();
                });
            }
        }

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
    }

    // সিট আপডেট করার মেথড (যে সিটে ইউজার থাকবে সেখানে ছবি দেখাবে, বাকিগুলো খালি থাকবে)
    private void updateSeatsDisplay() {
        for (int i = 1; i <= 10; i++) {
            if (seats[i] != null) {
                if (i == currentSeat) {
                    if (userImage != null && !userImage.isEmpty()) {
                        try {
                            seats[i].setImageURI(Uri.parse(userImage));
                        } catch (Exception e) {
                            seats[i].setImageResource(android.R.drawable.sym_def_app_icon);
                        }
                    } else {
                        seats[i].setImageResource(android.R.drawable.sym_def_app_icon);
                    }
                } else {
                    seats[i].setImageDrawable(null); // খালি সিট
                }
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
