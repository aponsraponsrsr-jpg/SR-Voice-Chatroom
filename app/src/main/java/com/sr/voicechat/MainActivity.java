package com.sr.voicechat;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // নিচের ৪টা বাটন - নতুন সিস্টেম
        Button btnHome = findViewById(R.id.nav_home);
        Button btnMoment = findViewById(R.id.nav_moment);
        Button btnMessage = findViewById(R.id.nav_message);
        Button btnMe = findViewById(R.id.nav_me);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());
        }
        if (btnMoment != null) {
            btnMoment.setOnClickListener(v -> Toast.makeText(this, "Moment", Toast.LENGTH_SHORT).show());
        }
        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> Toast.makeText(this, "Message - Chat List", Toast.LENGTH_SHORT).show());
        }
        if (btnMe != null) {
            btnMe.setOnClickListener(v -> Toast.makeText(this, "Me - My Profile", Toast.LENGTH_SHORT).show());
        }

        // + বাটন - Create Room
        Button btnCreateRoom = findViewById(R.id.btnCreateRoom);
        if (btnCreateRoom != null) {
            btnCreateRoom.setOnClickListener(v -> {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_create_room);
                Button btnConfirm = dialog.findViewById(R.id.btnConfirmRoom);
                if (btnConfirm != null) {
                    btnConfirm.setOnClickListener(v2 -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Room চালু হয়ে গেছে!", Toast.LENGTH_LONG).show();
                    });
                }
                dialog.show();
            });
        }
    }
}
