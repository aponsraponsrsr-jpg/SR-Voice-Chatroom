package com.sr.voicechat;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

// SEAT SHOW ONLY - NO MONEY LOGIC HERE - নতুন Setup
// RULE:
// 1. Eta sudhu SHOW - Target / Wallet er sathe kono somporko nai
// 2. Seat e bosa thakle - Namer niche - Joto Coin Receive korbe - Toto Show hobe
//    Example: Keu 2 Lakh Gift korle, niche 200000 show hobe
// 3. Leave / Group theke chole gele - Show 0 hoye jabe, ar show hobe na
// 4. Abar asle - Abar 0 theke notun kore suru hobe
// 5. Asol Target + 30% Exchange + Monthly Counting - GiftManager.java te alada vabe cholbe, eta te hat dibe na
// 6. Future - GiftManager / HostWorkData / Agency - Sobar sathe Mil thakbe - Monthly Key Same
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
        seatRef.child("lastGiftBanner").setValue(null);
    }

    // 2. User jokhon Leave korbe - Show CLEAR, kintu Database Target thakbe
    // Monthly Target মুছবে না - শুধু Screen এর Show মুছবে
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
    // GiftManager.sendGift() success howar PORE ei function ta call korbe - RoomActivity থেকে
    // NEW: Transaction দিয়ে - 2 জন একসাথে Gift করলেও হারাবে না - 100% Show হবে
    public static void onGiftReceivedShowOnly(String roomId, int receiverSeatNo,
                                               String senderName, String receiverName,
                                               long giftAmount, String giftName) {
        if (roomId == null) return;
        if (receiverSeatNo < 0) {
            // Seat No জানা না থাকলে - UID দিয়ে খুঁজে বের করো - নিচে Helper আছে
            // RoomActivity থেকে UID দিয়ে call করো: onGiftReceivedByUid(...)
            return;
        }
        DatabaseReference seatRef = FirebaseDatabase.getInstance()
            .getReference("rooms").child(roomId).child("seats").child(String.valueOf(receiverSeatNo));

        // Transaction - Race Condition Fix - আগের Bug Fix
        seatRef.child("liveShowCoins").runTransaction(new Transaction.Handler() {
            @NonNull @Override public Transaction.Result doTransaction(@NonNull MutableData current) {
                Long cur = current.getValue(Long.class);
                if (cur == null) cur = 0L;
                current.setValue(cur + giftAmount);
                return Transaction.success(current);
            }
            @Override public void onComplete(DatabaseError err, boolean committed, DataSnapshot snap) {}
        });

        // Banner - Room er sobai dekhbe - "OMG Send OMG x1" style
        String banner = senderName + " Send " + receiverName + " " + giftName + " x1";
        seatRef.child("lastGiftBanner").setValue(banner);
        seatRef.child("isOccupied").setValue(true);
        FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
            .child("lastGiftBanner").setValue(banner);
    }

    // 3-B. NEW HELPER - Seat No না জানলে - Receiver UID দিয়ে Seat খুঁজে Show বাড়াবে
    // RoomActivity থেকে এইটা Call করলেই - 100% সঠিক Seat এ Show হবে
    // Example: SeatGiftManager.onGiftReceivedByUid(roomId, receiverUid, senderName, receiverName, amount, giftName);
    public static void onGiftReceivedByUid(String roomId, String receiverUid,
                                           String senderName, String receiverName,
                                           long giftAmount, String giftName) {
        if (roomId == null || receiverUid == null) return;
        DatabaseReference seatsRef = FirebaseDatabase.getInstance()
            .getReference("rooms").child(roomId).child("seats");
        seatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                for (DataSnapshot seat : snap.getChildren()) {
                    Object uidObj = seat.child("uid").getValue();
                    if (uidObj != null && String.valueOf(uidObj).equals(receiverUid)) {
                        int seatNo = -1;
                        try { seatNo = Integer.parseInt(seat.getKey()); } catch (Exception ignored) {}
                        if (seatNo >= 0) {
                            onGiftReceivedShowOnly(roomId, seatNo, senderName, receiverName, giftAmount, giftName);
                        }
                        return;
                    }
                }
                // Seat এ বসে নাই - তাও Room Banner দেখাবে - যাতে Gift মিস না হয়
                String banner = senderName + " Send " + receiverName + " " + giftName + " x1";
                FirebaseDatabase.getInstance().getReference("rooms").child(roomId)
                    .child("lastGiftBanner").setValue(banner);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
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
