package com.sr.voicechat;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.HashMap;
public class MeActivity extends AppCompatActivity {
private ImageView imgAvatar;
private ImageView btnEditPencil;
private TextView txtName;
private TextView txtId;
private TextView txtCopyId;
private TextView txtGenderCountry;
private TextView txtFollow;
private TextView txtFans;
private TextView txtVisitor;
private SharedPreferences prefs;
private DatabaseReference userRef;
private String firebaseUid = "";
private String my9DigitId = "";
@Override protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
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
} else {
firebaseUid = prefs.getString("FIREBASE_UID", "");
}
if (firebaseUid.isEmpty()) {
Toast.makeText(this, "Login First", Toast.LENGTH_SHORT).show();
finish();
return;
}
prefs.edit().putString("FIREBASE_UID", firebaseUid).apply();
userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
loadPermanentUser();
if (txtCopyId != null) {
txtCopyId.setOnClickListener(v -> {
if (!my9DigitId.isEmpty()) {
ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
cm.setPrimaryClip(ClipData.newPlainText("SR ID", my9DigitId));
Toast.makeText(this, "ID Copied: " + my9DigitId, Toast.LENGTH_SHORT).show();
}
});
}
if (btnEditPencil != null) {
btnEditPencil.setOnClickListener(v -> {
Intent i = new Intent(MeActivity.this, EditProfileActivity.class);
startActivity(i);
});
}
if (imgAvatar != null) {
imgAvatar.setOnClickListener(v -> {
Intent i = new Intent(MeActivity.this, EditProfileActivity.class);
startActivity(i);
});
}
setupMenuClicks();
}
private void loadPermanentUser() {
userRef.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
if (snap.exists() && snap.hasChild("my9DigitId")) {
// OLD USER - Same Gmail, Same ID back - NEVER CHANGE
my9DigitId = snap.child("my9DigitId").getValue(String.class);
String n = getStringValue(snap, "name", "User");
String g = getStringValue(snap, "gender", "");
String c = getStringValue(snap, "country", "");
String bio = getStringValue(snap, "bio", "Hello Friends!");
String email = getStringValue(snap, "email", "");
prefs.edit().putString("MY_9_DIGIT_ID", my9DigitId).apply();
prefs.edit().putString("USER_NAME", n).apply();
prefs.edit().putString("USER_GENDER", g).apply();
prefs.edit().putString("USER_COUNTRY", c).apply();
prefs.edit().putString("USER_BIO", bio).apply();
prefs.edit().putString("USER_EMAIL", email).apply();
showUI(n, my9DigitId, g, c);
loadCounts(snap);
} else {
// NEW USER - Check if old local ID exists for migration, else create ONE TIME only
String oldLocal = prefs.getString("MY_9_DIGIT_ID", "");
if (!oldLocal.isEmpty() && oldLocal.length() >= 9) {
my9DigitId = oldLocal;
} else {
my9DigitId = generate9Digit();
}
HashMap<String, Object> map = new HashMap<>();
map.put("my9DigitId", my9DigitId);
map.put("firebaseUid", firebaseUid);
FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
if (fu != null) {
if (fu.getDisplayName() != null) map.put("name", fu.getDisplayName());
if (fu.getEmail() != null) map.put("email", fu.getEmail());
}
map.put("coins", 0);
map.put("dollars", 0);
map.put("followCount", 0);
map.put("fansCount", 0);
map.put("visitorCount", 0);
map.put("createdAt", System.currentTimeMillis());
userRef.updateChildren(map);
prefs.edit().putString("MY_9_DIGIT_ID", my9DigitId).apply();
showUI(map.get("name") != null ? String.valueOf(map.get("name")) : "User", my9DigitId, "", "");
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {
String local = prefs.getString("MY_9_DIGIT_ID", "");
if (!local.isEmpty()) {
my9DigitId = local;
showUI(prefs.getString("USER_NAME", "User"), local, prefs.getString("USER_GENDER", ""), prefs.getString("USER_COUNTRY", ""));
} else {
Toast.makeText(MeActivity.this, "No Internet - Try Again", Toast.LENGTH_SHORT).show();
}
}
});
}
private void showUI(String name, String id, String gender, String country) {
if (txtName != null) txtName.setText(name);
if (txtId != null) txtId.setText("ID: " + id);
if (txtGenderCountry != null) {
String gc = "";
if (!gender.isEmpty()) gc += gender;
if (!country.isEmpty()) {
if (!gc.isEmpty()) gc += " • ";
gc += country;
}
if (gc.isEmpty()) gc = "Complete Your Profile";
txtGenderCountry.setText(gc);
}
}
private void loadCounts(DataSnapshot snap) {
long f1 = getLongValue(snap, "followCount", 0);
long f2 = getLongValue(snap, "fansCount", 0);
long f3 = getLongValue(snap, "visitorCount", 0);
if (txtFollow != null) txtFollow.setText(String.valueOf(f1));
if (txtFans != null) txtFans.setText(String.valueOf(f2));
if (txtVisitor != null) txtVisitor.setText(String.valueOf(f3));
}
private void setupMenuClicks() {
int[] ids = new int[]{R.id.menuStore, R.id.menuDress, R.id.menuBirthday, R.id.menuLevel, R.id.menuNewbie, R.id.menuInvite, R.id.menuMedal, R.id.menuFamily, R.id.menuHost, R.id.menuDaily, R.id.menuSetting, R.id.menuVip, R.id.menuRecharge};
for (int id : ids) {
LinearLayout l = findViewById(id);
if (l != null) {
l.setOnClickListener(v -> {
Toast.makeText(MeActivity.this, "Coming Soon - Next Step", Toast.LENGTH_SHORT).show();
});
}
}
LinearLayout logout = findViewById(R.id.btnLogoutLayout);
if (logout != null) {
logout.setOnClickListener(v -> {
// IMPORTANT - Logout e ID / Data CLEAR korbo na. Sudhu Auth signOut.
try { FirebaseAuth.getInstance().signOut(); } catch (Exception ignored) {}
Intent i = new Intent(MeActivity.this, LoginActivity.class);
i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(i);
finish();
});
}
}
private String generate9Digit() {
int r = (int) (Math.random() * 900000000) + 100000000;
return String.valueOf(r);
}
private String getStringValue(DataSnapshot s, String k, String def) {
if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue());
return def;
}
private long getLongValue(DataSnapshot s, String k, long def) {
try { if (s.hasChild(k) && s.child(k).getValue() != null) return Long.parseLong(String.valueOf(s.child(k).getValue())); } catch (Exception ignored) {}
return def;
}
}
