package com.sr.voicechat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
public class EditProfileActivity extends AppCompatActivity {
private ImageView imgEditAvatar;
private ImageView imgRoomPhoto;
private EditText edtName;
private EditText edtBio;
private Spinner spinnerCountry;
private Spinner spinnerGender;
private Button btnSaveProfile;
private TextView txtShowId;
private SharedPreferences prefs;
private DatabaseReference userRef;
private DatabaseReference roomMetaRef;
private String firebaseUid = "";
private String my9DigitId = "";
private String savedProfilePhotoPath = "";
private String savedRoomPhotoPath = "";
private String newProfilePhotoPath = "";
private String newRoomPhotoPath = "";
private static final int PICK_PROFILE = 101;
private static final int PICK_ROOM = 102;
@Override protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_edit_profile);
prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
imgEditAvatar = findViewById(R.id.imgEditAvatar);
imgRoomPhoto = findViewById(R.id.imgRoomPhoto);
edtName = findViewById(R.id.edtName);
edtBio = findViewById(R.id.edtBio);
spinnerCountry = findViewById(R.id.spinnerCountry);
spinnerGender = findViewById(R.id.spinnerGender);
btnSaveProfile = findViewById(R.id.btnSaveProfile);
txtShowId = findViewById(R.id.txtShowId);
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
if (user != null) firebaseUid = user.getUid();
else firebaseUid = prefs.getString("FIREBASE_UID", "");
if (firebaseUid.isEmpty()) { finish(); return; }
my9DigitId = prefs.getString("MY_9_DIGIT_ID", "");
userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
if (!my9DigitId.isEmpty()) {
roomMetaRef = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + my9DigitId).child("meta");
}
// Country
List<String> countries = new ArrayList<>();
countries.add("Select Country / Location");
countries.addAll(Arrays.asList("Bangladesh","India","Pakistan","Nepal","Sri Lanka","Myanmar","Thailand","Vietnam","Indonesia","Malaysia","Singapore","Philippines","China","Japan","South Korea","Saudi Arabia","United Arab Emirates","Qatar","Kuwait","Oman","Bahrain","Jordan","Iraq","Iran","Turkey","Egypt","Nigeria","Kenya","South Africa","United States","Canada","United Kingdom","Germany","France","Italy","Spain","Portugal","Netherlands","Belgium","Switzerland","Austria","Sweden","Norway","Denmark","Finland","Russia","Ukraine","Poland","Australia","New Zealand","Brazil","Argentina","Mexico","Chile","Colombia","Peru","Morocco","Algeria","Tunisia","Ethiopia","Ghana","Other"));
ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
spinnerCountry.setAdapter(countryAdapter);
// Gender
List<String> genders = new ArrayList<>();
genders.add("Select Gender");
genders.add("Male");
genders.add("Female");
genders.add("Other");
ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
spinnerGender.setAdapter(genderAdapter);
loadUserData(countries, genders);
if (imgEditAvatar != null) {
imgEditAvatar.setOnClickListener(v -> {
Intent i = new Intent(Intent.ACTION_PICK);
i.setType("image/*");
startActivityForResult(i, PICK_PROFILE);
});
}
if (imgRoomPhoto != null) {
imgRoomPhoto.setOnClickListener(v -> {
Intent i = new Intent(Intent.ACTION_PICK);
i.setType("image/*");
startActivityForResult(i, PICK_ROOM);
});
}
if (btnSaveProfile != null) {
btnSaveProfile.setOnClickListener(v -> savePermanentProfile(countries));
}
}
private void loadUserData(List<String> countries, List<String> genders) {
userRef.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
if (!snap.exists()) return;
my9DigitId = getStr(snap, "my9DigitId", my9DigitId);
String name = getStr(snap, "name", prefs.getString("USER_NAME", ""));
String bio = getStr(snap, "bio", prefs.getString("USER_BIO", "Hello Friends!"));
String country = getStr(snap, "country", prefs.getString("USER_COUNTRY", ""));
String gender = getStr(snap, "gender", prefs.getString("USER_GENDER", ""));
// 1. NIJER ID ER PHOTO - ALADA
savedProfilePhotoPath = getStr(snap, "photoPath", prefs.getString("USER_PHOTO_PATH", ""));
if (savedProfilePhotoPath.isEmpty()) savedProfilePhotoPath = getStr(snap, "photoUri", "");
// 2. GROUP / ROOM ER PHOTO - ALADA
savedRoomPhotoPath = getStr(snap, "roomPhotoPath", prefs.getString("USER_ROOM_PHOTO_PATH", ""));
if (edtName != null) edtName.setText(name);
if (edtBio != null) edtBio.setText(bio);
if (txtShowId != null) txtShowId.setText("ID: " + my9DigitId + " (Permanent - Never Change)");
if (country != null && !country.isEmpty() && countries.contains(country)) spinnerCountry.setSelection(countries.indexOf(country));
else spinnerCountry.setSelection(0);
if (gender != null && !gender.isEmpty() && genders.contains(gender)) spinnerGender.setSelection(genders.indexOf(gender));
else spinnerGender.setSelection(0);
showLocalImage(savedProfilePhotoPath, imgEditAvatar);
showLocalImage(savedRoomPhotoPath, imgRoomPhoto);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void savePermanentProfile(List<String> countries) {
String name = edtName != null ? edtName.getText().toString().trim() : "";
String bio = edtBio != null ? edtBio.getText().toString().trim() : "";
int countryPos = spinnerCountry != null ? spinnerCountry.getSelectedItemPosition() : 0;
int genderPos = spinnerGender != null ? spinnerGender.getSelectedItemPosition() : 0;
if (name.isEmpty()) { Toast.makeText(this, "Enter Your Name", Toast.LENGTH_SHORT).show(); return; }
if (countryPos == 0) { Toast.makeText(this, "Select Your Country / Location", Toast.LENGTH_SHORT).show(); return; }
if (genderPos == 0) { Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show(); return; }
String country = countries.get(countryPos);
String gender = spinnerGender.getSelectedItem().toString();
if (bio.isEmpty()) bio = "Hello Friends!";
// Final photo path - notun ta thakle notun, na thakle ager ta
String finalProfilePath = !newProfilePhotoPath.isEmpty() ? newProfilePhotoPath : savedProfilePhotoPath;
String finalRoomPath = !newRoomPhotoPath.isEmpty() ? newRoomPhotoPath : savedRoomPhotoPath;
HashMap<String, Object> map = new HashMap<>();
map.put("name", name);
map.put("bio", bio);
map.put("country", country);
map.put("gender", gender);
map.put("profileUpdatedAt", System.currentTimeMillis());
// ID, coins, dollars ekhane pathai nai - kokhono change hobe na
map.put("photoPath", finalProfilePath);
map.put("photoUri", finalProfilePath);
map.put("roomPhotoPath", finalRoomPath);
map.put("roomPhotoUri", finalRoomPath);
userRef.updateChildren(map);
// Room er meta teo alada kore room photo save
if (roomMetaRef != null) {
roomMetaRef.child("roomPhotoPath").setValue(finalRoomPath);
roomMetaRef.child("roomPhotoUri").setValue(finalRoomPath);
roomMetaRef.child("roomOwnerName").setValue(name);
roomMetaRef.child("roomOwnerBio").setValue(bio);
roomMetaRef.child("roomOwnerPhotoPath").setValue(finalProfilePath);
}
prefs.edit().putString("USER_NAME", name).apply();
prefs.edit().putString("USER_BIO", bio).apply();
prefs.edit().putString("USER_COUNTRY", country).apply();
prefs.edit().putString("USER_GENDER", gender).apply();
prefs.edit().putString("USER_PHOTO_PATH", finalProfilePath).apply();
prefs.edit().putString("USER_PHOTO", finalProfilePath).apply();
prefs.edit().putString("USER_ROOM_PHOTO_PATH", finalRoomPath).apply();
prefs.edit().putBoolean("PROFILE_SETUP_DONE", true).apply();
Toast.makeText(this, "Profile Saved! ID: " + my9DigitId, Toast.LENGTH_SHORT).show();
finish();
}
@Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
super.onActivityResult(requestCode, resultCode, data);
if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
Uri uri = data.getData();
if (requestCode == PICK_PROFILE) {
String p = copyImageToInternal(uri, "my_profile_photo.jpg");
if (!p.isEmpty()) { newProfilePhotoPath = p; showLocalImage(p, imgEditAvatar); }
} else if (requestCode == PICK_ROOM) {
String p = copyImageToInternal(uri, "my_room_photo.jpg");
if (!p.isEmpty()) { newRoomPhotoPath = p; showLocalImage(p, imgRoomPhoto); }
}
}
// Gallery er chobi ke app er vitore copy kore rakhi - tahole logout / restart eo thakbe
private String copyImageToInternal(Uri uri, String fileName) {
try {
InputStream in = getContentResolver().openInputStream(uri);
Bitmap bmp = BitmapFactory.decodeStream(in);
if (in != null) in.close();
if (bmp == null) return "";
File f = new File(getFilesDir(), fileName);
FileOutputStream out = new FileOutputStream(f);
bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
out.flush(); out.close();
return f.getAbsolutePath();
} catch (Exception e) { return ""; }
}
private void showLocalImage(String path, ImageView iv) {
if (iv == null || path == null || path.isEmpty()) return;
try {
if (path.startsWith("/")) {
File f = new File(path);
if (f.exists()) { Bitmap b = BitmapFactory.decodeFile(path); if (b != null) iv.setImageBitmap(b); }
} else if (path.startsWith("content://")) {
iv.setImageURI(Uri.parse(path));
}
} catch (Exception e) {}
}
private String getStr(DataSnapshot s, String k, String def) {
if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue());
return def;
}
}
