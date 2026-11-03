package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageButton btnPower;
    private EditText etMessageInput;
    private Button btnSendMsg, btnMicToggle, btnChatOption, btnGiftBox, btnGameBox;
    private boolean isMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        // ১. পাওয়ার/এক্সিট বাটন
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

        // ২. চ্যাট মেসেজ পাঠানোর লজিক
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMsg = findViewById(R.id.btnSendMsg);
        
        if (btnSendMsg != null && etMessageInput != null) {
            btnSendMsg.setOnClickListener(v -> {
                String msg = etMessageInput.getText().toString().trim();
                if (!msg.isEmpty()) {
                    Toast.makeText(this, "Message Sent: " + msg, Toast.LENGTH_SHORT).show();
                    etMessageInput.setText("");
                } else {
                    Toast.makeText(this, "Please write a message first!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ৩. নিচের বাটনগুলো (MIC, CHAT, GIFT, GAME) সচল করা
        btnMicToggle = findViewById(R.id.btnMicToggle);
        if (btnMicToggle != null) {
            btnMicToggle.setOnClickListener(v -> {
                isMuted = !isMuted;
                if (isMuted) {
                    btnMicToggle.setText("🔇 Muted");
                    Toast.makeText(this, "Mic Muted", Toast.LENGTH_SHORT).show();
                } else {
                    btnMicToggle.setText("🎙️ MIC");
                    Toast.makeText(this, "Mic Unmuted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnChatOption = findViewById(R.id.btnChatOption);
        if (btnChatOption != null) {
            btnChatOption.setOnClickListener(v -> {
                CharSequence[] chatTypes = {"All Chat", "Room Notice", "Private Chat"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("💬 CHAT Options");
                builder.setItems(chatTypes, (dialog, which) -> {
                    Toast.makeText(this, "Selected: " + chatTypes[which], Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) {
            btnGiftBox.setOnClickListener(v -> {
                CharSequence[] gifts = {"🌹 Rose (10 Coins)", "👑 Crown (500 Coins)", "💎 Diamond (1000 Coins)"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎁 Send GIFT to Room");
                builder.setItems(gifts, (dialog, which) -> {
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
                builder.setTitle("🎮 Voice Room GAMES");
                builder.setItems(games, (dialog, which) -> {
                    Toast.makeText(this, "Starting " + games[which] + "...", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }

        // ৪. সিটগুলোতে ক্লিক করার ব্যবস্থা (Host এবং অন্যান্য সিট)
        // ইউজার রুমের যেকোনো সিটে টাচ করলেই যাতে মাইকে বসার অপশন আসে
        setupGeneralSeatClicks();
    }

    private void setupGeneralSeatClicks() {
        // যেহেতু লেআউটের সিট আইডিগুলো একেক জনের একেক রকম হতে পারে, তাই পুরো লেআউটে টাচ করলে বা সিটে ক্লিক করলে যেন ডায়ালগ আসে 
        // তার জন্য আমরা স্ক্রিন বা রুমের মূল লেআউটেও এটি রাখতে পারি অথবা নির্দিষ্ট আইডি পেলে যুক্ত করতে পারি।
        // আপাতত সিটে ক্লিক করার পপআপ মেথড নিচে রাখা হলো:
        
        View.OnClickListener seatClick = v -> {
            CharSequence[] micOptions = {"Take Mic", "Leave Mic"};
            AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
            builder.setTitle("Voice Seat Options");
            builder.setItems(micOptions, (dialog, which) -> {
                if (which == 0) {
                    Toast.makeText(VoiceRoomActivity.this, "Successfully taken the mic!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VoiceRoomActivity.this, "Left the mic.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        };

        // যদি তোমার সিটগুলোর আইডি নির্দিষ্ট থাকে (যেমন seat1, seat2...), তবে সেগুলোর সাথে বাইন্ড করা যাবে।
        // তুমি তোমার `activity_voice_room.xml` ফাইলের কোডটা একটু দিলে আমি সরাসরি ওই আইডিগুলো এখানে বসিয়ে দিতে পারব!
    }
}
