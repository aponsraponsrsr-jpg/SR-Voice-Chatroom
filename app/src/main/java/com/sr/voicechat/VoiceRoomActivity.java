package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
        chatMessages.add("💬 ইউজাররা এখন সিটে বসে কথা বলতে পারবেন।");

        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        if (chatListView != null) {
            chatListView.setAdapter(chatAdapter);
        }

        // ৩. ১০টি সিটে বসার এবং ছবি/নাম রেন্ডার করার লজিক
        setupSeatInteractions();

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

    // সিটে ক্লিক করলে ইউজারের প্রোফাইল ছবি ও নাম রেন্ডার করার লজিক
    private void setupSeatInteractions() {
        int[] seatCardIds = {
            R.id.seatCard1, R.id.seatCard2, R.id.seatCard3, R.id.seatCard4, R.id.seatCard5,
            R.id.seatCard6, R.id.seatCard7, R.id.seatCard8, R.id.seatCard9, R.id.seatCard10
        };

        int[] seatImgIds = {
            R.id.seatImg1, R.id.seatImg2, R.id.seatImg3, R.id.seatImg4, R.id.seatImg5,
            R.id.seatImg6, R.id.seatImg7, R.id.seatImg8, R.id.seatImg9, R.id.seatImg10
        };

        int[] seatTxtIds = {
            R.id.seatTxt1, R.id.seatTxt2, R.id.seatTxt3, R.id.seatTxt4, R.id.seatTxt5,
            R.id.seatTxt6, R.id.seatTxt7, R.id.seatTxt8, R.id.seatTxt9, R.id.seatTxt10
        };

        for (int i = 0; i < seatCardIds.length; i++) {
            final int seatIndex = i;
            final int seatNumber = i + 1;
            View seatContainer = findViewById(seatCardIds[i]);

            if (seatContainer != null) {
                seatContainer.setOnClickListener(v -> {
                    String seatTitle = (seatNumber == 1) ? "Host Seat" : "Seat " + seatNumber;
                    CharSequence[] seatOptions = {"Take Mic (" + seatTitle + ")", "Leave Mic"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                    builder.setTitle("🎙️ " + seatTitle + " Options");
                    builder.setItems(seatOptions, (dialog, which) -> {
                        String userName = prefs.getString("USER_NAME", "User");
                        String userImage = prefs.getString("USER_IMAGE", "");
                        ImageView seatImg = findViewById(seatImgIds[seatIndex]);
                        TextView seatTxt = findViewById(seatTxtIds[seatIndex]);

                        if (which == 0) {
                            // Take Mic: ইউজারের ছবি ও নাম সিটে সেট করা
                            if (!userImage.isEmpty() && seatImg != null) {
                                try {
                                    seatImg.setImageURI(Uri.parse(userImage));
                                    seatImg.setPadding(0, 0, 0, 0);
                                } catch (Exception e) {
                                    seatImg.setImageResource(android.R.drawable.ic_menu_camera);
                                }
                            }
                            if (seatTxt != null) {
                                seatTxt.setText(userName);
                            }

                            Toast.makeText(VoiceRoomActivity.this, "Joined " + seatTitle + " successfully!", Toast.LENGTH_SHORT).show();
                            chatMessages.add("🎙️ System: " + userName + " took " + seatTitle);
                            chatAdapter.notifyDataSetChanged();
                        } else {
                            // Leave Mic: সিট খালি করে ডিফল্ট অবস্থায় ফিরিয়ে আনা
                            if (seatImg != null) {
                                seatImg.setImageResource(android.R.drawable.ic_menu_camera);
                                seatImg.setPadding(8, 8, 8, 8);
                            }
                            if (seatTxt != null) {
                                seatTxt.setText(seatNumber == 1 ? "Host" : "Seat " + seatNumber);
                            }

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
