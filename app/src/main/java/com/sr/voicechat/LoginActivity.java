    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
                
                // --- এখানে পার্মানেন্ট ইউনিক আইডির লজিকটি বসাতে হবে ---
                if (user != null) {
                    String firebaseUid = user.getUid();
                    String savedUniqueId = prefs.getString("UID_" + firebaseUid, "");

                    if (savedUniqueId.isEmpty()) {
                        // প্রথমবার লগইন করলে নতুন ৯ ডিজিটের আইডি জেনারেট হবে এবং এই জিমেইল/ইউআইডি দিয়ে চিরতরে লক হয়ে যাবে
                        int randomId = 100000000 + (int)(Math.random() * 900000000);
                        savedUniqueId = String.valueOf(randomId);
                        prefs.edit()
                                .putString("UID_" + firebaseUid, savedUniqueId)
                                .putString("MY_9_DIGIT_ID", savedUniqueId)
                                .apply();
                    } else {
                        // পুরোনো ইউজার হলে আগের আইডিই ফিক্সড থাকবে, কখনোই বদলাবে না
                        prefs.edit().putString("MY_9_DIGIT_ID", savedUniqueId).apply();
                    }
                }
                // ----------------------------------------------------

                // চেক করা ইউজার কি প্রোফাইল সেটআপ করেছে কি না
                boolean isProfileCompleted = prefs.getBoolean("PROFILE_COMPLETED", false);

                if (!isProfileCompleted) {
                    startActivity(new Intent(LoginActivity.this, EditProfileActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                }
                finish();
            } else {
                Toast.makeText(this, "Auth Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
