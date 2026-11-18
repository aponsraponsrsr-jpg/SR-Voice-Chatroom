package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText editName;
    private EditText editEmail;
    private EditText editPassword;
    private Button btnRegister;
    private Button btnGoLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        db = FirebaseFirestore.getInstance();

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> doRegister());
        }
        if (btnGoLogin != null) {
            btnGoLogin.setOnClickListener(v -> {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            });
        }
    }

    private void doRegister() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String pass = editPassword.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be 6+", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalName = name;
        final String finalEmail = email;

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser u = task.getResult().getUser();
                        if (u != null) {
                            saveUserData(u.getUid(), finalName, finalEmail);
                        }
                    } else {
                        String msg = "Register Failed";
                        if (task.getException() != null) {
                            msg = task.getException().getMessage();
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(String uid, String name, String email) {
        // ============ ID LOCK SYSTEM - 3 LAYER ============
        // LAYER 1 - ORIGINAL ID: Firebase UID - Eta kokhono change hobe na. Eitai asol chabi.
        // LAYER 2 - PERMANENT 9-DIGIT ID: Ekbar banale kokhono change hobe na. Data er sathe lock.
        // LAYER 3 - SHORT / DISPLAY ID: Pore Official 4-Digit asle sudhu eitai change hobe. Data te hat dibe na.

        // 1. Age theke ID thakle oitai thakbe - Notun kore banabo na
        String myId = prefs.getString("MY_9_DIGIT_ID", "");
        if (myId.isEmpty()) myId = prefs.getString("MY_ID", "");
        if (myId.isEmpty()) myId = prefs.getString("PERMANENT_ID", "");

        // 2. Na thakle notun 9-Digit banao
        if (myId.isEmpty()) {
            int r = (int) (Math.random() * 900000000) + 100000000;
            myId = String.valueOf(r);
        }
        final String finalId = myId; // Permanent 9-Digit - kokhono change hobe na

        // 3. Short ID - Akhon 9-Digit er soman. Pore Official 4-Digit asle sudhu eitai bodlabe.
        String shortId = prefs.getString("SHORT_ID", "");
        String displayId = prefs.getString("DISPLAY_ID", "");
        if (shortId.isEmpty()) shortId = finalId;
        if (displayId.isEmpty()) displayId = shortId;
        final String finalShortId = shortId;
        final String finalDisplayId = displayId;

        // 4. Firebase e age theke data thakle - Oi ID e rakhbo, overwrite korbo na
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                String existingPermanent = null;
                String existingShort = null;
                if (snap.exists()) {
                    if (snap.hasChild("my9DigitId") && snap.child("my9DigitId").getValue() != null)
                        existingPermanent = String.valueOf(snap.child("my9DigitId").getValue());
                    if (snap.hasChild("permanentId") && snap.child("permanentId").getValue() != null)
                        existingPermanent = String.valueOf(snap.child("permanentId").getValue());
                    if (snap.hasChild("shortId") && snap.child("shortId").getValue() != null)
                        existingShort = String.valueOf(snap.child("shortId").getValue());
                    if (snap.hasChild("displayId") && snap.child("displayId").getValue() != null)
                        existingShort = String.valueOf(snap.child("displayId").getValue());
                }
                // Age theke Permanent ID thakle oitai final - kokhono bodlabe na
                String usePermanent = (existingPermanent != null && !existingPermanent.isEmpty()) ? existingPermanent : finalId;
                String useShort = (existingShort != null && !existingShort.isEmpty()) ? existingShort : finalShortId;
                String useDisplay = useShort; // Display = Short ID e dekhabe

                writeAllData(uid, name, email, usePermanent, useShort, useDisplay);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {
                writeAllData(uid, name, email, finalId, finalShortId, finalDisplayId);
            }
        });
    }

    private void writeAllData(String uid, String name, String email, String permanentId, String shortId, String displayId) {
        // Realtime Database - UID diye key. UID kokhono change hoy na.
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("uid", uid);
        userData.put("originalId", uid); // LOCK 1 - Asol ID
        userData.put("permanentId", permanentId); // LOCK 2 - Permanent 9-Digit
        userData.put("my9Id", permanentId);
        userData.put("my9DigitId", permanentId);
        userData.put("shortId", shortId); // LOCK 3 - Official 4-Digit pore ekhane asbe
        userData.put("displayId", displayId); // Profile e eitai dekhabe
        userData.put("uniqueId", shortId); // Onno jayga theke uniqueId diye khujle pabe
        userData.put("coins", 0L);
        userData.put("sendingCoins", 0L);
        userData.put("receivingCoins", 0L);
        userData.put("totalReceivedCoins", 0L);
        userData.put("createdAt", System.currentTimeMillis());
        usersRef.child(uid).updateChildren(userData);

        // MAPPING TABLE - Short ID / 9-Digit diye khujleo asol UID pabe. Data kokhono harabe na.
        // id_mapping/{shortId} -> uid
        // id_mapping/{permanentId} -> uid
        DatabaseReference mapRef = FirebaseDatabase.getInstance().getReference("id_mapping");
        mapRef.child(permanentId).child("uid").setValue(uid);
        mapRef.child(permanentId).child("permanentId").setValue(permanentId);
        mapRef.child(permanentId).child("shortId").setValue(shortId);
        if (!shortId.equals(permanentId)) {
            mapRef.child(shortId).child("uid").setValue(uid);
            mapRef.child(shortId).child("permanentId").setValue(permanentId);
            mapRef.child(shortId).child("shortId").setValue(shortId);
        }

        // Firestore Backup - Same 3 ID
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("uid", uid);
        map.put("originalId", uid);
        map.put("permanentId", permanentId);
        map.put("my9Id", permanentId);
        map.put("my9DigitId", permanentId);
        map.put("shortId", shortId);
        map.put("displayId", displayId);
        map.put("uniqueId", shortId);
        db.collection("users").document(uid).set(map);

        // Local - Chirotore save
        prefs.edit()
            .putString("MY_9_DIGIT_ID", permanentId)
            .putString("PERMANENT_ID", permanentId)
            .putString("ORIGINAL_ID", uid)
            .putString("SHORT_ID", shortId)
            .putString("DISPLAY_ID", displayId)
            .putString("UNIQUE_ID", shortId)
            .putString("MY_ID", permanentId) // Purano code er jonno
            .putString("USER_NAME", name)
            .putString("USER_EMAIL", email)
            .putString("FIREBASE_UID", uid)
            .apply();

        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
