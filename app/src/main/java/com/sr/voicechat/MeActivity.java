package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class MeActivity extends AppCompatActivity {
    private ImageView imgAvatar, btnEditPencil;
    private TextView txtName, txtId, txtCopyId, txtGenderCountry, txtFollow, txtFans, txtVisitor;
    private SharedPreferences prefs;
    private String firebaseUid = "";
    private String my9DigitId = "";
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_me);
            prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

            imgAvatar = findViewById(R.id.imgAvatar);
            btnEditPencil = findViewById(R.id.btnEditPencil);
            txtName = findViewById(R.id.txtName);
            txtId = findViewById(R.id.txtId);
            txtCopyId = findViewById(R.id.txtCopyId);
            txtGenderCountry = findViewById(R.id.txtGenderCountry);
            txtFollow = findViewById(R.id.txtFollow);
            txtFans = findViewById(R.id.txtFans);
            txtVisitor = findViewById(R.id.txtVisitor);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) firebaseUid = user.getUid();
            if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");

            // RULE: Me te kokhono notun ID banabo na. ID shudhu prefs / Firebase theke nibo.
            my9DigitId = prefs.getString("MY_9_DIGIT_ID", "");
            // FIX: Firebase e khujbo UID diye, 9-Digit ID diye NA
            if (!firebaseUid.isEmpty()) {
                userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
            }

            showFromPrefs();
            loadMeData();

            // Pencil = Edit Mode. Ekhane shudhu Name + Photo change hobe.
            // ID / Gender / Country kokhono change hobe na.
            View.OnClickListener editClickListener = v -> {
                try {
                    Intent i = new Intent(MeActivity.this, EditProfileActivity.class);
                    i.putExtra("EDIT_MODE", true);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(MeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            if (btnEditPencil != null) btnEditPencil.setOnClickListener(editClickListener);
            if (imgAvatar != null) imgAvatar.setOnClickListener(editClickListener);

            if (txtCopyId != null) {
                txtCopyId.setOnClickListener(v -> {
                    if (my9DigitId.isEmpty()) return;
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("ID", my9DigitId);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(this, "ID Copied: " + my9DigitId, Toast.LENGTH_SHORT).show();
                });
            }

            setupMenuClicks();

            LinearLayout btnLogout = findViewById(R.id.btnLogoutLayout);
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    prefs.edit().putBoolean("IS_LOGGED_IN", false).apply();
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(MeActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                });
            }

        } catch (Exception e) {
            Toast.makeText(this, "MeActivity Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showFromPrefs();
        loadMeData();
    }

    private void showFromPrefs() {
        String name = prefs.getString("USER_NAME", "User");
        String country = prefs.getString("USER_COUNTRY", "");
        String gender = prefs.getString("USER_GENDER", "");
        String photoBase64 = prefs.getString("USER_PHOTO", "");

        if (txtName != null) txtName.setText(name);
        if (txtId != null) txtId.setText("ID: " + my9DigitId);
        if (txtGenderCountry != null) {
            if (!gender.isEmpty() && !country.isEmpty()) txtGenderCountry.setText(gender + " | " + country);
            else if (!country.isEmpty()) txtGenderCountry.setText(country);
            else if (!gender.isEmpty()) txtGenderCountry.setText(gender);
            else txtGenderCountry.setText("Complete Your Profile");
        }
        showBase64Image(photoBase64, imgAvatar);
    }

    private void loadMeData() {
        if (userRef == null) return;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                try {
                    if (!snap.exists()) return;
                    String name = getStr(snap, "name", prefs.getString("USER_NAME", "User"));
                    String id = getStr(snap, "my9DigitId", my9DigitId);
                    String country = getStr(snap, "country", prefs.getString("USER_COUNTRY", ""));
                    String gender = getStr(snap, "gender", prefs.getString("USER_GENDER", ""));
                    // FIX: Photo ekhon photoBase64 diye porbo - EditProfile e eita diyei save hoy
                    String photoBase64 = getStr(snap, "photoBase64", "");
                    if (photoBase64.isEmpty()) photoBase64 = getStr(snap, "photoUrl", "");
                    String follow = getStr(snap, "followCount", "0");
                    String fans = getStr(snap, "fansCount", "0");
                    String visitor = getStr(snap, "visitorCount", "0");

                    if (!id.isEmpty()) {
                        my9DigitId = id;
                        prefs.edit().putString("MY_9_DIGIT_ID", id).apply();
                    }
                    prefs.edit()
                        .putString("USER_NAME", name)
                        .putString("USER_COUNTRY", country)
                        .putString("USER_GENDER", gender)
                        .putString("USER_PHOTO", photoBase64)
                        .apply();

                    if (txtName != null) txtName.setText(name);
                    if (txtId != null) txtId.setText("ID: " + my9DigitId);
                    if (txtGenderCountry != null) txtGenderCountry.setText(gender + " | " + country);
                    if (txtFollow != null) txtFollow.setText(follow);
                    if (txtFans != null) txtFans.setText(fans);
                    if (txtVisitor != null) txtVisitor.setText(visitor);

                    showBase64Image(photoBase64, imgAvatar);
                } catch (Exception ignored) {}
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showBase64Image(String base64, ImageView iv) {
        if (iv == null || base64 == null || base64.isEmpty()) return;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (b != null) iv.setImageBitmap(b);
        } catch (Exception ignored) {}
    }

    private void setupMenuClicks() {
        int[] ids = new int[]{R.id.menuSvip, R.id.menuVip, R.id.menuRecharge, R.id.menuStore, R.id.menuDress, R.id.menuBirthday, R.id.menuLevel, R.id.menuNewbie, R.id.menuInvite, R.id.menuMedal, R.id.menuFamily, R.id.menuHost, R.id.menuDaily, R.id.menuSetting};
        for (int id : ids) {
            View v = findViewById(id);
            if (v != null) {
                v.setOnClickListener(view -> {
                    Toast.makeText(MeActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private String getStr(DataSnapshot s, String k, String def) {
        if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue());
        return def;
    }
}
