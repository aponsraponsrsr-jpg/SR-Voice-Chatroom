package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Button btnGoogle = findViewById(R.id.btnGoogleLogin);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> { if (isNavigating) return; signIn(); });
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) checkUserInFirebase(user.getUid(), user.getEmail());
    }

    private void signIn() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Toast.makeText(this, "Google Play Service Problem: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) { isNavigating = false; Toast.makeText(this, "You Cancelled Google Login", Toast.LENGTH_SHORT).show(); return; }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) { isNavigating = false; Toast.makeText(this, "Google Sign-In Failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show(); }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) { Toast.makeText(this, "ID Token Null - Check google-services.json", Toast.LENGTH_LONG).show(); return; }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) checkUserInFirebase(user.getUid(), user.getEmail());
            } else {
                isNavigating = false;
                String msg = task.getException() != null ? task.getException().getMessage() : "Unknown";
                Toast.makeText(this, "Firebase Auth Failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ============ RESTORE LOCK - Same Gmail = Same Data ============
    // Google er same Gmail hole Firebase UID o same thake.
    // UID same thakle users/{uid} theke ager Permanent ID + sob data firiye anbo.
    // Notun ID / Notun Edit kokhono hobe na.
    private String getPermanentId(DataSnapshot s) {
        if (s != null && s.exists()) {
            if (s.hasChild("permanentId") && s.child("permanentId").getValue() != null) return String.valueOf(s.child("permanentId").getValue());
            if (s.hasChild("my9DigitId") && s.child("my9DigitId").getValue() != null) return String.valueOf(s.child("my9DigitId").getValue());
            if (s.hasChild("my9Id") && s.child("my9Id").getValue() != null) return String.valueOf(s.child("my9Id").getValue());
        }
        return "";
    }

    private String getDisplayId(DataSnapshot s, String permanentId) {
        // Official Short ID thakle oitai dekhabe, na thakle Permanent ID dekhabe
        if (s != null && s.exists()) {
            if (s.hasChild("displayId") && s.child("displayId").getValue() != null && !String.valueOf(s.child("displayId").getValue()).isEmpty())
                return String.valueOf(s.child("displayId").getValue());
            if (s.hasChild("shortId") && s.child("shortId").getValue() != null && !String.valueOf(s.child("shortId").getValue()).isEmpty())
                return String.valueOf(s.child("shortId").getValue());
            if (s.hasChild("uniqueId") && s.child("uniqueId").getValue() != null && !String.valueOf(s.child("uniqueId").getValue()).isEmpty())
                return String.valueOf(s.child("uniqueId").getValue());
        }
        String localDisplay = prefs.getString("DISPLAY_ID", "");
        if (!localDisplay.isEmpty()) return localDisplay;
        String localShort = prefs.getString("SHORT_ID", "");
        if (!localShort.isEmpty()) return localShort;
        return permanentId;
    }

    private String getPhoto(DataSnapshot s) {
        // PhotoUrl age, tarpor Base64 - 2 tar jekono ekta thaklei photo firbe
        if (s != null && s.exists()) {
            if (s.hasChild("photoUrl") && s.child("photoUrl").getValue() != null && !String.valueOf(s.child("photoUrl").getValue()).isEmpty())
                return String.valueOf(s.child("photoUrl").getValue());
            if (s.hasChild("photo") && s.child("photo").getValue() != null && !String.valueOf(s.child("photo").getValue()).isEmpty())
                return String.valueOf(s.child("photo").getValue());
            if (s.hasChild("photoBase64") && s.child("photoBase64").getValue() != null && !String.valueOf(s.child("photoBase64").getValue()).isEmpty())
                return String.valueOf(s.child("photoBase64").getValue());
        }
        return "";
    }

    private void checkUserInFirebase(String firebaseUid, String email) {
        if (isNavigating) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                // CASE 1 - Purano User: Profile complete ache - Sob data restore kore Home
                if (s.exists() && s.hasChild("my9DigitId") && s.hasChild("gender") && s.hasChild("country")) {
                    String permanentId = getPermanentId(s);
                    String displayId = getDisplayId(s, permanentId);
                    String name = s.child("name").getValue(String.class);
                    String gender = s.child("gender").getValue(String.class);
                    String country = s.child("country").getValue(String.class);
                    String photo = getPhoto(s);
                    if (photo == null) photo = "";

                    // Local e 3 ta ID e save - Permanent kokhono bodlabe na
                    prefs.edit()
                        .putString("FIREBASE_UID", firebaseUid)
                        .putString("ORIGINAL_ID", firebaseUid)
                        .putString("MY_9_DIGIT_ID", permanentId)
                        .putString("PERMANENT_ID", permanentId)
                        .putString("MY_ID", permanentId)
                        .putString("SHORT_ID", displayId)
                        .putString("DISPLAY_ID", displayId)
                        .putString("UNIQUE_ID", displayId)
                        .putString("USER_NAME", name != null ? name : "")
                        .putString("USER_GENDER", gender != null ? gender : "")
                        .putString("USER_COUNTRY", country != null ? country : "")
                        .putString("USER_EMAIL", email != null ? email : "")
                        .putString("USER_PHOTO", photo)
                        .putString("PROFILE_PHOTO", photo)
                        .putBoolean("IS_PROFILE_SETUP_DONE", true).apply();

                    // Firebase e missing field thakle puron kore dao - kintu Permanent ID / Coin / Target e hat dibo na
                    Map<String, Object> fix = new HashMap<>();
                    fix.put("uid", firebaseUid);
                    fix.put("originalId", firebaseUid);
                    fix.put("permanentId", permanentId);
                    fix.put("my9DigitId", permanentId);
                    fix.put("my9Id", permanentId);
                    fix.put("shortId", displayId);
                    fix.put("displayId", displayId);
                    fix.put("uniqueId", displayId);
                    if (email != null && !email.isEmpty()) fix.put("email", email);
                    ref.updateChildren(fix);

                    // id_mapping refresh - Short + Permanent 2 ta diyei asol UID pabe
                    DatabaseReference mapRef = FirebaseDatabase.getInstance().getReference("id_mapping");
                    if (!permanentId.isEmpty()) {
                        mapRef.child(permanentId).child("uid").setValue(firebaseUid);
                        mapRef.child(permanentId).child("permanentId").setValue(permanentId);
                        mapRef.child(permanentId).child("shortId").setValue(displayId);
                    }
                    if (!displayId.isEmpty() && !displayId.equals(permanentId)) {
                        mapRef.child(displayId).child("uid").setValue(firebaseUid);
                        mapRef.child(displayId).child("permanentId").setValue(permanentId);
                        mapRef.child(displayId).child("shortId").setValue(displayId);
                    }

                    goHome();
                    return;
                }

                // CASE 2 - Half Profile: Ager ID ache kintu Gender/Country nai - Edit e jabe kintu ID eki thakbe
                if (s.exists() && s.hasChild("my9DigitId")) {
                    String permanentId = getPermanentId(s);
                    String displayId = getDisplayId(s, permanentId);
                    String name = s.child("name").getValue(String.class);
                    String photo = getPhoto(s);
                    // Ager ID local e save kore rakhi - EditProfile e notun ID banabe na
                    prefs.edit()
                        .putString("FIREBASE_UID", firebaseUid)
                        .putString("ORIGINAL_ID", firebaseUid)
                        .putString("MY_9_DIGIT_ID", permanentId)
                        .putString("PERMANENT_ID", permanentId)
                        .putString("MY_ID", permanentId)
                        .putString("SHORT_ID", displayId)
                        .putString("DISPLAY_ID", displayId)
                        .putString("UNIQUE_ID", displayId)
                        .putString("USER_NAME", name != null ? name : "")
                        .putString("USER_EMAIL", email != null ? email : "")
                        .putString("USER_PHOTO", photo != null ? photo : "")
                        .putBoolean("IS_PROFILE_SETUP_DONE", false).apply();
                    goToEdit(firebaseUid, email);
                    return;
                }

                // CASE 3 - Ekdom Notun User: Kokhono aseni - Edit e jabe, okhane ekbar e 9-Digit banabe
                goToEdit(firebaseUid, email);
            }
            @Override public void onCancelled(DatabaseError e) { isNavigating = false; Toast.makeText(LoginActivity.this, "Database Error, Try Again", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void goToEdit(String firebaseUid, String email) {
        if (isNavigating) return; isNavigating = true;
        // Ager ID thakle muche felbo na - EditProfile oitai use korbe
        SharedPreferences.Editor e = prefs.edit();
        e.putString("FIREBASE_UID", firebaseUid);
        e.putString("USER_EMAIL", email != null ? email : "");
        e.putBoolean("IS_PROFILE_SETUP_DONE", false);
        e.apply();
        Intent i = new Intent(LoginActivity.this, EditProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }
    private void goHome() {
        if (isNavigating) return; isNavigating = true;
        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }
}
