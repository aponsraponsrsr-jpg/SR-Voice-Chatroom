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
    int currentSeat = -1; // শুরুতে কোনো সিটে থাকবে না, নিজের ইচ্ছায় বসবে

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        SharedPreferences prefs = getSharedPreferences("SR_Ultimate_Fixed_Prefs", MODE_PRIVATE);
        userName = prefs.getString("user_name", "User");
        userImage = prefs.getString("user_image", "");
        groupId = prefs.getString("group_id", "63913508");
        roomName = prefs.getString("room_name", userName + "'s Room");
        String savedTheme = prefs.getString("room_theme", "nature");

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

        applyThemeBackground(savedTheme);

        tvRoomName.setText(roomName);
        tvRoomIds.setText("ID: " + groupId);
        tvChat1.setText("Welcome to " + roomName);
        tvChat2.setText("");

        for (int i = 1; i <= 10; i++) {
            int seatResId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            int nameResId = getResources().getIdentifier("tvName" + i, "id", getPackageName());
            seats[i] = findViewById(seatResId);
            seatNames[i] = findViewById(nameResId);
        }

        updateSeatsDisplay();

        // সিটে ক্লিক করলে Take / Leave Mic অপশন আসবে
        for (int i = 1; i <= 10; i++) {
            final int targetSeat = i;
            if (seats[i] != null) {
                seats[i].setOnClickListener(v -> showSeatActionDialog(targetSeat));
            }
        }

        if (headerProfileLayout != null) {
            headerProfileLayout.setOnClickListener(v -> showRoomSettingsDialog());
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

        ivMute.setOnClickListener(v -> Toast.makeText(this, "Audio Muted/Unmuted", Toast.LENGTH_SHORT).show());
        ivMic.setOnClickListener(v -> Toast.makeText(this, "Mic On/Off", Toast.LENGTH_SHORT).show());
        ivGift.setOnClickListener(v -> Toast.makeText(this, "Gift Box Clicked (Coming Soon)", Toast.LENGTH_SHORT).show());
        ivGames.setOnClickListener(v -> Toast.makeText(this, "Games Hub Clicked (Coming Soon)", Toast.LENGTH_SHORT).show());

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

    private void applyThemeBackground(String themeKey) {
        // প্রকৃতি, সাগর, নদী, বৃষ্টি ভেজা ও টাইগার থিমের রিয়েল ভিজ্যুয়াল ব্যাকগ্রাউন্ড হ্যান্ডলিং
        switch (themeKey) {
            case "nature":
                rootLayout.setBackgroundColor(Color.parseColor("#0A2F2D")); // সবুজ প্রকৃতি শেড
                break;
            case "sea":
                rootLayout.setBackgroundColor(Color.parseColor("#0B1D3A")); // নীল সাগর ও নদী শেড
                break;
            case "rain":
                rootLayout.setBackgroundColor(Color.parseColor("#1A1A2E")); // বৃষ্টি ভেজা পরিবেশ শেড
                break;
            case "couple":
                rootLayout.setBackgroundColor(Color.parseColor("#2E1C2E")); // কাপেল রোমান্টিক থিম শেড
                break;
            case "tiger":
                rootLayout.setBackgroundColor(Color.parseColor("#3D1400")); // টাইগার গোল্ড থিম শেড
                break;
            default:
                rootLayout.setBackgroundColor(Color.parseColor("#121212"));
                break;
        }
    }

    private void showSeatActionDialog(int targetSeat) {
        CharSequence[] options = {"Take Mic (সিটে বসুন)", "Leave Mic (সিট ছাড়ুন)"};
        
        new AlertDialog.Builder(this)
                .setTitle("Seat " + targetSeat + " Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        currentSeat = targetSeat;
                        updateSeatsDisplay();
                        Toast.makeText(this, "Taken Seat " + targetSeat, Toast.LENGTH_SHORT).show();
                    } else if (which == 1) {
                        if (currentSeat == targetSeat) {
                            currentSeat = -1;
                            updateSeatsDisplay();
                            Toast.makeText(this, "Left Seat " + targetSeat, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "আপনি এই সিটে নেই!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
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
        tvThemeLabel.setText("\nপ্রকৃতি ও প্রিমিয়াম থিম সিলেক্ট করুন:");
        tvThemeLabel.setTextColor(Color.parseColor("#DDA0FF"));
        layout.addView(tvThemeLabel);

        String[] themeNames = {
            "🌿 প্রকৃতি ও সবুজ বন", 
            "🌊 নীল সাগর ও নদী", 
            "🌧️ বৃষ্টি ভেজা পরিবেশ", 
            "❤️ কাপেল রোমান্টিক থিম", 
            "🐅 টাইগার গোল্ড থিম"
        };
        String[] themeKeys = {"nature", "sea", "rain", "couple", "tiger"};

        builder.setView(layout);

        builder.setSingleChoiceItems(themeNames, -1, (dialog, which) -> {
            String selectedKey = themeKeys[which];
            applyThemeBackground(selectedKey);

            SharedPreferences.Editor editor = getSharedPreferences("SR_Ultimate_Fixed_Prefs", MODE_PRIVATE).edit();
            editor.putString("room_theme", selectedKey);
            editor.apply();
            Toast.makeText(this, "ব্যাকগ্রাউন্ড থিম সফলভাবে সেট হয়েছে!", Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Update Name", (dialog, which) -> {
            String newName = inputRoomName.getText().toString().trim();
            if (!newName.isEmpty()) {
                roomName = newName;
                tvRoomName.setText(roomName);
                SharedPreferences.Editor editor = getSharedPreferences("SR_Ultimate_Fixed_Prefs", MODE_PRIVATE).edit();
                editor.putString("room_name", roomName);
                editor.apply();
                Toast.makeText(this, "রুমের নাম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
