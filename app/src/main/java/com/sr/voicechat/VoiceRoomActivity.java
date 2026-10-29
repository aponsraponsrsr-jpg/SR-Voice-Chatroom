package com.sr.voicechat;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    String userName, userImage, groupId, roomName;
    TextView tvRoomName, tvRoomIds, tvEntryLog;
    ImageView ivExit, ivMute, ivMic, ivSendChat;
    EditText etChatInput;
    LinearLayout headerProfileLayout;
    RelativeLayout rootLayout;
    
    ImageView[] seats = new ImageView[11];
    int currentSeat = 3; // ডিফল্ট ৩ নাম্বার সিট

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userName = prefs.getString("user_name", "User");
        userImage = prefs.getString("user_image", "");
        groupId = prefs.getString("group_id", "11458875");
        roomName = prefs.getString("room_name", userName + " Room");
        String savedTheme = prefs.getString("room_theme", "#121212");

        // ভিউ ইনিশিয়ালাইজেশন
        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomIds = findViewById(R.id.tvRoomIds);
        tvEntryLog = findViewById(R.id.tvEntryLog);
        ivExit = findViewById(R.id.ivHomeSettings);
        headerProfileLayout = findViewById(R.id.headerProfileLayout);
        rootLayout = findViewById(R.id.rootLayout);
        
        ivMute = findViewById(R.id.ivMute);
        ivMic = findViewById(R.id.ivMic);
        ivSendChat = findViewById(R.id.ivSendChat);
        etChatInput = findViewById(R.id.etChatInput);

        // থিম ব্যাকগ্রাউন্ড সেট করা
        try {
            rootLayout.setBackgroundColor(Color.parseColor(savedTheme));
        } catch (Exception e) {
            rootLayout.setBackgroundColor(Color.parseColor("#121212"));
        }

        tvRoomName.setText(roomName);
        tvRoomIds.setText("ID: " + groupId);
        tvEntryLog.setText(userName + " joined seat 3");

        for (int i = 1; i <= 10; i++) {
            int resId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            seats[i] = findViewById(resId);
        }

        updateSeatsDisplay();

        // সিট এক্সচেঞ্জ ও ইউজার প্রোফাইল পপআপ (সিটে ক্লিক করলে নিজের আইডি ও নাম দেখাবে)
        for (int i = 1; i <= 10; i++) {
            final int targetSeat = i;
            if (seats[i] != null) {
                seats[i].setOnClickListener(v -> {
                    if (currentSeat == targetSeat) {
                        // নিজের সিটে ক্লিক করলে প্রোফাইল পপআপ দেখাবে (আইডি ও নাম সহ)
                        showProfilePopup();
                    } else {
                        // খালি সিটে ক্লিক করলে সিট চেঞ্জ হবে
                        currentSeat = targetSeat;
                        updateSeatsDisplay();
                        tvEntryLog.setText(userName + " moved to seat " + targetSeat);
                        Toast.makeText(this, "Moved to Seat " + targetSeat, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // হেডারে ক্লিক করলে রুম এডিট ও থিম পরিবর্তনের অপশন
        if (headerProfileLayout != null) {
            headerProfileLayout.setOnClickListener(v -> showEditRoomDialog());
        }

        // এক্সিট বাটন
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

        // কন্ট্রোল বার বাটনগুলো সক্রিয় করা
        ivMute.setOnClickListener(v -> Toast.makeText(this, "Audio Muted/Unmuted", Toast.LENGTH_SHORT).show());
        ivMic.setOnClickListener(v -> Toast.makeText(this, "Mic On/Off", Toast.LENGTH_SHORT).show());

        ivSendChat.setOnClickListener(v -> {
            String message = etChatInput.getText().toString().trim();
            if (!message.isEmpty()) {
                tvEntryLog.setText(userName + ": " + message);
                etChatInput.setText("");
                Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                    seats[i].setImageDrawable(null);
                }
            }
        }
    }

    // রুম এডিট এবং থিম পরিবর্তনের ডায়ালগ
    private void showEditRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Room Settings & Theme");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputRoomName = new EditText(this);
        inputRoomName.setHint("রুমের নাম লিখুন");
        inputRoomName.setText(roomName);
        inputRoomName.setTextColor(Color.WHITE);
        layout.addView(inputRoomName);

        final EditText inputThemeColor = new EditText(this);
        inputThemeColor.setHint("থিম কালার কোড (যেমন: #1E1E2F বা #221133)");
        inputThemeColor.setText("#121212");
        inputThemeColor.setTextColor(Color.WHITE);
        layout.addView(inputThemeColor);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = inputRoomName.getText().toString().trim();
            String newTheme = inputThemeColor.getText().toString().trim();

            if (!newName.isEmpty()) {
                roomName = newName;
                tvRoomName.setText(roomName);
            }

            try {
                rootLayout.setBackgroundColor(Color.parseColor(newTheme));
            } catch (Exception e) {
                newTheme = "#121212";
            }

            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
            editor.putString("room_name", roomName);
            editor.putString("room_theme", newTheme);
            editor.apply();

            Toast.makeText(this, "রুম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // ইউজারের নিজস্ব প্রোফাইল পপআপ (সিটে ক্লিক করলে আইডি ও নাম দেখাবে)
    private void showProfilePopup() {
        new AlertDialog.Builder(this)
                .setTitle("User Profile")
                .setMessage("Name: " + userName + "\nID: " + groupId)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
