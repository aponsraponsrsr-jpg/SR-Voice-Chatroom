package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.File;

public class MeActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private ImageView btnEditPencil;
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
            if (user != null) {
                firebaseUid = user.getUid();
            }
            if (firebaseUid == null || firebaseUid.isEmpty()) {
                firebaseUid = prefs.getString("FIREBASE_UID", "");
            }
            
            my9DigitId = prefs.getString("MY_9_DIGIT_ID", "");
            if (my9DigitId == null || my9DigitId.isEmpty()) {
                my9DigitId = "100000000";
            }

            if (firebaseUid != null && !firebaseUid.isEmpty()) {
                userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
            }

            // লোকাল ডেটা আগে শো করানো যাতে ক্র্যাশ বা হোমে ব্যাক করার সুযোগ না থাকে
            showFromPrefs();
            loadMeData();

            // পেন্সিল বা অবতার আইকনে ক্লিক করলে EditProfileActivity তে যাবে
            View.OnClickListener editClickListener = v -> {
                try {
                    Intent i = new Intent(MeActivity.this, EditProfileActivity.class);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(MeActivity.this, "Error opening edit profile", Toast.LENGTH_SHORT).show();
                }
            };

            if (btnEditPencil != null) btnEditPencil.setOnClickListener(editClickListener);
            if (imgAvatar != null) imgAvatar.setOnClickListener(editClickListener);
            if (txtName != null) txtName.setOnClickListener(editClickListener);

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
                    SharedPreferences.Editor e = prefs.edit();
                    e.putBoolean("IS_LOGGED_IN", false);
                    e.apply();
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(MeActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading Me page: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
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
        String photoPath = prefs.getString("USER_PHOTO_PATH", "");
        if (photoPath.isEmpty()) photoPath = prefs.getString("USER_PHOTO", "");

        if (txtName != null) txtName.setText(name);
        if (txtId != null) txtId.setText("ID: " + my9DigitId);
        
        if (txtGenderCountry != null) {
            if (!gender.isEmpty() && !country.isEmpty()) txtGenderCountry.setText(gender + " | " + country);
            else if (!country.isEmpty()) txtGenderCountry.setText(country);
            else if (!gender.isEmpty()) txtGenderCountry.setText(gender);
            else txtGenderCountry.setText("Complete Your Profile");
        }

        showLocalImage(photoPath, imgAvatar);
    }

    private void loadMeData() {
        if (userRef == null) return;
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;
                String name = getStr(snap, "name", prefs.getString("USER_NAME", "User"));
                String id = getStr(snap, "my9DigitId", my9DigitId);
                String country = getStr(snap, "country", prefs.getString("USER_COUNTRY", ""));
                String gender = getStr(snap, "gender", prefs.getString("USER_GENDER", ""));
                String photoPath = getStr(snap, "photoPath", prefs.getString("USER_PHOTO_PATH", ""));
                if (photoPath.isEmpty()) photoPath = getStr(snap, "photoUri", prefs.getString("USER_PHOTO", ""));
                
                String follow = getStr(snap, "followCount", "0");
                String fans = getStr(snap, "fansCount", "0");
                String visitor = getStr(snap, "visitorCount", "0");

                if (!id.isEmpty()) {
                    my9DigitId = id;
                    prefs.edit().putString("MY_9_DIGIT_ID", id).apply();
                }
                prefs.edit().putString("USER_NAME", name).apply();
                prefs.edit().putString("USER_COUNTRY", country).apply();
                prefs.edit().putString("USER_GENDER", gender).apply();

                if (txtName != null) txtName.setText(name);
                if (txtId != null) txtId.setText("ID: " + my9DigitId);
                
                if (txtGenderCountry != null) {
                    if (!gender.isEmpty() && !country.isEmpty()) txtGenderCountry.setText(gender + " | " + country);
                    else if (!country.isEmpty()) txtGenderCountry.setText(country);
                    else if (!gender.isEmpty()) txtGenderCountry.setText(gender);
                    else txtGenderCountry.setText("Complete Your Profile");
                }

                if (txtFollow != null) txtFollow.setText(follow);
                if (txtFans != null) txtFans.setText(fans);
                if (txtVisitor != null) txtVisitor.setText(visitor);

                showLocalImage(photoPath, imgAvatar);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showLocalImage(String path, ImageView iv) {
        if (iv == null || path == null || path.isEmpty()) return;
        try {
            if (path.startsWith("/")) {
                File f = new File(path);
                if (f.exists()) {
                    Bitmap b = BitmapFactory.decodeFile(path);
                    if (b != null) iv.setImageBitmap(b);
                }
            } else if (path.startsWith("content://")) {
                iv.setImageURI(Uri.parse(path));
            }
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
