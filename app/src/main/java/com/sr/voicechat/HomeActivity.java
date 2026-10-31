package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private ImageView btnCreateRoom, btnMeTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // এখানে তোমার লেআউটের আইডি অনুযায়ী প্লাস বাটন ও মি ট্যাব যুক্ত করা হলো
        // যদি আইডির নাম ভিন্ন হয়, তবে লেআউট অনুযায়ী মিলিয়ে নিতে পারো
        
        // উদাহরণস্বরূপ নিচের বারে 'Me' ট্যাবের আইডি (যদি tvMe বা btnMe হয়)
        // অথবা তুমি তোমার লেআউট অনুযায়ী ভিউ ফাইন্ড করে নিতে পারো।
        
        // আমরা সরাসরি নিচে প্লাস (+) বাটন এবং Me বাটনের ক্লিকের লজিক সেট করছি:
        
        // ধরা যাক হোমপেজের নিচে 'Me' অপশনের আইডি R.id.navMe অথবা তোমার লেআউট অনুযায়ী
        // নিচে রুম ক্রিয়েশনের জন্য '+' বাটনের ক্লিকের কাজ:
        
        // '+' বাটনে ক্লিক করলে রুম ক্রিয়েট করার ডায়ালগ আসবে
        // (তুমি চাইলে সরাসরি VoiceroomActivity-তেও রিডাইরেক্ট করতে পারো)
        
        // নিচে Me পেজে যাওয়ার Intent:
        // নিচের বারে যখন 'Me' অপশনে ক্লিক করা হবে:
        android.view.View meTab = findViewById(R.id.meTab); // লেআউটে আইডি না থাকলে জানিয়ে দিও
        if (meTab != null) {
            meTab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }

        // মাঝখানের '+' আইকনে ক্লিক করলে রুম ক্রিয়েশন পপআপ বা VoiceroomActivity ওপেন হবে
        android.view.View addRoomBtn = findViewById(R.id.addRoomBtn); // লেআউট অনুযায়ী আইডি
        if (addRoomBtn != null) {
            addRoomBtn.setOnClickListener(v -> {
                // রুম ক্রিয়েশন ডায়ালগ ওপেন করা
                showCreateRoomDialog();
            });
        }
    }

    private void showCreateRoomDialog() {
        Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.dialog_create_room); // যদি লেআউট থাকে অথবা সরাসরি ভয়েস রুমে যাবে
        
        EditText etRoomName = dialog.findViewById(R.id.etRoomName);
        Button btnSaveRoom = dialog.findViewById(R.id.btnSaveRoom);

        if (btnSaveRoom != null) {
            btnSaveRoom.setOnClickListener(v -> {
                String roomName = etRoomName.getText().toString().trim();
                if (!roomName.isEmpty()) {
                    dialog.dismiss();
                    // সরাসরি ১০-সিটের ভয়েস রুমে চলে যাবে
                    Intent intent = new Intent(HomeActivity.this, VoiceroomActivity.java);
                    intent.putExtra("ROOM_NAME", roomName);
                    startActivity(intent);
                }
            });
        }
        dialog.show();
    }
}
