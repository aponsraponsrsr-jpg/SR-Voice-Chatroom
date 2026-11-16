package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ChatRoomActivity extends AppCompatActivity {

    Button btnMic, btnGame, btnGift;
    TextView txtSeatShow;
    boolean isMicOn = false;

    // Tomar ager Login System thekei asbe - Ekhane karo ID / Name likha nai
    String roomId = "";
    int mySeatNo = -1;
    String myUid = "";
    String myName = "Guest";

    // Receiver - pore Seat e click kore change hobe
    // Ekhon test er jonno nijer Seat e Gift jabe, Wallet kata + Show dekha jabe
    String selectedReceiverUid = "";
    int selectedReceiverSeatNo = -1;
    String selectedReceiverName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        btnMic = findViewById(R.id.btnMic);
        btnGame = findViewById(R.id.btnGame);
        btnGift = findViewById(R.id.btnGift);
        txtSeatShow = findViewById(R.id.txtSeatShow);

        // Room + Seat - Room List theke asbe
        if (getIntent() != null) {
            if (getIntent().getStringExtra("roomId") != null)
                roomId = getIntent().getStringExtra("roomId");
            mySeatNo = getIntent().getIntExtra("seatNo", -1);
            // Jodi onno Activity theke Receiver pathano hoy
            if (getIntent().getStringExtra("receiverUid") != null)
                selectedReceiverUid = getIntent().getStringExtra("receiverUid");
            selectedReceiverSeatNo = getIntent().getIntExtra("receiverSeatNo", -1);
            if (getIntent().getStringExtra("receiverName") != null)
                selectedReceiverName = getIntent().getStringExtra("receiverName");
        }

        // User - Tomar ager Gmail Login theke asbe
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null)
                myName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        }

        // Default Receiver = Nijer Seat - pore Seat click e change korba
        if (selectedReceiverUid.isEmpty()) {
            selectedReceiverUid = myUid;
            selectedReceiverSeatNo = mySeatNo;
            selectedReceiverName = myName;
        }

        // Seat e bosle Show suru
        if (!roomId.isEmpty() && mySeatNo != -1 && !myUid.isEmpty()) {
            SeatGiftManager.onUserSitOnSeat(roomId, mySeatNo, myUid, myName);
            SeatGiftManager.listenSeatShow(roomId, mySeatNo, (showCoins, isOccupied, banner) -> {
                if (txtSeatShow != null) {
                    if (!isOccupied) txtSeatShow.setText("");
                    else if (showCoins > 0) txtSeatShow.setText(String.valueOf(showCoins));
                    else txtSeatShow.setText("");
                }
            });
        }

        btnMic.setOnClickListener(v -> {
            isMicOn = !isMicOn;
            btnMic.setText(isMicOn ? "🎤 Mic ON" : "🎤 Mic OFF");
        });

        btnGame.setOnClickListener(v -> {
            startActivity(new Intent(ChatRoomActivity.this, FruitPartyActivity.class));
        });

        // GIFT - FIXED - Age sudhu Toast chilo, ekhon GiftBox + Wallet khulbe
        if (btnGift != null) btnGift.setOnClickListener(v -> {
            if (myUid.isEmpty()) {
                Toast.makeText(this, "Login koro age", Toast.LENGTH_SHORT).show();
                return;
            }
            // GiftBoxDialog - Upor e Wallet e Real Coin dekhabe
            GiftBoxDialog.show(this, (giftName, giftAmount, giftEmoji) -> {
                // Gift pick korle Receiver er kache pathabe
                sendGiftToUser(selectedReceiverUid, selectedReceiverSeatNo, selectedReceiverName, giftAmount, giftName);
            });
        });
    }

    public void sendGiftToUser(String receiverUid, int receiverSeatNo, String receiverName, long giftAmount, String giftName) {
        if (myUid.isEmpty()) {
            Toast.makeText(this, "Login koro age", Toast.LENGTH_SHORT).show();
            return;
        }
        if (receiverUid == null || receiverUid.isEmpty()) {
            Toast.makeText(this, "Receiver select koro age", Toast.LENGTH_SHORT).show();
            return;
        }
        if (roomId.isEmpty()) {
            Toast.makeText(this, "Room e dhuko age", Toast.LENGTH_SHORT).show();
            return;
        }

        GiftManager.sendGift(myUid, receiverUid, giftAmount, new GiftManager.GiftCallback() {
            @Override public void onSuccess(long sentAmount, long exchangeAdded, long newTargetTotal) {
                SeatGiftManager.onGiftReceivedShowOnly(roomId, receiverSeatNo, myName, receiverName, sentAmount, giftName);
                Toast.makeText(ChatRoomActivity.this, "Gift Sent: " + sentAmount + " Coins", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailed(String msg) {
                Toast.makeText(ChatRoomActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try {
            if (!roomId.isEmpty() && mySeatNo != -1) SeatGiftManager.onUserLeaveSeat(roomId, mySeatNo);
        } catch (Exception e) {
            // Ignore - Activity bondho hocche
        }
    }
}
