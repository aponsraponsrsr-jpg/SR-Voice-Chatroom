package com.sr.voicechat;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // নিচের ৪টা বাটন - যদি থাকে তবেই চলবে
        try {
            BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
            if (bottomNav != null) {
                bottomNav.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_home) {
                        Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                    } else if (id == R.id.nav_moment) {
                        Toast.makeText(this, "Moment", Toast.LENGTH_SHORT).show();
                    } else if (id == R.id.nav_message) {
                        Toast.makeText(this, "Message", Toast.LENGTH_SHORT).show();
                    } else if (id == R.id.nav_me) {
                        Toast.makeText(this, "Me", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }
        } catch (Exception e) {
            // কিছু না, পরে ঠিক করবো
        }

        // + বাটন - যদি থাকে তবেই Create Room খুলবে
        try {
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
        } catch (Exception e) {
        }
    }

    public void openTakeMicDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_take_mic);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmMic);
        Button btnCancel = dialog.findViewById(R.id.btnCancelMic);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                Toast.makeText(this, "তুমি সিটে বসে গেছো! Mic On", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }
}
