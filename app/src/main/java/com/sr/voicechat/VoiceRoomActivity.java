package com.sr.voicechat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    String userName, userImage, groupId, roomName;
    TextView tvRoomName, tvRoomIds, tvSeat3Name, tvEntryLog;
    ImageView seat3, ivExit;
    LinearLayout headerProfileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        // সেভ করা ইউজার ডেটা লোড করা
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userName = prefs.getString("user_name", "User");
        userImage = prefs.getString("user_image", "");
        groupId = prefs.getString("group_id", "11458875");
        roomName = prefs.getString("room_name", userName + " Room");

        // ভিউগুলো ইনিশিয়ালাইজ করা
        tvRoomName = findViewById(R.id.tvRoomName);
        tvRoomIds = findViewById(R.id.tvRoomIds);
        tvSeat3Name = findViewById(R.id.tvSeat3Name);
        tvEntryLog = findViewById(R.id.tvEntryLog);
        seat3 = findViewById(R.id.seat3);
        ivExit = findViewById(R.id.ivHomeSettings); // কোডের এক্সিট বাটন
        headerProfileLayout = findViewById(R.id.headerProfileLayout); // লেআউটের হেডার লেআউট

        // রুমের নাম এবং আইডি সেট করা (আইডি ও রুম আইডি একদম সেম)
        tvRoomName.setText(roomName);
        tvRoomIds.setText("ID: " + groupId);
        tvSeat3Name.setText(userName);
        tvEntryLog.setText(userName + " entered the room");

        // ওনারের ছবি ৩ নাম্বার সিটে সেট করা
        if (userImage != null && !userImage.isEmpty()) {
            try {
                seat3.setImageURI(Uri.parse(userImage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ৩ নাম্বার সিটে ক্লিক করলে
        seat3.setOnClickListener(v -> {
            Toast.makeText(this, "You are on seat 3: " + userName, Toast.LENGTH_SHORT).show();
        });

        // ১. হেডারে (প্রোফাইল/নামের ওপর) ক্লিক করলে রুম এডিট ডায়ালগ ওপেন হবে
        if (headerProfileLayout != null) {
            headerProfileLayout.setOnClickListener(v -> showEditRoomDialog());
        }

        // ২. ডান পাশের এক্সিট (Close) বাটনে ক্লিক করলে রুম থেকে বের হয়ে লগইন পেজে বা মিনিমাইজ হবে
        ivExit.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Room")
                    .setMessage("আপনি কি রুম থেকে বের হতে চান?")
                    .setPositiveButton("হ্যাঁ", (dialog, which) -> {
                        finish(); // রুম বন্ধ করে বের হয়ে যাবে
                    })
                    .setNegativeButton("না", null)
                    .show();
        });

        // ১ থেকে ১০ পর্যন্ত বাকি সিটগুলোর হ্যান্ডলিং
        for (int i = 1; i <= 10; i++) {
            if (i == 3) continue; // ৩ নাম্বার সিট ওনারের জন্য ফিক্সড
            
            int resId = getResources().getIdentifier("seat" + i, "id", getPackageName());
            ImageView seat = findViewById(resId);
            
            if (seat != null) {
                int finalI = i;
                seat.setOnClickListener(v -> {
                    tvEntryLog.setText(userName + " want to sit on " + finalI);
                    Toast.makeText(this, "Seat " + finalI, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    // রুম এডিট করার পপআপ ডায়ালগ
    private void showEditRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(android.R.layout.select_dialog_item, null); // সিম্পল ডায়ালগ বা কাস্টম ফিল্ড
        
        EditText inputRoomName = new EditText(this);
        inputRoomName.setHint("নতুন রুমের নাম লিখুন");
        inputRoomName.setText(roomName);
        inputRoomName.setPadding(40, 30, 40, 30);
        inputRoomName.setTextColor(getResources().getColor(android.R.color.white));

        builder.setTitle("Edit Room Name")
                .setView(inputRoomName)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newName = inputRoomName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        roomName = newName;
                        tvRoomName.setText(roomName);

                        // SharedPreferences-এ আপডেট সেভ করা
                        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                        editor.putString("room_name", roomName);
                        editor.apply();

                        Toast.makeText(this, "রুমের নাম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
