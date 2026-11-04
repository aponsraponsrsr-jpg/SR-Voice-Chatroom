// সিটে ক্লিক করে নিজের নাম ও ছবি ফায়ারবেসে গ্লোবালি পাঠানোর লজিক (আপডেটেড ও সেফ)
private void setupSeatInteractions() {
    int[] seatCardIds = {
        R.id.seatCard1, R.id.seatCard2, R.id.seatCard3, R.id.seatCard4, R.id.seatCard5,
        R.id.seatCard6, R.id.seatCard7, R.id.seatCard8, R.id.seatCard9, R.id.seatCard10
    };

    for (int i = 0; i < seatCardIds.length; i++) {
        final int seatIndex = i;
        final int seatNumber = i + 1;
        View seatContainer = findViewById(seatCardIds[i]);

        if (seatContainer != null) {
            seatContainer.setOnClickListener(v -> {
                String seatTitle = (seatNumber == 1) ? "Host Seat" : "Seat " + seatNumber;
                CharSequence[] seatOptions = {"Take Mic (" + seatTitle + ")", "Leave Mic"};

                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎙️ " + seatTitle + " Options");
                builder.setItems(seatOptions, (dialog, which) -> {
                    String userName = prefs.getString("USER_NAME", "User");
                    String userImageUriStr = prefs.getString("USER_IMAGE", "");
                    String seatKey = "seat_" + seatIndex;

                    if (which == 0) {
                        final String[] imageBase64 = {""};
                        
                        // যদি ইমেজ ইউআরআই থাকে এবং সেটি কন্টেন্ট বা ফাইল পাথ হয়, তবে সেটিকে সেফলি বেস৬৪-এ কনভার্ট করব
                        if (!userImageUriStr.isEmpty()) {
                            try {
                                if (userImageUriStr.startsWith("content://") || userImageUriStr.startsWith("file://")) {
                                    Uri uri = Uri.parse(userImageUriStr);
                                    InputStream inputStream = getContentResolver().openInputStream(uri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    if (bitmap != null) {
                                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                        byte[] imageBytes = baos.toByteArray();
                                        imageBase64[0] = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                                    }
                                } else {
                                    // যদি অলরেডি বেস৬৪ স্ট্রিং থাকে
                                    imageBase64[0] = userImageUriStr;
                                }
                            } catch (Exception e) {
                                imageBase64[0] = "";
                            }
                        }

                        // ফায়ারবেসে পার্মানেন্ট ডেটা পাঠানো
                        databaseReference.child("seats").child(seatKey).child("userName").setValue(userName);
                        databaseReference.child("seats").child(seatKey).child("userImage").setValue(imageBase64[0]);

                        Toast.makeText(VoiceRoomActivity.this, "Joined " + seatTitle + " successfully!", Toast.LENGTH_SHORT).show();
                        chatMessages.add("🎙️ System: " + userName + " took " + seatTitle);
                        chatAdapter.notifyDataSetChanged();
                    } else {
                        databaseReference.child("seats").child(seatKey).removeValue();

                        Toast.makeText(VoiceRoomActivity.this, "Left " + seatTitle, Toast.LENGTH_SHORT).show();
                        chatMessages.add("❌ System: " + userName + " left " + seatTitle);
                        chatAdapter.notifyDataSetChanged();
                    }
                });
                builder.show();
            });
        }
    }
}
