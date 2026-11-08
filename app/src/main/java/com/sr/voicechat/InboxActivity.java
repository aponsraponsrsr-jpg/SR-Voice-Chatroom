package com.sr.voicechat;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class InboxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox); // Ensure you have activity_inbox.xml or use a simple view

        // Demo Hosting Invitation Alert for Testing
        showHostingInvitationAlert();
    }

    private void showHostingInvitationAlert() {
        new AlertDialog.Builder(this)
            .setTitle("🌟 SR Official Hosting Invitation")
            .setMessage("Agency ID #948590 has invited you to join as an Official Host. Do you accept?")
            .setPositiveButton("Accept", (dialog, which) -> {
                Toast.makeText(this, "Hosting Accepted! Badge Unlocked.", Toast.LENGTH_LONG).show();
                // Save hosting status in SharedPreferences
            })
            .setNegativeButton("Ignore", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }
}
