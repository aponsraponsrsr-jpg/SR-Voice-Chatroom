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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        btnMic = findViewById(R.id.btnMic);
        btnGame = findViewById(R.id.btnGame);
        try { btnGift = findViewById(R.id.btnGift); } catch (Exception e) { btnGift = null; }
        try { txtSeatShow = findViewById(R.id.txtSeatShow); } catch (Exception e) { txtSeatShow = null; }

        // Room + Seat - Room List theke asbe
        if (getIntent() != null) {
            if (getIntent().getStringExtra("roomId") != null)
                roomId = getIntent().getStringExtra("roomId");
            mySeatNo = getIntent().getIntExtra("seatNo", -1);
        }

        // User - Tomar ager Gmail Login theke asbe
        // Tumi login korle tomar ta, onno keu login korle tar ta
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null)
                myName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
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

        if (btnGift != null) btnGift.setOnClickListener(v -> {
            Toast.makeText(this, "Gift Center - Receiver select kore Gift pathao", Toast.LENGTH_SHORT).show();
        });
    }

    public void sendGiftToUser(String receiverUid, int receiverSeatNo, String receiverName, long giftAmount, String giftName) {
        if (myUid.isEmpty()) { Toast.makeText(this, "Login koro age", Toast.LENGTH_SHORT).show(); return; }
        GiftManager.sendGift(myUid, receiverUid, giftAmount, new GiftManager.GiftCallback() {
            @Override public void onSuccess(long sentAmount, long exchangeAdded, long newTargetTotal) {
                SeatGiftManager.onGiftReceivedShowOnly(roomId, receiverSeatNo, myName, receiverName, sentAmount, giftName);
                Toast.makeText(ChatRoomActivity.this, "Gift Sent: " + sentAmount + " Coins", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailed(String msg) { Toast.makeText(ChatRoomActivity.this, msg, Toast.LENGTH_SHORT).show(); }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (!roomId.isEmpty() && mySeatNo != -1) SeatGiftManager.onUserLeaveSeat(roomId, mySeatNo);
    }
}
