// --- TOP BAR ---
ImageView btnEditRoom = findViewById(R.id.btnEditRoom);
ImageView btnLeaveRoom = findViewById(R.id.btnLeaveRoom);
TextView txtRoomId = findViewById(R.id.txtRoomId);

// 1. Edit Icon ON
btnEditRoom.setOnClickListener(v -> {
    // Room Edit Dialog
    openEditRoomDialog();
});

// 2. Power OFF Icon ON - Room থেকে বের হবে
btnLeaveRoom.setOnClickListener(v -> {
    leaveRoom();
    finish();
});

// Room ID - Owner ID শো করবে
String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
txtRoomId.setText(uid.substring(0,4)); // 1024 এর জায়গায়

// --- SEAT LOGIC - OPEN ---
// তোমার seat click এর ভিতরে এই লজিকটা বসাও
private void onSeatClicked(int seatIndex) {
    // যদি সিট খালি থাকে -> বসবে
    if (seats[seatIndex].isEmpty()) {
        // Take Mic
        takeSeat(seatIndex);
        Toast.makeText(this, "Seat Taken - Mic On", Toast.LENGTH_SHORT).show();
    } else {
        // যদি নিজের সিট হয় -> নামবে
        if (seats[seatIndex].getUserId().equals(myUserId)) {
            // Leave Mic / Exit
            new AlertDialog.Builder(this)
                .setTitle("Leave Seat?")
                .setPositiveButton("Leave / Exit", (d,w) -> leaveSeat(seatIndex))
                .setNegativeButton("Cancel", null)
                .show();
        }
    }
}

// --- BOTTOM BAR ---
// 3. Chat ON
findViewById(R.id.btnChat).setOnClickListener(v -> openChatBox());

// 4. Mic ON - Seat এর সাথে কানেক্টেড
findViewById(R.id.btnMic).setOnClickListener(v -> toggleMicMute());

// 5. Gift OFF
findViewById(R.id.btnGift).setAlpha(0.3f); // ফ্যাকাশে দেখাবে
findViewById(R.id.btnGift).setOnClickListener(v -> {
    Toast.makeText(this, "Gift is Locked 🔒", Toast.LENGTH_SHORT).show();
});

// 6. Game OFF
findViewById(R.id.btnGame).setAlpha(0.3f);
findViewById(R.id.btnGame).setOnClickListener(v -> {
    Toast.makeText(this, "Game is Locked 🔒", Toast.LENGTH_SHORT).show();
});
