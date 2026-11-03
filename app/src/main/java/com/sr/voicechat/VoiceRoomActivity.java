package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageButton btnPower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        // Initializing Power/Exit Button in the top right corner
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
                            // Minimize Action
                            moveTaskToBack(true);
                        } else {
                            // Exit Action
                            finish();
                        }
                    });
                    builder.show();
                }
            });
        }
    }
}
