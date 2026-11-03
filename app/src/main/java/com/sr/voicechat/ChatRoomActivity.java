// Call this inside your Seat Adapter onClick
private void handleSeatClick(int seatPos) {
    String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    // Check current seat doc
    db.collection("rooms").document(roomId).collection("seats").document(String.valueOf(seatPos)).get().addOnSuccessListener(doc->{
        if(!doc.exists()){ // Empty seat
            if(myCurrentSeat!=-1 && myCurrentSeat!=seatPos){
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Change Seat?")
                    .setMessage("Do you want to change your seat?")
                    .setPositiveButton("Take Mic", (d,w)->{ leaveCurrentSeat(); joinSeat(seatPos); })
                    .setNegativeButton("Exit", (d,w)-> leaveCurrentSeat())
                    .setNeutralButton("Cancel", null).show();
            } else {
                joinSeat(seatPos);
            }
        } else {
            String occupier = doc.getString("userId");
            if(occupier.equals(myUid)){
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Seat Options").setMessage("Your seat")
                    .setPositiveButton("Exit", (d,w)-> leaveCurrentSeat())
                    .setNegativeButton("Cancel", null).show();
            } else {
                Toast.makeText(this, "Everything is alone right now - Seat taken", Toast.LENGTH_SHORT).show();
            }
        }
    });
}

private void joinSeat(int pos){
    Map<String, Object> m = new HashMap<>(); m.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()); m.put("seatNo", pos);
    db.collection("rooms").document(roomId).collection("seats").document(String.valueOf(pos)).set(m);
    myCurrentSeat = pos;
    // এখানে item_seat.xml এর background হলুদ করো - আলো জ্বলবে
}

private void leaveCurrentSeat(){
    if(myCurrentSeat!=-1) db.collection("rooms").document(roomId).collection("seats").document(String.valueOf(myCurrentSeat)).delete();
    myCurrentSeat=-1;
}
