package com.sr.voicechat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class LoginActivity extends AppCompatActivity {
private EditText editEmail;
private EditText editPassword;
private Button btnLogin;
private Button btnGoogle;
private Button btnGoRegister;
private FirebaseAuth mAuth;
private GoogleSignInClient mGoogleSignInClient;
private static final int RC_SIGN_IN = 9001;
private SharedPreferences prefs;
private DatabaseReference usersRef;
@Override protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_login);
prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
mAuth = FirebaseAuth.getInstance();
usersRef = FirebaseDatabase.getInstance().getReference("users");
editEmail = findViewById(R.id.editEmail);
editPassword = findViewById(R.id.editPassword);
btnLogin = findViewById(R.id.btnLogin);
btnGoogle = findViewById(R.id.btnGoogle);
btnGoRegister = findViewById(R.id.btnGoRegister);
GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
GoogleSignInOptions.DEFAULT_SIGN_IN)
.requestEmail()
.build();
mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
if (btnLogin != null) {
btnLogin.setOnClickListener(v -> loginWithEmail());
}
if (btnGoogle != null) {
btnGoogle.setOnClickListener(v -> signInWithGoogle());
}
if (btnGoRegister != null) {
btnGoRegister.setOnClickListener(v -> {
Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
startActivity(i);
});
}
FirebaseUser cur = mAuth.getCurrentUser();
if (cur != null) {
saveAndGoHome(cur.getUid(), cur.getDisplayName(), cur.getEmail());
}
}
private void loginWithEmail() {
String email = editEmail.getText().toString().trim();
String pass = editPassword.getText().toString().trim();
if (email.isEmpty() || pass.isEmpty()) {
Toast.makeText(this, "Enter Email and Password",
Toast.LENGTH_SHORT).show();
return;
}
mAuth.signInWithEmailAndPassword(email, pass)
.addOnCompleteListener(task -> {
if (task.isSuccessful() && task.getResult() != null) {
FirebaseUser u = task.getResult().getUser();
if (u != null) {
saveAndGoHome(u.getUid(), u.getDisplayName(), u.getEmail());
}
} else {
String msg = "Login Failed";
if (task.getException() != null) {
msg = task.getException().getMessage();
}
Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
}
});
}
private void signInWithGoogle() {
Intent signInIntent = mGoogleSignInClient.getSignInIntent();
startActivityForResult(signInIntent, RC_SIGN_IN);
}
@Override protected void onActivityResult(int requestCode,
int resultCode, @Nullable Intent data) {
super.onActivityResult(requestCode, resultCode, data);
if (requestCode == RC_SIGN_IN && data != null) {
Task<GoogleSignInAccount> task =
GoogleSignIn.getSignedInAccountFromIntent(data);
try {
GoogleSignInAccount account = task.getResult(ApiException.class);
if (account != null) {
firebaseAuthWithGoogle(account);
}
} catch (ApiException e) {
Toast.makeText(this, "Google Sign In Failed",
Toast.LENGTH_SHORT).show();
}
}
}
private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
String token = acct.getIdToken();
if (token == null || token.isEmpty()) {
String gName = acct.getDisplayName();
String gEmail = acct.getEmail();
String fakeUid = "google_";
fakeUid += acct.getId();
saveAndGoHome(fakeUid, gName, gEmail);
return;
}
AuthCredential credential =
GoogleAuthProvider.getCredential(token, null);
mAuth.signInWithCredential(credential)
.addOnCompleteListener(this, task -> {
if (task.isSuccessful() && task.getResult() != null) {
FirebaseUser u = task.getResult().getUser();
if (u != null) {
saveAndGoHome(u.getUid(), u.getDisplayName(), u.getEmail());
}
} else {
Toast.makeText(this, "Google Auth Failed",
Toast.LENGTH_SHORT).show();
}
});
}
private void saveAndGoHome(String uid, String gName, String email) {
if (uid == null || uid.isEmpty()) {
return;
}
if (email == null) {
email = "";
}
String finalName = gName;
if (finalName == null || finalName.isEmpty()) {
finalName = prefs.getString("USER_NAME", "");
}
if (finalName == null || finalName.isEmpty()) {
finalName = "User";
}
final String saveName = finalName;
final String saveEmail = email;
usersRef.child(uid).child("email").setValue(saveEmail);
usersRef.child(uid).child("name").setValue(saveName);
usersRef.child(uid).child("uid").setValue(uid);
String myId = prefs.getString("MY_9_DIGIT_ID", "");
if (myId.isEmpty()) {
int r = (int) (Math.random() * 900000000) + 100000000;
myId = String.valueOf(r);
}
final String finalId = myId;
usersRef.child(uid).child("my9Id").setValue(finalId);
prefs.edit().putString("MY_9_DIGIT_ID", finalId).apply();
prefs.edit().putString("USER_NAME", saveName).apply();
prefs.edit().putString("USER_EMAIL", saveEmail).apply();
prefs.edit().putString("FIREBASE_UID", uid).apply();
Intent i = new Intent(LoginActivity.this, MainActivity.class);
i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
| Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(i);
finish();
}
}
