    private void setup(TextView seat, TextView nameView, int num){
        if (seat == null || nameView == null) return;
        
        seat.setOnClickListener(v -> {
            if (myCurrentSeat == num) {
                new AlertDialog.Builder(this).setTitle("Seat " + num).setMessage("Leave this seat?").setPositiveButton("Leave", (d, w) -> {
                    seat.setText("+"); 
                    seat.setBackgroundResource(R.drawable.circle_seat_bg); 
                    seat.setTextColor(0xFFFFFFFF);
                    seat.setBackgroundTintList(null);
                    nameView.setText(""); 
                    myCurrentSeat = -1; 
                    if (tvOnline != null) tvOnline.setText("👥 online 1");
                    tvChatLog.append("\nSystem: left Seat " + num);
                }).setNegativeButton("Cancel", null).show();
            } else {
                new AlertDialog.Builder(this).setTitle("Take Seat " + num + "?").setMessage("Join as " + myName).setPositiveButton("Take Mic", (d, w) -> {
                    clearAll();
                    
                    // ইউজারের প্রোফাইল ছবি সেট করার লজিক
                    boolean imageLoaded = false;
                    if (myImageUriStr != null && !myImageUriStr.isEmpty()) {
                        try {
                            android.net.Uri uri = android.net.Uri.parse(myImageUriStr);
                            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                            android.graphics.drawable.Drawable dImg = android.graphics.drawable.Drawable.createFromStream(inputStream, toString());
                            seat.setBackground(dImg);
                            seat.setText(""); // ছবি থাকলে টেক্সট খালি থাকবে
                            imageLoaded = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // ছবি না থাকলে বা লোড না হলে নামের প্রথম অক্ষর ও কালার দেখাবে
                    if (!imageLoaded) {
                        String letter = myName.isEmpty() ? "?" : String.valueOf(myName.charAt(0)).toUpperCase();
                        seat.setText(letter);
                        seat.setBackgroundResource(R.drawable.circle_shape);
                        seat.setBackgroundTintList(android.content.res.ColorStateList.valueOf(profileColors[num % profileColors.length]));
                    }

                    nameView.setText(myName);
                    myCurrentSeat = num;
                    if (tvOnline != null) tvOnline.setText("👥 online 2");
                    tvChatLog.append("\nSystem: " + myName + " joined Seat " + num);
                }).setNegativeButton("Cancel", null).show();
            }
        });
    }
