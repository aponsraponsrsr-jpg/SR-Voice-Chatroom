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
        // এই Activity তে কোনো Layout লাগবে না, শুধু চেক করে পাঠায় দিবে
        // setContentView(R.layout.activity_main); // লাগলে খুলে দিও

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Login করা নাই - Login এ যাও
            goToLogin();
        } else {
            // Login করা আছে - Profile চেক করো
            checkProfile(user.getUid(), user.getEmail());
        }
    }

    // RULE: ID এখানে কখনো বানাবো না। ID বানাবে শুধু EditProfileActivity।
    // RULE: UID দিয়ে খুঁজবো, Email দিয়ে না।
    private void checkProfile(String firebaseUid, String email) {
        if (isNavigating) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                if (s.exists() && s.hasChild("my9DigitId") && s.hasChild("gender") && s.hasChild("country")) {
                    // Profile Complete - Home এ যাও
                    String id = s.child("my9DigitId").getValue(String.class);
                    String name = s.child("name").getValue(String.class);
                    String gender = s.child("gender").getValue(String.class);
                    String country = s.child("country").getValue(String.class);
                    String photo = s.child("photoBase64").getValue(String.class);
                    if (photo == null) photo = s.child("photoUrl").getValue(String.class);
                    if (photo == null) photo = "";

                    prefs.edit()
                        .putString("FIREBASE_UID", firebaseUid)
                        .putString("MY_9_DIGIT_ID", id != null ? id : "")
                        .putString("USER_NAME", name != null ? name : "")
                        .putString("USER_GENDER", gender != null ? gender : "")
                        .putString("USER_COUNTRY", country != null ? country : "")
                        .putString("USER_EMAIL", email != null ? email : "")
                        .putString("USER_PHOTO", photo)
                        .putBoolean("IS_PROFILE_SETUP_DONE", true)
                        .apply();
                    goToHome();
                } else {
                    // Profile Complete না - Edit এ যাও, ID ওখানেই বানবে
                    goToEdit(firebaseUid, email);
                }
            }
            @Override public void onCancelled(DatabaseError e) {
                // Database Error হলে Login এ পাঠাবো, আটকায় রাখবো না
                goToLogin();
            }
        });
    }

    private void goToLogin() {
        if (isNavigating) return;
        isNavigating = true;
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToEdit(String firebaseUid, String email) {
        if (isNavigating) return;
        isNavigating = true;
        prefs.edit()
            .putString("FIREBASE_UID", firebaseUid)
            .putString("USER_EMAIL", email != null ? email : "")
            .putBoolean("IS_PROFILE_SETUP_DONE", false)
            .apply();
        Intent i = new Intent(MainActivity.this, EditProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToHome() {
        if (isNavigating) return;
        isNavigating = true;
        Intent i = new Intent(MainActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
