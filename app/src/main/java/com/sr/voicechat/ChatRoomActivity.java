package com.sr.voicechat;

import com.sr.voicechat.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomActivity extends AppCompatActivity {

    GridLayout seatGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        seatGrid = findViewById(R.id.seatGrid);

        // 8 টা সিট দেখাবে - কোনো Firebase লাগবে না, তাই Error আসবে না
        for (int i = 0; i < 8; i++) {
            View seatView = LayoutInflater.from(this).inflate(R.layout.item_seat, null);
            
            TextView txtName = seatView.findViewById(R.id.txtSeatName);
            txtName.setText("Seat " + (i+1));

            int seatNumber = i + 1;
            seatView.setOnClickListener(v -> {
                Toast.makeText(ChatRoomActivity.this, "You clicked Seat " + seatNumber, Toast.LENGTH_SHORT).show();
            });

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.setMargins(15, 15, 15, 15);
            seatView.setLayoutParams(param);
            
            seatGrid.addView(seatView);
        }
    }
}
