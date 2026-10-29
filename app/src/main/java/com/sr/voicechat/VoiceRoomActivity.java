package com.sr.voicechat;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView roomNameText, roomIdText;
    private ImageView[] seatImages = new ImageView[10];
    private TextView[] seatNames = new TextView[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        roomNameText = findViewById(R.id.roomNameText);
        roomIdText = findViewById(R.id.roomIdText);

        // Seat Images - ImageView (not Circle)
        seatImages[0] = findViewById(R.id.seat1Image);
        seatImages[1] = findViewById(R.id.seat2Image);
        seatImages[2] = findViewById(R.id.seat3Image);
        seatImages[3] = findViewById(R.id.seat4Image);
        seatImages[4] = findViewById(R.id.seat5Image);
        seatImages[5] = findViewById(R.id.seat6Image);
        seatImages[6] = findViewById(R.id.seat7Image);
        seatImages[7] = findViewById(R.id.seat8Image);
        seatImages[8] = findViewById(R.id.seat9Image);
        seatImages[9] = findViewById(R.id.seat10Image);

        // Seat Names
        seatNames[0] = findViewById(R.id.seat1Name);
        seatNames[1] = findViewById(R.id.seat2Name);
        seatNames[2] = findViewById(R.id.seat3Name);
        seatNames[3] = findViewById(R.id.seat4Name);
        seatNames[4] = findViewById(R.id.seat5Name);
        seatNames[5] = findViewById(R.id.seat6Name);
        seatNames[6] = findViewById(R.id.seat7Name);
        seatNames[7] = findViewById(R.id.seat8Name);
        seatNames[8] = findViewById(R.id.seat9Name);
        seatNames[9] = findViewById(R.id.seat10Name);

        // Generate Unique Room ID - Mixed 7/8/9 digit
        generateUniqueRoomId();
    }

    private void generateUniqueRoomId() {
        Random random = new Random();
        int lengthChoice = random.nextInt(3); // 0,1,2
        long roomId;
        if (lengthChoice == 0) {
            roomId = 1000000L + random.nextInt(9000000); // 7 digit
        } else if (lengthChoice == 1) {
            roomId = 10000000L + random.nextInt(90000000); // 8 digit
        } else {
            roomId = 100000000L + (long)(random.nextDouble() * 900000000L); // 9 digit
        }
        roomIdText.setText("ID: " + roomId);
        roomNameText.setText("SR Voice Room");
    }

    public void onSmileClicked(android.view.View view) {
        Toast.makeText(this, "Smile Clicked!", Toast.LENGTH_SHORT).show();
    }
}
