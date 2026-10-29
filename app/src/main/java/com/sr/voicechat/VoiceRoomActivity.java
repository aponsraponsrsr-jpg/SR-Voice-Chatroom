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
            groupId = prefs.getString("group_id", "41525318");
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

            // Header profile picture setup (Circular)
            if (userImage != null && !userImage.isEmpty() && ivHeaderProfile != null) {
                try {
                    ivHeaderProfile.setImageURI(Uri.parse(userImage));
                    GradientDrawable headerCircular = new GradientDrawable();
                    headerCircular.setShape(GradientDrawable.OVAL);
                    ivHeaderProfile.setBackground(headerCircular);
                    ivHeaderProfile.setClipToOutline(true);
                } catch (Exception e) {
                    // Ignore
                }
            }

            if (headerProfileLayout != null) {
                headerProfileLayout.setOnClickListener(v -> showMyProfileDialog());
            }

            if (ivHeaderProfile != null) {
                ivHeaderProfile.setOnClickListener(v -> showMyProfileDialog());
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
                    seats[i].setOnClickListener(v -> {
                        if (currentSeat == targetSeat) {
                            showMyProfileDialog();
                        } else {
                            showSeatActionDialog(targetSeat);
                        }
                    });
                }
            }

            // Online members list dialog with Level & ID info matching video instructions
            if (ivOnlineMembers != null) {
                ivOnlineMembers.setOnClickListener(v -> showOnlineListDialog());
            }

            if (ivExit != null) {
                ivExit.setOnClickListener(v -> showKeepOrExitDialog());
            }

            // Locked features with "Coming Soon! SR Official" message
            if (ivGift != null) {
                ivGift.setOnClickListener(v -> Toast.makeText(this, "Coming Soon! SR Official", Toast.LENGTH_SHORT).show());
            }

            if (ivGames != null) {
                ivGames.setOnClickListener(v -> Toast.makeText(this, "Coming Soon! SR Official", Toast.LENGTH_SHORT).show());
            }

            if (ivMute != null) {
                ivMute.setOnClickListener(v -> Toast.makeText(this, "Mic Muted", Toast.LENGTH_SHORT).show());
            }

            if (ivMic != null) {
                ivMic.setOnClickListener(v -> Toast.makeText(this, "Speaking Active", Toast.LENGTH_SHORT).show());
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
        builder.setTitle("Room Options");
        builder.setMessage("Do you want to minimize the room (Keep) or exit completely?");
        builder.setPositiveButton("Keep", (dialog, which) -> moveTaskToBack(true));
        builder.setNegativeButton("Exit", (dialog, which) -> finish());
        builder.show();
    }

    private void showOnlineListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Online Members List");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView tvUser = new TextView(this);
        tvUser.setText("1. 👑 " + userName + "\n   🆔 ID: " + groupId + "\n   ⭐ Level: 0 | Role: Room Owner\n\n👉 [ Tap here to visit your profile ]");
        tvUser.setTextColor(Color.parseColor("#00FFCC"));
        tvUser.setTextSize(15);
        
        tvUser.setOnClickListener(v -> showMyProfileDialog());

        layout.addView(tvUser);

        builder.setView(layout);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showMyProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("My Profile Details");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView tvDetails = new TextView(this);
        tvDetails.setText("👤 Name: " + userName + "\n🆔 ID: " + groupId + "\n⭐ Level: 0\n👑 Status: Room Owner\n✨ Room: " + roomName);
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
                rootLayout.setBackgroundColor(Color.parseColor("#0B3D2E"));
                break;
            case "sea":
                rootLayout.setBackgroundColor(Color.parseColor("#08203E"));
                break;
            case "rain":
                rootLayout.setBackgroundColor(Color.parseColor("#131C27"));
                break;
            case "couple":
                rootLayout.setBackgroundColor(Color.parseColor("#4A1525"));
                break;
            case "tiger":
                rootLayout.setBackgroundColor(Color.parseColor("#5A2D0C"));
                break;
            default:
                rootLayout.setBackgroundColor(Color.parseColor("#5A2D0C"));
                break;
        }
    }

    private void showSeatActionDialog(int targetSeat) {
        CharSequence[] options = {"Take Mic", "Leave Mic"};
        
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
                            Toast.makeText(this, "You are not on this seat!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void updateSeatsDisplay() {
        GradientDrawable circularShape = new GradientDrawable();
        circularShape.setShape(GradientDrawable.OVAL);
        circularShape.setColor(Color.parseColor("#33FFFFFF"));

        for (int i = 1; i <= 10; i++) {
            if (seats[i] != null && seatNames[i] != null) {
                if (i == currentSeat) {
                    seatNames[i].setText(userName);
                    if (userImage != null && !userImage.isEmpty()) {
                        try {
                            seats[i].setImageURI(Uri.parse(userImage));
                            GradientDrawable dpCircular = new GradientDrawable();
                            dpCircular.setShape(GradientDrawable.OVAL);
                            seats[i].setBackground(dpCircular);
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
}
