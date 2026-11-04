package com.yourpackage.appname;

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

        // নিচের ৪টা বাটন - পার্মানেন্ট
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_moment) {
                Toast.makeText(this, "Moment", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_message) {
                Toast.makeText(this, "Message - Chat List", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_me) {
                Toast.makeText(this, "Me - My Profile", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // + বাটন - Create Room খুলবে
        Button btnCreateRoom = findViewById(R.id.btnCreateRoom);
        btnCreateRoom.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_create_room);

            Button btnConfirm = dialog.findViewById(R.id.btnConfirmRoom);
            btnConfirm.setOnClickListener(v2 -> {
                dialog.dismiss();
                Toast.makeText(this, "Owner Room চালু হয়ে গেছে! এখন সিটে বসতে পারো", Toast.LENGTH_LONG).show();
                // পরে এখানে রুম পেজ খুলবে
            });
            dialog.show();
        });
    }

    // সিটে ক্লিক করলে Take Mic খুলবে - এই ফাংশনটা পরে রুমে লাগাবো
    public void openTakeMicDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_take_mic);

        Button btnConfirm = dialog.findViewById(R.id.btnConfirmMic);
        Button btnCancel = dialog.findViewById(R.id.btnCancelMic);

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "তুমি সিটে বসে গেছো! Mic On", Toast.LENGTH_SHORT).show();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
