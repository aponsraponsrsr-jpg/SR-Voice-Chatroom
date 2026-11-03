        btnSave.setOnClickListener(v -> {
            String groupNameStr = etGroupName.getText().toString().trim();
            String announcementStr = etAnnouncement.getText().toString().trim();
            if(groupNameStr.isEmpty()){
                Toast.makeText(this,"Group Name likhun",Toast.LENGTH_SHORT).show();
                return;
            }
            final String finalAnnouncement = announcementStr.isEmpty() ? "welcome" : announcementStr;
            final String finalGroupName = groupNameStr;
            
            String gender = "Male";
            int id = rgGender.getCheckedRadioButtonId();
            if(id == R.id.rbFemale) gender = "Female";
            
            if(auth.getCurrentUser() == null) return;
            String uid = auth.getCurrentUser().getUid();

            // র্যান্ডম ৯ ডিজিট ইউনিক আইডি জেনারেট করা (যেমন: 837261945)
            int randomId = 100000000 + new java.util.Random().nextInt(900000000);
            
            Map<String,Object> user = new HashMap<>();
            user.put("uid", uid);
            user.put("name", finalGroupName);
            user.put("groupName", finalGroupName);
            user.put("announcement", finalAnnouncement);
            user.put("gender", gender);
            user.put("email", auth.getCurrentUser().getEmail());
            user.put("photoUrl", base64Image);
            user.put("userId", String.valueOf(randomId)); // পার্মানেন্ট ইউনিক আইডি
            
            db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    // লোকাল SharedPreferences-এ সেভ করে রাখা যাতে আর বারবার এডিট পেজে না আসে
                    android.content.SharedPreferences prefs = getSharedPreferences("SR_APP", MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("IS_LOGGED_IN", true);
                    editor.putString("USER_ID", String.valueOf(randomId));
                    editor.apply();

                    Toast.makeText(this,"Profile Saved Successfully!",Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(EditProfileActivity.this, VoiceRoomActivity.class);
                    intent2.putExtra("groupName", finalGroupName);
                    intent2.putExtra("announcement", finalAnnouncement);
                    startActivity(intent2);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
