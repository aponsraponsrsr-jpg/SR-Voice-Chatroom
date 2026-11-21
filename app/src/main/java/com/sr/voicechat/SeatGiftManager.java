package com.sr.voicechat;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

// SEAT SHOW ONLY - NO MONEY LOGIC HERE - FINAL CHECKED
// RULE:
// 1. Eta sudhu SHOW / Banner - Target / Wallet / Monthly er sathe kono somporko nai
// 2. Seat e bosa thakle - Namer niche - Joto Coin Receive korbe - Toto Show hobe
//    Example: Keu 2 Lakh Gift korle, niche 200000 show hobe
// 3. Leave / Seat theke chole gele - Show 0 hoye jabe, ar show hobe na
// 4. Abar asle - Abar 0 theke notun kore suru hobe
// 5. Asol Target + 30% Exchange + Monthly Counting - VoiceRoomActivity te alada vabe cholbe, eta te hat dibe na
// 6. Future - GiftManager / HostWorkData / Agency - Sobar sathe Mil thakbe - Monthly Key Same
// FIXED:
// - Path: active_rooms / ownerId / seats / seat_0  (age rooms / 0 chilo - vul chilo)
// - Field: userId (age uid chilo - vul chilo)
// - Double Count Fix: Ekhane liveShowCoins barano hobe NA - VoiceRoomActivity e 1 bar barbe - ekhane sudhu Banner
public class SeatGiftManager {

    private static DatabaseReference seatRef(String roomOwnerId, int seatNo) {
        return FirebaseDatabase.getInstance()
            .getReference("active_rooms").child(roomOwnerId)
            .child("seats").child("seat_" + seatNo);
    }

    // 1. User jokhon Seat e bosbe - Notun Session 0 theke suru
    public static void onUserSitOnSeat(String roomOwnerId, int seatNo, String userUid, String userName) {
        if (roomOwnerId == null || userUid == null) return;
        DatabaseReference ref = seatRef(roomOwnerId, seatNo);
        ref.child("userId").setValue(userUid);
        ref.child("name").setValue(userName);
        ref.child("liveShowCoins").setValue(0);
        ref.child("isOccupied").setValue(true);
        ref.child("lastGiftBanner").setValue(null);
    }

    // 2. User jokhon Leave korbe - Show CLEAR, kintu Monthly Target thakbe
    public static void onUserLeaveSeat(String roomOwnerId, int seatNo) {
        if (roomOwnerId == null) return;
        DatabaseReference ref = seatRef(roomOwnerId, seatNo);
        ref.child("userId").setValue(null);
        ref.child("name").setValue(null);
        ref.child("liveShowCoins").setValue(0);
        ref.child("isOccupied").setValue(false);
        ref.child("lastGiftBanner").setValue(null);
    }

    // 3. Gift asle - Sudhu Banner - Coin barano hobe NA - Double hobe na
    // VoiceRoomActivity.updateSeatShowCoins() e 1 bar Transaction diye Coin barbe
    // Ekhane sudhu Banner - jate Room er sobai dekhte pare
    public static void onGiftReceivedShowOnly(String roomOwnerId, int receiverSeatNo,
                                               String senderName, String receiverName,
                                               long giftAmount, String giftName) {
        if (roomOwnerId == null) return;
        if (receiverSeatNo < 0) return;
        DatabaseReference ref = seatRef(roomOwnerId, receiverSeatNo);
        String banner = senderName + " Send " + receiverName + " " + giftName + " x1";
        ref.child("lastGiftBanner").setValue(banner);
        ref.child("isOccupied").setValue(true);
        FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId)
            .child("lastGiftBanner").setValue(banner);
    }

    // 3-B. Seat No na janle - Receiver UID diye Seat khuje Banner dibe
    // Seat Key seat_0, Field userId - ei 2ta milano
    public static void onGiftReceivedByUid(String roomOwnerId, String receiverUid,
                                           String senderName, String receiverName,
                                           long giftAmount, String giftName) {
        if (roomOwnerId == null || receiverUid == null) return;
        DatabaseReference seatsRef = FirebaseDatabase.getInstance()
            .getReference("active_rooms").child(roomOwnerId).child("seats");
        seatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                for (DataSnapshot seat : snap.getChildren()) {
                    Object uidObj = seat.child("userId").getValue();
                    if (uidObj != null && String.valueOf(uidObj).equals(receiverUid)) {
                        int seatNo = -1;
                        try {
                            String k = seat.getKey(); // seat_0
                            if (k != null && k.startsWith("seat_")) seatNo = Integer.parseInt(k.replace("seat_", ""));
                            else seatNo = Integer.parseInt(k);
                        } catch (Exception ignored) {}
                        if (seatNo >= 0) {
                            onGiftReceivedShowOnly(roomOwnerId, seatNo, senderName, receiverName, giftAmount, giftName);
                        }
                        return;
                    }
                }
                String banner = senderName + " Send " + receiverName + " " + giftName + " x1";
                FirebaseDatabase.getInstance().getReference("active_rooms").child(roomOwnerId)
                    .child("lastGiftBanner").setValue(banner);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    // 4. Seat er niche Show korar jonno Listener
    public interface LiveShowListener {
        void onShowChanged(long showCoins, boolean isOccupied, String banner);
    }

    public static void listenSeatShow(String roomOwnerId, int seatNo, LiveShowListener listener) {
        if (roomOwnerId == null) return;
        DatabaseReference ref = seatRef(roomOwnerId, seatNo);
        ref.addValueEventListener(new ValueEventListener() {
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
                if (!occupied) show = 0;
                if (listener != null) listener.onShowChanged(show, occupied, banner);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
