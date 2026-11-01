package com.sr.voicechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.io.File;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageView[] seatImages = new ImageView[8];
    private TextView[] seatNames = new TextView[8];
    private String myName, myImagePath, myId;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_voice_room);
        } catch (Exception e) {
            // Layout না থাকলেও Crash করবে না
            Toast.makeText(this, "Room Opening...", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
        myName = prefs.getString("userName", "Sr voice");
        myImagePath = prefs.getString("userImage", "");
        myId = IdManager.getOrCreateUserId(this);

        // 8 টা Seat খুঁজে বের করো - ID না থাকলেও Crash হবে না
        for (int i = 0; i < 8; i++) {
            int num = i + 1;
            seatImages[i] = findImg("seat" + num + "Image", "imgSeat" + num, "seat_img_" + num, "ivSeat" + num);
            seatNames[i] = findTxt("seat" + num + "Name", "tvSeat" + num, "nameSeat" + num);
            
            final int index = i;
            if (seatImages[i] != null) {
                seatImages[i].setOnClickListener(v -> sitOnSeat(index));
            }
        }

        // Back Button
        try {
            android.view.View btnBack = findViewById(getResources().getIdentifier("btnBack", "id", getPackageName()));
            if (btnBack == null) btnBack = findViewById(getResources().getIdentifier("ivBack", "id", getPackageName()));
            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        } catch (Exception e) {}

        // Leave Button
        try {
            android.view.View btnLeave = findViewById(getResources().getIdentifier("btnLeave", "id", getPackageName()));
            if (btnLeave == null) btnLeave = findViewById(getResources().getIdentifier("btnExit", "id", getPackageName()));
            if (btnLeave != null) btnLeave.setOnClickListener(v -> finish());
        } catch (Exception e) {}

        Toast.makeText(this, "Welcome " + myName + " - Click any seat", Toast.LENGTH_SHORT).show();
    }

    // Seat এ বসার Main Logic - এখানেই ছবি Show হবে
    private void sitOnSeat(int index) {
        if (index < 0 || index >= 8) return;
        if (seatImages[index] == null) {
            Toast.makeText(this, "Seat " + (index+1) + " Clicked", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. নাম লাগাও
        if (seatNames[index] != null) {
            seatNames[index].setText(myName);
        }

        // 2. ছবি লাগাও - MeActivity তে Save করা Permanent ছবি
        if (myImagePath != null && !myImagePath.isEmpty()) {
            try {
                File file = new File(myImagePath);
                if (file.exists()) {
                    Glide.with(this).load(file).circleCrop().into(seatImages[index]);
                } else {
                    // পুরানো URI হলে
                    Glide.with(this).load(myImagePath).circleCrop().into(seatImages[index]);
                }
                Toast.makeText(this, "You are on Seat " + (index+1), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                try {
                    seatImages[index].setImageResource(android.R.drawable.sym_def_app_icon);
                } catch (Exception e2) {}
            }
        } else {
            Toast.makeText(this, "First set photo in Me", Toast.LENGTH_SHORT).show();
            // Default Icon
            try {
                seatImages[index].setImageResource(android.R.drawable.sym_def_app_icon);
            } catch (Exception e) {}
            if (seatNames[index] != null) seatNames[index].setText(myName);
        }
    }

    private ImageView findImg(String... names) {
        for (String n : names) {
            int id = getResources().getIdentifier(n, "id", getPackageName());
            if (id != 0) {
                try {
                    ImageView v = findViewById(id);
                    if (v != null) return v;
                } catch (Exception e) {}
            }
        }
        return null;
    }

    private TextView findTxt(String... names) {
        for (String n : names) {
            int id = getResources().getIdentifier(n, "id", getPackageName());
            if (id != 0) {
                try {
                    TextView v = findViewById(id);
                    if (v != null) return v;
                } catch (Exception e) {}
            }
        }
        return null;
    }
}
