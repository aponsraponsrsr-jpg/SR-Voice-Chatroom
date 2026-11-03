// === NEXT STEP: SEAT + BOTTOM BAR LOGIC ===
private void takeSeat(int index) {
    // Firebase এ সিটে বসা
    String myUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
    // তোমার seats এর reference অনুযায়ী এখানে বসবে
    // Example: mDatabase.child("rooms").child(roomId).child("seats").child(String.valueOf(index)).child("userId").setValue(myUid);
    android.widget.Toast.makeText(this, "Seat " + (index+1) + " Taken", android.widget.Toast.LENGTH_SHORT).show();
}

private void leaveSeat(int index) {
    // Firebase থেকে সিট খালি করা
    // Example: mDatabase.child("rooms").child(roomId).child("seats").child(String.valueOf(index)).removeValue();
    android.widget.Toast.makeText(this, "Left Seat - Exit Done", android.widget.Toast.LENGTH_SHORT).show();
}

private void toggleMicMute() {
    android.widget.Toast.makeText(this, "Mic Mute/Unmute", android.widget.Toast.LENGTH_SHORT).show();
    // এখানে তোমার mic mute কোড থাকবে
}

private void openChatBox() {
    // Message Box ওপেন
    android.widget.Toast.makeText(this, "Message Box Open", android.widget.Toast.LENGTH_SHORT).show();
}
