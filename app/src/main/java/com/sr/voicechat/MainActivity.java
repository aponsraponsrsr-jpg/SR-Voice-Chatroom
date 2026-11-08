package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navMoment, navInbox, navMe;
    private ImageView btnSearch, btnCreateRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Binding UI elements
        navHome = findViewById(R.id.navHome);
        navMoment = findViewById(R.id.navMoment);
        navInbox = findViewById(R.id.navInbox);
        navMe = findViewById(R.id.navMe);
        btnSearch = findViewById(R.id.btnSearch);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);

        // Top Search Click
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                showSearchDialog();
            });
        }

        // Top Create Room (+) Button Click -> Opens Room Creation Dialog
        if (btnCreateRoom != null) {
            btnCreateRoom.setOnClickListener(v -> {
                showCreateRoomDialog();
            });
        }

        // Bottom Navigation Clicks
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Toast.makeText(this, "Home Loaded", Toast.LENGTH_SHORT).show();
                // Already on Home or reload Home view
            });
        }

        if (navMoment != null) {
            navMoment.setOnClickListener(v -> {
                Toast.makeText(this, "Moment Feed Opening...", Toast.LENGTH_SHORT).show();
                // Open Moment activity or fragment if needed
            });
        }

        if (navInbox != null) {
            navInbox.setOnClickListener(v -> {
                // Open Inbox & Hosting Invitations
                Intent intent = new Intent(MainActivity.this, InboxActivity.class);
                startActivity(intent);
            });
        }

        if (navMe != null) {
            navMe.setOnClickListener(v -> {
                // Open Me Profile Activity
                Intent intent = new Intent(MainActivity.this, MeActivity.class);
                startActivity(intent);
            });
        }
    }

    // Search Dialog Method
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔍 Search Rooms / Users");
        
        final EditText input = new EditText(this);
        input.setHint("Enter User ID or Room Name...");
        input.setPadding(32, 24, 32, 24);
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString().trim();
            Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Create Voice Chat Room Dialog Method
    private void showCreateRoomDialog() {
        Dialog roomDialog = new Dialog(this);
        roomDialog.setContentView(R.layout.dialog_create_room); // Optional or quick alert
        
        new AlertDialog.Builder(this)
            .setTitle("🎙️ Create Voice Chat Room")
            .setMessage("Do you want to start your own live voice chat room now?")
            .setPositiveButton("Create Room", (dialog, which) -> {
                Toast.makeText(this, "Room Created Successfully!", Toast.LENGTH_SHORT).show();
                // Direct to Voice Room Live Activity
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }
}
