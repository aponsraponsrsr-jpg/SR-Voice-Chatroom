package com.sr.voicechat;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    String userName, userImage, groupId, roomName;
    TextView tvRoomName, tvRoomIds, tvChat1, tvChat2;
    ImageView ivExit, ivMute, ivMic, ivSendChat, ivGift, ivGames;
    EditText etChatInput;
    LinearLayout headerProfileLayout;
    RelativeLayout rootLayout;
    
    ImageView[] seats = new ImageView[11];
    TextView[] seatNames = new TextView[11];
    int currentSeat = 3; // ডিফল্ট ৩ নাম্বার সিট

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userName = prefs.getString("user_name", "User");
        userImage = prefs.getString("user_image", "");
        groupId = prefs.getString("group_id", "21133805");
        roomName = prefs.getString("room_name", userName + "'s Room");
        String savedTheme = prefs.getString("room_theme", "#121212");

        // ভিউ ইনিশিয়ালাইজেশন
        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomIds = findViewById(R.id.tvRoomIds);
        tvChat1 = findViewById(R.id.tvChat1);
        tvChat2 = findViewById(R.id.tvChat2);
        ivExit = findViewById(R.id.ivHomeSettings);
        headerProfileLayout = findViewById(R.id.headerProfileLayout);
        rootLayout = findViewById(R.id.rootLayout);
        
        ivMute = findViewById(R.id.ivMute);
        ivMic = findViewById(R.id.ivMic);
        ivSendChat = findViewById(R.id.ivSendChat);
        etChatInput = findViewById(R.id.etChatInput);
        ivGift = findViewById(R.id.ivGift);
        ivGames = findViewById(R.id.ivGames);

        // থিম কালার সেট করা
        try {
            rootLayout.setBackgroundColor(Color.parseColor(savedTheme));
        } catch (Exception e) {
            rootLayout.setBackgroundColor(Color.parseColor("#121212"));
        }

        tvRoomName.setText(roomName);
        tvRoomIds.setText("ID: " + groupId);
        tvChat1.setText(userName + ": Welcome to the room!");
        tvChat2.setText("");

        // সিট এবং নাম বাইন্ডিং
        for (int i = 1; i <= 10; i++) {
            int seatResId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            int nameResId = getResources().getIdentifier("tvName" + i, "id", getPackageName());
            seats[i] = findViewById(seatResId);
            seatNames[i] = findViewById(nameResId);
        }

        updateSeatsDisplay();

        // সিট এক্সচেঞ্জ ও পপআপ
        for (int i = 1; i <= 10; i++) {
            final int targetSeat = i;
            if (seats[i] != null) {
                seats[i].setOnClickListener(v -> {
                    if (currentSeat == targetSeat) {
                        showProfilePopup();
                    } else {
                        currentSeat = targetSeat;
                        updateSeatsDisplay();
                        tvChat1.setText(tvChat2.getText().toString());
                        tvChat2.setText(userName + " moved to seat " + targetSeat);
                        Toast.makeText(this, "Moved to Seat " + targetSeat, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // রুম সেটিংস ডায়ালগ
        if (headerProfileLayout != null) {
            headerProfileLayout.setOnClickListener(v -> showRoomSettingsDialog());
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

        // মাইক, সাউন্ড, গিফট ও গেম বাটন হ্যান্ডলিং
        ivMute.setOnClickListener(v -> Toast.makeText(this, "Audio Muted/Unmuted", Toast.LENGTH_SHORT).show());
        ivMic.setOnClickListener(v -> Toast.makeText(this, "Mic On/Off", Toast.LENGTH_SHORT).show());
        
        ivGift.setOnClickListener(v -> Toast.makeText(this, "Gift Box Clicked (Coming Soon)", Toast.LENGTH_SHORT).show());
        ivGames.setOnClickListener(v -> Toast.makeText(this, "Games Hub Clicked (Coming Soon)", Toast.LENGTH_SHORT).show());

        // বাঁ পাশে রিয়েল-টাইম চ্যাট মেসেজ শো করানোর লজিক
        ivSendChat.setOnClickListener(v -> {
            String message = etChatInput.getText().toString().trim();
            if (!message.isEmpty()) {
                tvChat1.setText(tvChat2.getText().toString());
                tvChat2.setText(userName + ": " + message);
                etChatInput.setText("");
            } else {
                Toast.makeText(this, "দয়া করে কিছু লিখুন", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSeatsDisplay() {
        for (int i = 1; i <= 10; i++) {
            if (seats[i] != null && seatNames[i] != null) {
                if (i == currentSeat) {
                    seatNames[i].setText(userName);
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
                    seatNames[i].setText("");
                    seats[i].setImageDrawable(null);
                }
            }
        }
    }

    private void showRoomSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Room Settings & Themes");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputRoomName = new EditText(this);
        inputRoomName.setHint("রুমের নাম লিখুন");
        inputRoomName.setText(roomName);
        inputRoomName.setTextColor(Color.WHITE);
        layout.addView(inputRoomName);

        final TextView tvThemeLabel = new TextView(this);
        tvThemeLabel.setText("\nথিম সিলেক্ট করুন:");
        tvThemeLabel.setTextColor(Color.parseColor("#DDA0FF"));
        layout.addView(tvThemeLabel);

        String[] themeNames = {"Dark Classic (#121212)", "Royal Dark (#1E1E2F)", "Tiger Gold (#2E1C0C)", "Dragon Teal (#0A2F2D)"};
        String[] themeCodes = {"#121212", "#1E1E2F", "#2E1C0C", "#0A2F2D"};

        builder.setView(layout);

        builder.setSingleChoiceItems(themeNames, -1, (dialog, which) -> {
            String selectedTheme = themeCodes[which];
            try {
                rootLayout.setBackgroundColor(Color.parseColor(selectedTheme));
            } catch (Exception e) {
                selectedTheme = "#121212";
            }

            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
            editor.putString("room_theme", selectedTheme);
            editor.apply();
            Toast.makeText(this, "থিম ব্যাকগ্রাউন্ড পরিবর্তন হয়েছে!", Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Update Name", (dialog, which) -> {
            String newName = inputRoomName.getText().toString().trim();
            if (!newName.isEmpty()) {
                roomName = newName;
                tvRoomName.setText(roomName);
                SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                editor.putString("room_name", roomName);
                editor.apply();
                Toast.makeText(this, "রুমের নাম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showProfilePopup() {
        new AlertDialog.Builder(this)
                .setTitle("User Profile")
                .setMessage("Name: " + userName + "\nID: " + groupId)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
