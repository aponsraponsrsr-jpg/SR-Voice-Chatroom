package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences walletPrefs;
    private boolean isNavigating = false;
    private Handler timeoutHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIX - ekhane kono setContentView dibo na. Kono UI dekhabe na, tai Login page flash korbe na.
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        walletPrefs = getSharedPreferences("main_wallet", MODE_PRIVATE);

        // FIX 1 - Sobcheye fast rasta: Phone e jodi age thekei ID + Profile save thake, Firebase er jonno wait na kore sathe sathe Home e jao.
        boolean isProfileDone = prefs.getBoolean("IS_PROFILE_SETUP_DONE", false);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        // FIX: 3 ta nam ke ek korlam - MY_9_DIGIT_ID / profileId / my9DigitId - sob ekhon PROFILE_ID
        String savedId = prefs.getString("MY_9_DIGIT_ID", "");
        if(savedId == null || savedId.isEmpty()) savedId = prefs.getString("PROFILE_ID", "");
        if(savedId == null || savedId.isEmpty()) savedId = walletPrefs.getString("user_id", "");
        String savedUid = prefs.getString("FIREBASE_UID", "");

        if ((isProfileDone || isLoggedIn) && savedId != null && !savedId.isEmpty() && savedUid != null && !savedUid.isEmpty()) {
            // FIX: Wallet er satheo ID miliye dao - naile Game er Wallet alada dekhabe
            prefs.edit().putString("PROFILE_ID", savedId).putString("MY_9_DIGIT_ID", savedId).apply();
            walletPrefs.edit().putString("user_id", savedId).apply();
            // Firebase ke background e sync korte dao, user ke atke rakha lagbe na
            syncInBackground(savedUid);
            goToHome();
            return;
        }

        // Jodi phone e save na thake, tokhon e Firebase check korbo
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // FIX: Firebase na thakle 1 sec pore Login e jao - atke thakbe na
            timeoutHandler.postDelayed(() -> goToLogin(), 800);
        } else {
            checkProfile(user.getUid(), user.getEmail());
            // FIX: Firebase atke gele 5 sec pore Home/Login e pathao - Home faka hoye atke thakbe na
            timeoutHandler.postDelayed(() -> {
                if(!isNavigating) goToLogin();
            }, 5000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { timeoutHandler.removeCallbacksAndMessages(null); } catch (Exception ignored) {}
    }

    private void syncInBackground(String firebaseUid) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot s) {
                    if (!s.exists()) return;
                    try {
                        // FIX: 3 ta nam thekei ID khujbo - jeta pabo setai nibo
                        String id = s.child("my9DigitId").getValue(String.class);
                        if(id == null || id.isEmpty()) id = s.child("profileId").getValue(String.class);
                        if(id == null || id.isEmpty()) id = s.child("displayId").getValue(String.class);
                        String name = s.child("name").getValue(String.class);
                        if(name == null) name = s.child("userName").getValue(String.class);
                        if(name == null) name = s.child("displayName").getValue(String.class);
                        String gender = s.child("gender").getValue(String.class);
                        String country = s.child("country").getValue(String.class);
                        String photo = s.child("photoUrl").getValue(String.class);
                        if (photo == null) photo = s.child("photo").getValue(String.class);
                        if (photo == null) photo = s.child("photoBase64").getValue(String.class);
                        if (photo == null) photo = "";
                        // FIX: Coins tao sync koro - Me Option = Game Wallet
                        Long coinsObj = s.child("coins").getValue(Long.class);
                        long coins = coinsObj != null ? coinsObj : -1;

                        SharedPreferences.Editor e = prefs.edit();
                        if(id != null && !id.isEmpty()){
                            e.putString("MY_9_DIGIT_ID", id);
                            e.putString("PROFILE_ID", id);
                        }
                        if(name != null) e.putString("USER_NAME", name);
                        if(gender != null) e.putString("USER_GENDER", gender);
                        if(country != null) e.putString("USER_COUNTRY", country);
                        e.putString("USER_PHOTO", photo);
                        e.putString("PROFILE_PHOTO", photo);
                        e.apply();

                        // Wallet sync - Game + Me ek Wallet
                        SharedPreferences.Editor w = walletPrefs.edit();
                        if(id != null && !id.isEmpty()) w.putString("user_id", id);
                        if(name != null) w.putString("user_name", name);
                        if(!photo.isEmpty()) w.putString("user_pic", photo);
                        if(coins >= 0) w.putLong("wallet_coins", coins);
                        w.apply();
                    } catch (Exception ignored) {}
                }
                @Override public void onCancelled(DatabaseError e) {}
            });
        } catch (Exception ignored) {}
    }

    private void checkProfile(String firebaseUid, String email) {
        if (isNavigating) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                if (isNavigating) return;
                // FIX: profileId thaklei Profile Done dhorbo - my9DigitId na thakleo cholbe
                boolean hasId = s.exists() && (s.hasChild("my9DigitId") || s.hasChild("profileId") || s.hasChild("displayId"));
                boolean hasBasic = s.exists() && (s.hasChild("gender") || s.hasChild("name"));
                if (hasId && hasBasic) {
                    String id = s.child("my9DigitId").getValue(String.class);
                    if(id == null || id.isEmpty()) id = s.child("profileId").getValue(String.class);
                    if(id == null || id.isEmpty()) id = s.child("displayId").getValue(String.class);
                    String name = s.child("name").getValue(String.class);
                    if(name == null) name = s.child("userName").getValue(String.class);
                    if(name == null) name = "";
                    String gender = s.child("gender").getValue(String.class);
                    if(gender == null) gender = "";
                    String country = s.child("country").getValue(String.class);
                    if(country == null) country = "";
                    String photo = s.child("photoUrl").getValue(String.class);
                    if (photo == null) photo = s.child("photo").getValue(String.class);
                    if (photo == null) photo = s.child("photoBase64").getValue(String.class);
                    if (photo == null) photo = "";
                    Long coinsObj = s.child("coins").getValue(Long.class);
                    long coins = coinsObj != null ? coinsObj : 0;

                    prefs.edit()
                        .putString("FIREBASE_UID", firebaseUid)
                        .putString("MY_9_DIGIT_ID", id != null ? id : "")
                        .putString("PROFILE_ID", id != null ? id : "")
                        .putString("USER_NAME", name)
                        .putString("USER_GENDER", gender)
                        .putString("USER_COUNTRY", country)
                        .putString("USER_EMAIL", email != null ? email : "")
                        .putString("USER_PHOTO", photo)
                        .putString("PROFILE_PHOTO", photo)
                        .putBoolean("IS_LOGGED_IN", true)
                        .putBoolean("IS_PROFILE_SETUP_DONE", true).apply();

                    // Wallet eo same ID + Coins
                    walletPrefs.edit()
                        .putString("user_id", id != null ? id : "")
                        .putString("user_name", name)
                        .putString("user_pic", photo)
                        .putLong("wallet_coins", coins)
                        .apply();

                    goToHome();
                } else {
                    goToEdit(firebaseUid, email);
                }
            }
            @Override public void onCancelled(DatabaseError e) {
                // FIX: Firebase error hole Login e na pathiye Home e pathao - jodi phone e save thake
                // Naile user bar bar Login e atke jabe
                if(!isNavigating){
                    String savedId = prefs.getString("MY_9_DIGIT_ID", "");
                    if(savedId != null && !savedId.isEmpty()) goToHome();
                    else goToLogin();
                }
            }
        });
    }

    private void goToLogin() {
        if (isNavigating) return; isNavigating = true;
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }
    private void goToEdit(String firebaseUid, String email) {
        if (isNavigating) return; isNavigating = true;
        prefs.edit().putString("FIREBASE_UID", firebaseUid).putString("USER_EMAIL", email != null ? email : "").putBoolean("IS_PROFILE_SETUP_DONE", false).apply();
        Intent i = new Intent(MainActivity.this, EditProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }
    private void goToHome() {
        if (isNavigating) return; isNavigating = true;
        try { timeoutHandler.removeCallbacksAndMessages(null); } catch (Exception ignored) {}
        Intent i = new Intent(MainActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }
}
