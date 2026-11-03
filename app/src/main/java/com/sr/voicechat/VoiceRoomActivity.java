package com.sr.voicechat;

import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        // ১. পাওয়ার/এক্সিট বাটন ইনিশিয়ালাইজেশন
        btnPower = findViewById(R.id.btnPower);
        if (btnPower != null) {
            btnPower.setOnClickListener(v -> {
                CharSequence[] options = {"Minimize", "Exit Room"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("Room Options");
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        moveTaskToBack(true); // Minimize Action
                    } else {
                        finish(); // Exit Action
                    }
                });
                builder.show();
            });
        }

        // ২. ১০টি সিটে ক্লিক করার লজিক সেটআপ
        setupSeatClicks();

        // ৩. চ্যাট মেসেজ পাঠানোর লজিক
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMsg = findViewById(R.id.btnSendMsg);
        
        if (btnSendMsg != null && etMessageInput != null) {
            btnSendMsg.setOnClickListener(v -> {
                String msg = etMessageInput.getText().toString().trim();
                if (!msg.isEmpty()) {
                    Toast.makeText(this, "Message sent: " + msg, Toast.LENGTH_SHORT).show();
                    etMessageInput.setText(""); // বক্স খালি করে দেওয়া
                } else {
                    Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ৪. নিচের অন্যান্য বাটনগুলোর ক্লিক ইভেন্ট
        btnMicToggle = findViewById(R.id.btnMicToggle);
        if (btnMicToggle != null) {
            btnMicToggle.setOnClickListener(v -> Toast.makeText(this, "Mic Muted/Unmuted", Toast.LENGTH_SHORT).show());
        }

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) {
            btnGiftBox.setOnClickListener(v -> Toast.makeText(this, "Opening Gift Box...", Toast.LENGTH_SHORT).show());
        }

        btnGameBox = findViewById(R.id.btnGameBox);
        if (btnGameBox != null) {
            btnGameBox.setOnClickListener(v -> Toast.makeText(this, "Opening Game Box...", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupSeatClicks() {
        // ১০টি সিটের আইডি অ্যারেতে রেখে লুপের মাধ্যমে ক্লিক লিসেনার সেট করা হলো (যাতে কোড পরিষ্কার থাকে)
        int[] seatLayoutIds = {
            R.id.layoutSeat1, R.id.layoutSeat2, R.id.layoutSeat3, R.id.layoutSeat4, R.id.layoutSeat5,
            R.id.layoutSeat6, R.id.layoutSeat7, R.id.layoutSeat8, R.id.layoutSeat9, R.id.layoutSeat10
        };

        View.OnClickListener seatClickListener = v -> showMicActionDialog();

        for (int id : seatLayoutIds) {
            View seatView = findViewById(id);
            if (seatView != null) {
                seatView.setOnClickListener(seatClickListener);
            }
        }
    }

    private void showMicActionDialog() {
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
    }
}
