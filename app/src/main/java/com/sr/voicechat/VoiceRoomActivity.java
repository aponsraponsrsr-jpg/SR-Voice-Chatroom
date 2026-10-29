package com.sr.voicechat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashSet;
import java.util.Set;

public class VoiceRoomActivity extends AppCompatActivity {

    private TextView roomIdText, roomNameText;
    private CircleImageView[] seatImages = new CircleImageView[10];
    private TextView[] seatNames = new TextView[10];
    private boolean[] isSeatLocked = new boolean[10];
    private boolean[] isSeatTaken = new boolean[10];
    private boolean[] isSeatMuted = new boolean[10];
    private String myUserId;
    private String currentRoomName = "SR Voice Room";
    private boolean isOwner = true;
    private Set<Integer> adminSeats = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        myUserId = IdManager.getOrCreateUserId(this);

        roomIdText = findViewById(R.id.roomIdText);
        roomNameText = findViewById(R.id.roomNameText);

        roomIdText.setText("ID: " + myUserId + " | 7/8/9 digit unique");
        roomNameText.setText(currentRoomName);

        roomNameText.setOnClickListener(v -> showEditRoomNameDialog());

        initSeats();
    }

    private void initSeats() {
        int[] imageIds = {
                R.id.seat1Image, R.id.seat2Image, R.id.seat3Image, R.id.seat4Image, R.id.seat5Image,
                R.id.seat6Image, R.id.seat7Image, R.id.seat8Image, R.id.seat9Image, R.id.seat10Image
        };
        int[] nameIds = {
                R.id.seat1Name, R.id.seat2Name, R.id.seat3Name, R.id.seat4Name, R.id.seat5Name,
                R.id.seat6Name, R.id.seat7Name, R.id.seat8Name, R.id.seat9Name, R.id.seat10Name
        };

        for (int i = 0; i < 10; i++) {
            seatImages[i] = findViewById(imageIds[i]);
            seatNames[i] = findViewById(nameIds[i]);
            seatNames[i].setText("No." + (i + 1));
            final int index = i;
            seatImages[i].setOnClickListener(v -> onSeatClicked(index));
        }
        takeSeat(0);
    }

    public void onSmileClicked(View view) {
        showEmojiPanel();
    }

    private void showEmojiPanel() {
        String[] emojis = {"😊", "😂", "❤️", "🔥", "🎉", "😍", "👏", "🥳", "😎", "🙏", "💯", "✨"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Emoji")
                .setItems(emojis, (dialog, which) -> {
                    Toast.makeText(this, "Sent: " + emojis[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showEditRoomNameDialog() {
        if (!isOwner) {
            Toast.makeText(this, "Only Owner can edit room", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText input = new EditText(this);
        input.setText(currentRoomName);
        input.setPadding(30,30,30,30);
        new AlertDialog.Builder(this)
                .setTitle("Edit Room Name")
                .setMessage("Room Thumbnail Edit is in Profile")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    currentRoomName = input.getText().toString();
                    roomNameText.setText(currentRoomName);
                    Toast.makeText(this, "Room Name Updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onSeatClicked(int index) {
        if (isSeatLocked[index]) {
            Toast.makeText(this, "Mic Locked", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isSeatTaken[index]) showManageDialog(index);
        else showTakeDialog(index);
    }

    private void showTakeDialog(int index) {
        new AlertDialog.Builder(this)
                .setTitle("Seat No." + (index + 1))
                .setItems(new String[]{"Take The Mic", "Lock this mic", "Cancel"}, (d, w) -> {
                    if (w == 0) takeSeat(index);
                    else if (w == 1) lockSeat(index);
                }).show();
    }

    private void showManageDialog(int index) {
        String[] options;
        if (isOwner) {
            boolean isAdmin = adminSeats.contains(index);
            options = new String[]{
                    isSeatMuted[index] ? "Unmute Mic" : "Mute this mic",
                    "Lock this mic",
                    "Kick from mic",
                    isAdmin ? "Remove Admin" : "Make Admin",
                    "Cancel"
            };
        } else {
            options = new String[]{"View Profile", "Cancel"};
        }

        new AlertDialog.Builder(this)
                .setTitle("Manage No." + (index + 1) + " - ID: " + myUserId)
                .setItems(options, (d, w) -> {
                    if (!isOwner) return;
                    if (w == 0) toggleMute(index);
                    else if (w == 1) lockSeat(index);
                    else if (w == 2) leaveSeat(index);
                    else if (w == 3) toggleAdmin(index);
                }).show();
    }

    private void toggleMute(int index) {
        isSeatMuted[index] = !isSeatMuted[index];
        String msg = isSeatMuted[index] ? "Mic Muted No." + (index+1) : "Mic Unmuted No." + (index+1);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void toggleAdmin(int index) {
        if (adminSeats.contains(index)) {
            adminSeats.remove(index);
            Toast.makeText(this, "Admin Removed No." + (index+1), Toast.LENGTH_SHORT).show();
        } else {
            adminSeats.add(index);
            Toast.makeText(this, "Now Admin No." + (index+1), Toast.LENGTH_SHORT).show();
        }
    }

    private void takeSeat(int index) {
        for (int i = 0; i < 10; i++) {
            if (seatNames[i].getText().toString().contains(myUserId)) leaveSeat(i);
        }
        isSeatTaken[index] = true;
        seatNames[index].setText("ID:" + myUserId);
        seatImages[index].setBorderColor(getResources().getColor(android.R.color.holo_green_light));
        seatImages[index].setBorderWidth(6);
        Toast.makeText(this, "Take Seat No." + (index + 1) + " Done", Toast.LENGTH_SHORT).show();
    }

    private void leaveSeat(int index) {
        isSeatTaken[index] = false;
        isSeatMuted[index] = false;
        seatNames[index].setText("No." + (index + 1));
        seatImages[index].setBorderWidth(0);
        seatImages[index].setImageResource(android.R.drawable.sym_def_app_icon);
    }

    private void lockSeat(int index) {
        isSeatLocked[index] = true;
        seatImages[index].setImageResource(android.R.drawable.ic_lock_lock);
        Toast.makeText(this, "Locked No." + (index + 1), Toast.LENGTH_SHORT).show();
    }
}
