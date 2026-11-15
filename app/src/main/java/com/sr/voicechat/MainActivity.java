package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIX - ekhane kono setContentView dibo na. Kono UI dekhabe na, tai Login page flash korbe na.
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // FIX 1 - Sobcheye fast rasta: Phone e jodi age thekei ID + Profile save thake, Firebase er jonno wait na kore sathe sathe Home e jao.
        // Etai tomar 1-2 second er flash ta bondho korbe.
        boolean isProfileDone = prefs.getBoolean("IS_PROFILE_SETUP_DONE", false);
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);
        String savedId = prefs.getString("MY_9_DIGIT_ID", "");
        String savedUid = prefs.getString("FIREBASE_UID", "");

        if ((isProfileDone || isLoggedIn) && savedId != null && !savedId.isEmpty() && savedUid != null && !savedUid.isEmpty()) {
            // Firebase ke background e sync korte dao, user ke atke rakha lagbe na
            syncInBackground(savedUid);
            goToHome();
            return;
        }

        // Jodi phone e save na thake, tokhon e Firebase check korbo
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) goToLogin();
        else checkProfile(user.getUid(), user.getEmail());
    }

    private void syncInBackground(String firebaseUid) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot s) {
                    if (!s.exists()) return;
                    try {
                        String id = s.child("my9DigitId").getValue(String.class);
                        String name = s.child("name").getValue(String.class);
                        String gender = s.child("gender").getValue(String.class);
                        String country = s.child("country").getValue(String.class);
                        String photo = s.child("photoUrl").getValue(String.class);
                        if (photo == null) photo = s.child("photo").getValue(String.class);
                        if (photo == null) photo = s.child("photoBase64").getValue(String.class);
                        if (photo == null) photo = "";
                        prefs.edit()
                            .putString("MY_9_DIGIT_ID", id != null ? id : prefs.getString("MY_9_DIGIT_ID", ""))
                            .putString("USER_NAME", name != null ? name : prefs.getString("USER_NAME", ""))
                            .putString("USER_GENDER", gender != null ? gender : prefs.getString("USER_GENDER", ""))
                            .putString("USER_COUNTRY", country != null ? country : prefs.getString("USER_COUNTRY", ""))
                            .putString("USER_PHOTO", photo)
                            .putString("PROFILE_PHOTO", photo)
                            .apply();
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
                if (s.exists() && s.hasChild("my9DigitId") && s.hasChild("gender") && s.hasChild("country")) {
                    String id = s.child("my9DigitId").getValue(String.class);
                    String name = s.child("name").getValue(String.class);
                    String gender = s.child("gender").getValue(String.class);
                    String country = s.child("country").getValue(String.class);
                    String photo = s.child("photoUrl").getValue(String.class);
                    if (photo == null) photo = s.child("photo").getValue(String.class);
                    if (photo == null) photo = s.child("photoBase64").getValue(String.class);
                    if (photo == null) photo = "";
                    prefs.edit()
                        .putString("FIREBASE_UID", firebaseUid)
                        .putString("MY_9_DIGIT_ID", id != null ? id : "")
                        .putString("USER_NAME", name != null ? name : "")
                        .putString("USER_GENDER", gender != null ? gender : "")
                        .putString("USER_COUNTRY", country != null ? country : "")
                        .putString("USER_EMAIL", email != null ? email : "")
                        .putString("USER_PHOTO", photo)
                        .putString("PROFILE_PHOTO", photo)
                        .putBoolean("IS_LOGGED_IN", true)
                        .putBoolean("IS_PROFILE_SETUP_DONE", true).apply();
                    goToHome();
                } else {
                    goToEdit(firebaseUid, email);
                }
            }
            @Override public void onCancelled(DatabaseError e) { goToLogin(); }
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
        Intent i = new Intent(MainActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }
}
