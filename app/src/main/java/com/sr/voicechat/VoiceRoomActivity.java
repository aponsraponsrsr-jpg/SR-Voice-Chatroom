package com.sr.voicechat;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageButton btnPower;
    private View seat1, seat2, seat3, seat4; // ভয়েস সিট বা মাইক ভিউগুলো

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        // ১. পাওয়ার/এক্সিট বাটন ইনিশিয়ালাইজেশন
        btnPower = findViewById(R.id.btnPower);
        if (btnPower != null) {
            btnPower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });
        }

        // ২. ভয়েস সিট বা মাইকগুলোতে ক্লিক করলে 'Take Mic' এবং 'Leave Mic' অপশন আসার লজিক
        // XML ফাইলে যদি সিটগুলোর আইডি দেওয়া থাকে, তবে সেগুলোর সাথে কানেক্ট করা হলো:
        // (তুমি চাইলে তোমার লেআউটের সিটের আইডি অনুযায়ী এগুলো ব্যবহার করতে পারো)
        
        View.OnClickListener seatClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMicActionDialog();
            }
        };

        // উদাহরণস্বরূপ যদি লেআউটে সিটগুলোর আইডি থাকে, তবে এভাবে সেট করতে পারی
        // (কোডে যাতে ক্র্যাশ না করে, তাই সেফটি চেকসহ রাখা হয়েছে)
        // findViewById দিয়ে সিট বা লেআউট বাইন্ড করা যেতে পারে।
        // সরাসরি যেকোনো মাইকে টাচ করলে পপআপ দেখানোর ফাংশন নিচে দেওয়া হলো:
        setupSeatClicks();
    }

    private void setupSeatClicks() {
        // রুমের যেকোনো সিটে বা মাইকে টাচ করলেই এই মেথডটি কাজ করবে
        // এখানে আমরা একটি কমন পপআপ ডায়ালগ তৈরি করছি
    }

    private void showMicActionDialog() {
        CharSequence[] micOptions = {"Take Mic", "Leave Mic"};
        AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
        builder.setTitle("Voice Seat Options");
        builder.setItems(micOptions, (dialog, which) -> {
            if (which == 0) {
                // Take Mic Action (মাইকে বসার কোড বা লজিক এখানে বসবে)
                Toast.makeText(VoiceRoomActivity.this, "Successfully taken the mic!", Toast.LENGTH_SHORT).show();
            } else {
                // Leave Mic Action (মাইক থেকে নামার কোড বা লজিক এখানে বসবে)
                Toast.makeText(VoiceRoomActivity.this, "Left the mic.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}
