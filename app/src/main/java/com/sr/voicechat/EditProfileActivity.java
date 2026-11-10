package com.sr.voicechat;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
public class EditProfileActivity extends AppCompatActivity {
private ImageView imgEditAvatar;
private EditText edtName;
private EditText edtBio;
private Spinner spinnerCountry;
private Spinner spinnerGender;
private Button btnSaveProfile;
private TextView txtShowId;
private SharedPreferences prefs;
private DatabaseReference userRef;
private String firebaseUid = "";
private String my9DigitId = "";
private Uri pickedImageUri = null;
private static final int PICK_IMAGE = 101;
@Override protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_edit_profile);
prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
imgEditAvatar = findViewById(R.id.imgEditAvatar);
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
userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
List<String> countries = new ArrayList<>();
countries.add("Select Country / Location");
countries.addAll(Arrays.asList(
"Bangladesh","India","Pakistan","Nepal","Sri Lanka","Myanmar","Thailand","Vietnam","Indonesia","Malaysia","Singapore","Philippines","China","Japan","South Korea","Saudi Arabia","United Arab Emirates","Qatar","Kuwait","Oman","Bahrain","Jordan","Iraq","Iran","Turkey","Egypt","Nigeria","Kenya","South Africa","United States","Canada","United Kingdom","Germany","France","Italy","Spain","Portugal","Netherlands","Belgium","Switzerland","Austria","Sweden","Norway","Denmark","Finland","Russia","Ukraine","Poland","Australia","New Zealand","Brazil","Argentina","Mexico","Chile","Colombia","Peru","Morocco","Algeria","Tunisia","Ethiopia","Ghana","Other"));
ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
spinnerCountry.setAdapter(countryAdapter);
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
startActivityForResult(i, PICK_IMAGE);
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
my9DigitId = getStr(snap, "my9DigitId", prefs.getString("MY_9_DIGIT_ID", ""));
String name = getStr(snap, "name", prefs.getString("USER_NAME", ""));
String bio = getStr(snap, "bio", prefs.getString("USER_BIO", "Hello Friends!"));
String country = getStr(snap, "country", prefs.getString("USER_COUNTRY", ""));
String gender = getStr(snap, "gender", prefs.getString("USER_GENDER", ""));
if (edtName != null) edtName.setText(name);
if (edtBio != null) edtBio.setText(bio);
if (txtShowId != null) txtShowId.setText("ID: " + my9DigitId + " (Permanent - Never Change)");
if (country != null && !country.isEmpty() && countries.contains(country)) {
spinnerCountry.setSelection(countries.indexOf(country));
} else {
spinnerCountry.setSelection(0);
}
if (gender != null && !gender.isEmpty() && genders.contains(gender)) {
spinnerGender.setSelection(genders.indexOf(gender));
} else {
spinnerGender.setSelection(0);
}
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
HashMap<String, Object> map = new HashMap<>();
map.put("name", name);
map.put("bio", bio);
map.put("country", country);
map.put("gender", gender);
map.put("profileUpdatedAt", System.currentTimeMillis());
if (pickedImageUri != null) map.put("photoUri", pickedImageUri.toString());
userRef.updateChildren(map);
prefs.edit().putString("USER_NAME", name).apply();
prefs.edit().putString("USER_BIO", bio).apply();
prefs.edit().putString("USER_COUNTRY", country).apply();
prefs.edit().putString("USER_GENDER", gender).apply();
if (pickedImageUri != null) prefs.edit().putString("USER_PHOTO", pickedImageUri.toString()).apply();
Toast.makeText(this, "Profile Saved! ID: " + my9DigitId, Toast.LENGTH_SHORT).show();
finish();
}
@Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
super.onActivityResult(requestCode, resultCode, data);
if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
pickedImageUri = data.getData();
if (imgEditAvatar != null) imgEditAvatar.setImageURI(pickedImageUri);
}
}
private String getStr(DataSnapshot s, String k, String def) {
if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue());
return def;
}
}
