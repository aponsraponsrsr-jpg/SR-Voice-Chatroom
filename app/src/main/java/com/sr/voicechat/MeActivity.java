package com.sr.voicechat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
public class MeActivity extends AppCompatActivity {
private TextView txtName;
private TextView txtId;
private TextView txtEmail;
private TextView txtBio;
private Button btnEditProfile;
private Button btnLogout;
private SharedPreferences prefs;
@Override protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_me);
prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
txtName = findViewById(R.id.txtName);
txtId = findViewById(R.id.txtId);
txtEmail = findViewById(R.id.txtEmail);
txtBio = findViewById(R.id.txtBio);
btnEditProfile = findViewById(R.id.btnEditProfile);
btnLogout = findViewById(R.id.btnLogout);
refreshUI();
if (btnEditProfile != null) {
btnEditProfile.setOnClickListener(v -> {
try {
Intent i = new Intent(MeActivity.this, EditProfileActivity.class);
startActivity(i);
} catch (Exception e) {
Toast.makeText(this, "Edit Profile Not Found", Toast.LENGTH_SHORT).show();
}
});
}
if (btnLogout != null) {
btnLogout.setOnClickListener(v -> {
try {
FirebaseAuth.getInstance().signOut();
} catch (Exception ignored) {}
prefs.edit().clear().apply();
Intent i = new Intent(MeActivity.this, LoginActivity.class);
i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(i);
finish();
});
}
}
@Override protected void onResume() {
super.onResume();
refreshUI();
}
private void refreshUI() {
String n = prefs.getString("USER_NAME", "User");
String id = prefs.getString("MY_9_DIGIT_ID", "");
String em = prefs.getString("USER_EMAIL", "");
String bio = prefs.getString("USER_BIO", "Hello Friends!");
if (txtName != null) {
txtName.setText(n);
}
if (txtId != null) {
String t = "ID: ";
t += id;
txtId.setText(t);
}
if (txtEmail != null) {
txtEmail.setText(em);
}
if (txtBio != null) {
txtBio.setText(bio);
}
}
}
