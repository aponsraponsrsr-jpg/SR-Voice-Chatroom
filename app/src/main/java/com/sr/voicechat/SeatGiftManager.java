package com.sr.voicechat;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// SEAT SHOW ONLY - NO MONEY LOGIC HERE
// RULE:
// 1. Eta sudhu SHOW - Target / Wallet er sathe kono somporko nai
// 2. Jotokkhon Seat e bosa thakbe, totokkhon niche counting show hobe
//    Example: Keu 2 Lakh Gift korle, niche 200000 show hobe
// 3. Leave / Group theke chole gele - Show 0 hoye jabe, ar show hobe na
// 4. Abar asle - Abar 0 theke notun kore suru hobe
// 5. Asol Target + 30% Exchange - GiftManager.java te alada vabe cholbe, eta te hat dibe na
public class SeatGiftManager {

    // 1. User jokhon Seat e bosbe - Notun Session 0 theke suru
    public static void onUserSitOnSeat(String roomId, int seatNo, String userUid, String userName) {
        if (roomId == null || userUid == null) return;
        DatabaseReference seatRef = FirebaseDatabase.getInstance()
            .getReference("rooms").child(roomId).child("seats").child(String.valueOf(seatNo));
        seatRef.child("uid").setValue(userUid);
        seatRef.child("name").setValue(userName);
        seatRef.child("liveShowCoins").setValue(0);
        seatRef.child("isOccupied").setValue(true);
    }

    // 2. User jokhon Leave korbe - Show CLEAR, kintu Database Target thakbe
    public static void onUserLeaveSeat(String roomId, int seatNo) {
        if (roomId == null) return;
        DatabaseReference seatRef = FirebaseDatabase.getInstance()
            .getReference("rooms").child(roomId).child("seats").child(String.valueOf(seatNo));
        seatRef.child("uid").setValue(null);
        seatRef.child("name").setValue(null);
        seatRef.child("liveShowCoins").setValue(0);
        seatRef.child("isOccupied").setValue(false);
        seatRef.child("lastGiftBanner").setValue(null);
    }

    // 3. Gift asle - Sudhu SHOW barbe, Target barbe na ekhane
    // GiftManager.sendGift() success howar PORE ei function ta call korbe
    public static void onGiftReceivedShowOnly(String roomId, int receiverSeatNo,
                                               String senderName, String receiverName,
                                               long giftAmount, String giftName) {
        if (roomId == null) return;
        DatabaseReference seatRef = FirebaseDatabase.getInstance()
            .getReference("rooms").child(roomId).child("seats").child(String.valueOf(receiverSeatNo));

        seatRef.child("liveShowCoins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long cur = 0;
                if (snap.getValue() != null) {
                    try { cur = Long.parseLong(String.valueOf(snap.getValue())); } catch (Exception ignored) {}
                }
                seatRef.child("liveShowCoins").setValue(cur + giftAmount);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        // Banner - Room er sobai dekhbe - "OMG Send OMG x1" style
        String banner = senderName + " Send " + receiverName + " " + giftName + " x1";
        seatRef.child("lastGiftBanner").setValue(banner);
        FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
            .child("lastGiftBanner").setValue(banner);
    }

    // 4. RoomActivity te Seat er niche Show korar jonno - Listener
    public interface LiveShowListener {
        void onShowChanged(long showCoins, boolean isOccupied, String banner);
    }

    public static void listenSeatShow(String roomId, int seatNo, LiveShowListener listener) {
        if (roomId == null) return;
        DatabaseReference seatRef = FirebaseDatabase.getInstance()
            .getReference("rooms").child(roomId).child("seats").child(String.valueOf(seatNo));
        seatRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long show = 0;
                if (snap.hasChild("liveShowCoins") && snap.child("liveShowCoins").getValue() != null) {
                    try { show = Long.parseLong(String.valueOf(snap.child("liveShowCoins").getValue())); } catch (Exception ignored) {}
                }
                boolean occupied = false;
                if (snap.hasChild("isOccupied") && snap.child("isOccupied").getValue() != null) {
                    occupied = Boolean.parseBoolean(String.valueOf(snap.child("isOccupied").getValue()));
                }
                String banner = "";
                if (snap.hasChild("lastGiftBanner") && snap.child("lastGiftBanner").getValue() != null)
                    banner = String.valueOf(snap.child("lastGiftBanner").getValue());

                if (!occupied) show = 0; // Leave korle show hobe na
                if (listener != null) listener.onShowChanged(show, occupied, banner);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
