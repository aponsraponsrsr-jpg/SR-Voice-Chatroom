package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageButton btnPower;
    private Button btnMicToggle, btnChatOption, btnGiftBox, btnGameBox;
    private boolean isMuted = false;

    private ListView chatListView;
    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // ১. পাওয়ার / এক্সিট বাটন
        btnPower = findViewById(R.id.btnPower);
        if (btnPower != null) {
            btnPower.setOnClickListener(v -> {
                CharSequence[] options = {"Minimize", "Exit Room"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("Room Options");
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        moveTaskToBack(true);
                    } else {
                        finish();
                    }
                });
                builder.show();
            });
        }

        // ২. স্ক্রিন চ্যাট লিস্ট ইনিশিয়ালাইজেশন
        chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatMessages.add("🌟 System: SR Voice Chatroom এ সবাইকে স্বাগতম!");
        chatMessages.add("💬 ইউজাররা এখন স্ক্রিন চ্যাট ব্যবহার করতে পারবেন।");

        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        if (chatListView != null) {
            chatListView.setAdapter(chatAdapter);
        }

        // ৩. ১০টি সিটে যেকোনো সিটে বসার এবং সিট পরিবর্তনের লজিক
        setupSeatClicks();

        // ৪. নিচের অ্যাকশন বার বাটনগুলো
        btnMicToggle = findViewById(R.id.btnMicToggle);
        if (btnMicToggle != null) {
            btnMicToggle.setOnClickListener(v -> {
                isMuted = !isMuted;
                if (isMuted) {
                    btnMicToggle.setText("🔇 Muted");
                    Toast.makeText(this, "Mic Muted", Toast.LENGTH_SHORT).show();
                } else {
                    btnMicToggle.setText("🎙️ Mic");
                    Toast.makeText(this, "Mic Unmuted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnChatOption = findViewById(R.id.btnChatOption);
        if (btnChatOption != null) {
            btnChatOption.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("💬 Send Message to Room");

                final EditText input = new EditText(VoiceRoomActivity.this);
                input.setHint("Type your message here...");
                builder.setView(input);

                builder.setPositiveButton("Send", (dialog, which) -> {
                    String message = input.getText().toString().trim();
                    String userName = prefs.getString("USER_NAME", "User");
                    if (!message.isEmpty()) {
                        chatMessages.add("👤 " + userName + ": " + message);
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            });
        }

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) {
            btnGiftBox.setOnClickListener(v -> {
                CharSequence[] gifts = {"🌹 Rose (10 Coins)", "👑 Crown (500 Coins)", "💎 Diamond (1000 Coins)"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎁 Send Gift");
                builder.setItems(gifts, (dialog, which) -> {
                    chatMessages.add("🎁 Gift Sent: " + gifts[which]);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Successfully Sent Gift!", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }

        btnGameBox = findViewById(R.id.btnGameBox);
        if (btnGameBox != null) {
            btnGameBox.setOnClickListener(v -> {
                CharSequence[] games = {"🎮 Ludo Game", "🎯 Lucky Wheel", "🎲 Dice Battle"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎮 Voice Room Games");
                builder.setItems(games, (dialog, which) -> {
                    Toast.makeText(this, "Starting " + games[which] + "...", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }
    }

    // যেকোনো সিটে ট্যাপ করলে সিট অকুপাই এবং ইউজারের নাম/ছবি রেন্ডার করার লজিক
    private void setupSeatClicks() {
        int[] seatIds = {
            R.id.seat1, R.id.seat2, R.id.seat3, R.id.seat4, R.id.seat5,
            R.id.seat6, R.id.seat7, R.id.seat8, R.id.seat9, R.id.seat10
        };

        for (int i = 0; i < seatIds.length; i++) {
            final int seatNumber = i + 1;
            View seatView = findViewById(seatIds[i]);
            if (seatView != null) {
                seatView.setOnClickListener(v -> {
                    String seatTitle = (seatNumber == 1) ? "Host Seat" : "Seat " + seatNumber;
                    CharSequence[] seatOptions = {"Take Mic (" + seatTitle + ")", "Switch Seat", "Leave Mic"};
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                    builder.setTitle("🎙️ " + seatTitle + " Options");
                    builder.setItems(seatOptions, (dialog, which) -> {
                        String userName = prefs.getString("USER_NAME", "User");
                        if (which == 0) {
                            Toast.makeText(VoiceRoomActivity.this, "Joined " + seatTitle + " successfully!", Toast.LENGTH_SHORT).show();
                            chatMessages.add("🎙️ System: " + userName + " joined " + seatTitle);
                            chatAdapter.notifyDataSetChanged();
                        } else if (which == 1) {
                            Toast.makeText(VoiceRoomActivity.this, "Switched to " + seatTitle, Toast.LENGTH_SHORT).show();
                            chatMessages.add("🔄 System: " + userName + " switched to " + seatTitle);
                            chatAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(VoiceRoomActivity.this, "Left " + seatTitle, Toast.LENGTH_SHORT).show();
                            chatMessages.add("❌ System: " + userName + " left " + seatTitle);
                            chatAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.show();
                });
            }
        }
    }
}
