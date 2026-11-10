package com.sr.voicechat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
@Override protected void onCreate(Bundle savedInstanceState) {
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
Toast.makeText(this, "Enter Email and Password",
Toast.LENGTH_SHORT).show();
return;
}
if (pass.length() < 6) {
Toast.makeText(this, "Password must be 6+",
Toast.LENGTH_SHORT).show();
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
String myId = prefs.getString("MY_9_DIGIT_ID", "");
if (myId.isEmpty()) {
int r = (int) (Math.random() * 900000000) + 100000000;
myId = String.valueOf(r);
}
final String finalId = myId;
usersRef.child(uid).child("name").setValue(name);
usersRef.child(uid).child("email").setValue(email);
usersRef.child(uid).child("uid").setValue(uid);
usersRef.child(uid).child("my9Id").setValue(finalId);
usersRef.child(uid).child("coins").setValue(0L);
usersRef.child(uid).child("sendingCoins").setValue(0L);
usersRef.child(uid).child("receivingCoins").setValue(0L);
Map<String, Object> map = new HashMap<>();
map.put("name", name);
map.put("email", email);
map.put("uid", uid);
map.put("my9Id", finalId);
db.collection("users").document(uid).set(map);
prefs.edit().putString("MY_9_DIGIT_ID", finalId).apply();
prefs.edit().putString("USER_NAME", name).apply();
prefs.edit().putString("USER_EMAIL", email).apply();
prefs.edit().putString("FIREBASE_UID", uid).apply();
Toast.makeText(this, "Registered Successfully",
Toast.LENGTH_SHORT).show();
Intent i = new Intent(RegisterActivity.this, MainActivity.class);
i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
| Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(i);
finish();
}
}
