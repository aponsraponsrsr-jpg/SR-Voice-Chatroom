package com.sr.voicechat;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- হোমপেজ - ১ ফাইলে সব, ব্যাকগ্রাউন্ড হালকা পিংক ---
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#FFF0F5")); // হালকা পিংক / গোলাপি
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        // উপরের বার
        TextView topBar = new TextView(this);
        topBar.setText("🌟 SR Voice Chatroom");
        topBar.setTextColor(Color.WHITE);
        topBar.setTextSize(22);
        topBar.setPadding(30, 30, 30, 30);
        topBar.setBackgroundColor(Color.parseColor("#E91E63"));
        mainLayout.addView(topBar);

        // মাঝখানে
        TextView welcome = new TextView(this);
        welcome.setText("Welcome! Tap + to create room");
        welcome.setTextColor(Color.parseColor("#880E4F"));
        welcome.setTextSize(18);
        welcome.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams welcomeParams = new LinearLayout.LayoutParams(-1, 0, 1);
        welcome.setLayoutParams(welcomeParams);
        mainLayout.addView(welcome);

        // + বাটন
        LinearLayout plusRow = new LinearLayout(this);
        plusRow.setGravity(Gravity.END);
        plusRow.setPadding(0, 0, 40, 20);
        Button btnPlus = new Button(this);
        btnPlus.setText("+");
        btnPlus.setTextSize(30);
        btnPlus.setTextColor(Color.WHITE);
        btnPlus.setBackgroundColor(Color.parseColor("#E91E63"));
        btnPlus.setOnClickListener(v -> openCreateRoomDialog());
        plusRow.addView(btnPlus);
        mainLayout.addView(plusRow);

        // নিচের বার - ইমারজেন্সি ৪টা অপশন
        LinearLayout bottomBar = new LinearLayout(this);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setBackgroundColor(Color.parseColor("#FFFFFF")); // সাদা, পিংক ব্যাকগ্রাউন্ডের সাথে সুন্দর লাগবে
        bottomBar.setPadding(0, 12, 0, 12);

        bottomBar.addView(makeNavButton("🏠\nHome", "Home"));
        bottomBar.addView(makeNavButton("✨\nMoment", "Moment"));
        bottomBar.addView(makeNavButton("💬\nSMS", "SMS - Chat List"));
        bottomBar.addView(makeNavButton("👤\nMe", "Me - My Profile"));

        mainLayout.addView(bottomBar);

        setContentView(mainLayout);
    }

    private Button makeNavButton(String text, String toastMsg) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor("#E91E63"));
        btn.setTextSize(13);
        btn.setBackgroundColor(Color.parseColor("#FFFFFF"));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        btn.setLayoutParams(p);
        btn.setOnClickListener(v -> Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show());
        return btn;
    }

    private void openCreateRoomDialog() {
        Dialog dialog = new Dialog(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.parseColor("#FFF0F5"));

        TextView title = new TextView(this);
        title.setText("Create Room");
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.parseColor("#E91E63"));
        layout.addView(title);

        EditText etName = new EditText(this);
        etName.setHint("Room Name লিখো");
        layout.addView(etName);

        Button btnConfirm = new Button(this);
        btnConfirm.setText("Confirm");
        btnConfirm.setBackgroundColor(Color.parseColor("#E91E63"));
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Room চালু হয়ে গেছে!", Toast.LENGTH_LONG).show();
        });
        layout.addView(btnConfirm);

        dialog.setContentView(layout);
        dialog.show();
    }
}
