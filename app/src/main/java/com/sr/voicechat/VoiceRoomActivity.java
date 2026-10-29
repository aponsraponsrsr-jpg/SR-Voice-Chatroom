package com.sr.voicechat;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class VoiceRoomActivity extends AppCompatActivity {

    String userName, userImage = "", groupId, roomName;
    TextView tvRoomName, tvRoomIds, tvChat1, tvChat2, tvOnlineCount;
    ImageView ivExit, ivMute, ivMic, ivSendChat, ivGift, ivGames, ivHeaderProfile;
    LinearLayout ivOnlineMembers, headerProfileLayout;
    EditText etChatInput;
    RelativeLayout rootLayout;
    
    ImageView[] seats = new ImageView[11];
    TextView[] seatNames = new TextView[11];
    int currentSeat = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        try {
            SharedPreferences prefs = getSharedPreferences("SR_Ultimate_Fixed_Prefs", MODE_PRIVATE);
            userName = prefs.getString("user_name", "SR User");
            groupId = prefs.getString("group_id", "11458875");
            roomName = prefs.getString("room_name", userName + "'s Room");
            String savedTheme = prefs.getString("room_theme", "tiger");

            File imgFile = new File(getFilesDir(), "permanent_profile.jpg");
            if (imgFile.exists()) {
                userImage = Uri.fromFile(imgFile).toString();
            } else {
                userImage = prefs.getString("user_image", "");
            }

            tvRoomName = findViewById(R.id.tvRoomName);
            tvRoomIds = findViewById(R.id.tvRoomIds);
            tvChat1 = findViewById(R.id.tvChat1);
            tvChat2 = findViewById(R.id.tvChat2);
            tvOnlineCount = findViewById(R.id.tvOnlineCount);
            ivExit = findViewById(R.id.ivHomeSettings);
            headerProfileLayout = findViewById(R.id.headerProfileLayout);
            ivOnlineMembers = findViewById(R.id.ivOnlineMembers);
            rootLayout = findViewById(R.id.rootLayout);
            ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
            
            ivMute = findViewById(R.id.ivMute);
            ivMic = findViewById(R.id.ivMic);
            ivSendChat = findViewById(R.id.ivSendChat);
            etChatInput = findViewById(R.id.etChatInput);
            ivGift = findViewById(R.id.ivGift);
            ivGames = findViewById(R.id.ivGames);

            applyThemeBackground(savedTheme);

            if (tvRoomName != null) tvRoomName.setText(roomName);
            if (tvRoomIds != null) tvRoomIds.setText("ID: " + groupId);
            if (tvOnlineCount != null) tvOnlineCount.setText("1");
            if (tvChat1 != null) tvChat1.setText("Welcome to " + roomName);
            if (tvChat2 != null) tvChat2.setText("");

            if (userImage != null && !userImage.isEmpty() && ivHeaderProfile != null) {
                try {
                    ivHeaderProfile.setImageURI(Uri.parse(userImage));
                } catch (Exception e) {
                    // Ignore
                }
            }

            for (int i = 1; i <= 10; i++) {
                int seatResId = getResources().getIdentifier("seat" + i, "id", getPackageName());
                int nameResId = getResources().getIdentifier("tvName" + i, "id", getPackageName());
                if (seatResId != 0) seats[i] = findViewById(seatResId);
                if (nameResId != 0) seatNames[i] = findViewById(nameResId);
            }

            updateSeatsDisplay();

            for (int i = 1; i <= 10; i++) {
                final int targetSeat = i;
                if (seats[i] != null) {
                    seats[i].setOnClickListener(v -> showSeatActionDialog(targetSeat));
                }
            }

            if (headerProfileLayout != null) {
                headerProfileLayout.setOnClickListener(v -> showRoomSettingsDialog());
            }

            if (ivOnlineMembers != null) {
                ivOnlineMembers.setOnClickListener(v -> showOnlineListDialog());
            }

            if (ivExit != null) {
                ivExit.setOnClickListener(v -> showKeepOrExitDialog());
            }

            if (ivSendChat != null && etChatInput != null) {
                ivSendChat.setOnClickListener(v -> {
                    String message = etChatInput.getText().toString().trim();
                    if (!message.isEmpty()) {
                        if (tvChat1 != null && tvChat2 != null) {
                            tvChat1.setText(tvChat2.getText().toString());
                            tvChat2.setText(userName + ": " + message);
                        }
                        etChatInput.setText("");
                    }
                });
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    @Override
    public void onBackPressed() {
        showKeepOrExitDialog();
    }

    private void showKeepOrExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Room Options")
               .setMessage("আপনি কি রুমটি মিনিমাইজ (Keep) করতে চান নাকি বের হয়ে যেতে চান (Exit)?")
               .setPositiveButton("Keep (মিনিমাইজ)", (dialog, which) -> moveTaskToBack(true))
               .setNegativeButton("Exit (বের হন)", (dialog, which) -> finish())
               .show();
    }

    private void showOnlineListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Online Members & Owner Profile");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView tvUser = new TextView(this);
        tvUser.setText("👑 " + userName + " (ID: " + groupId + ")\n⭐ Role: Room Owner (গ্রুপ ওনার)\n\n👉 [ নিজের প্রোফাইল ভিজিট করতে ট্যাপ করুন ]");
        tvUser.setTextColor(Color.parseColor("#00FFCC"));
        tvUser.setTextSize(15);
        
        tvUser.setOnClickListener(v -> {
            showMyProfileDialog();
        });

        layout.addView(tvUser);

        builder.setView(layout);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showMyProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("My Profile & ID Details");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView tvDetails = new TextView(this);
        tvDetails.setText("👤 Name: " + userName + "\n🆔 ID: " + groupId + "\n👑 Status: Room Owner\n✨ Room: " + roomName);
        tvDetails.setTextColor(Color.WHITE);
        tvDetails.setTextSize(16);
        layout.addView(tvDetails);

        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void applyThemeBackground(String themeKey) {
        if (rootLayout == null) return;
        switch (themeKey) {
            case "nature":
                rootLayout.setBackgroundColor(Color.parseColor("#0F3830"));
                break;
            case "sea":
                rootLayout.setBackgroundColor(Color.parseColor("#0A2540"));
                break;
            case "rain":
                rootLayout.setBackgroundColor(Color.parseColor("#16222A"));
                break;
            case "couple":
                rootLayout.setBackgroundColor(Color.parseColor("#3A1725"));
                break;
            case "tiger":
                rootLayout.setBackgroundColor(Color.parseColor("#4A2500"));
                break;
            default:
                rootLayout.setBackgroundColor(Color.parseColor("#4A2500"));
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
        GradientDrawable circularShape = new GradientDrawable();
        circularShape.setShape(GradientDrawable.OVAL);
        circularShape.setColor(Color.parseColor("#22FFFFFF"));

        for (int i = 1; i <= 10; i++) {
            if (seats[i] != null && seatNames[i] != null) {
                if (i == currentSeat) {
                    seatNames[i].setText(userName);
                    if (userImage != null && !userImage.isEmpty()) {
                        try {
                            seats[i].setImageURI(Uri.parse(userImage));
                            seats[i].setClipToOutline(true);
                        } catch (Exception e) {
                            seats[i].setImageResource(android.R.drawable.sym_def_app_icon);
                        }
                    } else {
                        seats[i].setImageResource(android.R.drawable.sym_def_app_icon);
                    }
                } else {
                    seatNames[i].setText("");
                    seats[i].setImageDrawable(circularShape);
                }
            }
        }
    }

    private void showRoomSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Room Settings & Theme Gallery");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputRoomName = new EditText(this);
        inputRoomName.setHint("রুমের নাম লিখুন");
        inputRoomName.setText(roomName);
        inputRoomName.setTextColor(Color.WHITE);
        layout.addView(inputRoomName);

        final TextView tvThemeLabel = new TextView(this);
        tvThemeLabel.setText("\n🎨 প্রিমিয়াম থিম গ্যালারি (সিলেক্ট করুন):");
        tvThemeLabel.setTextColor(Color.parseColor("#00FFCC"));
        layout.addView(tvThemeLabel);

        String[] themeNames = {
            "🌿 প্রকৃতি ও সবুজ বন (Forest Theme)", 
            "🌊 নীল সাগর ও নদী (Ocean Theme)", 
            "🌧️ বৃষ্টি ভেজা পরিবেশ (Rain Theme)", 
            "❤️ কাপেল রোমান্টিক থিম (Romantic Theme)", 
            "🐅 টাইগার গোল্ড থিম (Tiger Theme)"
        };
        String[] themeKeys = {"nature", "sea", "rain", "couple", "tiger"};

        builder.setView(layout);

        builder.setSingleChoiceItems(themeNames, -1, (dialog, which) -> {
            String selectedKey = themeKeys[which];
            applyThemeBackground(selectedKey);

            SharedPreferences.Editor editor = getSharedPreferences("SR_Ultimate_Fixed_Prefs", MODE_PRIVATE).edit();
            editor.putString("room_theme", selectedKey);
            editor.apply();
            Toast.makeText(this, "থিম ব্যাকগ্রাউন্ড সফলভাবে এপ্লাই হয়েছে!", Toast.LENGTH_SHORT).show();
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
